package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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

    private Map<UUID, String> cachedUsernames = new HashMap<>();

    private MojangAPI(JavaPlugin main){
        this.main = main;
    }

    private abstract class InternalApiAnswer{

        InternalApiAnswer(){}

        abstract void run(JsonElement e);

    }

    public void getNameFromUUID(final UUID id, final APIReturnAction action) {
        if(cachedUsernames.containsKey(id)) {
            action.onFinish(id.toString());
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
        JsonArray arr = e.getAsJsonArray();
        if(arr.size()<=0) return "";
        return arr.get(arr.size()-1).getAsJsonObject().get("name").getAsString();
    }

    private void getAPIRequest(final String targetUrl, final InternalApiAnswer action) {
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

}
