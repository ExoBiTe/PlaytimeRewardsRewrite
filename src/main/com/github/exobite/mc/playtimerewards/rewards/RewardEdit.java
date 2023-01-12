package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.gui.CustomItem;
import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import com.github.exobite.mc.playtimerewards.utils.Msg;
import com.github.exobite.mc.playtimerewards.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private static final Material INPUT_SOUND_ITEM = Material.NOTE_BLOCK;

    private final UUID editor;
    private final Reward rw;
    private final Map<String, GUIManager.GUI> guis = new HashMap<>();
    private final RewardEdit inst;

    private ChatInputAction nextAction;
    private GUIManager.GUI guiAfterChat;

    protected RewardEdit(@NotNull Reward rw, @NotNull Player p) {
        super(rw.getName(), rw.getType(), rw.timeMs, rw.isRepeating, rw.grantFirst);
        inst = this;
        rw.setEditStatus(true); //Block reward for other Edits
        editor = p.getUniqueId();
        this.rw = rw;
        copyFields(rw, inst);
        createMainGui();
        guis.get("main").openInventory(p);
    }

    private void copyFields(@NotNull RewardOptions src, @NotNull RewardOptions dst) {
        for(Field f:src.getClass().getSuperclass().getDeclaredFields()) {
            //Only copy protected fields
            if(!Modifier.isProtected(f.getModifiers())) continue;
            try {
                dst.getClass().getSuperclass().getDeclaredField(f.getName()).set(dst, f.get(src));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                //None of them should ever be called, as they are both the same class
                //and this method only accesses protected fields
                e.printStackTrace();
            }
        }
    }

    void openMainGUI() {
        Player p = Bukkit.getPlayer(editor);
        if(p==null) {
            discardChanges();
            return;
        }
        guis.get("main").openInventory(p);
        nextAction = null;
        guiAfterChat = null;
    }

    protected void passStringFromChat(Player p, String message) {
        if(nextAction!=null) {
            String dat = message.replace("&", Config.getInstance().getColorCode() + "");
            dat = dat.replaceAll("[\\\\\"]+", "");   //Remove backslashes & quotation marks
            ChatResponse r = nextAction.parseInput(p, dat);
            if(r.success){
                nextAction = null;
                if(guiAfterChat!=null) {
                    guiAfterChat.openInventory(p);
                    guiAfterChat = null;
                }
            }
            p.sendMessage(Lang.getInstance().getMessage(p, r.message, r.args));
        }
    }

    private void renewAllGuis() {
        for(GUIManager.GUI g: guis.values()) {
            g.deleteGUI();
        }
        guis.clear();
        createMainGui();
    }

    private void createMainGui() {
        Lang langInst = Lang.getInstance();
        String guiTitle = langInst.getMessage(Msg.GUI_EDIT_REWARD_WINDOWNAME, rw.getName());
        GUIManager.GUI editGui = GUIManager.getInstance().createGUI(guiTitle, 27)
                .cancelAll(true)
                .canClose(false);

        CustomItem ci = new CustomItem(Material.BARRIER)
                .setDisplayName(langInst.getMessage(Msg.GUI_EDIT_REWARD_EXIT_NOSAVE_NAME))
                .setLoreFromString(langInst.getMessage(Msg.GUI_EDIT_REWARD_EXIT_NOSAVE_LORE));
        editGui.setItemstack(18, ci.getItemStack());
        editGui.setSlotAction(18, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                Player p = (Player) e.getWhoClicked();
                p.closeInventory();
                discardChanges();
                p.sendMessage(Lang.getInstance().getMessage(p, Msg.CMD_SUC_PTR_EDIT_ABORTED));
            }
        });

        ci = new CustomItem(Material.EMERALD)
                .setDisplayName(langInst.getMessage(Msg.GUI_EDIT_REWARD_EXIT_SAVE_NAME))
                .setLoreFromString(langInst.getMessage(Msg.GUI_EDIT_REWARD_EXIT_SAVE_LORE));
        editGui.setItemstack(26, ci.getItemStack());
        editGui.setSlotAction(26, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                Player p = (Player) e.getWhoClicked();
                p.closeInventory();
                saveDataToReward();
                p.sendMessage(Lang.getInstance().getMessage(p, Msg.CMD_SUC_PTR_EDIT_SAVED));
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

    private void setFieldDataToSlot(GUIManager.@NotNull GUI gui, final int slot, final Field f) {
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
        String fieldname = Lang.getInstance().getMessage(Msg.valueOf("GUI_EDIT_TRANSL_" + f.getName().toUpperCase(Locale.ROOT)));
        String name = Lang.getInstance().getMessage(Msg.GUI_EDIT_REWARD_FIELD_ITEM_NAME, fieldname);
        String lore = "";
        try {
            if(f.get(inst) == null){
                lore = "";
            }else if(m == INPUT_ARRAY_ITEM) {
                //Item is Array; Show ArraySize instead of Value
                lore = getArrayFromField(f).length + " Entries";
            }else if(m == INPUT_TIME_ITEM) {
                //Item is the Time, Parse it to a readable Format
                lore = Utils.formatTimeMsToString((long) f.get(inst), "%d%h%m%s");
            }else{
                lore = f.get(inst).toString();
            }
        }catch (IllegalAccessException ignored){
        }
        CustomItem ci = new CustomItem(m).setDisplayName(name).setLoreFromString(lore);
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

    private void clickedItem(@NotNull HumanEntity clicker, final @NotNull Field f) {
        //Array(or Member) is printed as [java.lang String, single-String field is printed as java.lang.String
        if(f.getType().isArray()) {
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
            createAndOpenArrayGUI(f).openInventory(clicker);
        }else if(f.getType()==String.class) {
            nextAction = new ChatInputAction() {
                @Override
                ChatResponse parseInput(Player p, String input) {
                    try {
                        f.set(inst, input);
                        return new ChatResponse(true, Msg.CMD_SUC_PTR_EDIT_TYPE_IN_CHAT, "DisplayName", input);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return new ChatResponse(false, Msg.CMD_ERR_PTR_EDIT_UNKNOWN);
                }
            };
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
            clicker.sendMessage(Lang.getInstance().getMessage((Player) clicker, Msg.CMD_NOTIF_PTR_EDIT_TYPE_IN_CHAT));
        }else if(f.getType()==long.class && f.getName().equals("timeMs")) {
            nextAction = new ChatInputAction() {
                @Override
                ChatResponse parseInput(Player p, String input) {
                    long newms = Utils.convertTimeStringToMS(input);
                    if(newms>0) {
                        try {
                            f.set(inst, newms);
                            return new ChatResponse(true, Msg.CMD_SUC_PTR_EDIT_TYPE_IN_CHAT, "Time", String.valueOf(newms));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                    return new ChatResponse(false, Msg.CMD_ERR_PTR_EDIT_UNKNOWN);
                }
            };
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
            clicker.sendMessage(Lang.getInstance().getMessage((Player) clicker, Msg.CMD_NOTIF_PTR_EDIT_TYPE_IN_CHAT));
        }
    }

    private void clickedItemInArray(@NotNull HumanEntity clicker, final @NotNull Field f, final @NotNull Object arrayInstance, int index) {
        if(!arrayInstance.getClass().isArray()) {
            return;
        }
        System.out.println(arrayInstance.getClass().getName() + ", " + RewardParticle.class.getName());
        String strippedName = arrayInstance.getClass().getName().substring(2).substring(0, arrayInstance.getClass().getName().length()-3);
        if(strippedName.equals(String.class.getName())) {
            nextAction = new ChatInputAction() {
                @Override
                ChatResponse parseInput(Player p, String input) {
                    Array.set(arrayInstance, index, input);
                    return new ChatResponse(true, null);
                }
            };
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
        }else if(strippedName.equals(long.class.getName())) {
            nextAction = new ChatInputAction() {
                @Override
                ChatResponse parseInput(Player p, String input) {
                    long newms = Utils.convertTimeStringToMS(input);
                    Array.set(arrayInstance, index, newms);
                    return new ChatResponse(true, null);
                }
            };
            GUIManager.getInstance().setAllowNextGUIClose(true, clicker.getUniqueId());
            clicker.closeInventory();
        }else if(strippedName.equals(RewardParticle.class.getName())) {
            //TODO: Add RewardParticle Handling
            createRewardParticleGUI(f, arrayInstance, index, clicker);
        }else if(strippedName.equals(RewardSound.class.getName())) {
            //TODO: Add RewardSound Handling

        }

    }

    @NotNull
    private GUIManager.GUI createAndOpenArrayGUI(final @NotNull Field f) {
        System.out.println("Called 'create&open gui'!");
        GUIManager.GUI g = GUIManager.getInstance().createGUI("Array "+f.getName(), 27)
                .cancelAll(true)
                .canClose(false);
        Object[] content = getArrayFromField(f);
        Object arrayInst = null;
        Material m;
        try {
            arrayInst = f.get(inst);
            if(content.length>0) {
                //Get Array Type
                if(content[0].getClass()==String.class) {
                    m = INPUT_STRING_ITEM;
                }else if(content[0].getClass()==Boolean.class) {
                    m = (boolean) content[0] ? INPUT_BOOL_TRUE_ITEM : INPUT_BOOL_FALSE_ITEM;
                }else if(content[0].getClass()==Long.class) {
                    m = INPUT_TIME_ITEM;
                }else if(content[0].getClass()==RewardParticle.class) {
                    m = INPUT_PARTICLE_ITEM;
                }else if(content[0].getClass()==RewardSound.class) {
                    m = INPUT_SOUND_ITEM;
                }else{
                    //Unknown
                    m = Material.BEDROCK;
                }
            }else{
                //Array is empty, no Items added to the GUI.
                m = Material.BEDROCK;
            }
        } catch (IllegalAccessException e) {
            content = new Object[0];
            e.printStackTrace();
            m = Material.BEDROCK;
        }
        int idx = 0;
        for(Object e:content) {
            String name = "Debug  idx "+ (idx);
            String lore = ChatColor.AQUA+e.toString()+ChatColor.RESET+"\nClick to edit!";
            g.setItemstack(idx, new CustomItem(m).setDisplayName(name).setLoreFromString(lore).getItemStack());
            final int fidx = idx;
            Object finalArrayInst = arrayInst;
            g.setSlotAction(idx, new GUIManager.GUIClickAction() {
                @Override
                protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                    clickedItemInArray(e.getWhoClicked(), f, finalArrayInst, fidx);
                    guiAfterChat = g;
                }
            });
            idx++;
            //Only support 18 Entries, last row is needed for GUI Control
            if(idx>=g.getSize() || idx>17) break;
        }
        g.setItemstack(18, new CustomItem(Material.BARRIER).setDisplayName("Go back").getItemStack());
        g.setSlotAction(18, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                guis.get("main").openInventory(e.getWhoClicked());
                g.deleteGUI();
            }
        });
        if(content.length<18) {
            g.setItemstack(26, new CustomItem(Material.BEACON).setDisplayName("Add Entry").getItemStack());
            g.setSlotAction(26, new GUIManager.GUIClickAction() {
                @Override
                protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                    GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());

                    guis.get("main").openInventory(e.getWhoClicked());
                    g.deleteGUI();
                }
            });
        }


        return g;
    }

    private Object[] getArrayFromField(Field f) {
        Object[] content;
        try {
            Object arrayInst = null;
            arrayInst = f.get(inst);
            int len = Array.getLength(arrayInst);
            content = new Object[len];
            for(int i=0;i<len;i++) {
                content[i] = Array.get(arrayInst, i);
            }
        }catch(IllegalAccessException e){
            return new Object[]{};
        }
        return content;
    }

    private void createRewardParticleGUI(final @NotNull Field f, final @NotNull Object arrayInstance, int index, HumanEntity he) {
        RewardParticle rp = (RewardParticle) Array.get(arrayInstance, index);
        GUIManager.GUI g = GUIManager.getInstance().createGUI("Edit Particle", 27)
                .cancelAll(true)
                .canClose(false);
        //Go Back
        g.setItemstack(18, new CustomItem(Material.BARRIER).setDisplayName("Go Back").getItemStack());
        g.setSlotAction(18, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                guiAfterChat.openInventory(e.getWhoClicked());
                guiAfterChat = null;
                g.deleteGUI();
            }
        });

        g.setItemstack(22, new CustomItem(Material.LAVA_BUCKET).setDisplayName("Delete Entry").getItemStack());
        g.setSlotAction(22, new GUIManager.GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUIManager.GUI gui) {
                //TODO: Remove Object in srcArray at correct Index, write new Array to Field.
                GUIManager.getInstance().setAllowNextGUIClose(true, e.getWhoClicked().getUniqueId());
                guiAfterChat.openInventory(e.getWhoClicked());
                guiAfterChat = null;
                g.deleteGUI();
            }
        });
        GUIManager.getInstance().setAllowNextGUIClose(true, he.getUniqueId());
        g.openInventory(he);
    }

    private void saveDataToReward() {
        //TODO: Copy data to Reward
        copyFields(inst, rw);
        RewardManager.getInstance().setRewardsChanged();
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

    private abstract static class ChatInputAction {

        abstract ChatResponse parseInput(Player p, String input);

    }

    private static class ChatResponse {

        boolean success;
        Msg message;
        String[] args;

        ChatResponse(boolean success, @Nullable Msg error, @Nullable String ... args) {
            this.success = success;
            this.message = error;
            this.args = args;
        }

    }

}
