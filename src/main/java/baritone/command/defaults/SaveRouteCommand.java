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

package baritone.command.defaults;

import baritone.api.IBaritone;
import baritone.api.command.Command;
import baritone.api.command.argument.IArgConsumer;
import baritone.api.command.exception.CommandException;
import baritone.api.utils.BetterBlockPos;
import baritone.utils.RouteStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SaveRouteCommand extends Command {

    public SaveRouteCommand(IBaritone baritone) {
        super(baritone, "saveroute");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        String name = args.getString();
        RouteStore store = new RouteStore();
        List<RouteStore.RouteWaypoint> waypoints = new ArrayList<>();
        while (args.hasAny()) {
            String wpName = args.getString();
            int x = args.getAs(Integer.class);
            int y = args.getAs(Integer.class);
            int z = args.getAs(Integer.class);
            waypoints.add(new RouteStore.RouteWaypoint(wpName, new BetterBlockPos(x, y, z)));
        }
        if (waypoints.isEmpty()) {
            waypoints.add(new RouteStore.RouteWaypoint("here", ctx.playerFeet()));
        }
        try {
            store.save(name, waypoints);
            logDirect(String.format("Saved route '%s' with %d waypoints", name, waypoints.size()));
        } catch (Exception e) {
            logDirect("Failed to save route: " + e.getMessage());
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Save a named multi-waypoint route to JSON";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Saves a named route (an ordered list of waypoints) to a JSON file under",
                "<baritone-dir>/routes/<name>.json. Load it later with #loadroute.",
                "",
                "Usage:",
                "> saveroute myroute base 100 64 200 portal 100 64 250 endpoint 150 64 300",
                "> saveroute quickroute   (saves current position as a single-waypoint route)"
        );
    }
}