package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import hu.trigary.advancementcreator.Advancement;
import hu.trigary.advancementcreator.shared.ItemObject;
import hu.trigary.advancementcreator.trigger.ImpossibleTrigger;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class AdvancementManager {

    public static void register() {
        if(instance==null) {
            instance = new AdvancementManager();
        }
    }

    public static AdvancementManager getInstance() {
        return instance;
    }

    private static AdvancementManager instance;

    private final List<String> registeredAdvancements = new ArrayList<>();

    private AdvancementManager() {
        createDummyAdvancements();
    }

    private Advancement createAdvancement(String name, Material icon, String title, String description) {
        if(registeredAdvancements.contains(name)) return null;
        Advancement rv = new hu.trigary.advancementcreator.Advancement(new NamespacedKey(PluginMaster.getInstance(), name),
                new ItemObject().setItem(icon),
                new TextComponent(title), new TextComponent(description))
                .setAnnounce(false);
        registeredAdvancements.add(name);
        return rv;
    }

    private void createDummyAdvancements() {
        Advancement a = createAdvancement("reward1/root", Material.GLOWSTONE, "I'm a Advancement",
                "Back in my Days this was called Achievement!");
        a.setFrame(Advancement.Frame.GOAL);
        a.addTrigger("dummy", new ImpossibleTrigger());
        a.makeRoot("blocks/tnt", true);
        a.activate(false);

        Advancement a2 = createAdvancement("reward1/gogo", Material.NAME_TAG, "I'm the 2nd", "Number twooo!");
        a2.setFrame(Advancement.Frame.TASK);
        a2.addTrigger("dummy", new ImpossibleTrigger());
        a2.makeChild(a.getId());
        a2.activate(true);
    }

    public Advancement getAdvancementFromConfigSection(FileConfiguration conf, String key) {
        //TODO: Create advancement formatting in yml, and read it here
        //Make sure that we don't call bukkit stuff async (Advancement#activate) may call bukkit stuff, need to check that
        return null;
    }

    public boolean awardAdvancement(Player p, Advancement a) {
        org.bukkit.advancement.Advancement bukkitAdvancement = Bukkit.getAdvancement(a.getId());
        if(bukkitAdvancement==null) return false;
        AdvancementProgress ap = p.getAdvancementProgress(bukkitAdvancement);
        for(String criteria:ap.getRemainingCriteria()) {
            ap.awardCriteria(criteria);
        }
        return true;
    }

    public void deleteAdvancementFiles() {
        final String path = "datapacks" +
                File.separator + "bukkit" +
                File.separator + "data" +
                File.separator + PluginMaster.getInstance().getDescription().getName().toLowerCase(Locale.ROOT) +
                File.separator + "advancements";
        for(World w:Bukkit.getWorlds()) {
            if(w.getEnvironment() == World.Environment.NETHER || w.getEnvironment() == World.Environment.THE_END) continue;
            File folder = new File(w.getWorldFolder(), path);
            if(!deleteAllFilesInFolder(folder)) {
                PluginMaster.sendConsoleMessage(Level.SEVERE, "Failed to delete temporary Advancements from your worlds.\n" +
                        "Please delete them yourself from all world folders, they're in the following path:\n"+
                        w.getWorldFolder()+File.separator+path);
            }
        }
    }

    private boolean deleteAllFilesInFolder(File folder) {
        if(!folder.isDirectory()) return false;
        boolean rv = true;
        for(File f:folder.listFiles()) {
            if(f.isDirectory()) {
                rv = deleteAllFilesInFolder(f) && rv;
            }
            rv = f.delete() && rv;
        }
        return rv;
    }

}
