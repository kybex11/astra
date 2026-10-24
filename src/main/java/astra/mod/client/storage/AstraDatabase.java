package astra.mod.client.storage;

import astra.mod.client.waypoints.WaypointManager;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AstraDatabase {
    public static final AstraDatabase INSTANCE = new AstraDatabase();

    private Connection connection;
    private boolean initialized = false;

    public synchronized void init() {
        if (initialized) return;
        try {
            File baseDir = new File(Minecraft.getInstance().gameDirectory, "config");
            if (!baseDir.exists()) baseDir.mkdirs();
            File dbFile = new File(baseDir, "astra_storage.db");

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            try (Statement stmt = connection.createStatement()) {
                // Table 1: Generic Key-Value Settings
                stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                        "key TEXT PRIMARY KEY, " +
                        "value TEXT" +
                        ");");

                // Table 2: HUD Coordinates & Scale
                stmt.execute("CREATE TABLE IF NOT EXISTS hud_elements (" +
                        "element TEXT PRIMARY KEY, " +
                        "x REAL, " +
                        "y REAL, " +
                        "scale REAL" +
                        ");");

                // Table 3: Waypoints
                stmt.execute("CREATE TABLE IF NOT EXISTS waypoints (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT, " +
                        "x REAL, " +
                        "y REAL, " +
                        "z REAL, " +
                        "dim TEXT, " +
                        "created_at INTEGER" +
                        ");");

                // Table 4: User Cosmetics & Skins
                stmt.execute("CREATE TABLE IF NOT EXISTS cosmetics (" +
                        "uuid TEXT, " +
                        "cosmetic_key TEXT, " +
                        "val TEXT, " +
                        "PRIMARY KEY(uuid, cosmetic_key)" +
                        ");");

                // Table 5: Replay / Rollback Records
                stmt.execute("CREATE TABLE IF NOT EXISTS replays (" +
                        "id TEXT PRIMARY KEY, " +
                        "name TEXT, " +
                        "server TEXT, " +
                        "duration_ms INTEGER, " +
                        "created_at INTEGER" +
                        ");");
            }

            initialized = true;
            System.out.println("[Astra] SQLite3 database initialized successfully: " + dbFile.getAbsolutePath());
        } catch (Throwable t) {
            System.err.println("[Astra] Failed to initialize SQLite3 database: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public synchronized void setSetting(String key, String value) {
        if (!initialized || connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO settings (key, value) VALUES (?, ?);")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized String getSetting(String key, String defaultValue) {
        if (!initialized || connection == null) return defaultValue;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT value FROM settings WHERE key = ?;")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public synchronized void saveWaypoints(List<WaypointManager.Waypoint> waypoints) {
        if (!initialized || connection == null) return;
        try {
            connection.setAutoCommit(false);
            try (Statement st = connection.createStatement()) {
                st.execute("DELETE FROM waypoints;");
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO waypoints (name, x, y, z, dim, created_at) VALUES (?, ?, ?, ?, ?, ?);")) {
                long now = System.currentTimeMillis();
                for (WaypointManager.Waypoint wp : waypoints) {
                    ps.setString(1, wp.name);
                    ps.setDouble(2, wp.x);
                    ps.setDouble(3, wp.y);
                    ps.setDouble(4, wp.z);
                    ps.setString(5, wp.dim);
                    ps.setLong(6, now);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            try { connection.rollback(); } catch (Exception ignored) {}
            e.printStackTrace();
        }
    }

    public synchronized List<WaypointManager.Waypoint> loadWaypoints() {
        List<WaypointManager.Waypoint> list = new ArrayList<>();
        if (!initialized || connection == null) return list;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT name, x, y, z, dim FROM waypoints ORDER BY id ASC;")) {
            while (rs.next()) {
                list.add(new WaypointManager.Waypoint(
                        rs.getString("name"),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getString("dim")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ignored) {}
            connection = null;
            initialized = false;
        }
    }
}
