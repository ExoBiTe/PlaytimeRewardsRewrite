package com.github.exobite.mc.playtimerewards.web;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.VersionHelper;
import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.utils.ReflectionHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class AutoUpdater {

    private static AutoUpdater instance;

    public static AutoUpdater getInstance(){
        return instance;
    }

    public static AutoUpdater createAutoUpdater(JavaPlugin main, boolean sync) {
        if(instance==null){
            instance = new AutoUpdater(main);
            instance.start(sync);
        }
        return instance;
    }

    private final String MY_USER_AGENT = "ExobitePlugin";
    private final int RESOURCE_ID = 100231;
    private final String GET_LATEST_VERSION = "https://api.spiget.org/v2/resources/"+RESOURCE_ID+"/versions/latest";
    private final String GET_LATEST_DOWNLOAD = "https://api.spiget.org/v2/resources/"+RESOURCE_ID+"/download";
    private final String GET_LATEST_UPDATE = "https://api.spiget.org/v2/resources/"+RESOURCE_ID+"/updates?size=1&sort=-date";
    private final File updateTarget = new File(PluginMaster.getInstance().getDataFolder() + File.separator + "PTR_Updated.jar");

    private final JavaPlugin main;

    private String latestVersion = null;
    private boolean updateAvailable = false;
    private boolean downloadedUpdate;

    private AutoUpdater(JavaPlugin main) {
        this.main = main;
    }

    private void start(boolean sync) {
        BukkitRunnable br = new BukkitRunnable() {
            @Override
            public void run() {
                if(checkForNewerVersion()) {
                    updateAvailable = true;
                    //Send the Message on the Main thread for a nicer-looking console prefix.
                    final String updateTitle = getLatestUpdateTitle();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            StringBuilder sb = new StringBuilder("A new Version (v").append(latestVersion).append(") is available!");
                            if(updateTitle!=null) {
                                final String lines = "---------------------------------";
                                sb.append("\n").append(lines);
                                sb.append("\nUpdate Content:\n  \"").append(updateTitle).append("\"");
                                sb.append("\n").append(lines);
                            }
                            PluginMaster.sendConsoleMessage(Level.INFO, sb.toString());
                        }
                    }.runTask(main);

                    if(Config.getInstance().allowAutoDownload()) {
                        //Auto download enabled, start download
                        downloadJar();
                    }
                }
            }
        };
        if(sync) {
            br.runTask(main);
        }else{
            br.runTaskAsynchronously(main);
        }
    }

    private boolean checkForNewerVersion() {
        String currentVersion = main.getDescription().getVersion();
        try {
            URL url = new URL(GET_LATEST_VERSION);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", MY_USER_AGENT);

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't check for a newer Update, HTTP Error code: "+responseCode);
                return false;
            }

            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            JsonElement e = ReflectionHelper.getInstance().parseReader(isr);

            latestVersion = unpackVersionFromJson(e);

            if(latestVersion==null) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't find the Latest Version in the HTTP Response.");
                return false;
            }

            isr.close();
            is.close();
        } catch (IOException e) {
            latestVersion = "0.0.0";
        }

        return VersionHelper.isLarger(VersionHelper.getVersionFromString(latestVersion), VersionHelper.getVersionFromString(currentVersion));
    }

    private String unpackVersionFromJson(JsonElement e){
        if(!(e instanceof JsonObject jo)) return null;
        return jo.get("name").getAsString();
    }

    private void downloadJar(){
        try {
            long ms = System.currentTimeMillis();

            URL url = new URL(GET_LATEST_DOWNLOAD);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", MY_USER_AGENT);

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't download the Updated Jar, HTTP Error code: "+responseCode);
                return;
            }

            ReadableByteChannel readByteChan = Channels.newChannel(url.openStream());
            FileOutputStream fOut = new FileOutputStream(updateTarget);
            FileChannel fc = fOut.getChannel();

            fc.transferFrom(readByteChan, 0, Long.MAX_VALUE);

            fc.close();
            fOut.close();
            readByteChan.close();

            downloadedUpdate = true;
            PluginMaster.sendConsoleMessage(Level.INFO, "Updated jar was downloaded (took "+(System.currentTimeMillis()-ms)+" ms)!\n" +
                    "It will get moved upon the next Server Start.");

        } catch(IOException e){
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't download the Update.");
            e.printStackTrace();
        }
    }

    public void moveUpdate(){
        if(!downloadedUpdate) return;
        URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        if(!updateTarget.exists()) {
            PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't update the Plugin, can't find the downloaded File.\nWas it moved or deleted?");
            return;
        }
        File target = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));

        try(InputStream in = new BufferedInputStream(new FileInputStream(updateTarget));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(target)))
        {
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Delete the src File from the dataFolder
        updateTarget.delete();

    }

    private String getLatestUpdateTitle() {
        String rVal = null;
        try {
            URL url = new URL(GET_LATEST_UPDATE);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", MY_USER_AGENT);

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't check for a newer Update, HTTP Error code: "+responseCode);
                return null;
            }

            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            JsonElement e = ReflectionHelper.getInstance().parseReader(isr);

            if(e instanceof JsonArray ja) {
                JsonObject jo = (JsonObject) ja.get(0);
                rVal = jo.get("title").getAsString();
            }else{
                rVal = "Some Error happened by parsing the Update Title. Sorry.";
            }

            isr.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rVal;
    }


    public boolean isUpdateAvailable(){
        return updateAvailable;
    }

    public String getLatestVersion(){
        return latestVersion;
    }


}
