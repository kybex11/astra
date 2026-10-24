package astra.mod.client.waypoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WaypointManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Waypoint> WAYPOINTS = new ArrayList<>();
    private static boolean loaded = false;

    public static class Waypoint {
        public String name;
        public double x;
        public double y;
        public double z;
        public String dim;

        public Waypoint(String name, double x, double y, double z, String dim) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
        }
    }

    private static File getFile() {
        File dir = new File(Minecraft.getInstance().gameDirectory, "config");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "astra_waypoints.json");
    }

    public static List<Waypoint> getAll() {
        ensureLoaded();
        return Collections.unmodifiableList(WAYPOINTS);
    }

    public static void add(Waypoint wp) {
        ensureLoaded();
        WAYPOINTS.add(wp);
        save();
    }

    public static void clear() {
        WAYPOINTS.clear();
        save();
    }

    public static void ensureLoaded() {
        if (loaded) return;
        loaded = true;

        // Try SQLite first
        try {
            List<Waypoint> dbList = astra.mod.client.storage.AstraDatabase.INSTANCE.loadWaypoints();
            if (dbList != null && !dbList.isEmpty()) {
                WAYPOINTS.addAll(dbList);
                return;
            }
        } catch (Exception ignored) {}

        File file = getFile();
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            if (!root.has("waypoints")) return;
            JsonArray arr = root.getAsJsonArray("waypoints");
            for (JsonElement el : arr) {
                try {
                    JsonObject o = el.getAsJsonObject();
                    WAYPOINTS.add(new Waypoint(
                            o.has("name") ? o.get("name").getAsString() : "Point",
                            o.has("x") ? o.get("x").getAsDouble() : 0.0,
                            o.has("y") ? o.get("y").getAsDouble() : 64.0,
                            o.has("z") ? o.get("z").getAsDouble() : 0.0,
                            o.has("dim") ? o.get("dim").getAsString() : "overworld"
                    ));
                } catch (Exception ignored) {}
            }
            // Migrate to SQLite
            astra.mod.client.storage.AstraDatabase.INSTANCE.saveWaypoints(WAYPOINTS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            // Save to SQLite3
            astra.mod.client.storage.AstraDatabase.INSTANCE.saveWaypoints(WAYPOINTS);

            JsonObject root = new JsonObject();
            JsonArray arr = new JsonArray();
            for (Waypoint wp : WAYPOINTS) {
                JsonObject o = new JsonObject();
                o.addProperty("name", wp.name);
                o.addProperty("x", wp.x);
                o.addProperty("y", wp.y);
                o.addProperty("z", wp.z);
                o.addProperty("dim", wp.dim);
                arr.add(o);
            }
            root.add("waypoints", arr);
            try (FileWriter writer = new FileWriter(getFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
