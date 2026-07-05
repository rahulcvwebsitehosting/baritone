/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.process;

import baritone.Baritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.api.utils.interfaces.IGoalRenderPos;
import baritone.utils.BaritoneProcessHelper;
import baritone.utils.BlockStateInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Drives a boat across large bodies of water. Engages when boatNavigationEnabled is true and
 * the path-to-goal would require crossing >= boatMinWaterWidth blocks of open water.
 * Places a boat from the hotbar (if boatAutoPlace), drives toward the goal's XZ via yaw
 * steering + MOVE_FORWARD, and dismounts when land is reached.
 */
public final class BoatProcess extends BaritoneProcessHelper {

    private enum Phase { IDLE, EMBARK, DRIVING, DISEMBARK }

    private Phase phase = Phase.IDLE;
    private Goal pendingGoal;
    private int goalX, goalZ;
    private int placeCooldown;

    public BoatProcess(Baritone baritone) {
        super(baritone);
    }

    @Override
    public boolean isActive() {
        return phase != Phase.IDLE;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        Goal g = baritone.getPathingBehavior().getGoal();
        if (g != null) pendingGoal = g;

        switch (phase) {
            case IDLE: {
                // Never engage if elytra is active — it has its own aerial movement subsystem.
                if (baritone.getElytraProcess().isActive()) {
                    return null;
                }
                // Never engage if the player is already in a vehicle that isn't a boat (e.g. minecart).
                if (ctx.player().getVehicle() != null
                        && !isRidingBoat()) {
                    return null;
                }
                if (!Baritone.settings().boatNavigationEnabled.value) {
                    return null;
                }
                if (pendingGoal == null) return null;
                int[] targetXZ = extractGoalXZ(pendingGoal);
                if (targetXZ == null) return null;
                goalX = targetXZ[0];
                goalZ = targetXZ[1];
                BlockPos feet = ctx.playerFeet();
                if (isLargeWaterAhead(feet, goalX, goalZ)) {
                    phase = Phase.EMBARK;
                    placeCooldown = 0;
                } else {
                    return null;
                }
            }
            case EMBARK: {
                if (isRidingBoat()) {
                    phase = Phase.DRIVING;
                    return drivingCommand();
                }
                if (placeCooldown > 0) {
                    placeCooldown--;
                    return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
                }
                BlockPos placeAt = ctx.playerFeet().above();
                if (Baritone.settings().boatAutoPlace.value) {
                    if (!selectBoatInHotbar()) {
                        logDirect("No boat in hotbar, falling back to normal pathing");
                        onLostControl();
                        return null;
                    }
                }
                Optional<Rotation> rot = RotationUtils.reachable(ctx, placeAt);
                if (rot.isPresent()) {
                    baritone.getLookBehavior().updateTarget(rot.get(), true);
                    if (ctx.playerRotations().isReallyCloseTo(rot.get())) {
                        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
                        placeCooldown = 10;
                    }
                }
                return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
            }
            case DRIVING: {
                return drivingCommand();
            }
            case DISEMBARK: {
                baritone.getInputOverrideHandler().clearAllKeys();
                if (isRidingBoat()) {
                    baritone.getInputOverrideHandler().setInputForceState(Input.SNEAK, true);
                    return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
                }
                onLostControl();
                return null;
            }
        }
        return null;
    }

    /**
     * Drive the boat toward (goalX, goalZ). Steer by aiming the player's yaw at a point a few
     * blocks ahead, hold MOVE_FORWARD, and let vanilla boat physics carry us. Detect landfall
     * and switch to DISEMBARK.
     */
    private PathingCommand drivingCommand() {
        if (baritone.getElytraProcess().isActive()
                && !ctx.player().onGround()
                && !isRidingBoat()) {
            baritone.getInputOverrideHandler().clearAllKeys();
            onLostControl();
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        Entity vehicle = ctx.player().getVehicle();
        if (!isRidingBoat() || vehicle == null) {
            phase = Phase.EMBARK;
            placeCooldown = 0;
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        Vec3 boatPos = vehicle.position();
        double dx = goalX + 0.5 - boatPos.x;
        double dz = goalZ + 0.5 - boatPos.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001) {
            phase = Phase.DISEMBARK;
            return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
        }
        int lookahead = Baritone.settings().boatLookaheadBlocks.value;
        double aimX = boatPos.x + (dx / len) * lookahead;
        double aimZ = boatPos.z + (dz / len) * lookahead;

        Rotation steer = RotationUtils.calcRotationFromVec3d(
                ctx.playerHead(),
                new Vec3(aimX, boatPos.y + 1.0, aimZ),
                ctx.playerRotations());
        baritone.getLookBehavior().updateTarget(new Rotation(steer.getYaw(), 0.0f), true);

        baritone.getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);

        BlockPos ahead = new BlockPos((int) Math.floor(boatPos.x + (dx / len) * 2),
                (int) Math.floor(boatPos.y),
                (int) Math.floor(boatPos.z + (dz / len) * 2));
        BlockState aheadState = baritone.bsi.get0(ahead);
        if (!(aheadState.getBlock() instanceof AirBlock)
                && aheadState.getFluidState().isEmpty()) {
            phase = Phase.DISEMBARK;
        }
        return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
    }

    /**
     * Returns true if the column of water from `feet` toward (goalX, goalZ) is at least
     * boatMinWaterWidth blocks wide. Counts contiguous water blocks (still or flowing).
     */
    private boolean isLargeWaterAhead(BlockPos feet, int goalX, int goalZ) {
        int minWidth = Baritone.settings().boatMinWaterWidth.value;
        int dx = Integer.signum(goalX - feet.getX());
        int dz = Integer.signum(goalZ - feet.getZ());
        if (dx == 0 && dz == 0) return false;
        int run = 0;
        BlockPos cursor = feet;
        for (int i = 0; i < minWidth * 2; i++) {
            cursor = cursor.offset(dx, 0, dz);
            BlockState s = baritone.bsi.get0(cursor);
            if (!s.getFluidState().isEmpty()
                    && (s.getFluidState().getType() == Fluids.WATER
                        || s.getFluidState().getType() == Fluids.FLOWING_WATER)) {
                run++;
                if (run >= minWidth) return true;
            } else {
                run = 0;
            }
        }
        return false;
    }

    /**
     * Extract an (x, z) target from common Goal types. Returns null for goals with no
     * concrete XZ (e.g. GoalYLevel alone).
     */
    private int[] extractGoalXZ(Goal goal) {
        if (goal instanceof IGoalRenderPos) {
            BlockPos p = ((IGoalRenderPos) goal).getGoalPos();
            return new int[]{p.getX(), p.getZ()};
        }
        if (goal instanceof GoalXZ) {
            GoalXZ g = (GoalXZ) goal;
            return new int[]{g.getX(), g.getZ()};
        }
        if (goal instanceof GoalBlock) {
            GoalBlock g = (GoalBlock) goal;
            return new int[]{g.x, g.z};
        }
        return null;
    }

    /** Search the hotbar for any boat item and select it. Returns true if found+selected. */
    private boolean selectBoatInHotbar() {
        var inv = ctx.player().getInventory();
        for (int i = 0; i < 9; i++) {
            var stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BoatItem) {
                inv.setSelectedSlot(i);
                return true;
            }
        }
        return false;
    }

    private boolean isRidingBoat() {
        Entity v = ctx.player().getVehicle();
        return v != null && v.getClass().getSimpleName().toLowerCase().contains("boat");
    }

    /**
     * Mark as temporary so we don't wipe other processes' goals when we take control.
     */
    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public void onLostControl() {
        phase = Phase.IDLE;
        pendingGoal = null;
        baritone.getInputOverrideHandler().clearAllKeys();
    }

    @Override
    public String displayName0() {
        return "Boat Navigation -> " + (pendingGoal == null ? "?" : pendingGoal);
    }

    /** Public entry so a future #boat command can engage manually. */
    public void engage() {
        phase = Phase.EMBARK;
        placeCooldown = 0;
    }
}