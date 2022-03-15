package com.github.exobite.mc.playtimerewards.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GUIManager implements Listener {

    //Static stuff
    private static GUIManager instance;

    //Instance
    private final JavaPlugin main;
    private final Map<Inventory, GUI> guis;
    private final Map<UUID, Boolean> canClose;

    //Singleton
    /**
     * Registers the GUI Manager for this Plugin
     * @param main The JavaPlugin Instance
     * @return The GUIManager instance
     */
    public static GUIManager registerGUIManager(JavaPlugin main){
        if(instance==null){
            instance = new GUIManager(main);
        }
        return instance;
    }

    public static GUIManager getInstance() {
        return instance;
    }

    public GUI createGUI(String title, int size){
        return new GUI(title, size);
    }


    /**
     * @param main The Plugins JavaPlugin instance
     */
    private GUIManager(JavaPlugin main) {
        this.main = main;
        //Register the needed Events:
        // - InventoryClickEvent
        // - InventoryCloseEvent
        main.getServer().getPluginManager().registerEvents(this, main);
        guis = new HashMap<>();
        canClose = new HashMap<>();
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e){
        Inventory i = e.getClickedInventory();
        if(!guis.containsKey(i)) return;
        GUI g = guis.get(i);
        if(g.cancelAll) e.setCancelled(true);
        int slot = e.getSlot();
        GUISlot gs = g.getSlotData(slot, false);
        if(gs==null) return;
        gs.action.click(e, g);
        if(gs.cancelClick) e.setCancelled(true);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent e){
        if(guis.containsKey(e.getInventory())) {
            final GUI g = guis.get(e.getInventory());
            final HumanEntity p = e.getPlayer();
            final UUID id = p.getUniqueId();
            if(!g.canClose && !allowNextGUIClose(id)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.openInventory(g.inv);
                    }
                }.runTaskLater(main, 1L);
            }
            setAllowNextGUIClose(false, id);
        }
    }

    public void setAllowNextGUIClose(boolean allow, UUID id) {
        canClose.put(id, allow);
    }

    public boolean allowNextGUIClose(UUID id) {
        return canClose.getOrDefault(id, false);
    }


    //GUI Class
    public class GUI {

        private Inventory inv;
        private boolean canClose;
        private boolean cancelAll;
        private Map<Integer, GUISlot> slotData;

        private GUI(String title, int size) {
            inv = Bukkit.createInventory(null, size, title);
            slotData = new HashMap<>();
            GUIManager.instance.guis.put(inv, this);
        }

        /**
         * Deletes the GUI and all it´s contents
         */
        public void deleteGUI(){
            GUIManager.instance.guis.remove(inv);
            inv = null;
            slotData = null;
        }

        /**
         * Opens the Inventory for the specified Player
         * @param p The target Player
         */
        public void openInventory(HumanEntity p){
            p.openInventory(inv);
        }

        /**
         * Specifies if the Inventory can be closed by the Player
         * @param canClose Allow the Player to Close the Inventory
         * @return The GUI Instance
         */
        public GUI canClose(boolean canClose){
            this.canClose = canClose;
            return this;
        }

        public GUI cancelAll(boolean cancelAll) {
            this.cancelAll = cancelAll;
            return this;
        }

        public GUI setItemstack(int slot, ItemStack is) {
            if(slot > inv.getSize() || slot < 0) return this;
            GUISlot gs = getSlotData(slot, true);
            gs.setItemStack(is);
            return this;
        }

        public GUI setSlotAction(int slot, GUIClickAction action) {
            if(slot > inv.getSize() || slot < 0) return this;
            GUISlot gs = getSlotData(slot, true);
            gs.setAction(action);
            return this;
        }

        /**
         * Searches for an existing GUISlot and creates a new Instance if none is found.
         * @param slot  The Target Slot
         * @param createIfNeeded    Specifies if the method create an empty guiSlot or not
         * @return  A new or existing GUISlot Instance
         */
        private GUISlot getSlotData(int slot, boolean createIfNeeded) {
            GUISlot gs;
            if(slotData.containsKey(slot)) {
                gs = slotData.get(slot);
            }else if(createIfNeeded) {
                gs = new GUISlot(this, slot);
                slotData.put(slot, gs);
            }else{
                gs = null;
            }
            return gs;
        }

        /**
         * Copies the data from the "from" slot to the "to" slot
         * @param from  Which Slot should be copied
         * @param to    The Target Slot
         */
        public void copySlot(int from, int to) {
            if(from<1 || from > inv.getSize()-1 || to<1 || to>inv.getSize()-1) return;
            GUISlot src = slotData.get(from);
            if(src==null) return;
            GUISlot tar = src.copySlotTo(to);
            slotData.put(to, tar);
        }

        public int getSize(){
            return inv.getSize();
        }

    }

    private class GUISlot {

        private final GUI g;
        private final int slot;
        private boolean cancelClick;
        private GUIClickAction action;

        GUISlot(GUI g, int slot) {
            this.g = g;
            this.slot = slot;
        }

        GUISlot setItemStack(ItemStack is){
            g.inv.setItem(slot, is);
            return this;
        }

        GUISlot setAction(GUIClickAction action) {
            this.action = action;
            return this;
        }

        GUISlot copySlotTo(int target) {
            GUISlot tSlot = new GUISlot(g, target);
            tSlot.cancelClick = cancelClick;
            tSlot.action = action;
            tSlot.setItemStack(g.inv.getItem(slot));
            return tSlot;
        }

    }

    public abstract static class GUIClickAction {
        /**
         *
         * @param e The corresponding ClickEvent
         */
        protected abstract void click(InventoryClickEvent e, GUI gui);
    }

    //Utility, default methods

    public GUIClickAction getDefaultCloseGUIAction(){
        return new GUIClickAction() {
            @Override
            protected void click(InventoryClickEvent e, GUI gui) {
                UUID id = e.getWhoClicked().getUniqueId();
                setAllowNextGUIClose(true, id);
                e.getWhoClicked().closeInventory();
            }
        };
    }

    public abstract class GUIHolder {
        public abstract Map<String, GUI> getGUIs();

        public void removeGUIS(){
            for(GUI g:getGUIs().values()) {
                g.deleteGUI();
            }
            getGUIs().clear();
        }
    }


}
