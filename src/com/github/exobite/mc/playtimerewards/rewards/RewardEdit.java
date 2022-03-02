package com.github.exobite.mc.playtimerewards.rewards;

import com.github.exobite.mc.playtimerewards.gui.CustomItem;
import com.github.exobite.mc.playtimerewards.gui.GUIManager;
import com.github.exobite.mc.playtimerewards.utils.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.UUID;

public class RewardEdit {

    private GUIManager.GUI editGui;
    private UUID editor;
    private Reward rw;

    protected RewardEdit(Reward rw, Player p) {
        rw.setEditStatus(true); //Block reward for other Edits
        editor = p.getUniqueId();
        this.rw = rw;
        createGui();
        editGui.openInventory(p);
    }

    protected void passStringFromChat(String message) {

    }

    private void createGui() {
        Lang langInst = Lang.getInstance();
        String guiTitle = langInst.getMessageWithArgs("GUI_EDIT_REWARD_WINDOWNAME", rw.getName());
        editGui = GUIManager.getInstance().createGUI(guiTitle, 27);
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

    }

    //private void

    private void saveDataToReward() {
        cleanUp();
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
