/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
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
package com.frdfsnlght.transporter.api;

import java.net.InetSocketAddress;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;

/**
 * Represents a locally configured server that the plugin connects to.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface RemoteServer {

    /**
     * Returns the name of server.
     *
     * @return the name of the server
     */
    public String getName();

    /**
     * Returns true if the server is currently connected.
     *
     * @return true if the server is currently connected
     */
    public boolean isConnected();

    /**
     * Returns a set of players currently online on the server.
     *
     * @return a set of {@link RemotePlayer} objects
     */
    public Set<RemotePlayer> getRemotePlayers();

    /**
     * Returns a set of worlds on the server.
     *
     * @return a set of {@link RemoteWorld} objects
     */
    public Set<RemoteWorld> getRemoteWorlds();

    /**
     * Returns a set of gates on the server.
     *
     * @return a set of {@link RemoteGate} objects
     */
    public Set<RemoteGate> getRemoteGates();

    /**
     * Returns a {@link RemoteWorld} object for the specified world, or
     * null if the world doesn't exist.
     *
     * @param worldName     the name of the world
     * @return              a RemoteWorld object or null
     */
    public RemoteWorld getRemoteWorld(String worldName);

    /**
     * Returns a {@link RemoteGate} object for the specified gate, or
     * null if the gate doesn't exist.
     *
     * @param gateName      the name of the gate.
     * @return              a RemoteGate object or null
     */
    public RemoteGate getRemoteGate(String gateName);

    /**
     * Returns the method used to transfer players to this server.
     *
     * @return              the method of transfer
     */
    public TransferMethod getTransferMethod();

    /**
     * Returns the message used to kick the player with the specified address and
     * reconnect them to this server when the transfer method is ClientKick.
     *
     * @param clientAddress
     * @return the kick message, or null if there's a problem
     */
    public String getKickMessage(InetSocketAddress clientAddress);

    /**
     * Broadcasts a message to all players on the remote server with the specified permission.
     *
     * @param cb            the callback to use when the call completes
     * @param message       the message
     * @param permission    the permission
     */
    public void broadcast(Callback<Integer> cb, String message, String permission);

    /**
     * Broadcasts a message to all players on the remote server.
     *
     * @param cb            the callback to use when the call completes
     * @param message       the message
     */
    public void broadcastMessage(Callback<Integer> cb, String message);

    /**
     * Executes a command on the remote server.
     *
     * @param cb            the callback to use when the call completes
     * @param sender        the sender of the command
     * @param commandLine   the command to execute
     */
    public void dispatchCommand(Callback<Boolean> cb, CommandSender sender, String commandLine);

    /**
     * Returns the configured default game mode on the remote server.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getDefaultGameMode(Callback<GameMode> cb);

    /**
     * Returns the configured name of the remote server.
     * <p>
     * This method returns the name of the remote server as configured in that
     * server's properties file.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getName(Callback<String> cb);

    /**
     * Returns the Id of the remote server.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getServerId(Callback<String> cb);

    /**
     * Returns the version of the remote server.
     *
     * @param cb    the callback to use when the call completes
     */
    public void getVersion(Callback<String> cb);

    /* Options */

    /**
     * Returns the locally configured key for this server.
     *
     * @return the key
     */
    public String getKey();

    /**
     * Sets the "key" option.
     *
     * @param key   the option value
     */
    public void setKey(String key);

    /**
     * Returns the value of the "key" option.
     *
     * @return the option value
     */
    public boolean isEnabled();

    /**
     * Sets the "enabled" option.
     *
     * @param en the option value
     */
    public void setEnabled(boolean en);

    /**
     * Returns the value of the "publicAddress" option.
     *
     * @return the option value
     */
    public String getPublicAddress();

    /**
     * Sets the "publicAddress" option.
     *
     * @param address the option value
     */
    public void setPublicAddress(String address);

    /**
     * Returns the value of the "privateAddress" option.
     *
     * @return the option value
     */
    public String getPrivateAddress();

    /**
     * Sets the "privateAddress" option.
     *
     * @param address   the option value
     */
    public void setPrivateAddress(String address);

    /**
     * Returns the value of the "chatFormat" option.
     *
     * @return the option value
     */
    public String getChatFormat();

    /**
     * Sets the "chatFormat" option.
     *
     * @param s the option value
     */
    public void setChatFormat(String s);

    /**
     * Returns the value of the "pmFormat" option.
     *
     * @return the option value
     */
    public String getPmFormat();

    /**
     * Sets the "pmFormat" option.
     *
     * @param s the option value
     */
    public void setPmFormat(String s);

    /**
     * Returns the value of the "sendChat" option.
     *
     * @return the option value
     */
    public boolean getSendChat();

    /**
     * Sets the "sendChat" option.
     *
     * @param b the option value
     */
    public void setSendChat(boolean b);

    /**
     * Returns the value of the "sendChatFilter" option.
     *
     * @return the option value
     */
    public String getSendChatFilter();

    /**
     * Sets the "sendChatFilter" option.
     *
     * @param s the option value
     */
    public void setSendChatFilter(String s);

    /**
     * Returns the value of the "sendChatFormatFilter" option.
     *
     * @return the option value
     */
    public String getSendChatFormatFilter();

    /**
     * Sets the "sendChatFormatFilter" option.
     *
     * @param s the option value
     */
    public void setSendChatFormatFilter(String s);

    /**
     * Returns the value of the "receiveChat" option.
     *
     * @return the option value
     */
    public boolean getReceiveChat();

    /**
     * Sets the "receiveChat" option.
     *
     * @param b the option value
     */
    public void setReceiveChat(boolean b);

    /**
     * Returns the value of the "receiveChatFilter" option.
     *
     * @return the option value
     */
    public String getReceiveChatFilter();

    /**
     * Sets the "receiveChatFilter" option.
     *
     * @param s the option value
     */
    public void setReceiveChatFilter(String s);

    /**
     * Returns the value of the "announcePlayers" option.
     *
     * @return the option value
     */
    public boolean getAnnouncePlayers();

    /**
     * Sets the "announcePlayers" option.
     *
     * @param b the option value
     */
    public void setAnnouncePlayers(boolean b);

    /**
     * Returns the value of the "playerListFormat" option.
     *
     * @return the option value
     */
    public String getPlayerListFormat();

    /**
     * Sets the "playerListFormat" option.
     *
     * @param s the option value
     */
    public void setPlayerListFormat(String s);

    /**
     * Returns the value of the "mExecTarget" option.
     *
     * @return the option value
     */
    public boolean getMExecTarget();

    /**
     * Sets the "mExecTarget" option.
     *
     * @param s the option value
     */
    public void setMExecTarget(boolean b);

    /**
     * Returns the value of the "allowRemoteCommands" option.
     *
     * @return the option value
     */
    public boolean getAllowRemoteCommands();

    /**
     * Sets the "allowRemoteCommands" option.
     *
     * @param s the option value
     */
    public void setAllowRemoteCommands(boolean b);

    /* End Options */

}
