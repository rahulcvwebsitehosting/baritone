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

package baritone.behavior;

import baritone.Baritone;
import baritone.api.event.events.TickEvent;
import baritone.api.utils.Helper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

/**
 * Auto-pauses Baritone pathing when another (non-spectator) player comes within a configurable
 * radius. Toggled by the {@code pauseOnPlayersNearby} setting; radius by
 * {@code pausePlayersNearbyRadius}. When the nearby player leaves, pathing resumes on its own
 * because {@link baritone.api.behavior.IPathingBehavior#requestPause()} only pauses one tick at
 * a time, so we just keep re-requesting the pause while the player is present.
 */
public final class PlayerProximityBehavior extends Behavior implements Helper {

    private boolean playerWasNearby;

    public PlayerProximityBehavior(Baritone baritone) {
        super(baritone);
    }

    @Override
    public void onTick(TickEvent event) {
        if (event.getType() == TickEvent.Type.OUT) {
            return;
        }
        if (!Baritone.settings().pauseOnPlayersNearby.value) {
            return;
        }
        // Don't react if we're not even pathing.
        if (!baritone.getPathingBehavior().isPathing()) {
            return;
        }
        double radius = Baritone.settings().pausePlayersNearbyRadius.value;
        double radiusSq = radius * radius;
        double meX = ctx.playerFeet().getX();
        double meY = ctx.playerFeet().getY();
        double meZ = ctx.playerFeet().getZ();

        // Iterate all loaded player entities. The client level exposes them via .players().
        if (!(ctx.world() instanceof ClientLevel level)) {
            return;
        }
        boolean found = false;
        for (Player other : level.players()) {
            if (other == ctx.player()) {
                continue; // don't pause on ourselves
            }
            // Skip spectators — they can't interact. Vanilla exposes isSpectator() on Entity.
            if (other.isSpectator()) {
                continue;
            }
            double dx = other.getX() - meX;
            double dy = other.getY() - meY;
            double dz = other.getZ() - meZ;
            if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                found = true;
                baritone.getPathingBehavior().requestPause();
                if (!playerWasNearby) {
                    logDirect("Pausing pathing: player " + other.getName().getString()
                            + " is within " + (int) radius + " blocks");
                    playerWasNearby = true;
                }
                return;
            }
        }
        // No nearby players — reset announcement flag so we re-announce when one arrives
        playerWasNearby = false;
    }
}
