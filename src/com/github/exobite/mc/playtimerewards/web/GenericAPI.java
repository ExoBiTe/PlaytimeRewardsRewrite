package com.github.exobite.mc.playtimerewards.web;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.ReflectionHelper;
import com.google.gson.JsonElement;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class GenericAPI {

    public static abstract class APIReturnAction {

        public APIReturnAction(){}

        public abstract void onFinish(JsonElement data);

    }

    private static GenericAPI instance;

    public static GenericAPI register(JavaPlugin main) {
        if(instance==null) {
            instance = new GenericAPI(main);
        }
        return instance;
    }

    public static GenericAPI getInstance() {
        return instance;
    }

    private JavaPlugin main;

    private GenericAPI(JavaPlugin main) {
        this.main = main;
    }

    public void getApiRequest(String request, APIReturnAction response) {
        JsonElement rVal = null;
        try {
            URL url = new URL(request);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Caught an Error while an API-Request. Responsecode:"+responseCode);
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
        response.onFinish(rVal);
    }

}
