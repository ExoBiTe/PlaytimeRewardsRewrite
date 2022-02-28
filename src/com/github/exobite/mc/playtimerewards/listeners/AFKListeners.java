package com.github.exobite.mc.playtimerewards.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AFKListeners implements Listener {

    //Gets later replaced by Config values
    private boolean cancelOnInteract = true,
            cancelOnMove = true,
            cancelOnChat = true,
            cancelOnLook = true;

    /*
    Notes:
    What happens when a afk user gets targeted by /pt?
        -> Is it gonna subtract the AFK-Time until then before delivering the result?
        -> Or am i gonna ignore this fact and just correct the playtime when the user comes back
    How to keep up with the afk-time?
        -> Bukkit only allows to decrement a statistic by 2^32/2 -> ~ 30.000h -> 1.242d -> 3,4years
        -> Nah... i guess this is enough time
    BB

     */

    private void onMove(PlayerMoveEvent e){

    }

    private void onInteract(PlayerInteractEvent e) {

    }

    private void onChat(AsyncPlayerChatEvent e) {

    }

}
