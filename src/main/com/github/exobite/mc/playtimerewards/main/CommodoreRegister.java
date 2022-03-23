package com.github.exobite.mc.playtimerewards.main;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.commodore.Commodore;
import org.bukkit.command.PluginCommand;

public class CommodoreRegister {

    static void registerCompletionPlaytime(Commodore com, PluginCommand cmd) {
        LiteralCommandNode<?> ptCommand = LiteralArgumentBuilder.literal("playtime")
                .then(LiteralArgumentBuilder.literal("player"))
                .build();
        com.register(cmd, ptCommand);
    }

}
