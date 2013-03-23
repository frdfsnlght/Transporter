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

import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.RemotePlayer;
import com.frdfsnlght.transporter.api.RemoteServer;
import com.frdfsnlght.transporter.api.ReservationException;
import com.frdfsnlght.transporter.api.TransporterException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.mcsg.double0negative.tabapi.TabAPI;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class PlayerListenerImpl implements Listener {

    // Logic map for player interaction
    private static final Map<Integer,String> ACTIONS = new HashMap<Integer,String>();

    // Masks are strings composed of zeros and ones. Each character position
    // corresponds to a bit position (bit 0 is first position).
    // 0 1  : Is the gate currently open?
    // 1 2  : Does the player have trp.gate.open permission?
    // 2 4  : Does the player have trp.gate.close permission?
    // 3 8  : Does the player have trp.gate.changeLink permission?
    // 4 16 : Does the gate have a valid destination?
    // 5 32 : Is the gate on its last link?
    // 6 64 : Is the gate block a trigger?
    // 7 128: Is the gate block a switch?

    // Values are a comma separated list of actions to perform:
    // OPEN: open the gate
    // CLOSE: close the gate
    // CHANGELINK: change the gate's link

    static {
        // gate is closed
        addAction("01xxxx1x", "OPEN");
        addAction("0xx1xx01", "CHANGELINK");
        addAction("00x1xxx1", "CHANGELINK");
        addAction("01x10x11", "CHANGELINK,OPEN");

        // gate is open
        addAction("1x1xxx10", "CLOSE");
        addAction("1x10xx11", "CLOSE");
        addAction("1x11x111", "CLOSE,CHANGELINK");
        addAction("1x01xxx1", "CHANGELINK");
        addAction("1xx1xx01", "CHANGELINK");
        addAction("1xx1x011", "CHANGELINK");
    }

    private static void addAction(String mask, String action) {
        Set<Integer> masks = expandMask(mask);
        for (Integer m : masks) {
            //System.out.println("add " + expandMask(m) + " (" + m + ")");
            ACTIONS.put(m, action);
        }
    }

    public static Set<Integer> expandMask(String mask) {
        return expandMask(0, 0, mask.charAt(0), mask.substring(1));
    }

    private static Set<Integer> expandMask(int bitPos, int prefix, char bit, String suffix) {
        switch (bit) {
            case '0':
            case '1':
                int bitValue = (bit == '0') ? 0 : (int)Math.pow(2, bitPos);
                if (suffix.isEmpty()) {
                    Set<Integer> masks = new HashSet<Integer>();
                    masks.add(prefix + bitValue);
                    return masks;
                }
                return expandMask(bitPos + 1, prefix + bitValue, suffix.charAt(0), suffix.substring(1));
            default:
                Set<Integer> masks = new HashSet<Integer>();
                masks.addAll(expandMask(bitPos, prefix, '0', suffix));
                masks.addAll(expandMask(bitPos, prefix, '1', suffix));
                return masks;
        }
    }

    /*
    private static void checkAction(String mask, String action) {
        Set<Integer> masks = expandMask(mask);
        for (Integer m : masks) {
            String act = ACTIONS.get(m);
            if (act == null) {
                System.out.println("mask " + expandMask(m) + " (" + m + ") is not permitted but should be " + action);
                continue;
            }
            if (! act.equals(action))
                System.out.println("mask " + expandMask(m) + " (" + m + ") is " + act + " but should be " + action);
        }
    }

    private static String expandMask(int mask) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 8; i++)
            b.append(((mask & (int)Math.pow(2, i)) > 0) ? "1" : "0");
        return b.toString();
    }

    private static void dumpAction(String action) {
        List<String> masks = new ArrayList<String>();
        for (int mask : ACTIONS.keySet()) {
            if (! ACTIONS.get(mask).equals(action)) continue;
            String m = expandMask(mask);
            if (! masks.contains(m)) masks.add(m);
        }
        Collections.sort(masks);
        for (String mask : masks)
            System.out.println(mask);
    }
    */


    public PlayerListenerImpl() {

        // This funkyness allows the code to work for both 1.2.5 and 1.3.1 releases of Bukkit/Tekkit
        // TODO: once Tekkit is based on 1.3.1, all this can be removed along with the synchronous player chat handler.
        try {
            Class.forName("org.bukkit.event.player.AsyncPlayerChatEvent");
            PlayerAsyncChatListenerImpl l = new PlayerAsyncChatListenerImpl();
            PluginManager pm = Global.plugin.getServer().getPluginManager();
            pm.registerEvents(l, Global.plugin);
            /*
            AsyncPlayerChatEvent.getHandlerList().register(new RegisteredListener(
                    this,
                    new EventExecutor() {
                        @Override
                        public void execute(Listener listener, Event event) throws EventException {
                            onPlayerChatAsync((AsyncPlayerChatEvent)event);
                        }
                    },
                    EventPriority.MONITOR,
                    Global.plugin,
                    true)
                    );
            */
            Utils.debug("registered as listener for Asynchronous chat events");
        } catch (ClassNotFoundException e) {
            PlayerChatEvent.getHandlerList().register(new RegisteredListener(
                    this,
                    new EventExecutor() {
                        @Override
                        public void execute(Listener listener, Event event) throws EventException {
                            onPlayerChatSync((PlayerChatEvent)event);
                        }
                    },
                    EventPriority.MONITOR,
                    Global.plugin,
                    true)
                    );
            Utils.debug("registered as listener for Synchronous chat events");
        }
    }


    //private Map<Player,Location> playerLocations = new HashMap<Player,Location>();

    public static Player testPlayer = null;

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            Utils.debug("no block was interacted with?");
            return;
        }
        Location location = block.getLocation();
        Context ctx = new Context(event.getPlayer());

        LocalGateImpl triggerGate = Gates.findGateForTrigger(location);
        LocalGateImpl switchGate = Gates.findGateForSwitch(location);
        if (event.getPlayer() == testPlayer) {
            Utils.debug("-Interaction-----------------------------------------");
            Utils.debug("location: %s", Utils.blockCoords(location));
            Utils.debug("triggerGate: %s", (triggerGate == null) ? "none" : triggerGate.getFullName());
            Utils.debug("switchGate: %s", (switchGate == null) ? "none" : switchGate.getFullName());
            if ((triggerGate == null) && (switchGate == null)) {
                Utils.debug("triggerMap: %s", Gates.triggerMap.toString(testPlayer.getWorld()));
                Utils.debug("switchMap: %s", Gates.switchMap.toString(testPlayer.getWorld()));
            }
        }

        if ((triggerGate == null) && (switchGate == null)) return;
        if ((triggerGate != null) && (switchGate != null) && (triggerGate != switchGate)) switchGate = null;

        LocalGateImpl testGate = (triggerGate == null) ? switchGate : triggerGate;
        Player player = event.getPlayer();
        Gates.setSelectedGate(player, testGate);

        int key =
                (testGate.isOpen() ? 1 : 0) +
                (Permissions.has(player, "trp.gate.open." + testGate.getFullName()) ? 2 : 0) +
                (Permissions.has(player, "trp.gate.close." + testGate.getFullName()) ? 4 : 0) +
                (Permissions.has(player, "trp.gate.changeLink." + testGate.getFullName()) ? 8 : 0) +
                (testGate.hasValidDestination() ? 16 : 0) +
                (testGate.isLastLink() ? 32 : 0) +
                ((triggerGate != null) ? 64 : 0) +
                ((switchGate != null) ? 128 : 0);
        String value = ACTIONS.get(key);
        Utils.debug("gate key/action is %s/%s", key, value);

        if (value == null) {
            ctx.send("not permitted");
            return;
        }
        String[] actions = value.split(",");

        for (String action : actions) {

            if (action.equals("OPEN")) {
                try {
                    testGate.open();
                    ctx.send("opened gate '%s'", testGate.getName());
                    Utils.debug("player '%s' open gate '%s'", player.getName(), testGate.getName());
                } catch (GateException ee) {
                    ctx.warnLog(ee.getMessage());
                }
            }

            if (action.equals("CLOSE")) {
                testGate.close();
                ctx.send("closed gate '%s'", testGate.getName());
                Utils.debug("player '%s' closed gate '%s'", player.getName(), testGate.getName());
            }

            if (action.equals("CHANGELINK")) {
                try {
                    testGate.nextLink();
                    Utils.debug("player '%s' changed link for gate '%s'", player.getName(), testGate.getName());
                } catch (TransporterException te) {
                    ctx.warnLog(te.getMessage());
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if ((event.getFrom().getBlockX() == event.getTo().getBlockX()) &&
            (event.getFrom().getBlockY() == event.getTo().getBlockY()) &&
            (event.getFrom().getBlockZ() == event.getTo().getBlockZ())) return;

        Player player = event.getPlayer();
        LocalGateImpl fromGate = Gates.findGateForPortal(event.getTo());
        if (fromGate == null) {
            ReservationImpl.removeGateLock(player);
            ReservationImpl.removeCountdown(player);
            return;
        }
        if (ReservationImpl.isGateLocked(player)) return;

        if (ReservationImpl.hasCountdown(player)) return;
        if (fromGate.getCountdown() > 0) {
            Countdown countdown = new Countdown(player, fromGate);
            countdown.start();
            return;
        }

        Context ctx = new Context(player);
        try {
            ReservationImpl r = new ReservationImpl(player, fromGate);
            r.depart();
            Location newLoc = r.getToLocation();
            if (newLoc != null) {
                event.setFrom(newLoc);
                event.setTo(newLoc);
            }
        } catch (ReservationException re) {
            ctx.warnLog(re.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location location = event.getTo();
        if ((location == null) ||
            (location.getWorld() == null)) return;

        // Realm handling
        Realm.onTeleport(player, location);

 Utils.debug("teleported %s", Utils.blockCoords(location));

        for (Server server : Servers.getAll())
            server.sendPlayerChangeWorld(player);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        ReservationImpl r = ReservationImpl.get(player);

        if ((r == null) && Realm.onJoin(player)) return;

        Context ctx = new Context(player);

        if ((r != null) && r.isDeparting()) {
            ctx.warnLog("You're not supposed to be here.");
            r = null;
        }

        for (Server server : Servers.getAll())
            server.sendPlayerJoin(player, r != null);
        
        
        if (r == null) {
            LocalGateImpl gate = Gates.findGateForPortal(player.getLocation());
            if (gate != null)
                ReservationImpl.addGateLock(player);
            return;
        }
        try {
            r.arrive();
            event.setJoinMessage(null);
        } catch (ReservationException e) {
            ctx.warnLog("there was a problem processing your arrival: ", e.getMessage());
        }
        
		TabAPI.setPriority(Global.plugin, player, 1);
		Bukkit.getScheduler().scheduleSyncDelayedTask(Global.plugin, new Runnable(){ public void run(){
			updateAll();
		}}, 3);

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ReservationImpl r = ReservationImpl.get(player);

        for (Server server : Servers.getAll())
            server.sendPlayerQuit(player, r != null);
        
		updateAll();

        if (r != null)
            event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        ReservationImpl r = ReservationImpl.get(player);

        for (Server server : Servers.getAll())
            server.sendPlayerKick(player, r != null);
        if (r != null)
            event.setLeaveMessage(null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = (Player)event.getEntity();
        for (Server server : Servers.getAll())
            server.sendPlayerDeath(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Realm.onRespawn(player);
    }

    // TODO: uncomment this when Tekkit goes to a 1.3.1, also remove PlayerAsyncChatListenerImpl
    /*
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChatAsync(final AsyncPlayerChatEvent event) {
        if (event.isAsynchronous())
            Utils.fire(new Runnable() {
                @Override
                public void run() {
                    Chat.send(event.getPlayer(), event.getMessage(), event.getFormat());
                }
            });
        else
            Chat.send(event.getPlayer(), event.getMessage(), event.getFormat());
    }
    */

    // TODO: remove this when Tekkit goes to a 1.3.1 RB
    public void onPlayerChatSync(PlayerChatEvent event) {
        Chat.send(event.getPlayer(), event.getMessage(), event.getFormat());
    }

    
	public void updateAll(){
		for(Player p: Bukkit.getOnlinePlayers()){
			update(p);
		}
	}

    
	public void update(Player p){

		int a = 0;
		int b = 0;
		
		setTabString( p, a, 0, "§9§l----------" + TabAPI.nextNull(), 9999);
		setTabString( p, a, 1, "§9§lServerliste", 9999);
		setTabString( p, a, 2, "§9§l----------" + TabAPI.nextNull(), 9999);
		a++;

		setTabString( p, a, 0, "§9Lokal [" + Global.plugin.getServer().getOnlinePlayers().length + "]", 0);
		b++;

		for (RemoteServer rServer : Global.plugin.getAPI().getRemoteServers()) {
			boolean isConnected = rServer.isConnected();
			
			String rsName = rServer.getName();
			String rsPlayers = isConnected ? Integer.toString(rServer.getRemotePlayers().size()) : "";
			
			if (rsName.length() + rsPlayers.length() + (isConnected ? 3 : 0) > 13) {
				rsName = rsName.substring(0, (isConnected ? 7 - rsPlayers.length() : 10)) + "...";
			}
			
			setTabString( p, a, b, "§9" + rsName + (isConnected ? " [" + rsPlayers + "]" : ""), isConnected ? 0 : -1);
			b++;
			
			if(b == 3){ b = 0; a++;}
			if(a >= TabAPI.getVertSize()) break;
		}
		
		if (fillLine(p, a, b)) { b = 0; a++; }

		if (a <= TabAPI.getVertSize()) {
			setTabString( p, a, 0, "§2§l----------" + TabAPI.nextNull(), 9999);
			setTabString( p, a, 1, "§a§lUserliste", 9999);
			setTabString( p, a, 2, "§2§l----------" + TabAPI.nextNull(), 9999);
			a++;
		}
		
		if (a <= TabAPI.getVertSize()) {
			for(Player pl:Bukkit.getOnlinePlayers()){
				setTabString( p, a, b, pl.getPlayerListName(), 0);
				b++;
				
				if(b == 3){ b = 0; a++;}
				
				if(a >= TabAPI.getVertSize()) break;
			}
		}
		
//		if (fillLine(p, a, b)) { b = 0; a++; }
		
		if (a <= TabAPI.getVertSize() && b < 3) {
			for (RemoteServer rServer : Global.plugin.getAPI().getRemoteServers()) {
				for (RemotePlayer rPlayer : rServer.getRemotePlayers()) {
					String pFormattedName = Server.formatPlayerListName(rServer.getPlayerListFormat(), rPlayer, rServer.getName());
					setTabString( p, a, b, pFormattedName, 0);
					b++;
					
					if(b == 3){ b = 0; a++;}
					
					if(a >= TabAPI.getVertSize()) break;
				}
				if(a >= TabAPI.getVertSize()) break;
			}
		}
		
		TabAPI.updatePlayer(p);
	}

	private boolean fillLine(Player p, int a, int b) {
		if (a <= TabAPI.getVertSize() && b > 0) {
			while (b < 3) {
				setTabString( p, a, b, " " + TabAPI.nextNull(), 9999);
				b++;
			}
			a++;
			b = 0;
			return true;
		}
		return false;
	}

	private void setTabString(Player p, int a, int b, String text, int ping) {
		text = text.trim();
		TabAPI.setTabString(Global.plugin, p, a, b, text, ping);
	}

}
