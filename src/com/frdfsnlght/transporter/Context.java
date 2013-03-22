/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.transporter;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class Context {

    private static final ChatColor HL_ON = ChatColor.DARK_PURPLE;
    private static final ChatColor HL_OFF = ChatColor.WHITE;

    private CommandSender sender = null;

    public Context() {}

    public Context(CommandSender sender) {
        this.sender = sender;
    }

    public Context(String playerName) {
        if (playerName != null)
            this.sender = Global.plugin.getServer().getPlayer(playerName);
    }

    public CommandSender getSender() {
        return sender;
    }

    public void send(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        if (msg.isEmpty()) return;
        if (sender == null)
            Utils.info(msg);
        else if (isPlayer()) {
            msg = Chat.colorize(msg);
            sender.sendMessage(HL_ON + "[" + Global.pluginName + "] " + HL_OFF + msg);
        } else {
            msg = ChatColor.stripColor(msg);
            sender.sendMessage("[" + Global.pluginName + "] " + msg);
        }
    }

    public void sendLog(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        if (msg.isEmpty()) return;
        send(msg);
        if (! isPlayer()) return;
        Utils.info("->[%s] %s", ((Player)sender).getName(), msg);
    }

    public void warn(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        if (msg.isEmpty()) return;
        if (sender == null)
            Utils.warning(msg);
        else
            sender.sendMessage(HL_ON + "[" + Global.pluginName + "] " + ChatColor.RED + msg);
    }

    public void warnLog(String msg, Object ... args) {
        if (args.length > 0)
            msg = String.format(msg, args);
        if (msg.isEmpty()) return;
        warn(msg);
        if (! isPlayer()) return;
        Utils.warning("->[%s] %s", ((Player)sender).getName(), msg);
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public boolean isConsole() {
        return (sender != null) && (! (sender instanceof Player));
    }

    public boolean isSystem() {
        return sender == null;
    }

    public boolean isHuman() {
        return sender != null;
    }

    public boolean isOp() {
        return isConsole() || (isPlayer() && ((Player)sender).isOp());
    }

    public Player getPlayer() {
        if (! isPlayer()) return null;
        return (Player)sender;
    }

}
