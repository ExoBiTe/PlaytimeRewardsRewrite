package com.github.exobite.mc.playtimerewards.main;

import com.github.exobite.mc.playtimerewards.gui.CustomItem;
import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.gui.GUIManagerOLD;
import com.github.exobite.mc.playtimerewards.gui.guiHolder;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData implements guiHolder {

    //Object
    private long loginTimestamp;
    private UUID id;

    private Map<String, GUIManagerOLD.GUI> Guis;

    public GUIManager.GUI GUI;

    PlayerData(Player p) {
        id = p.getUniqueId();
        loginTimestamp = Utils.getPlaytimeInMS(p);
        Guis = new HashMap<>();

        GUIManagerOLD.GUI g = GUIManagerOLD.createGui("Testgui", 27, true);
        Guis.put("Testgui", g);
        CustomItem ci = new CustomItem(Material.AMETHYST_SHARD);
        ci.setDisplayName("Click me!");
        g.setItemFunc(ci, 0, "", true, ClickType.LEFT, null);

        GUI = GUIManager.createGUI("I´m a cool GUI", 18);
        GUI.canClose(false);

    }

    public long getSessionTime() {
        return Utils.getPlaytimeInMS(p()) - loginTimestamp;
    }

    public Player p() {
        return Bukkit.getPlayer(id);
    }

    @Override
    public Map<String, GUIManagerOLD.GUI> getGuis() {
        return Guis;
    }

    @Override
    public Map<String, String> getPlaceholders() {
        return null;
    }

    @Override
    public void addGui(String internalName, GUIManagerOLD.GUI Gui) {

    }
}
