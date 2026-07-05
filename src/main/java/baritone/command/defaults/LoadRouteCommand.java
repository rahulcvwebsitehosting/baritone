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
import baritone.api.pathing.goals.GoalBlock;
import baritone.utils.RouteStore;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LoadRouteCommand extends Command {

    public LoadRouteCommand(IBaritone baritone) {
        super(baritone, "loadroute");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        args.requireMin(1);
        String name = args.getString();
        RouteStore store = new RouteStore();
        List<RouteStore.RouteWaypoint> route;
        try {
            route = store.load(name);
        } catch (Exception e) {
            logDirect("Failed to load route: " + e.getMessage());
            return;
        }
        if (route == null) {
            logDirect("No saved route named '" + name + "'. Use #saveroute to create one.");
            return;
        }
        if (route.isEmpty()) {
            logDirect("Route '" + name + "' is empty.");
            return;
        }
        RouteStore.RouteWaypoint first = route.get(0);
        StringBuilder summary = new StringBuilder("Route '" + name + "' (").append(route.size()).append(" waypoints): ");
        for (int i = 0; i < route.size(); i++) {
            RouteStore.RouteWaypoint wp = route.get(i);
            if (i > 0) summary.append(" -> ");
            summary.append(wp.name).append("(").append(wp.x).append(",").append(wp.y).append(",").append(wp.z).append(")");
        }
        logDirect(summary.toString());
        logDirect("Pathing to waypoint 1 (" + first.name + "). Use #loadroute again after reaching it.");
        baritone.getCustomGoalProcess().setGoalAndPath(new GoalBlock(first.toPos()));
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasExactly(1)) {
            return new RouteStore().listRoutes().stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Load a saved multi-waypoint route and path toward it";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Loads a named route from <baritone-dir>/routes/<name>.json and begins pathing",
                "toward the nearest waypoint in the route. The route is shared as plain JSON so",
                "you can copy it between worlds/accounts.",
                "",
                "Usage:",
                "> loadroute myroute   (paths toward the nearest waypoint in 'myroute')"
        );
    }
}