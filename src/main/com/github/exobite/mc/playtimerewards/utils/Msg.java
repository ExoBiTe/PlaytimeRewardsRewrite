package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.logging.Level;

public enum Msg {

    //Commands
    CMD_SUC_PT_OWN("§aYour Playtime is %[0]d %[1]h %[2]m %[3]s\nYour Sessiontime is %[4]d %[5]h %[6]m %[7]s",
            "The Return Message from the /playtime Command",
            true),
    CMD_SUC_PT_OTHER("§6%[0]§a's Playtime is %[1]d %[2]h %[3]m %[4]s\n§6%[5]§a's Sessiontime is %[6]d %[7]h %[8]m %[9]s",
            "The Return Message from the /playtime <player> Command",
            true),
    CMD_SUC_PT_OTHER_OFFLINE("§6%[0]§a's Playtime is %[1]d %[2]h %[3]m %[4]s",
            "The Return Message from the /playtime <offlineplayer> Command",
            true),

    CMD_SUC_PTTOP_HEADER("§7Listing the top §b%[0] §7Playtimes:",
            "The First Part of the /Playtimetop Command",
            true),
    CMD_SUC_PTTOP_ENTRY("§6%[0]§7: §b%[1]§7 - has played %[2]d %[3]h %[4]m and %[5]s",
            "This Message gets printed once for each Entry from the /Playtimetop Command",
            true),

    CMD_SUC_PTR_LIST_HEADER("§7Listing all §b%[0]§7 Rewards:\n§8Internal Name -- DisplayName -- CountType",
            "The first Part of the /Playtimerewards list Command",
            true),
    CMD_SUC_PTR_LIST_ENTRY("§3%[0] §8-- §1%[1] §8-- §7%[2]",
            "This Message gets printed once for each Entry from the /Playtimerewards list Command",
            true),
    CMD_SUC_PTR_RELOAD_SUCCESS("§aSuccessfully reloaded the external Data!",
            "The Return Message from the /Playtimerewards reload Command",
            true),
    CMD_WARN_PTR_RELOAD("§4Content from rewards.yml doesn't get reloaded, as there are Rewards being edited.",
            "A Warn Message that Rewards dont get reloaded from the /ptr reload Command",
            true),

    CMD_NOTIF_PTR_EDIT_TYPE_IN_CHAT("§6Type in chat! §8(Your Message will not get sent! Use '&' as Color-Char.)",
            "The Notification sent to the Player upon expecting an Input for the Reward-Editing",
            false),

    CMD_SUC_PTR_EDIT_SAVED("§aSuccessfully saved the Reward!",
            "",
            false),

    CMD_SUC_PTR_EDIT_ABORTED("§cAborted the Editing!",
            "",
            false),

    CMD_ERR_PTR_EDIT_UNKNOWN("§4An unknown Error occurred! §8Try again",
            "",
            false),

    CMD_ERR_TOO_MANY_REQUESTS("§4You can't do that right now. Try again later!",
            "A Error Message when too many Requests are sent from a Player using /pt <offlineplayer>",
            true),
    CMD_ERR_NO_PERMISSION("§4You don't have the Permission to do this.",
            "The No Permission Message from all Commands",
            true),
    CMD_ERR_PLAYER_NOT_FOUND("§4Can't find the Player '§6%[0]§4'!",
            "The No-Player-Found Error Message from all Commands",
            true),
    CMD_ERR_REWARD_NOT_FOUND("§4Can't find the Reward '§6%[0]§4'!",
            "The No-Reward-Found Error Message from all Commands",
            true),

    //Ingame Notifications
    NOTIF_UPDATE_AVAILABLE("§6Version %[0] of PlaytimeRewards is available (Running v%[1])!",
            "The Notification upon Login when a new Version is available",
            true),

    NOTIF_AFK_USER_WENT_AFK("§7You are now flagged as AFK",
            "The Message sent to Players who get flagged as afk",
            true),

    NOTIF_AFK_USER_CAME_BACK("§7You are no longer flagged as AFK",
            "The Message sent to Players when coming back",
            true),

    //External stuff
    EXT_PAPI_TIME_FORMAT("%[0]d %[1]h %[2]m %[3]s",
            "The Format in which all Times get sent to the PlaceholderAPI",
            true),

    //GUI
    GUI_EDIT_REWARD_WINDOWNAME("§6Editing Reward: §2%[0]",
            "The Title of the /ptr editreward <reward> GUI",
            false),
    GUI_EDIT_REWARD_EXIT_NOSAVE_NAME("§4Exit and discard Changes",
            "The Displayname of the 'Exit without Saving'-Item in the Edit GUI",
            false),
    GUI_EDIT_REWARD_EXIT_NOSAVE_LORE("§cThis Option discards all changes\n§cyou've made and ends the editing.",
            "The Lore of the 'Exit without Saving'-Item in the Edit GUI",
            false),
    GUI_EDIT_REWARD_EXIT_SAVE_NAME("§2Exit and Save Changes",
            "The Displayname of the 'Exit and Save'-Item in the Edit GUI",
            false),
    GUI_EDIT_REWARD_EXIT_SAVE_LORE("§aThis Option saves all changes\n§ayou've made and ends the editing.",
            "The Lore of the 'Exit and Save'-Item in the Edit GUI",
            false),
    GUI_EDIT_REWARD_FIELD_ITEM_NAME("§6Change the %[0]§6.",
            "The first part of every Editing-Option in the Reward GUI",
            false),

    //These Messages must be exactly named after the Field
    //e.g. GUI_EDIT_TRANSL_<fieldname in uppercase>
    //Otherwise errors will get thrown
    //These are all referenced indirectly btw, in RewardEdit#setFieldDataToSlot()
    GUI_EDIT_TRANSL_TIMEMS("needed §btime",
            "The Second part of every Editing-Option in the Reward GUI, represents the needed Time",
            false),
    GUI_EDIT_TRANSL_DISPLAYNAME("display §bname",
            "The Second part of every Editing-Option in the Reward GUI, represents the displayname",
            false),
    GUI_EDIT_TRANSL_ISREPEATING("reward is §brepeating",
            "The Second part of every Editing-Option in the Reward GUI, represents the 'isRepeating' Value",
            false),
    GUI_EDIT_TRANSL_GRANTFIRST("reward is grant §bfirst",
            "The Second part of every Editing-Option in the Reward GUI, represents the 'grantFirst' Value",
            false),
    GUI_EDIT_TRANSL_CONSOLECOMMANDS("executed §bcommands",
            "The Second part of every Editing-Option in the Reward GUI, contains the executed commands",
            false),
    GUI_EDIT_TRANSL_PLAYERMESSAGES("sent §bplayer messages",
            "The Second part of every Editing-Option in the Reward GUI, contains all Messages sent to the Player",
            false),
    GUI_EDIT_TRANSL_GLOBALMESSAGES("sent §bglobal messages",
            "The Second part of every Editing-Option in the Reward GUI, contains all Broadcasted Messages",
            false),
    GUI_EDIT_TRANSL_ACTIONBARMESSAGE("sent §baction bar message",
            "The Second part of every Editing-Option in the Reward GUI, contains the Actionbar Message sent to the Player",
            false),
    GUI_EDIT_TRANSL_PERMISSIONNEEDED("needed §bpermission",
            "The Second part of every Editing-Option in the Reward GUI, represents the needed Permission",
            false),
    GUI_EDIT_TRANSL_PARTICLES("§bparticles",
            "The Second part of every Editing-Option in the Reward GUI, contains all Particles that are being spawned",
            false),
    GUI_EDIT_TRANSL_SOUNDS("§bsounds",
            "The Second part of every Editing-Option in the Reward GUI, contains all Sounds that are being played",
            false);

    private static boolean papiIsRegistered;

    static void setPapiIsRegistered(boolean papiIsRegistered) {
        Msg.papiIsRegistered = papiIsRegistered;
    }

    private String message;
    private final String comment;
    private int argAmount;
    private final boolean showInFile;
    private boolean usesPapi;

    Msg(String message, String comment, boolean showInFile) {
        this.comment = comment;
        this.showInFile = showInFile;
        verifyMessage(message, true);
    }

    private void verifyMessage(String newMsg, boolean initial) {
        String replaced = newMsg.replaceAll("%\\[[0-9]]", "%[#]");
        int newArgs = Utils.countMatches(replaced, "%[#]");
        if(initial) {
            this.argAmount = newArgs;
        }else if(argAmount!=newArgs) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "You used a wrong Amount of Args for Message '"+this+"'!\n" +
                    "Change the Args to "+argAmount+" instead of "+newArgs+"!\n" +
                    "Using the default Message for now...");
            return;
        }
        usesPapi = false;
        if(papiIsRegistered) {
            usesPapi = PlaceholderAPI.containsPlaceholders(newMsg);
        }
        message = newMsg;
    }

    public String getMessage() {
        return message;
    }

    public String getComment() {
        return comment;
    }

    void setMessage(String newMessage) {
        verifyMessage(newMessage, false);
    }

    public int getArgAmount(){
        return argAmount;
    }

    public boolean showInFile() {
        return showInFile;
    }

    public boolean usesPapi() {
        return usesPapi;
    }

}
