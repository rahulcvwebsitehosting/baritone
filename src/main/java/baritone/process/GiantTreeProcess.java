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
import baritone.api.pathing.goals.GoalComposite;
import baritone.api.pathing.goals.GoalYLevel;
import baritone.api.process.IBaritoneProcess;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.BlockOptionalMetaLookup;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.api.utils.interfaces.IGoalRenderPos;
import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.MovementHelper;
import baritone.utils.BaritoneProcessHelper;
import baritone.utils.BlockStateInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Mines giant trees (tall 2x2 spruce/oak) top-down: climb to the top, then break logs
 * downward while standing on the remaining trunk. Avoids the falling-log pile and the
 * throwaway-block placement that MineProcess does for tall trees.
 */
public final class GiantTreeProcess extends BaritoneProcessHelper {

    private BlockOptionalMetaLookup filter;
    private BlockPos trunkTop;
    private BlockPos trunkBase;
    private boolean climbing;
    private boolean miningDown;

    public GiantTreeProcess(Baritone baritone) {
        super(baritone);
    }

    @Override
    public boolean isActive() {
        return filter != null;
    }

    @Override
    public PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {
        if (trunkTop == null) {
            Optional<BlockPos> found = findGiantTrunk();
            if (found.isEmpty()) {
                logDirect("No giant tree trunk found within search radius, cancelling");
                onLostControl();
                return null;
            }
            trunkTop = found.get();
            trunkBase = findTrunkBase(trunkTop);
            climbing = true;
            miningDown = false;
        }

        if (climbing) {
            if (hasArrivedAtTop()) {
                climbing = false;
                miningDown = true;
            } else {
                Goal climbGoal = new GoalBlock(trunkTop.above());
                return new PathingCommand(climbGoal, PathingCommandType.REVALIDATE_GOAL_AND_PATH);
            }
        }

        if (miningDown) {
            BlockPos feet = ctx.playerFeet();
            BlockPos below = feet.below();

            if (below.getY() < trunkBase.getY() - 1) {
                logDirect("Giant tree fully mined");
                onLostControl();
                return null;
            }

            BlockState state = baritone.bsi.get0(below);
            if (state.getBlock() instanceof AirBlock || !filter.has(state)) {
                return new PathingCommand(new GoalBlock(below),
                        PathingCommandType.REVALIDATE_GOAL_AND_PATH);
            }

            if (!MovementHelper.avoidBreaking(baritone.bsi, below.getX(), below.getY(),
                    below.getZ(), state)) {
                Optional<Rotation> rot = RotationUtils.reachable(ctx, below);
                if (rot.isPresent() && isSafeToCancel) {
                    baritone.getLookBehavior().updateTarget(rot.get(), true);
                    MovementHelper.switchToBestToolFor(ctx, state);
                    if (ctx.isLookingAt(below)
                            || ctx.playerRotations().isReallyCloseTo(rot.get())) {
                        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
                    }
                    return new PathingCommand(null, PathingCommandType.REQUEST_PAUSE);
                }
            }
            return new PathingCommand(new GoalBlock(below),
                    PathingCommandType.REVALIDATE_GOAL_AND_PATH);
        }

        onLostControl();
        return null;
    }

    private boolean hasArrivedAtTop() {
        BlockPos target = trunkTop.above();
        BlockPos feet = ctx.playerFeet();
        return feet.getX() == target.getX()
                && feet.getZ() == target.getZ()
                && Math.abs(feet.getY() - target.getY()) <= 1;
    }

    private Optional<BlockPos> findGiantTrunk() {
        int minH = Baritone.settings().giantTreeMinHeight.value;
        int radius = Baritone.settings().giantTreeSearchRadius.value;
        BlockPos feet = ctx.playerFeet();

        int yTop = feet.getY() + 80;
        int yBottom = feet.getY() - 8;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int run = 0;
                int runTop = yTop;
                for (int y = yTop; y >= yBottom; y--) {
                    BlockState s = baritone.bsi.get0(feet.getX() + dx, y, feet.getZ() + dz);
                    if (isLog(s)) {
                        if (run == 0) runTop = y;
                        run++;
                        if (run >= minH) {
                            return Optional.of(new BlockPos(feet.getX() + dx, runTop, feet.getZ() + dz));
                        }
                    } else {
                        run = 0;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private boolean isLog(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.LOGS);
    }

    private BlockPos findTrunkBase(BlockPos top) {
        BlockPos cursor = top;
        while (isLog(baritone.bsi.get0(cursor.below()))) {
            cursor = cursor.below();
        }
        return cursor;
    }

    public void mine(BlockOptionalMetaLookup filter) {
        this.filter = filter;
        this.trunkTop = null;
        this.trunkBase = null;
        this.climbing = false;
        this.miningDown = false;
    }

    @Override
    public void onLostControl() {
        this.filter = null;
        this.trunkTop = null;
        this.trunkBase = null;
        this.climbing = false;
        this.miningDown = false;
    }

    @Override
    public String displayName0() {
        return "Giant Tree Mine " + (filter == null ? "" : filter);
    }
}