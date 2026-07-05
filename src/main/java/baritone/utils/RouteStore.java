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

package baritone.utils;

import baritone.Baritone;
import baritone.api.BaritoneAPI;
import baritone.api.utils.BetterBlockPos;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Saves and loads named multi-waypoint routes as JSON files under
 * {@code <baritone-dir>/routes/<name>.json}. Each route is an ordered list of
 * {@link RouteWaypoint} tuples (name, x, y, z). Used by {@code SaveRouteCommand} and
 * {@code LoadRouteCommand}.
 */
public final class RouteStore {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type ROUTE_TYPE = new TypeToken<List<RouteWaypoint>>() {}.getType();

    /** A single waypoint in a route. Gson serializes these public fields directly. */
    public static final class RouteWaypoint {
        public String name;
        public int x, y, z;

        // no-arg constructor required by Gson
        public RouteWaypoint() {}

        public RouteWaypoint(String name, BetterBlockPos pos) {
            this.name = name;
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
        }

        public BetterBlockPos toPos() {
            return new BetterBlockPos(x, y, z);
        }
    }

    private final Path routesDir;

    public RouteStore() {
        // getDirectory() is on the concrete Baritone class, not the IBaritone interface,
        // so we must cast the primary baritone instance.
        this.routesDir = ((Baritone) BaritoneAPI.getProvider().getPrimaryBaritone())
                .getDirectory().resolve("routes");
    }

    private Path routeFile(String name) {
        // sanitize: only allow [a-zA-Z0-9_-]
        if (!name.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("Route name must be alphanumeric/underscore/dash");
        }
        return routesDir.resolve(name + ".json");
    }

    public void save(String name, List<RouteWaypoint> waypoints) throws IOException {
        Files.createDirectories(routesDir);
        Path file = routeFile(name);
        String json = GSON.toJson(waypoints, ROUTE_TYPE);
        Files.writeString(file, json);
    }

    public List<RouteWaypoint> load(String name) throws IOException {
        Path file = routeFile(name);
        if (!Files.exists(file)) {
            return null;
        }
        String json = Files.readString(file);
        List<RouteWaypoint> loaded = GSON.fromJson(json, ROUTE_TYPE);
        return loaded == null ? Collections.emptyList() : loaded;
    }

    public List<String> listRoutes() {
        List<String> names = new ArrayList<>();
        if (!Files.exists(routesDir)) {
            return names;
        }
        try {
            Files.list(routesDir).forEach(p -> {
                String fn = p.getFileName().toString();
                if (fn.endsWith(".json")) {
                    names.add(fn.substring(0, fn.length() - 5));
                }
            });
        } catch (IOException ignored) {}
        return names;
    }

    public boolean delete(String name) throws IOException {
        Path file = routeFile(name);
        return Files.deleteIfExists(file);
    }
}
