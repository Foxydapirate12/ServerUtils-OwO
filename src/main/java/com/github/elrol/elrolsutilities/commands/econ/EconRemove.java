package com.github.elrol.elrolsutilities.commands.econ;

import com.github.elrol.elrolsutilities.Main;
import com.github.elrol.elrolsutilities.api.data.IPlayerData;
import com.github.elrol.elrolsutilities.libs.text.Msgs;
import com.github.elrol.elrolsutilities.libs.text.TextUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

public class EconRemove {
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("remove")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(EconRemove::execute)));
    }

    private static int execute(CommandContext<CommandSource> c) {
        try {
            ServerPlayerEntity player = EntityArgument.getPlayer(c, "target");
            int amount = IntegerArgumentType.getInteger(c,"amount");
            IPlayerData data = Main.database.get(player.getUUID());
            data.charge(amount);
            String cur = TextUtils.parseCurrency(amount, false);

            if(player.getName().equals(c.getSource().getDisplayName())) {
                TextUtils.msg(c, Msgs.charged_self.get(cur));
            } else {
                TextUtils.msg(c, Msgs.charged_player.get(data.getDisplayName(), cur));
                TextUtils.msg(player, Msgs.charged_by.get("The Server", cur));
            }
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
