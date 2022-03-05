package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.gui.CustomItem;
import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

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
    private final RewardEdit inst;

    private ChatInputAction nextAction;
    private GUIManager.GUI guiAfterChat;

    protected RewardEdit(Reward rw, Player p) {
        super(rw.getName(), rw.getType(), rw.timeMs, rw.isRepeating, rw.grantFirst);
        inst = this;
        rw.setEditStatus(true); //Block reward for other Edits
        editor = p.getUniqueId();
        this.rw = rw;
        copyFields(rw, inst);
        createMainGui();
        guis.get("main").openInventory(p);
    }

    private void copyFields(RewardOptions src, RewardOptions dst) {
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
    }

    protected void passStringFromChat(Player p, String message) {
        if(nextAction!=null) {
            if(nextAction.parseInput(p, message)){
                nextAction = null;
                if(guiAfterChat!=null) {
                    guiAfterChat.openInventory(p);
                    guiAfterChat = null;
                }
            }else{
                p.sendMessage("Error, try again.");
            }
        }
    }

    private void createMainGui() {
        Lang langInst = Lang.getInstance();
        String guiTitle = langInst.getMessageWithArgs("GUI_EDIT_REWARD_WINDOWNAME", rw.getName());
        GUIManager.GUI editGui = GUIManager.getInstance().createGUI(guiTitle, 27)
                .cancelAll(true)
                .canClose(false);

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
                m = (boolean) f.get(inst) ? INPUT_BOOL_TRUE_ITEM : INPUT_BOOL_FALSE_ITEM;
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
                clickedItem(e.getWhoClicked(), f);
                guiAfterChat = guis.get("main");
            }
        };
        gui.setSlotAction(slot, action);
    }

    private void clickedItem(HumanEntity clicker, final Field f) {
        if(f.getType().isArray()) {
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
            createAndOpenArrayGUI(f).openInventory(clicker);
        }else if(f.getType()==String.class) {
            nextAction = new ChatInputAction() {
                @Override
                boolean parseInput(Player p, String input) {
                    try {
                        f.set(inst, input);
                        return true;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
        }else if(f.getType()==long.class && f.getName().equals("timeMs")) {
            nextAction = new ChatInputAction() {
                @Override
                boolean parseInput(Player p, String input) {
                    long newms = Utils.convertTimeStringToMS(input);
                    if(newms>0) {
                        try {
                            f.set(inst, newms);
                            return true;
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            };
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
        }
    }

    private GUIManager.GUI createAndOpenArrayGUI(final Field f) {
        GUIManager.GUI g = GUIManager.getInstance().createGUI("Array "+f.getName(), 27)
                .cancelAll(true)
                .canClose(false);
        Material m;
        if(f.getType()==String.class) {
            m = INPUT_STRING_ITEM;
        }else if(f.getType()==boolean.class) {
            try {
                m = (boolean) f.get(inst) ? INPUT_BOOL_TRUE_ITEM : INPUT_BOOL_FALSE_ITEM;
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
        Object[] content;
        try {
            f.setAccessible(true);
            Object arr = f.get(inst);
            System.out.println(f.getName()+", "+f.getType()+", "+f.getType().isArray());
            System.out.println(inst + ", " + arr);
            int len = Array.getLength(arr);
            content = new Object[len];
            for(int i=0;i<len;i++) {
                content[i] = Array.get(arr, i);
            }
        } catch (IllegalAccessException e) {
            content = new Object[0];
            e.printStackTrace();
        }
        int idx = 2;
        for(Object e:content) {
            g.setItemstack(idx, new CustomItem(m).setDisplayName(String.valueOf(idx)).getItemStack());
            g.setSlotAction(idx, new GUIManager.GUIClickAction() {
                @Override
                protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                    clickedItem(e.getWhoClicked(), f);
                    guiAfterChat = g;
                }
            });
            idx++;
            if(idx>=g.getSize()) break;
        }
        g.setItemstack(0, new CustomItem(Material.BARRIER).setDisplayName("Go back").getItemStack());
        g.setSlotAction(0, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                guiAfterChat = guis.get("main");
                g.deleteGUI();
            }
        });
        return g;
    }

    private void saveDataToReward() {
        //TODO: Copy data to Reward
        copyFields(inst, rw);
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
        for(GUIManager.GUI g:guis.values()) {
            g.deleteGUI();
        }
        RewardManager.getInstance().removeFromEditMap(editor);
        rw.setEditStatus(false);
    }

    private abstract class ChatInputAction {

        abstract boolean parseInput(Player p, String input);

    }

}
