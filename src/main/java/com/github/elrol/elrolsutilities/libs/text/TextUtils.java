package com.github.elrol.elrolsutilities.libs.text;

import com.github.elrol.elrolsutilities.Main;
import com.github.elrol.elrolsutilities.api.IElrolAPI;
import com.github.elrol.elrolsutilities.api.data.IPlayerData;
import com.github.elrol.elrolsutilities.config.FeatureConfig;
import com.github.elrol.elrolsutilities.libs.Logger;
import com.github.elrol.elrolsutilities.libs.Methods;
import com.github.elrol.elrolsutilities.libs.ModInfo;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;

import java.util.*;

public class TextUtils {
    private static int dev;

    public static String ticksToTime(long ticks) {
        long years = ticks / 525600;
        long weeks = (ticks % 525600) / 10080;
        long days = (ticks % 10080) / 1440;
        long hours = (ticks) / 60;
        long min = ticks % 60;
        String time = "";
        if(years > 0) time += years + ":";
        if(weeks > 0) time += weeks + ":";
        if(days > 0) time += days + ":";
        if(hours > 0) time += days + ":";
        return time + min;
    }

    public static void staffChat(String message, UUID sender) {
        StringBuilder format = new StringBuilder("&r");
        for(char c : FeatureConfig.sc_format.get().toCharArray()) {
            format.append("&").append(c);
        }
        message = message.replace("&r", format);
        String text = formatString(message);
        Main.serverData.staffList.forEach(uuid -> {
            ServerPlayerEntity staff = Methods.getPlayerFromUUID(uuid);
            staff.sendMessage(new StringTextComponent(text), sender);
        });
    }

    public static void sendToChat(String message){
        sendToChat(new StringTextComponent(formatString(message)));
    }

    public static void sendToChat(TextComponent message){
        Main.mcServer.getPlayerList().broadcastMessage(message,ChatType.CHAT, UUID.randomUUID());
        Main.bot.sendChatMessage(message.getString());
    }

    public static void sendToStaff(String name, UUID uuid, String message) {
        ServerPlayerEntity player = Methods.getPlayerFromUUID(uuid);

        String tag = FeatureConfig.sc_tag.get();
        IPlayerData data = Main.database.get(uuid);
        if(data.isJailed()) tag = FeatureConfig.sc_jail_tag.get();
        staffChat(tag + "&r " + name + FeatureConfig.chat_seperator.get() + message, uuid);
        if(!uuid.equals(Main.bot.botUUID)) Main.bot.sendStaffMessage(player, message);
    }

    public static void sendToStaff(UUID uuid, String message) {
        sendToStaff(Methods.getDisplayName(uuid), uuid, message);

    }

    public static void sendToStaff(CommandSource source, String message) {
        UUID uuid;
        try {
            uuid = source.getPlayerOrException().getUUID();
        } catch (CommandSyntaxException e) {
            uuid = UUID.randomUUID();
        }
        sendToStaff(uuid, message);
    }

    public static String listToString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for(String s : list) {
            if(builder.toString().isEmpty()) builder.append(s);
            else builder.append(", ").append(s);
        }
        return builder.toString();
    }

    public static StringTextComponent commandHelp(String[] content) {
        StringTextComponent text = new StringTextComponent(formatString(FeatureConfig.command_help_spacer.get() + "\n"));
        for(String s : content){
            if(s.contains(":")) {
                String[] temp = s.split(":");
                text.append(formatString(FeatureConfig.command_help_entry.get().replace("KEY", temp[0]).replace("VALUE", temp[1])) + "\n");
            } else {
                text.append(formatString(FeatureConfig.command_help_info.get().replace("INFO", s)) + "\n");
            }
        }
        text.append(formatString(FeatureConfig.command_help_spacer.get()));
        return text;
    }

    public static StringTextComponent formatChat(UUID uuid, ITextComponent text) {
        for (ITextComponent sibling : text.getSiblings()) {
            Logger.log(sibling.getContents());
        }
        return formatChat(uuid, "");
    }

    public static StringTextComponent formatChat(UUID uuid, String msg){
        IPlayerData data = Main.database.get(uuid);
        StringTextComponent text = new StringTextComponent("");
        StringBuilder string = new StringBuilder();
        if (data.isPatreon()) string.append("&5[&dPatreon&5]&r");
        if (!data.getPrefix().isEmpty()) {
            String p = data.getPrefix();
            string.append(p);
            String s = p.substring(p.length() - 2);
            if(!s.startsWith("&")) string.append("&r ");
        }
        if (!data.getTitle().isEmpty()) {
            string.append(data.getTitle()).append("&r ");
        }
        string.append(data.getDisplayName());
        if (!data.getSuffix().isEmpty()) {
            string.append(" ").append(data.getSuffix());
        } else {
            string.append("&r");
        }
        string.append(FeatureConfig.chat_seperator.get());
        text.append(TextUtils.formatString(string.toString()));
        if (FeatureConfig.color_chat_enable.get()) {
            if (IElrolAPI.getInstance().getPermissionHandler().hasPermission(uuid, FeatureConfig.color_chat_perm.get())) {
                Logger.debug("Color chat enabled");
                text.append(TextUtils.format(msg));
            } else {
                Logger.debug("Doesnt have permission to use color chat");
                text.append(msg);
            }
        } else {
            Logger.debug("Color chat disabled");
            text.append(msg);
        }
        return text;
    }

    public static void msgNoTag(ServerPlayerEntity player, TextComponent message) {
        player.sendMessage(message, player.getUUID());
    }

    public static void action(ServerPlayerEntity player, TextComponent message) {
        player.sendMessage(message, ChatType.GAME_INFO, player.getUUID());
    }

    public static void msg(CommandContext<CommandSource> context, TextComponent translation) {
        TextUtils.msg(context.getSource(), translation);
    }

    public static void msg(ServerPlayerEntity player, TextComponent translation) {
        TextUtils.msg(player.createCommandSourceStack(), translation);
    }

    public static void msg(CommandSource source, String text) {
        source.sendSuccess(new StringTextComponent(ModInfo.getTag() + text), false);
    }

    public static void msgNoTag(CommandSource source, StringTextComponent text){
        source.sendSuccess(text, false);
    }

    public static void msg(CommandSource source, TextComponent translation) {
        source.sendSuccess(new StringTextComponent(ModInfo.getTag()).append(translation), false);
    }

    public static void err(CommandContext<CommandSource> context, TextComponent translation) {
        TextUtils.err(context.getSource(), translation);
    }

    public static void err(ServerPlayerEntity player, TextComponent translation) {
        TextUtils.err(player.createCommandSourceStack(), translation);
    }

    public static void err(CommandSource source, String text) {
        source.sendFailure(new StringTextComponent(ModInfo.getTag() + TextFormatting.RED + text));
    }

    public static void err(CommandSource source, TextComponent translation) {
        source.sendFailure(new StringTextComponent(ModInfo.getTag() + TextFormatting.RED).append(translation));
    }

    public static void tab_msg(CommandSource source, String string) {
        String tab = "    ";
        source.sendFailure(new StringTextComponent(tab + string));
    }

    public static void msg(CommandSource source, String[] stringArray) {
        for (int i = 0; i < stringArray.length; ++i) {
            if (i == 0) {
                source.sendFailure(new StringTextComponent(ModInfo.getTag() + stringArray[0]));
                continue;
            }
            TextUtils.tab_msg(source, stringArray[i]);
        }
    }

    public static String holiday(String string) {
        int month = Calendar.getInstance().get(2);
        Main.getLogger().info("The month is: " + month);
        switch (month) {
            case 0: return CustTextFormatting.jan(string);
            case 1: return CustTextFormatting.feb(string);
            case 2: return CustTextFormatting.mar(string);
            case 3: return CustTextFormatting.apr(string);
            case 4: return CustTextFormatting.may(string);
            case 5: return CustTextFormatting.jun(string);
            case 6: return CustTextFormatting.jul(string);
            case 7: return CustTextFormatting.aug(string);
            case 8: return CustTextFormatting.sep(string);
            case 9: return CustTextFormatting.oct(string);
            case 10: return CustTextFormatting.nov(string);
            case 11: return CustTextFormatting.dec(string);
        }
        return string;
    }

    public static String format(String string){
        StringBuilder output = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        if(string.contains("http")){
            for(String s : string.split(" ")){
                if(s.startsWith("http")) {
                    output.append(formatString(builder.toString()));
                    output.append(s).append(" ");
                    builder = new StringBuilder();
                    Logger.debug("found http");
                }
                else {
                    builder.append(s).append(" ");
                    Logger.debug("not http");
                }
            }
        } else {
            builder.append(string);
        }
        return output.append(formatString(builder.toString())).toString();
    }


    public static String formatString(String string) {
        if(string == null) return "";
        string = string.replace("\\n", "\n");
        new StringTextComponent(string);
        StringBuilder text = new StringBuilder();
        Map<Character, TextFormatting> colors = getColors();
        if(!string.contains("&")) return string;
        String[] splitString = string.split("&");
        for (int i = 0; i < splitString.length; ++i) {
            if (i == 0) {
                text.append(splitString[i]);
                continue;
            }
            String split = splitString[i];
            if (split.isEmpty()) continue;
            char id = split.charAt(0);
            if(colors.containsKey(id)){
                split = colors.get(id) + split.substring(1);
            } else if(id == 'g' && FeatureConfig.rainbow_code_enable.get()) {
                split = CustTextFormatting.rainbow(split.substring(1));
            } else if(id == 'h' && FeatureConfig.holiday_code_enable.get()) {
                split = TextUtils.holiday(split.substring(1));
            } else {
                split = "&" + split;
            }
            text.append(split);
        }
        return text.toString();
    }

    public static void sendMessage(CommandSource source, ServerPlayerEntity player, String msg) {
        String message = TextFormatting.DARK_GRAY + "[" + TextFormatting.GRAY;
        IPlayerData pData = Main.database.get(player.getUUID());
        UUID uuid = UUID.randomUUID();
        try {
            ServerPlayerEntity p = source.getPlayerOrException();
            uuid = p.getUUID();
            IPlayerData sData = Main.database.get(p.getUUID());
            sData.setLastMsg(player.getUUID());
            pData.setLastMsg(p.getUUID());
            Logger.debug("Source was a player");
        } catch (CommandSyntaxException e) {
            Logger.debug("Source was not player");
        }
        message += Methods.getDisplayName(source);
        message += TextFormatting.DARK_GRAY + " >> " + TextFormatting.GRAY;
        message += Methods.getDisplayName(player.createCommandSourceStack());
        message += TextFormatting.DARK_GRAY + "] " + TextFormatting.GRAY;
        message = FeatureConfig.color_chat_enable.get() && IElrolAPI.getInstance().getPermissionHandler().hasPermission(player.createCommandSourceStack(), FeatureConfig.color_chat_perm.get()) ? message + TextUtils.formatString(msg) : message + msg;
        player.sendMessage(new StringTextComponent(message), uuid);
        source.sendSuccess(new StringTextComponent(message), false);
    }

    public static Map<Character, TextFormatting> getColors() {
        Map<Character, TextFormatting> map = new HashMap<>();

        map.put('0', TextFormatting.BLACK);
        map.put('1', TextFormatting.DARK_BLUE);
        map.put('2', TextFormatting.DARK_GREEN);
        map.put('3', TextFormatting.DARK_AQUA);
        map.put('4', TextFormatting.DARK_RED);
        map.put('5', TextFormatting.DARK_PURPLE);
        map.put('6', TextFormatting.GOLD);
        map.put('7', TextFormatting.GRAY);
        map.put('8', TextFormatting.DARK_GRAY);
        map.put('9', TextFormatting.BLUE);
        map.put('a', TextFormatting.GREEN);
        map.put('b', TextFormatting.AQUA);
        map.put('c', TextFormatting.RED);
        map.put('d', TextFormatting.LIGHT_PURPLE);
        map.put('e', TextFormatting.YELLOW);
        map.put('f', TextFormatting.WHITE);

        map.put('k', TextFormatting.OBFUSCATED);
        map.put('l', TextFormatting.BOLD);
        map.put('m', TextFormatting.STRIKETHROUGH);
        map.put('n', TextFormatting.UNDERLINE);
        map.put('o', TextFormatting.ITALIC);
        map.put('r', TextFormatting.RESET);

        return map;
    }

    public static String stringToGolden(String parString, int parShineLocation, boolean parReturnToBlack){
        int stringLength = parString.length();
        if (stringLength < 1){
            return "";
        }
        StringBuilder outputString = new StringBuilder();
        for (int i = 0; i < stringLength; i++){
            if ((i+parShineLocation + Main.mcServer.getNextTickTime()/20)%88==0){
                outputString.append(TextFormatting.WHITE).append(parString, i, i + 1);
            } else if ((i+parShineLocation + Main.mcServer.getNextTickTime()/20)%88==1){
                outputString.append(TextFormatting.YELLOW).append(parString, i, i + 1);
            } else if ((i + parShineLocation+ Main.mcServer.getNextTickTime()/20)%88==87){
                outputString.append(TextFormatting.YELLOW).append(parString, i, i + 1);
            } else {
                outputString.append(TextFormatting.GOLD).append(parString, i, i + 1);
            }
        }
        if (parReturnToBlack){
            return outputString.toString() +TextFormatting.BLACK;
        }
        return outputString.toString() + TextFormatting.WHITE;
}

    /***
     *
     * @param amount The amount of currency to format
     * @param symbol If true, parses using symbol, otherwise uses singular/plural
     * @return The amount formatted to be readable
     */
    public static String parseCurrency(double amount, boolean symbol) {
        String formatted = String.format("%.2f", amount);

        if(!symbol){
            if(amount == 1) return formatted + " " + FeatureConfig.currency_singular.get();
            return formatted + " " + FeatureConfig.currency_plural.get();
        }
        return formatted + FeatureConfig.currency_symbol.get();
    }

    public static float parseCurrency(String string) {
        String symbol = FeatureConfig.currency_symbol.get();
        if(string.startsWith(symbol)) {
            string = string.substring(symbol.length());
        }
        if(string.isEmpty()) return 0F;
        return Float.parseFloat(string);
    }

    public static String stripFormatting(String string) {
        StringBuilder stripped = new StringBuilder();
        String[] split = string.split("[&§]");
        for(int i = 0; i < split.length; i++) {
            String s = split[i];
            if(i > 0) s = s.substring(1);
            stripped.append(s);
        }
        return stripped.toString();
    }

    public static void sendConfirmation(ServerPlayerEntity player, TextComponent[] lines) {
        UUID uuid = player.getUUID();
        StringTextComponent spacer = new StringTextComponent(TextFormatting.AQUA + String.join("", Collections.nCopies(45, "#")));
        player.sendMessage(spacer, uuid);
        for(TextComponent line : lines) player.sendMessage(line, uuid);
        player.sendMessage(spacer, uuid);
    }

    public static String generateString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
}

