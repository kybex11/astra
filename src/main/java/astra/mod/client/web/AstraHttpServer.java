package astra.mod.client.web;

import astra.mod.client.module.Module;
import astra.mod.client.module.ModuleManager;
import astra.mod.client.setting.BooleanSetting;
import astra.mod.client.setting.ModeSetting;
import astra.mod.client.setting.NumberSetting;
import astra.mod.client.setting.Setting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AstraHttpServer {
    public static final AstraHttpServer INSTANCE = new AstraHttpServer();

    private HttpServer server;
    private int port = 3000;

    public synchronized void start(int preferredPort) {
        if (server != null) return;
        this.port = preferredPort;

        for (int p = preferredPort; p < preferredPort + 10; p++) {
            try {
                server = HttpServer.create(new InetSocketAddress(p), 0);
                this.port = p;
                break;
            } catch (IOException e) {
                // Port busy, try next
            }
        }

        if (server == null) {
            System.err.println("[Astra] Failed to bind HTTP Web Server to any port around " + preferredPort);
            return;
        }

        // Static files handler
        server.createContext("/", new StaticFileHandler());

        // API handler
        server.createContext("/api/modules", new ModulesApiHandler());
        server.createContext("/api/toggle", new ToggleApiHandler());
        server.createContext("/api/setting", new SettingApiHandler());
        server.createContext("/api/keybind", new KeybindApiHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("[Astra] Web Dashboard Server started at http://127.0.0.1:" + this.port);
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public int getPort() {
        return port;
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            String resourcePath = "/assets/astra/web" + path;
            InputStream in = AstraHttpServer.class.getResourceAsStream(resourcePath);

            if (in == null) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            in.close();

            String contentType = "text/html; charset=utf-8";
            if (path.endsWith(".css")) contentType = "text/css; charset=utf-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=utf-8";
            else if (path.endsWith(".svg")) contentType = "image/svg+xml";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    private static class ModulesApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JsonArray array = new JsonArray();
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", m.getName());
                obj.addProperty("category", m.getCategory().name());
                obj.addProperty("description", m.getDescription());
                obj.addProperty("enabled", m.isEnabled());

                String keyName = "None";
                if (m.getKeyCode() > 0) {
                    String n = GLFW.glfwGetKeyName(m.getKeyCode(), 0);
                    keyName = n != null ? n.toUpperCase() : "KEY_" + m.getKeyCode();
                }
                obj.addProperty("key", keyName);

                JsonArray settingsArr = new JsonArray();
                for (Setting<?> s : m.getSettings()) {
                    JsonObject sObj = new JsonObject();
                    sObj.addProperty("name", s.getName());
                    sObj.addProperty("description", s.getDescription());

                    if (s instanceof NumberSetting) {
                        NumberSetting ns = (NumberSetting) s;
                        sObj.addProperty("type", "NUMBER");
                        sObj.addProperty("value", ns.getValue());
                        sObj.addProperty("min", ns.getMin());
                        sObj.addProperty("max", ns.getMax());
                        sObj.addProperty("step", ns.getStep());
                    } else if (s instanceof BooleanSetting) {
                        BooleanSetting bs = (BooleanSetting) s;
                        sObj.addProperty("type", "BOOLEAN");
                        sObj.addProperty("value", bs.getValue());
                    } else if (s instanceof ModeSetting) {
                        ModeSetting ms = (ModeSetting) s;
                        sObj.addProperty("type", "MODE");
                        sObj.addProperty("value", ms.getValue());
                        JsonArray modesArr = new JsonArray();
                        for (String mode : ms.getModes()) {
                            modesArr.add(mode);
                        }
                        sObj.add("modes", modesArr);
                    }
                    settingsArr.add(sObj);
                }
                obj.add("settings", settingsArr);
                array.add(obj);
            }

            byte[] jsonBytes = array.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, jsonBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(jsonBytes);
            os.close();
        }
    }

    private static class ToggleApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String name = params.get("name");
            if (name != null) {
                Minecraft.getInstance().execute(() -> {
                    Module m = ModuleManager.INSTANCE.getModule(name);
                    if (m != null) m.toggle();
                });
            }
            sendOk(exchange);
        }
    }

    private static class SettingApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String modName = params.get("module");
            String settingName = params.get("setting");
            String val = params.get("value");

            if (modName != null && settingName != null && val != null) {
                Minecraft.getInstance().execute(() -> {
                    Module m = ModuleManager.INSTANCE.getModule(modName);
                    if (m != null) {
                        for (Setting<?> s : m.getSettings()) {
                            if (s.getName().equalsIgnoreCase(settingName)) {
                                if (s instanceof NumberSetting) {
                                    try {
                                        ((NumberSetting) s).setValue(Double.parseDouble(val));
                                    } catch (NumberFormatException ignored) {}
                                } else if (s instanceof BooleanSetting) {
                                    ((BooleanSetting) s).setValue(Boolean.parseBoolean(val));
                                } else if (s instanceof ModeSetting) {
                                    ModeSetting ms = (ModeSetting) s;
                                    for (String mode : ms.getModes()) {
                                        if (mode.equalsIgnoreCase(val)) {
                                            ms.setValue(mode);
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                });
            }
            sendOk(exchange);
        }
    }

    private static class KeybindApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String modName = params.get("module");
            String key = params.get("key");

            if (modName != null && key != null) {
                Minecraft.getInstance().execute(() -> {
                    Module m = ModuleManager.INSTANCE.getModule(modName);
                    if (m != null) {
                        if (key.equalsIgnoreCase("None")) {
                            m.setKeyCode(GLFW.GLFW_KEY_UNKNOWN);
                        } else if (key.length() == 1) {
                            m.setKeyCode((int) key.toUpperCase().charAt(0));
                        }
                    }
                });
            }
            sendOk(exchange);
        }
    }

    private static void sendOk(HttpExchange exchange) throws IOException {
        String resp = "{\"status\":\"ok\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, resp.length());
        OutputStream os = exchange.getResponseBody();
        os.write(resp.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                try {
                    result.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
                } catch (Exception e) {
                    result.put(pair[0], pair[1]);
                }
            }
        }
        return result;
    }
}
