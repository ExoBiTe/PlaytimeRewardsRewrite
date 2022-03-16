package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MojangAPI {

    private static MojangAPI instance;

    private final static String API_UUID_TO_NAMES = "https://api.mojang.com/user/profiles/<uuid>/names";
    private final static String API_NAME_TO_UUID = "https://api.mojang.com/users/profiles/minecraft/<name>";
    private final static long RESET_REQUESTS_INTERVAL = 60000 * 10;  //10 Minutes
    private final static int MAX_REQUESTS_PER_INTERVAL = 200;   //Mojang Limits it to 600 per 10 Minutes

    public static MojangAPI getInstance(){
        return instance;
    }

    public static MojangAPI register(JavaPlugin main) {
        if(instance==null) {
            instance = new MojangAPI(main);
        }
        return instance;
    }

    private final JavaPlugin main;

    private final Map<UUID, String> cachedUsernames = new HashMap<>();
    private long resetTimestamp;
    private int requestsSinceLastReset;

    private MojangAPI(JavaPlugin main){
        this.main = main;
        resetTimestamp = System.currentTimeMillis();
        requestsSinceLastReset = 0;
    }

    private abstract static class InternalApiAnswer{

        InternalApiAnswer(){}

        abstract void run(JsonElement e);

    }

    private boolean allowRequest(){
        long msnow = System.currentTimeMillis();
        if(msnow >= resetTimestamp+RESET_REQUESTS_INTERVAL) {
            resetTimestamp = msnow;
            requestsSinceLastReset = 0;
        }
        return requestsSinceLastReset < MAX_REQUESTS_PER_INTERVAL;
    }

    private void getAPIRequest(final String targetUrl, final InternalApiAnswer action) {
        if(!allowRequest()) {
            //Too many requests
            new BukkitRunnable() {
                @Override
                public void run() {
                    action.run(null);
                }
            }.runTask(main);
            PluginMaster.sendConsoleMessage(Level.WARNING, "Can't send more API-Requests. Try again later!");
            return;
        }
        requestsSinceLastReset++;
        JsonElement rVal = null;
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Caught an Error from the Mojang-API. Responsecode:"+responseCode);
            }else{
                InputStream is = con.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);

                rVal = ReflectionHelper.getInstance().parseReader(isr);

                isr.close();
                is.close();
            }
        } catch (IOException e) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "An Exception occurred from a Mojang-API Call: ");
            e.printStackTrace();
        }

        final JsonElement finalRVal = rVal;
        new BukkitRunnable() {
            @Override
            public void run() {
                action.run(finalRVal);
            }
        }.runTask(main);
    }

    private UUID getIdFromMap(String name){
        if(!cachedUsernames.containsValue(name)) return null;
        for(UUID id:cachedUsernames.keySet()) {
            if(cachedUsernames.get(id).equals(name)) return id;
        }
        //Should never be reached
        return null;
    }

    public String getNameFromCachedUUID(UUID id) {
        return cachedUsernames.getOrDefault(id, null);
    }

    public UUID getUUIDFromCachedName(String name) {
        return getIdFromMap(name);
    }

    public void getNameFromUUID(final UUID id, final APIReturnAction action) {
        if(cachedUsernames.containsKey(id)) {
            action.onFinish(cachedUsernames.get(id));
            return;
        }
        InternalApiAnswer toRun = new InternalApiAnswer() {
            @Override
            public void run(JsonElement data) {
                String username = null;
                if(data!=null) {
                    username = getLastUsernameFromApiAnswer(data);
                    cachedUsernames.put(id, username);  //Cache for Future use
                }
                action.onFinish(username);
            }
        };
        String requestUrl = API_UUID_TO_NAMES.replace("<uuid>", id.toString());
        new BukkitRunnable() {
            @Override
            public void run() {
                getAPIRequest(requestUrl, toRun);
            }
        }.runTaskAsynchronously(main);
    }

    private String getLastUsernameFromApiAnswer(JsonElement e) {
        if(e==null) return "";
        if(e.isJsonNull()) return "";
        JsonArray arr = e.getAsJsonArray();
        if(arr.size()<=0) return "";
        return arr.get(arr.size()-1).getAsJsonObject().get("name").getAsString();
    }

    public void getUUIDFromName(final String name, final APIReturnAction action) {
        if(cachedUsernames.containsValue(name)) {
            UUID id = getIdFromMap(name);
            if(id!=null) {
                action.onFinish(id.toString());
            }else{
                action.onFinish("");
            }
            return;
        }

        InternalApiAnswer toRun = new InternalApiAnswer() {
            @Override
            void run(JsonElement e) {
                UUID id = null;
                if(e!=null) {
                    id = getUUIDFromApiAnswer(e);
                    cachedUsernames.put(id, name);
                }
                action.onFinish(id==null ? null : id.toString());
            }
        };
        String requestUrl = API_NAME_TO_UUID.replace("<name>", name);
        new BukkitRunnable() {
            @Override
            public void run() {
                getAPIRequest(requestUrl, toRun);
            }
        }.runTaskAsynchronously(main);
    }

    private UUID getUUIDFromApiAnswer(JsonElement e) {
        if(e==null) return null;
        if(e.isJsonNull()) return null;
        String rawID = e.getAsJsonObject().get("id").getAsString();
        return UUID.fromString(rawID
                .replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"));
    }

}
