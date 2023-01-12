package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewardEditSession extends RewardOptions {

    private static final Material INPUT_STRING_ITEM = Material.WRITABLE_BOOK;
    private static final Material INPUT_BOOL_TRUE_ITEM = Material.LIME_DYE;
    private static final Material INPUT_BOOL_FALSE_ITEM = Material.RED_DYE;
    private static final Material INPUT_TIME_ITEM = Material.CLOCK;
    private static final Material INPUT_ARRAY_ITEM = Material.SHULKER_BOX;
    private static final Material INPUT_PARTICLE_ITEM = Material.FIREWORK_ROCKET;
    private static final Material INPUT_SOUND_ITEM = Material.NOTE_BLOCK;

    private final UUID editor;
    private final Reward rw;
    private final Map<String, GUIManager.GUI> guis = new HashMap<>();

    protected RewardEditSession(UUID editor, Reward rw) {
        super(rw.getName(), rw.getType(), rw.timeMs, rw.isRepeating, rw.grantFirst);
        this.editor = editor;
        this.rw = rw;
    }

    private void copyContents(RewardOptions source, RewardOptions target) {
        //name & type aren't copied
        target.setDisplayName(source.getDisplayName());
        target.setRepeating(source.isRepeating());
        target.setGrantFirst(source.grantFirst());
        target.setTimeMs(source.getTimeMs());
        target.setConsoleCommands(source.getConsoleCommands());
        target.setPlayerMessages(source.getPlayerMessages());
        target.setGlobalMessages(source.getGlobalMessages());
        target.setActionBarMessage(source.getActionBarMessage());
        target.setParticles(source.getParticles());
        target.setSounds(source.getSounds());
        target.setNeededPermission(source.getPermissionNeeded());
    }

    protected void onPlayerChat(AsyncPlayerChatEvent e) {

    }



}
