package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.gui.CustomItem;
import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;

public class RewardEdit extends RewardOptions {

    private static final Material INPUT_STRING_ITEM = Material.WRITABLE_BOOK;
    private static final Material INPUT_BOOL_TRUE_ITEM = Material.LIME_DYE;
    private static final Material INPUT_BOOL_FALSE_ITEM = Material.RED_DYE;
    private static final Material INPUT_TIME_ITEM = Material.CLOCK;
    private static final Material INPUT_ARRAY_ITEM = Material.SHULKER_BOX;
    private static final Material INPUT_PARTICLE_ITEM = Material.FIREWORK_ROCKET;
    private static final Material INPUT_SOUND_ITEM = Material.MUSIC_DISC_CHIRP;

    private final UUID editor;
    private final Reward rw;
    private final Map<String, GUIManager.GUI> guis = new HashMap<>();

    protected RewardEdit(Reward rw, Player p) {
        super(rw.getName(), rw.getType(), rw.timeMs, rw.isRepeating, rw.grantFirst);
        rw.setEditStatus(true); //Block reward for other Edits
        editor = p.getUniqueId();
        this.rw = rw;
        copyFields(rw, this);
        createMainGui();
        guis.get("main").openInventory(p);
    }

    private void copyFields(RewardOptions src, RewardOptions dst) {
        long ms = System.currentTimeMillis();
        for(Field f:src.getClass().getDeclaredFields()) {
            //Only copy protected fields
            if(!Modifier.isProtected(f.getModifiers())) continue;
            try {
                dst.getClass().getField(f.getName()).set(dst, f.get(src));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                //None of them should ever be called, as they are both the same class
                //and this method only accesses protected fields
                e.printStackTrace();
            }
        }
        PluginMaster.sendConsoleMessage(Level.INFO, "Copying took "+(System.currentTimeMillis()-ms)+"ms.");
    }

    protected void passStringFromChat(String message) {

    }

    private void createMainGui() {
        Lang langInst = Lang.getInstance();
        String guiTitle = langInst.getMessageWithArgs("GUI_EDIT_REWARD_WINDOWNAME", rw.getName());
        GUIManager.GUI editGui = GUIManager.getInstance().createGUI(guiTitle, 27);
        editGui.canClose(false);

        CustomItem ci = new CustomItem(Material.BARRIER)
                .setDisplayName(langInst.getMessageWithArgs("GUI_EDIT_REWARD_EXIT_NOSAVE_NAME"))
                .setLoreFromString(langInst.getMessageWithArgs("GUI_EDIT_REWARD_EXIT_NOSAVE_LORE"));
        editGui.setItemstack(18, ci.getItemStack());
        editGui.setSlotAction(18, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                e.getWhoClicked().closeInventory();
                discardChanges();
            }
        });

        ci = new CustomItem(Material.EMERALD)
                .setDisplayName(langInst.getMessageWithArgs("GUI_EDIT_REWARD_EXIT_SAVE_NAME"))
                .setLoreFromString(langInst.getMessageWithArgs("GUI_EDIT_REWARD_EXIT_SAVE_LORE"));
        editGui.setItemstack(26, ci.getItemStack());
        editGui.setSlotAction(26, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                e.getWhoClicked().closeInventory();
                saveDataToReward();
            }
        });

        int idx = 0;
        for(Field f:RewardOptions.class.getDeclaredFields()) {
            if(!Modifier.isProtected(f.getModifiers())) continue;
            setFieldDataToSlot(editGui, idx, f);
            idx++;
        }


        guis.put("main", editGui);
    }

    private void setFieldDataToSlot(GUIManager.GUI gui, final int slot, final Field f) {
        if(slot>=gui.getSize()) return;
        Material m;
        if(f.getType().isArray()) {
            m = INPUT_ARRAY_ITEM;
        }else if(f.getType()==String.class) {
            m = INPUT_STRING_ITEM;
        }else if(f.getType()==boolean.class) {
            try {
                m = (boolean) f.get(this) ? INPUT_BOOL_TRUE_ITEM : INPUT_BOOL_FALSE_ITEM;
            } catch (IllegalAccessException e) {
                m = INPUT_BOOL_FALSE_ITEM;
            }
        }else if(f.getType()==long.class) {
            m = INPUT_TIME_ITEM;
        }else if(f.getType()==RewardParticle.class) {
            m = INPUT_PARTICLE_ITEM;
        }else if(f.getType()==RewardSound.class) {
            m = INPUT_SOUND_ITEM;
        }else{
            //Unknown
            m = Material.BEDROCK;
        }
        String fieldname = Lang.getInstance().getMessageWithArgs("GUI_EDIT_TRANSL_" + f.getName().toUpperCase(Locale.ROOT));
        String name = Lang.getInstance().getMessageWithArgs("GUI_EDIT_REWARD_FIELD_ITEM_NAME", fieldname);
        CustomItem ci = new CustomItem(m).setDisplayName(name);
        gui.setItemstack(slot, ci.getItemStack());

        GUIManager.GUIClickAction action = new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {

            }
        };
        gui.setSlotAction(slot, action);
    }

    private void saveDataToReward() {
        //TODO: Copy data to Reward
        cleanUp();
    }

    protected void forceClose(){
        Player p = Bukkit.getPlayer(editor);
        if(p!=null) {
            GUIManager.getInstance().setAllowNextGUIClose(true, editor);
            p.closeInventory();
            p.sendMessage(ChatColor.RED + "The Reward Edit got cancelled because a forced reload was requested.\nYour editing Progress is lost.");
        }
        discardChanges();
    }

    protected void discardChanges() {
        //TODO: Just remove everything
        cleanUp();
    }

    private void cleanUp() {
        RewardManager.getInstance().removeFromEditMap(editor);
        rw.setEditStatus(false);
    }

}
