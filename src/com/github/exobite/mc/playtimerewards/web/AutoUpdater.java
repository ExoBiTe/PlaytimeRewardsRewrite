package com.github.exobite.mc.playtimerewards.web;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.ReflectionHelper;
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
                if(checkForNewerVersion(true, true, true)) {
                    updateAvailable = true;
                    //Send the Message on the Main thread for a nicer-looking console prefix.
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PluginMaster.sendConsoleMessage(Level.INFO, "A new Version (v"+latestVersion+") is available!");
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

    private boolean checkForNewerVersion(boolean checkAtMajor, boolean checkAtMinor, boolean checkAtPatch) {
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
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't find the newest Latest Version in the HTTP Response.");
                return false;
            }

            isr.close();
            is.close();
        } catch (IOException e) {
            latestVersion = "0.0.0";
        }

        return latestIsNewer(latestVersion, currentVersion, checkAtMajor, checkAtMinor, checkAtPatch);
    }

    private String unpackVersionFromJson(JsonElement e){
        if(!(e instanceof JsonObject jo)) return null;
        return jo.get("name").getAsString();
    }

    private boolean latestIsNewer(String versionLatest, String versionLocal, boolean checkAtMajor, boolean checkAtMinor, boolean checkAtPatch) {
        String[] splitLatest = versionLatest.split("\\.");
        String[] splitLocal = versionLocal.split("\\.");
        if(splitLatest.length<2 || splitLocal.length<2) {
            return false;
        }
        int[] numbersLatest = new int[splitLatest.length];
        int[] numbersLocal = new int[splitLocal.length];
        for(int i=0;i<splitLatest.length;i++) {
            numbersLatest[i] = Integer.parseInt(splitLatest[i]);
        }
        for(int i=0;i<splitLocal.length;i++) {
            numbersLocal[i] = Integer.parseInt(splitLocal[i]);
        }
        return (checkAtMajor && numbersLatest[0] > numbersLocal[0]) ||
                (checkAtMinor && numbersLatest[0] >= numbersLocal[0] && numbersLatest[1] > numbersLocal[1]) ||
                (checkAtPatch && (numbersLatest.length > numbersLocal.length ||
                        (numbersLatest.length >= 3 && numbersLatest[2] > numbersLocal[2])));

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
            PluginMaster.sendConsoleMessage(Level.INFO, "Updated jar downloaded (took "+(System.currentTimeMillis()-ms)+" ms)!");

        } catch(IOException e){
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't download the Update.");
            e.printStackTrace();
        }
    }

    public void moveUpdate(){
        if(!downloadedUpdate) return;
        URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        if(!updateTarget.exists()) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't update the Plugin, can't find the downloaded File.");
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


    public boolean isUpdateAvailable(){
        return updateAvailable;
    }

    public String getLatestVersion(){
        return latestVersion;
    }


}
