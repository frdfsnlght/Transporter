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

import java.util.Map;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;

/**
 * Represents a local gate on the local server.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface LocalGate extends Gate {

    /**
     * Saves the gate's configuration to disk.
     *
     * @param force     true to force the save even if no changes have been made
     */
    public void save(boolean force);

    /**
     * Rebuilds the block structure of the gate.
     */
    public void rebuild();

    /**
     * Adds a PIN to the gate's list of allowed PINs.
     *
     * @param pin               the PIN to add
     * @return                  true if the PIN was added, false if it already exists
     * @throws GateException    if the PIN is invalid
     */
    public boolean addPin(String pin) throws GateException;

    /**
     * Removes a PIN from the gate's list of allowed PINs.
     *
     * @param pin   the PIN to remove
     * @return      true if the PIN was removed, false if it doesn't exist
     */
    public boolean removePin(String pin);

    /**
     * Removes all PINs from the gate's list of allowed PINs.
     */
    public void removeAllPins();

    /**
     * Returns true if the gate's list of allowed PINs contains the specified PIN.
     *
     * @param pin   the pin
     * @return      true if the PIN is in the gate's list of allowed PINs
     */
    public boolean hasPin(String pin);

    /**
     * Returns the set of items that are in the gate's banned item list.
     *
     * @return a set of item strings
     */
    public Set<String> getBannedItems();

    /**
     * Adds an item to the gate's banned item list.
     *
     * @param item              the item to add
     * @return                  true if the item was added, false if it already exists
     * @throws GateException    if the item is invalid
     */
    public boolean addBannedItem(String item) throws GateException;

    /**
     * Removes an item from the gate's banned item list.
     *
     * @param item              the item to remove
     * @return                  true if the item was removed, false if it doesn't exist
     * @throws GateException    if the item is invalid
     */
    public boolean removeBannedItem(String item) throws GateException;

    /**
     * Removes all the items from the gate's banned item list.
     */
    public void removeAllBannedItems();

    /**
     * Returns the set of items that are in the gate's allowed item list.
     *
     * @return a set of item strings
     */
    public Set<String> getAllowedItems();

    /**
     * Adds an item to the gate's allowed item list.
     *
     * @param item              the item to add
     * @return                  true if the item was added, false if it already exists
     * @throws GateException    if the item is invalid
     */
    public boolean addAllowedItem(String item) throws GateException;

    /**
     * Removes an item from the gate's allowed item list.
     *
     * @param item              the item to remove
     * @return                  true if the item was removed, false if it doesn't exist
     * @throws GateException    if the item is invalid
     */
    public boolean removeAllowedItem(String item) throws GateException;

    /**
     * Removes all the items from the gate's allowed item list.
     */
    public void removeAllAllowedItems();

    /**
     * Returns the map of items that are in the gate's replace item map.
     *
     * @return a map of item strings
     */
    public Map<String,String> getReplaceItems();

    /**
     * Adds an item mapping to the gate's replace item map.
     *
     * @param fromItem          the item to replace
     * @param toItem            the item to substitute
     * @return                  true if the item was added, false if it already exists
     * @throws GateException    if either item is invalid
     */
    public boolean addReplaceItem(String fromItem, String toItem) throws GateException;

    /**
     * Removes an item from the gate's replace item map.
     *
     * @param fromItem          the item to remove
     * @return                  true if the item was removed, false if it doesn't exist
     * @throws GateException    if the item is invalid
     */
    public boolean removeReplaceItem(String fromItem) throws GateException;

    /**
     * Removes all the items from the gate's replace item map.
     */
    public void removeAllReplaceItems();

    /**
     * Returns the set of potion effects that are in the gate's banned potion effect list.
     *
     * @return a set of potion effect strings
     */
    public Set<String> getBannedPotions();

    /**
     * Adds a potion effect to the gate's banned potion effect list.
     *
     * @param potion            the potion effect to add
     * @return                  true if the potion effect was added, false if it already exists
     * @throws GateException    if the potion effect is invalid
     */
    public boolean addBannedPotion(String potion) throws GateException;

    /**
     * Removes a potion effect from the gate's banned potion effect list.
     *
     * @param potion            the potion effect to remove
     * @return                  true if the potion effect was removed, false if it doesn't exist
     * @throws GateException    if the potion effect is invalid
     */
    public boolean removeBannedPotion(String potion) throws GateException;

    /**
     * Removes all the potion effects from the gate's banned potion effect list.
     */
    public void removeAllBannedPotions();

    /**
     * Returns the set of potion effects that are in the gate's allowed potion effect list.
     *
     * @return a set of potion effect strings
     */
    public Set<String> getAllowedPotions();

    /**
     * Adds a potion effect to the gate's allowed potion effect list.
     *
     * @param potion            the potion effect to add
     * @return                  true if the potion effect was added, false if it already exists
     * @throws GateException    if the potion effect is invalid
     */
    public boolean addAllowedPotion(String potion) throws GateException;

    /**
     * Removes a potion effect from the gate's allowed potion effect list.
     *
     * @param potion            the potion effect to remove
     * @return                  true if the potion effect was removed, false if it doesn't exist
     * @throws GateException    if the potion effect is invalid
     */
    public boolean removeAllowedPotion(String potion) throws GateException;

    /**
     * Removes all the potion effects from the gate's allowed potion effect list.
     */
    public void removeAllAllowedPotions();

    /**
     * Returns the map of potion effects that are in the gate's replace potion effect map.
     *
     * @return a map of potion effect strings
     */
    public Map<String,String> getReplacePotions();

    /**
     * Adds a potion effect mapping to the gate's replace potion effect map.
     *
     * @param fromPotion        the potion effect to replace
     * @param toPotion          the potion effect to substitute
     * @return                  true if the potion effect was added, false if it already exists
     * @throws GateException    if either potion effect is invalid
     */
    public boolean addReplacePotion(String fromPotion, String toPotion) throws GateException;

    /**
     * Removes a potion effect from the gate's replace potion effect map.
     *
     * @param fromPotion        the potion effect to remove
     * @return                  true if the potion effect was removed, false if it doesn't exist
     * @throws GateException    if the potion effect is invalid
     */
    public boolean removeReplacePotion(String fromPotion) throws GateException;

    /**
     * Removes all the potion effects from the gate's replace potion effect map.
     */
    public void removeAllReplacePotions();

    /* Options */

    /**
     * Returns the value of the "duration" option.
     *
     * @return      the option value
     */
    public int getDuration();

    /**
     * Sets the value of the "duration" option.
     *
     * @param i      the option value
     */
    public void setDuration(int i);

    /**
     * Returns the value of the "direction" option.
     *
     * @return      the option value
     */
    public BlockFace getDirection();

    /**
     * Sets the value of the "direction" option.
     *
     * @param i      the option value
     */
    public void setDirection(BlockFace dir);

    /**
     * Returns the value of the "linkLocal" option.
     *
     * @return      the option value
     */
    public boolean getLinkLocal();

    /**
     * Sets the value of the "linkLocal" option.
     *
     * @param b      the option value
     */
    public void setLinkLocal(boolean b);

    /**
     * Returns the value of the "linkWorld" option.
     *
     * @return      the option value
     */
    public boolean getLinkWorld();

    /**
     * Sets the value of the "linkWorld" option.
     *
     * @param b      the option value
     */
    public void setLinkWorld(boolean b);

    /**
     * Returns the value of the "linkServer" option.
     *
     * @return      the option value
     */
    public boolean getLinkServer();

    /**
     * Sets the value of the "linkServer" option.
     *
     * @param b      the option value
     */
    public void setLinkServer(boolean b);

    /**
     * Returns the value of the "linkNoneFormat" option.
     *
     * @return      the option value
     */
    public String getLinkNoneFormat();

    /**
     * Sets the value of the "linkNoneFormat" option.
     *
     * @param s      the option value
     */
    public void setLinkNoneFormat(String s);

    /**
     * Returns the value of the "linkUnselectedFormat" option.
     *
     * @return      the option value
     */
    public String getLinkUnselectedFormat();

    /**
     * Sets the value of the "linkUnselectedFormat" option.
     *
     * @param s      the option value
     */
    public void setLinkUnselectedFormat(String s);

    /**
     * Returns the value of the "linkOfflineFormat" option.
     *
     * @return      the option value
     */
    public String getLinkOfflineFormat();

    /**
     * Sets the value of the "linkOfflineFormat" option.
     *
     * @param s      the option value
     */
    public void setLinkOfflineFormat(String s);

    /**
     * Returns the value of the "linkLocalFormat" option.
     *
     * @return      the option value
     */
    public String getLinkLocalFormat();

    /**
     * Sets the value of the "linkLocalFormat" option.
     *
     * @param s      the option value
     */
    public void setLinkLocalFormat(String s);

    /**
     * Returns the value of the "linkWorldFormat" option.
     *
     * @return      the option value
     */
    public String getLinkWorldFormat();

    /**
     * Sets the value of the "linkWorldFormat" option.
     *
     * @param s      the option value
     */
    public void setLinkWorldFormat(String s);

    /**
     * Returns the value of the "linkServerFormat" option.
     *
     * @return      the option value
     */
    public String getLinkServerFormat();

    /**
     * Sets the value of the "linkServerFormat" option.
     *
     * @param s      the option value
     */
    public void setLinkServerFormat(String s);

    /**
     * Returns the value of the "multiLink" option.
     *
     * @return      the option value
     */
    public boolean getMultiLink();

    /**
     * Sets the value of the "multiLink" option.
     *
     * @param b      the option value
     */
    public void setMultiLink(boolean b);

    /**
     * Returns the value of the "protect" option.
     *
     * @return      the option value
     */
    public boolean getProtect();

    /**
     * Sets the value of the "protect" option.
     *
     * @param b      the option value
     */
    public void setProtect(boolean b);

    /**
     * Returns the value of the "requirePin" option.
     *
     * @return      the option value
     */
    public boolean getRequirePin();

    /**
     * Sets the value of the "requirePin" option.
     *
     * @param b      the option value
     */
    public void setRequirePin(boolean b);

    /**
     * Returns the value of the "requireValidPin" option.
     *
     * @return      the option value
     */
    public boolean getRequireValidPin();

    /**
     * Sets the value of the "requireValidPin" option.
     *
     * @param b      the option value
     */
    public void setRequireValidPin(boolean b);

    /**
     * Returns the value of the "requireLevel" option.
     *
     * @return      the option value
     */
    public int getRequireLevel();

    /**
     * Sets the value of the "requireLevel" option.
     *
     * @param i      the option value
     */
    public void setRequireLevel(int i);

    /**
     * Returns the value of the "invalidPinDamage" option.
     *
     * @return      the option value
     */
    public int getInvalidPinDamage();

    /**
     * Sets the value of the "invalidPinDamage" option.
     *
     * @param i      the option value
     */
    public void setInvalidPinDamage(int i);

    /**
     * Returns the value of the "sendChat" option.
     *
     * @return      the option value
     */
    public boolean getSendChat();

    /**
     * Sets the value of the "sendChat" option.
     *
     * @param b      the option value
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
     * Returns the value of the "sendChatDistance" option.
     *
     * @return      the option value
     */
    public int getSendChatDistance();

    /**
     * Sets the value of the "sendChatDistance" option.
     *
     * @param i      the option value
     */
    public void setSendChatDistance(int i);

    /**
     * Returns the value of the "receiveChat" option.
     *
     * @return      the option value
     */
    public boolean getReceiveChat();

    /**
     * Sets the value of the "receiveChat" option.
     *
     * @param b      the option value
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
     * Returns the value of the "receiveChatDistance" option.
     *
     * @return      the option value
     */
    public int getReceiveChatDistance();

    /**
     * Sets the value of the "receiveChatDistance" option.
     *
     * @param i      the option value
     */
    public void setReceiveChatDistance(int i);

    /**
     * Returns the value of the "requireAllowedItems" option.
     *
     * @return      the option value
     */
    public boolean getRequireAllowedItems();

    /**
     * Sets the value of the "requireAllowedItems" option.
     *
     * @param b      the option value
     */
    public void setRequireAllowedItems(boolean b);

    /**
     * Returns the value of the "receiveInventory" option.
     *
     * @return      the option value
     */
    public boolean getReceiveInventory();

    /**
     * Sets the value of the "receiveInventory" option.
     *
     * @param b      the option value
     */
    public void setReceiveInventory(boolean b);

    /**
     * Returns the value of the "deleteInventory" option.
     *
     * @return      the option value
     */
    public boolean getDeleteInventory();

    /**
     * Sets the value of the "deleteInventory" option.
     *
     * @param b      the option value
     */
    public void setDeleteInventory(boolean b);

    /**
     * Returns the value of the "receiveGameMode" option.
     *
     * @return      the option value
     */
    public boolean getReceiveGameMode();

    /**
     * Sets the value of the "receiveGameMode" option.
     *
     * @param b      the option value
     */
    public void setReceiveGameMode(boolean b);

    /**
     * Returns the value of the "allowGameModes" option.
     *
     * @return      the option value
     */
    public String getAllowGameModes();

    /**
     * Sets the value of the "allowGameModes" option.
     *
     * @param s      the option value
     */
    public void setAllowGameModes(String s);

    /**
     * Returns the value of the "gameMode" option.
     *
     * @return      the option value
     */
    public GameMode getGameMode();

    /**
     * Sets the value of the "gameMode" option.
     *
     * @param m      the option value
     */
    public void setGameMode(GameMode m);

    /**
     * Returns the value of the "receiveXP" option.
     *
     * @return      the option value
     */
    public boolean getReceiveXP();

    /**
     * Sets the value of the "receiveXP" option.
     *
     * @param b      the option value
     */
    public void setReceiveXP(boolean b);

    /**
     * Returns the value of the "receivePotions" option.
     *
     * @return      the option value
     */
    public boolean getReceivePotions();

    /**
     * Sets the value of the "receivePotions" option.
     *
     * @param b      the option value
     */
    public void setReceivePotions(boolean b);

    /**
     * Returns the value of the "requireAllowedPotions" option.
     *
     * @return      the option value
     */
    public boolean getRequireAllowedPotions();

    /**
     * Sets the value of the "requireAllowedPotions" option.
     *
     * @param b      the option value
     */
    public void setRequireAllowedPotions(boolean b);

    /**
     * Returns the value of the "receiveStats" option.
     *
     * @return      the option value
     */
    public boolean getReceiveStats();

    /**
     * Sets the value of the "receiveStats" option.
     *
     * @param b      the option value
     */
    public void setReceiveStats(boolean b);


    /**
     * Returns the value of the "randomNextLink" option.
     *
     * @return      the option value
     */
    public boolean getRandomNextLink();

    /**
     * Sets the value of the "randomNextLink" option.
     *
     * @param b      the option value
     */
    public void setRandomNextLink(boolean b);

    /**
     * Returns the value of the "sendNextLink" option.
     *
     * @return      the option value
     */
    public boolean getSendNextLink();

    /**
     * Sets the value of the "sendNextLink" option.
     *
     * @param b      the option value
     */
    public void setSendNextLink(boolean b);

    /**
     * Returns the value of the "teleportFormat" option.
     *
     * @return      the option value
     */
    public String getTeleportFormat();

    /**
     * Sets the value of the "teleportFormat" option.
     *
     * @param s      the option value
     */
    public void setTeleportFormat(String s);

    /**
     * Returns the value of the "noLinksFormat" option.
     *
     * @return      the option value
     */
    public String getNoLinksFormat();

    /**
     * Sets the value of the "noLinksFormat" option.
     *
     * @param s      the option value
     */
    public void setNoLinksFormat(String s);

    /**
     * Returns the value of the "noLinkSelectedFormat" option.
     *
     * @return      the option value
     */
    public String getNoLinkSelectedFormat();

    /**
     * Sets the value of the "noLinkSelectedFormat" option.
     *
     * @param s      the option value
     */
    public void setNoLinkSelectedFormat(String s);

    /**
     * Returns the value of the "invalidLinkFormat" option.
     *
     * @return      the option value
     */
    public String getInvalidLinkFormat();

    /**
     * Sets the value of the "invalidLinkFormat" option.
     *
     * @param s      the option value
     */
    public void setInvalidLinkFormat(String s);

    /**
     * Returns the value of the "unknownLinkFormat" option.
     *
     * @return      the option value
     */
    public String getUnknownLinkFormat();

    /**
     * Sets the value of the "unknownLinkFormat" option.
     *
     * @param s      the option value
     */
    public void setUnknownLinkFormat(String s);

    /**
     * Returns the value of the "markerFormat" option.
     *
     * @return      the option value
     */
    public String getMarkerFormat();

    /**
     * Sets the value of the "markerFormat" option.
     *
     * @param s      the option value
     */
    public void setMarkerFormat(String s);

    /**
     * Returns the value of the "countdown" option.
     *
     * @return      the option value
     */
    public int getCountdown();

    /**
     * Sets the value of the "countdown" option.
     *
     * @param i      the option value
     */
    public void setCountdown(int i);

    /**
     * Returns the value of the "countdownInterval" option.
     *
     * @return      the option value
     */
    public int getCountdownInterval();

    /**
     * Sets the value of the "countdownInterval" option.
     *
     * @param i      the option value
     */
    public void setCountdownInterval(int i);

    /**
     * Returns the value of the "countdownFormat" option.
     *
     * @return      the option value
     */
    public String getCountdownFormat();

    /**
     * Sets the value of the "countdownFormat" option.
     *
     * @param s      the option value
     */
    public void setCountdownFormat(String s);

    /**
     * Returns the value of the "countdownIntervalFormat" option.
     *
     * @return      the option value
     */
    public String getCountdownIntervalFormat();

    /**
     * Sets the value of the "countdownIntervalFormat" option.
     *
     * @param s      the option value
     */
    public void setCountdownIntervalFormat(String s);

    /**
     * Returns the value of the "countdownCancelFormat" option.
     *
     * @return      the option value
     */
    public String getCountdownCancelFormat();

    /**
     * Sets the value of the "countdownCancelFormat" option.
     *
     * @param s      the option value
     */
    public void setCountdownCancelFormat(String s);

    /**
     * Returns the value of the "linkLocalCost" option.
     *
     * @return      the option value
     */
    public double getLinkLocalCost();

    /**
     * Sets the value of the "linkLocalCost" option.
     *
     * @param cost      the option value
     */
    public void setLinkLocalCost(double cost);

    /**
     * Returns the value of the "linkWorldCost" option.
     *
     * @return      the option value
     */
    public double getLinkWorldCost();

    /**
     * Sets the value of the "linkWorldCost" option.
     *
     * @param cost      the option value
     */
    public void setLinkWorldCost(double cost);

    /**
     * Returns the value of the "linkServerCost" option.
     *
     * @return      the option value
     */
    public double getLinkServerCost();

    /**
     * Sets the value of the "linkServerCost" option.
     *
     * @param cost      the option value
     */
    public void setLinkServerCost(double cost);

    /**
     * Returns the value of the "sendLocalCost" option.
     *
     * @return      the option value
     */
    public double getSendLocalCost();

    /**
     * Sets the value of the "sendLocalCost" option.
     *
     * @param cost      the option value
     */
    public void setSendLocalCost(double cost);

    /**
     * Returns the value of the "sendWorldCost" option.
     *
     * @return      the option value
     */
    public double getSendWorldCost();

    /**
     * Sets the value of the "sendWorldCost" option.
     *
     * @param cost      the option value
     */
    public void setSendWorldCost(double cost);

    /**
     * Returns the value of the "sendServerCost" option.
     *
     * @return      the option value
     */
    public double getSendServerCost();

    /**
     * Sets the value of the "sendServerCost" option.
     *
     * @param cost      the option value
     */
    public void setSendServerCost(double cost);

    /**
     * Returns the value of the "receiveLocalCost" option.
     *
     * @return      the option value
     */
    public double getReceiveLocalCost();

    /**
     * Sets the value of the "receiveLocalCost" option.
     *
     * @param cost      the option value
     */
    public void setReceiveLocalCost(double cost);

    /**
     * Returns the value of the "receiveWorldCost" option.
     *
     * @return      the option value
     */
    public double getReceiveWorldCost();

    /**
     * Sets the value of the "receiveWorldCost" option.
     *
     * @param cost      the option value
     */
    public void setReceiveWorldCost(double cost);

    /**
     * Returns the value of the "receiveServerCost" option.
     *
     * @return      the option value
     */
    public double getReceiveServerCost();

    /**
     * Sets the value of the "receiveServerCost" option.
     *
     * @param cost      the option value
     */
    public void setReceiveServerCost(double cost);

    /**
     * Returns the value of the "linkAddDistance" option.
     *
     * @return      the option value
     */
    public int getLinkAddDistance();

    /**
     * Sets the value of the "linkAddDistance" option.
     *
     * @param i     the option value
     */
    public void setLinkAddDistance(int i);

    /**
     * Returns the value of the "hidden" option.
     *
     * @return      the option value
     */
    @Override
    public boolean getHidden();

    /**
     * Sets the value of the "hidden" option.
     *
     * @param b     the option value
     */
    public void setHidden(boolean b);

    /* End Options */

}
