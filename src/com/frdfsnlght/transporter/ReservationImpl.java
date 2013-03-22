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

import com.frdfsnlght.transporter.api.Gate;
import com.frdfsnlght.transporter.api.GateException;
import com.frdfsnlght.transporter.api.Reservation;
import com.frdfsnlght.transporter.api.ReservationException;
import com.frdfsnlght.transporter.api.event.EntityArriveEvent;
import com.frdfsnlght.transporter.api.event.EntityDepartEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class ReservationImpl implements Reservation {

    private static final Map<Integer,Long> gateLocks = new HashMap<Integer,Long>();
    private static final Map<Integer,Countdown> countdowns = new HashMap<Integer,Countdown>();

    private static long nextId = 1;
    private static final Map<Long,ReservationImpl> reservations = new HashMap<Long,ReservationImpl>();

    public static ReservationImpl get(long id) {
        return reservations.get(id);
    }

    public static ReservationImpl get(String playerName) {
        for (ReservationImpl r : reservations.values())
            if (playerName.equals(r.playerName)) return r;
        return null;
    }

    public static ReservationImpl get(Player player) {
        return get(player.getName());
    }

    private static boolean put(ReservationImpl r) {
        if (reservations.put(r.localId, r) == null) {
            Utils.debug("put reservation %s", r.localId);
            return true;
        }
        return false;
    }

    private static boolean remove(ReservationImpl r) {
        if (reservations.remove(r.localId) != null) {
            Utils.debug("removed reservation %s", r.localId);
            return true;
        }
        return false;
    }

    public static void removeGateLock(Entity entity) {
        if (entity == null) return;
        Long expiry = gateLocks.get(entity.getEntityId());
        if (expiry == null) return;
        if (expiry <= System.currentTimeMillis()) {
            gateLocks.remove(entity.getEntityId());
            Utils.debug("removed gate lock for entity %d", entity.getEntityId());
        }
    }

    public static boolean isGateLocked(Entity entity) {
        if (entity == null) return false;
        return gateLocks.containsKey(entity.getEntityId());
    }

    public static void addGateLock(Entity entity) {
        if (entity == null) return;
        gateLocks.put(entity.getEntityId(), System.currentTimeMillis() + Config.getGateLockExpiration());
        Utils.debug("added gate lock for entity %d", entity.getEntityId());
    }

    public static void removeCountdown(Entity entity) {
        if (entity == null) return;
        Countdown countdown = countdowns.get(entity.getEntityId());
        if (countdown == null) return;
        countdowns.remove(entity.getEntityId());
        countdown.cancel();
        Utils.debug("removed countdown for entity %d", entity.getEntityId());
    }

    public static void removeCountdown(Countdown countdown) {
        if (countdown == null) return;
        for (Iterator<Integer> i = countdowns.keySet().iterator(); i.hasNext(); ) {
            int id = i.next();
            if (countdowns.get(id) == countdown) {
                i.remove();
                Utils.debug("removed countdown for entity %d", countdown.getPlayer().getEntityId());
            }
        }
    }

    public static void removeCountdowns(LocalGateImpl gate) {
        for (Iterator<Integer> i = countdowns.keySet().iterator(); i.hasNext(); ) {
            int id = i.next();
            Countdown countdown = countdowns.get(id);
            if (countdown.getGate() == gate) {
                i.remove();
                Utils.debug("removed countdown for entity %d", countdown.getPlayer().getEntityId());
            }
        }
    }

    public static void addCountdown(Countdown countdown) {
        if (countdown == null) return;
        countdowns.put(countdown.getPlayer().getEntityId(), countdown);
        Utils.debug("added countdown for entity %d", countdown.getPlayer().getEntityId());
    }

    public static boolean hasCountdown(Entity entity) {
        if (entity == null) return false;
        return countdowns.containsKey(entity.getEntityId());
    }

    private long localId = nextId++;
    private long remoteId = 0;
    private boolean departing = true;

    private EntityType entityType = null;
    private Entity entity = null;
    private int localEntityId = 0;
    private int remoteEntityId = 0;

    private Player player = null;
    private String playerName = null;
    private String playerPin = null;
    private String clientAddress = null;

    private ItemStack[] inventory = null;
    private int health = 0;
    private int remainingAir = 0;
    private int fireTicks = 0;
    private int foodLevel = 0;
    private float exhaustion = 0;
    private float saturation = 0;
    private String gameMode = null;
    private int heldItemSlot = 0;
    private ItemStack[] armor = null;
    private int level = 0;
    private float xp = 0;
    private PotionEffect[] potionEffects = null;

    private Location fromLocation = null;
    private Vector fromVelocity = null;
    private BlockFace fromDirection = null;
    private GateImpl fromGate = null;
    private String fromWorldName = null;
    private String fromGateName = null;
    private LocalGateImpl fromGateLocal = null; // local gate
    private World fromWorld = null;         // local gate
    private Server fromServer = null;       // remote gate

    private Location toLocation = null;
    private Vector toVelocity = null;
    private BlockFace toDirection = null;
    private GateImpl toGate = null;
    private String toWorldName = null;
    private String toGateName = null;
    private LocalGateImpl toGateLocal = null;   // local gate
    private World toWorld = null;           // local gate
    private Server toServer = null;         // remote gate

    private boolean createdEntity = false;

    // player stepping into gate
    public ReservationImpl(Player player, LocalGateImpl fromGate) throws ReservationException {
        addGateLock(player);
        extractPlayer(player);
        extractFromGate(fromGate);
    }

    // vehicle moving into gate
    public ReservationImpl(Vehicle vehicle, LocalGateImpl fromGate) throws ReservationException {
        addGateLock(vehicle);
        extractVehicle(vehicle);
        extractFromGate(fromGate);
    }

    // player direct to gate
    public ReservationImpl(Player player, GateImpl toGate) throws ReservationException {
        addGateLock(player);
        extractPlayer(player);
        extractToGate(toGate);
    }

    // player direct to location on this server
    public ReservationImpl(Player player, Location location) throws ReservationException {
        addGateLock(player);
        extractPlayer(player);
        toLocation = location;
    }

    // player direct to remote server, default world, spawn location
    public ReservationImpl(Player player, Server server) throws ReservationException {
        addGateLock(player);
        extractPlayer(player);
        toServer = server;
    }

    // player direct to remote server, specified world, spawn location
    public ReservationImpl(Player player, Server server, String worldName) throws ReservationException {
        addGateLock(player);
        extractPlayer(player);
        toServer = server;
        toWorldName = worldName;
    }

    // player direct to remote server, specified world, specified location
    public ReservationImpl(Player player, Server server, String worldName, double x, double y, double z) throws ReservationException {
        addGateLock(player);
        extractPlayer(player);
        toServer = server;
        toWorldName = worldName;
        toLocation = new Location(null, x, y, z);
    }

    // reception of reservation from sending server
    public ReservationImpl(TypeMap in, Server server) throws ReservationException {
        remoteId = in.getInt("id");
        departing = false;
        try {
            entityType = Utils.valueOf(EntityType.class, in.getString("entityType"));
        } catch (IllegalArgumentException e) {
            throw new ReservationException(e.getMessage() + " entityType '%s'", in.getString("entityType"));
        }
        remoteEntityId = in.getInt("entityId");
        playerName = in.getString("playerName");
        if (playerName != null) {
            ReservationImpl other = get(playerName);
            if (other != null)
                remove(other);
                //throw new ReservationException("a reservation for player '%s' already exists", playerName);
            player = Global.plugin.getServer().getPlayer(playerName);
        }
        playerPin = in.getString("playerPin");
        clientAddress = in.getString("clientAddress");
        fromLocation = new Location(null, in.getDouble("fromX"), in.getDouble("fromY"), in.getDouble("fromZ"), in.getFloat("fromYaw"), in.getFloat("fromPitch"));
        fromVelocity = new Vector(in.getDouble("velX"), in.getDouble("velY"), in.getDouble("velZ"));
        inventory = Inventory.decodeItemStackArray(in.getMapList("inventory"));
        health = in.getInt("health");
        remainingAir = in.getInt("remainingAir");
        fireTicks = in.getInt("fireTicks");
        foodLevel = in.getInt("foodLevel");
        exhaustion = in.getFloat("exhaustion");
        saturation = in.getFloat("saturation");
        gameMode = in.getString("gameMode");
        heldItemSlot = in.getInt("heldItemSlot");
        armor = Inventory.decodeItemStackArray(in.getMapList("armor"));
        level = in.getInt("level");
        xp = in.getFloat("xp");
        potionEffects = PotionEffects.decodePotionEffects(in.getMapList("potionEffects"));

        fromWorldName = in.getString("fromWorld");

        fromServer = server;

        if (in.get("toX") != null)
            toLocation = new Location(null, in.getDouble("toX"), in.getDouble("toY"), in.getDouble("toZ"));

        toWorldName = in.getString("toWorldName");
        if (toWorldName != null) {
            toWorld = Global.plugin.getServer().getWorld(toWorldName);
            if (toWorld == null)
                throw new ReservationException("unknown world '%s'", toWorldName);
        }

        fromGateName = in.getString("fromGate");
        if (fromGateName != null) {
            fromGateName = server.getName() + "." + fromGateName;
            fromGate = Gates.get(fromGateName);
            if (fromGate == null)
                throw new ReservationException("unknown fromGate '%s'", fromGateName);
            if (fromGate.isSameServer())
                throw new ReservationException("fromGate '%s' is not a remote gate", fromGateName);
            try {
                fromDirection = Utils.valueOf(BlockFace.class, in.getString("fromGateDirection"));
            } catch (IllegalArgumentException e) {
                throw new ReservationException(e.getMessage() + " fromGateDirection '%s'", in.getString("fromGateDirection"));
            }
        }

        toGateName = in.getString("toGate");
        if (toGateName != null) {
            toGateName = toGateName.substring(toGateName.indexOf(".") + 1);
            toGate = Gates.get(toGateName);
            if (toGate == null)
                throw new ReservationException("unknown toGate '%s'", toGateName);
            if (! toGate.isSameServer())
                throw new ReservationException("toGate '%s' is not a local gate", toGateName);
            toGateLocal = (LocalGateImpl)toGate;
            toDirection = toGateLocal.getDirection();
            toWorld = toGateLocal.getWorld();
            toWorldName = toWorld.getName();
            if (fromDirection == null)
                fromDirection = toDirection;
        }
    }

    /* Reservation interface */

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public Gate getDepartureGate() {
        return fromGate;
    }

    @Override
    public Gate getArrivalGate() {
        return toGate;
    }

    /* End Reservation interface */

    private void extractPlayer(Player player) {
        entityType = EntityType.PLAYER;
        entity = player;
        localEntityId = player.getEntityId();
        this.player = player;
        playerName = player.getName();
        playerPin = Pins.get(player);
        clientAddress = player.getAddress().getAddress().getHostAddress();
        health = player.getHealth();
        remainingAir = player.getRemainingAir();
        fireTicks = player.getFireTicks();
        foodLevel = player.getFoodLevel();
        exhaustion = player.getExhaustion();
        saturation = player.getSaturation();
        gameMode = player.getGameMode().toString();
        PlayerInventory inv = player.getInventory();
        inventory = Arrays.copyOf(inv.getContents(), inv.getSize());
        heldItemSlot = inv.getHeldItemSlot();
        armor = inv.getArmorContents();
        level = player.getLevel();
        xp = player.getExp();
        potionEffects = player.getActivePotionEffects().toArray(new PotionEffect[] {});
        fromLocation = player.getLocation();
        fromVelocity = player.getVelocity();
        fromWorldName = player.getWorld().getName();

//        Utils.debug("player location: %s", fromLocation);
//        Utils.debug("player velocity: %s", fromVelocity);
    }

    private void extractVehicle(Vehicle vehicle) {
        if (vehicle.getPassenger() instanceof Player)
            extractPlayer((Player)vehicle.getPassenger());

        if (vehicle instanceof Minecart)
            entityType = EntityType.MINECART;
        else if (vehicle instanceof PoweredMinecart)
            entityType = EntityType.POWERED_MINECART;
        else if (vehicle instanceof StorageMinecart) {
            entityType = EntityType.STORAGE_MINECART;
            org.bukkit.inventory.Inventory inv = ((StorageMinecart)vehicle).getInventory();
            inventory = Arrays.copyOf(inv.getContents(), inv.getSize());
        } else if (vehicle instanceof Boat)
            entityType = EntityType.BOAT;
        else
            throw new IllegalArgumentException("can't create state for " + vehicle.getClass().getName());
        entity = vehicle;
        localEntityId = vehicle.getEntityId();
        fireTicks = vehicle.getFireTicks();
        fromLocation = vehicle.getLocation();
        fromVelocity = vehicle.getVelocity();
        fromWorldName = vehicle.getWorld().getName();
        Utils.debug("vehicle location: %s", fromLocation);
        Utils.debug("vehicle velocity: %s", fromVelocity);
    }

    private void extractFromGate(LocalGateImpl fromGate) throws ReservationException {
        this.fromGate = fromGateLocal = fromGate;
        fromGateName = fromGate.getFullName();
        fromDirection = fromGate.getDirection();
        fromWorld = fromGate.getWorld();
        fromWorldName = fromGate.getWorld().getName();

        if (fromGate.getSendNextLink())
            try {
                fromGate.nextLink();
            } catch (GateException ge) {
                throw new ReservationException(ge.getMessage());
            }

        try {
            toGate = fromGateLocal.getDestinationGate();
        } catch (GateException ge) {
            throw new ReservationException(ge.getMessage());
        }

        toGateName = toGate.getFullName();
        if (toGate.isSameServer()) {
            toGateLocal = (LocalGateImpl)toGate;
            toWorld = toGateLocal.getWorld();
        } else
            toServer = (Server)((RemoteGateImpl)toGate).getRemoteServer();
    }

    private void extractToGate(GateImpl toGate) {
        this.toGate = toGate;
        toGateName = toGate.getFullName();
        if (toGate.isSameServer()) {
            toGateLocal = (LocalGateImpl)toGate;
            toWorld = toGateLocal.getWorld();
            toDirection = toGateLocal.getDirection();
            if (fromDirection == null)
                fromDirection = toDirection;
        } else
            toServer = (Server)((RemoteGateImpl)toGate).getRemoteServer();
    }

    public TypeMap encode() {
        TypeMap out = new TypeMap();
        out.put("id", localId);
        out.put("entityType", entityType.toString());
        out.put("entityId", localEntityId);
        out.put("playerName", playerName);
        out.put("playerPin", playerPin);
        out.put("clientAddress", clientAddress);
        out.put("velX", fromVelocity.getX());
        out.put("velY", fromVelocity.getY());
        out.put("velZ", fromVelocity.getZ());
        out.put("fromX", fromLocation.getX());
        out.put("fromY", fromLocation.getY());
        out.put("fromZ", fromLocation.getZ());
        out.put("fromPitch", fromLocation.getPitch());
        out.put("fromYaw", fromLocation.getYaw());
        out.put("fromWorld", fromWorldName);
        out.put("inventory", Inventory.encodeItemStackArray(inventory));
        out.put("health", health);
        out.put("remainingAir", remainingAir);
        out.put("fireTicks", fireTicks);
        out.put("foodLevel", foodLevel);
        out.put("exhaustion", exhaustion);
        out.put("saturation", saturation);
        out.put("gameMode", gameMode);
        out.put("heldItemSlot", heldItemSlot);
        out.put("armor", Inventory.encodeItemStackArray(armor));
        out.put("level", level);
        out.put("xp", xp);
        out.put("potionEffects", PotionEffects.encodePotionEffects(potionEffects));
        out.put("fromGate", fromGateName);
        if (fromDirection != null)
            out.put("fromGateDirection", fromDirection.toString());
        out.put("toGate", toGateName);
        out.put("toWorldName", toWorldName);
        if (toLocation != null) {
            out.put("toX", toLocation.getX());
            out.put("toY", toLocation.getY());
            out.put("toZ", toLocation.getZ());
        }
        return out;
    }

    public boolean isDeparting() {
        return departing;
    }

    public boolean isArriving() {
        return ! departing;
    }

    // called to handle departure on the sending side
    public void depart() throws ReservationException {
        put(this);
        try {
            addGateLock(entity);
            if (entity != player)
                addGateLock(player);

            checkLocalDepartureGate();

            if (toServer == null) {
                // staying on this server
                checkLocalArrivalGate();

                EntityDepartEvent event = new EntityDepartEvent(this);
                Global.plugin.getServer().getPluginManager().callEvent(event);

                arrive();
                completeLocalDepartureGate();

            } else {
                // going to remote server
                try {
                    Utils.debug("sending reservation for %s to %s...", getTraveler(), getDestination());
                    toServer.sendReservation(this);

                    // setup delayed task to remove the reservation on this side if it doesn't work out
                    final ReservationImpl me = this;
                    Utils.fireDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (! remove(me)) return;
                            Utils.warning("reservation for %s to %s timed out", getTraveler(), getDestination());
                        }
                    }, Config.getArrivalWindow());

                } catch (ServerException e) {
                    Utils.severe(e, "reservation send for %s to %s failed:", getTraveler(), getDestination());
                    remove(this);
                    throw new ReservationException("teleport %s to %s failed", getTraveler(), getDestination());
                }
            }
        } catch (ReservationException e) {
            remove(this);
            throw e;
        }
    }

    // called on the receiving side to indicate this reservation has been sent from the sender
    public void receive() {
        try {
            Utils.debug("received reservation for %s to %s from %s...", getTraveler(), getDestination(), fromServer.getName());
            if (playerName != null) {
                try {
                    Permissions.connect(playerName);
                } catch (PermissionsException e) {
                    throw new ReservationException(e.getMessage());
                }
            }
            checkLocalArrivalGate();
            put(this);
            try {
                fromServer.sendReservationApproved(remoteId);
            } catch (ServerException e) {
                Utils.severe(e, "send reservation approval for %s to %s to %s failed:", getTraveler(), getDestination(), fromServer.getName());
                remove(this);
                return;
            }

            Utils.debug("reservation for %s to %s approved", getTraveler(), getDestination());

            if (playerName == null) {
                // there's no player coming, so handle the "arrival" now
                try {
                    arrive();
                } catch (ReservationException e) {
                    Utils.warning("reservation arrival for %s to %s to %s failed:", getTraveler(), getDestination(), fromServer.getName(), e.getMessage());
                }
            } else {
                // set up a delayed task to cancel the arrival if they never arrive
                final ReservationImpl res = this;
                Utils.fireDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (! remove(res)) return;
                        Utils.warning("reservation for %s to %s timed out", getTraveler(), getDestination());
                        try {
                            fromServer.sendReservationTimeout(remoteId);
                        } catch (ServerException e) {
                            Utils.severe(e, "send reservation timeout for %s to %s to %s failed:", getTraveler(), getDestination(), fromServer.getName());
                        }
                    }
                }, Config.getArrivalWindow());
            }

        } catch (ReservationException e) {
            Utils.debug("reservation for %s to %s denied: %s", getTraveler(), getDestination(), e.getMessage());
            remove(this);
            try {
                fromServer.sendReservationDenied(remoteId, e.getMessage());
            } catch (ServerException e2) {
                Utils.severe(e, "send reservation denial for %s to %s to %s failed:", getTraveler(), getDestination(), fromServer.getName());
            }
        }
    }

    // called on the receiving side to handle arrival
    public void arrive() throws ReservationException {
        remove(this);

        if (toGateLocal != null)
            toGateLocal.attach(fromGate);

        prepareDestination();
        prepareTraveler();
        addGateLock(entity);
        if (entity != player)
            addGateLock(player);
        if ((player != null) && (playerPin != null))
            Pins.add(player, playerPin);
        if (toLocation != null) {
            // NEW: Bukkit no longer teleports vehicles with passengers
            if ((entity == player) || (player == null)) {
                if (! entity.teleport(toLocation)) {
                    rollbackTraveler();
                    throw new ReservationException("teleport %s to %s failed", getTraveler(), getDestination());
                }
            }
        }
        commitTraveler();

        Utils.debug("%s arrived at %s", getTraveler(), getDestination());

        completeLocalArrivalGate();

        if (fromServer == null)
            arrived();
        else
            try {
                fromServer.sendReservationArrived(remoteId);
            } catch (ServerException e) {
                Utils.severe(e, "send reservation arrival for %s to %s to %s failed:", getTraveler(), getDestination(), fromServer.getName());
            }
    }

    // called on the sending side to confirm reception of the valid reservation on the receiving side
    public void approved() {
        Utils.debug("reservation to send %s to %s was approved", getTraveler(), getDestination());

        if (player != null) {
            Context ctx = new Context(player);

            completeLocalDepartureGate();

            switch (toServer.getTransferMethod()) {
                case ClientKick:
                    String kickMessage = toServer.getKickMessage(player.getAddress());
                    if (kickMessage == null) return;
                    Utils.schedulePlayerKick(player, kickMessage);
                    break;
                case Bungee:
                    Utils.sendPlayerToBungeeServer(player, toServer.getRemoteBungeeServer());
                    break;
            }
        }
        if ((entity != null) && (entity != player))
            entity.remove();

        EntityDepartEvent event = new EntityDepartEvent(this);
        Global.plugin.getServer().getPluginManager().callEvent(event);

    }

    // called on the sending side to indicate a reservation was denied by the receiving side
    public void denied(final String reason) {
        remove(this);
        if (player == null)
            Utils.warning("reservation to send %s to %s was denied: %s", getTraveler(), getDestination(), reason);
        else {
            Context ctx = new Context(player);
            ctx.warn(reason);
        }
    }

    // called on the sending side to indicate an expected arrival arrived on the receiving side
    public void arrived() {
        remove(this);
        Utils.debug("reservation to send %s to %s was completed", getTraveler(), getDestination());

        if ((toServer != null) && (player != null)) {
            if ((fromGateLocal != null) && fromGateLocal.getDeleteInventory()) {
                PlayerInventory inv = player.getInventory();
                inv.clear();
                inv.setBoots(new ItemStack(Material.AIR));
                inv.setHelmet(new ItemStack(Material.AIR));
                inv.setChestplate(new ItemStack(Material.AIR));
                inv.setLeggings(new ItemStack(Material.AIR));
                player.saveData();
            }
        }

        EntityArriveEvent event = new EntityArriveEvent(this);
        Global.plugin.getServer().getPluginManager().callEvent(event);

    }

    // called on the sending side to indicate an expected arrival never happened on the receiving side
    public void timeout() {
        remove(this);
        Utils.warning("reservation to send %s to %s timed out", getTraveler(), getDestination());
    }




    // called after arrival to get the destination on the local server where the entity arrived
    public Location getToLocation() {
        return toLocation;
    }






    private void checkLocalDepartureGate() throws ReservationException {
        if (fromGateLocal == null) return;

        if (player != null) {
            // player permission
            try {
                Permissions.require(player, "trp.gate.use." + fromGateLocal.getFullName());
            } catch (PermissionsException e) {
                throw new ReservationException(e.getMessage());
            }
            // player PIN
            if (fromGateLocal.getRequirePin()) {
                if (playerPin == null)
                    throw new ReservationException("this gate requires a pin");
                if (! fromGateLocal.hasPin(playerPin))
                    throw new ReservationException("this gate rejected your pin");
            }
            // player level
            if (fromGateLocal.getRequireLevel() > 0) {
                if (level < fromGateLocal.getRequireLevel())
                    throw new ReservationException("this gate requires you to be level %s or above", fromGateLocal.getRequireLevel());
            }
            // player cost
            double cost = fromGateLocal.getSendCost(toGate);
            if (cost > 0)
                try {
                    Economy.requireFunds(player, cost);
                } catch (EconomyException e) {
                    throw new ReservationException("this gate requires %s", Economy.format(cost));
                }
            if ((toGate != null) && toGate.isSameServer()) {
                cost += ((LocalGateImpl)toGate).getReceiveCost(fromGateLocal);
                if (cost > 0)
                    try {
                        Economy.requireFunds(player, cost);
                    } catch (EconomyException e) {
                        throw new ReservationException("total travel cost requires %s", Economy.format(cost));
                    }
            }
        }

        // check gate permission
        if ((toGate != null) && Config.getUseGatePermissions()) {
            try {
                Permissions.require(fromGateLocal.getWorld().getName(), fromGateLocal.getName(), "trp.send." + toGate.getGlobalName());
            } catch (PermissionsException e) {
                throw new ReservationException("this gate is not permitted to send to the remote gate");
            }
        }
    }

    private void checkLocalArrivalGate() throws ReservationException {
        if (toGateLocal == null) return;

        if (player != null) {
            // player permission
            try {
                Permissions.require(player, "trp.gate.use." + toGateLocal.getFullName());
            } catch (PermissionsException e) {
                throw new ReservationException(e.getMessage());
            }
            // player PIN
            if (toGateLocal.getRequirePin()) {
                if (playerPin == null)
                    throw new ReservationException("remote gate requires a pin");
                if ((! toGateLocal.hasPin(playerPin)) && toGateLocal.getRequireValidPin())
                    throw new ReservationException("remote gate rejected your pin");
            }
            // player level
            if (toGateLocal.getRequireLevel() > 0) {
                if (level < toGateLocal.getRequireLevel())
                    throw new ReservationException("remote gate requires you to be level %s or above", toGateLocal.getRequireLevel());
            }
            // player game mode
            if (toGateLocal.getReceiveGameMode()) {
                if (! toGateLocal.isAllowedGameMode(gameMode))
                    throw new ReservationException("remote gate rejected your game mode");
            }
            // player cost
            if (fromServer != null) {
                // only check this side since the departure side already checked itself
                double cost = toGateLocal.getReceiveCost(fromGate);
                if (cost > 0)
                    try {
                        Economy.requireFunds(player, cost);
                    } catch (EconomyException e) {
                        throw new ReservationException("remote gate requires %s", Economy.format(cost));
                    }
            }
        }

        // check inventory
        // this is only checked on the arrival side
        if (toGateLocal.getReceiveInventory() &&
                ((! toGateLocal.isAcceptableInventory(inventory)) ||
                 (! toGateLocal.isAcceptableInventory(armor))))
            throw new ReservationException("remote gate won't allow some inventory items");

        // check potions
        // this is only checked on the arrival side
        if (toGateLocal.getReceivePotions() && (! toGateLocal.isAcceptablePotions(potionEffects)))
            throw new ReservationException("remote gate won't allow some potion effects");

        // check gate permission
        if ((fromGate != null) && Config.getUseGatePermissions()) {
            try {
                Permissions.require(toGateLocal.getWorld().getName(), toGateLocal.getName(), "trp.receive." + fromGateLocal.getGlobalName());
            } catch (PermissionsException e) {
                throw new ReservationException("the remote gate is not permitted to receive from this gate");
            }
        }

    }

    private void completeLocalDepartureGate() {
        if (fromGateLocal == null) return;

        // Handle lightning strike...

        fromGateLocal.onSend(entity);

        if (player != null) {

            Context ctx = new Context(player);

            // player cost
            double cost = fromGateLocal.getSendCost(toGate);
            if ((toGate != null) && toGate.isSameServer())
                cost += ((LocalGateImpl)toGate).getReceiveCost(fromGateLocal);
            if (cost > 0)
                try {
                    if (Economy.deductFunds(player, cost))
                        ctx.send("debited %s for travel costs", Economy.format(cost));
                } catch (EconomyException e) {
                    // too late to do anything useful
                    Utils.warning("unable to debit travel costs for %s: %s", getTraveler(), e.getMessage());
                }
        }

    }

    private void completeLocalArrivalGate() {
        if (toGateLocal == null) return;

        toGateLocal.onReceive(entity);

        if (player != null) {

            Context ctx = new Context(player);

            if (toGateLocal.getTeleportFormat() != null) {
                String format = toGateLocal.getTeleportFormat();
                format = format.replace("%player%", player.getDisplayName());
                format = format.replace("%toGateCtx%", toGateLocal.getName(ctx));
                format = format.replace("%toGate%", toGateLocal.getName());
                format = format.replace("%toWorld%", toGateLocal.getWorld().getName());
                format = format.replace("%fromGateCtx%", (fromGate == null) ? "" : fromGate.getName(ctx));
                format = format.replace("%fromGate%", (fromGate == null) ? "" : fromGate.getName());
                format = format.replace("%fromWorld%", fromWorldName);
                format = format.replace("%fromServer%", (fromServer == null) ? "local" : fromServer.getName());
                if (! format.isEmpty())
                    ctx.send(format);
            }

            // player PIN
            if (toGateLocal.getRequirePin() &&
                (! toGateLocal.hasPin(playerPin)) &&
                (! toGateLocal.getRequireValidPin()) &&
                (toGateLocal.getInvalidPinDamage() > 0)) {
                ctx.send("invalid pin");
                player.damage(toGateLocal.getInvalidPinDamage());
            }

            // player cost
            if (fromServer != null) {
                // only deduct this side since the departure side already deducted itself
                double cost = toGateLocal.getReceiveCost(fromGate);
                if (cost > 0)
                    try {
                        if (Economy.deductFunds(player, cost))
                            ctx.sendLog("debited %s for travel costs", Economy.format(cost));
                    } catch (EconomyException e) {
                        // too late to do anything useful
                        Utils.warning("unable to debit travel costs for %s: %s", getTraveler(), e.getMessage());
                    }
            }
        } else {
            Utils.debug("%s arrived at '%s'", getTraveler(), toGateLocal.getFullName());
        }
    }

    private void prepareDestination() {
        if (toGateLocal != null) {
            toLocation = toGateLocal.getSpawnLocation(fromLocation, fromDirection);
            toVelocity = fromVelocity.clone();
            Utils.rotate(toVelocity, fromDirection, toGateLocal.getDirection());
            Utils.debug("fromLocation: %s", fromLocation);
            Utils.debug("fromVelocity: %s", fromVelocity);
            Utils.debug("toLocation: %s", toLocation);
            Utils.debug("toVelocity: %s", toVelocity);
        } else {
            if (toLocation == null) {
                if (toWorld == null)
                    toWorld = Global.plugin.getServer().getWorlds().get(0);
                toLocation = toWorld.getSpawnLocation();
            } else if (toLocation.getWorld() == null)
                toLocation.setWorld(Global.plugin.getServer().getWorlds().get(0));
            toLocation.setYaw(fromLocation.getYaw());
            toLocation.setPitch(fromLocation.getPitch());
            toVelocity = fromVelocity.clone();
        }
        if (toLocation != null) {
            Utils.prepareChunk(toLocation);

            // tweak velocity so we don't get buried in a block
            Location nextLocation = toLocation.clone().add(toVelocity.getX(), toVelocity.getY(), toVelocity.getZ());
            Utils.prepareChunk(nextLocation);
            switch (nextLocation.getBlock().getType()) {
                // there are probably others
                case AIR:
                case WATER:
                case STATIONARY_WATER:
                case LAVA:
                case STATIONARY_LAVA:
                case WEB:
                case TORCH:
                case REDSTONE_TORCH_OFF:
                case REDSTONE_TORCH_ON:
                case SIGN_POST:
                case RAILS:
                    break;
                default:
                    // should we try to zero just each ordinate and test again?
                    Utils.debug("zeroing velocity to avoid block");
                    toVelocity.zero();
                    break;
            }
        }

        Utils.debug("destination location: %s", toLocation);
        Utils.debug("destination velocity: %s", toVelocity);
    }

    private void prepareTraveler() throws ReservationException {
        Utils.debug("prepareTraveler %s", getTraveler());
        if ((player == null) && (playerName != null)) {
            player = Global.plugin.getServer().getPlayer(playerName);
            if (player == null)
                throw new ReservationException("player '%s' not found", playerName);
        }

        if ((toGateLocal != null) && toGateLocal.getReceiveInventory()) {
            // filter inventory
            boolean invFiltered = toGateLocal.filterInventory(inventory);
            boolean armorFiltered = toGateLocal.filterInventory(armor);
            if (invFiltered || armorFiltered) {
                if (player == null)
                    Utils.debug("some inventory items where filtered by the arrival gate");
                else
                    (new Context(player)).send("some inventory items where filtered by the arrival gate");
            }
        }
        if ((toGateLocal != null) && toGateLocal.getReceivePotions()) {
            // filter potions
            boolean potionsFiltered = toGateLocal.filterPotions(potionEffects);
            if (potionsFiltered) {
                if (player == null)
                    Utils.debug("some potion effects where filtered by the arrival gate");
                else
                    (new Context(player)).send("some potion effects where filtered by the arrival gate");
            }
        }

        World theWorld;
        Location theLocation;
        if (toLocation == null) {
            theWorld = Global.plugin.getServer().getWorlds().get(0);
            theLocation = theWorld.getSpawnLocation();
        } else {
            theWorld = toLocation.getWorld();
            theLocation = toLocation;
        }

        if (entity == null) {
            switch (entityType) {
                case PLAYER:
                    entity = player;
                    break;
                case MINECART:
                    entity = theWorld.spawn(theLocation, Minecart.class);
                    createdEntity = true;
                    if (player != null)
                        ((Minecart)entity).setPassenger(player);
                    break;
                case POWERED_MINECART:
                    entity = theWorld.spawn(theLocation, PoweredMinecart.class);
                    createdEntity = true;
                    break;
                case STORAGE_MINECART:
                    entity = theWorld.spawn(theLocation, StorageMinecart.class);
                    createdEntity = true;
                    break;
                case BOAT:
                    entity = theWorld.spawn(theLocation, Boat.class);
                    createdEntity = true;
                    break;
                default:
                    throw new ReservationException("unknown entity type '%s'", entityType);
            }
        } else if ((entity != player) && (player != null)) {
            Utils.debug("spoofing vehicle/passenger teleportation");
            switch (entityType) {
                case MINECART:
                    entity.remove();
                    entity = theWorld.spawn(theLocation, Minecart.class);
                    break;
                case BOAT:
                    entity.remove();
                    entity = theWorld.spawn(theLocation, Boat.class);
                    break;
            }
        }
        if (player != null) {
            if (health < 0) health = 0;
            if (remainingAir < 0) remainingAir = 0;
            if (foodLevel < 0) foodLevel = 0;
            if (exhaustion < 0) exhaustion = 0;
            if (saturation < 0) saturation = 0;
            if (level < 0) level = 0;
            if (xp < 0) xp = 0;

            if (toGateLocal == null) {
                player.setHealth(health);
                player.setRemainingAir(remainingAir);
                player.setFoodLevel(foodLevel);
                player.setExhaustion(exhaustion);
                player.setSaturation(saturation);
                player.setFireTicks(fireTicks);
            } else {
                if (toGateLocal.getReceiveStats()) {
                    player.setHealth(health);
                    player.setRemainingAir(remainingAir);
                    player.setFoodLevel(foodLevel);
                    player.setExhaustion(exhaustion);
                    player.setSaturation(saturation);
                    player.setFireTicks(fireTicks);
                }
                if (toGateLocal.getReceiveXP()) {
                    player.setLevel(level);
                    player.setExp(xp);
                }
                if (toGateLocal.getGameMode() != null)
                    player.setGameMode(toGateLocal.getGameMode());
                else if (toGateLocal.getReceiveGameMode())
                    player.setGameMode(Utils.valueOf(GameMode.class, gameMode));
                if (! toGateLocal.getReceiveInventory()) {
                    inventory = null;
                    armor = null;
                }
                if (! toGateLocal.getReceivePotions())
                    potionEffects = null;
            }
            player.setVelocity(toVelocity);
            if (inventory != null) {
                PlayerInventory inv = player.getInventory();
                for (int slot = 0; slot < inventory.length; slot++) {
                    if (inventory[slot] == null)
                        inv.setItem(slot, new ItemStack(Material.AIR.getId()));
                    else
                        inv.setItem(slot, inventory[slot]);
                }
                // PENDING: This doesn't work as expected. it replaces whatever's
                // in slot 0 with whatever's in the held slot. There doesn't appear to
                // be a way to change just the slot of the held item
                //inv.setItemInHand(inv.getItem(heldItemSlot));
            }
            if (armor != null) {
                PlayerInventory inv = player.getInventory();
                inv.setArmorContents(armor);
            }
            if (potionEffects != null) {
                for (PotionEffectType pet : PotionEffectType.values()) {
                    if (pet == null) continue;
                    if (player.hasPotionEffect(pet))
                        player.removePotionEffect(pet);
                }
                for (PotionEffect effect : potionEffects) {
                    if (effect == null) continue;
                    player.addPotionEffect(effect);
                }
            }
        }

        switch (entityType) {
            case MINECART:
                if ((player != null) && (entity.getPassenger() != player)) {
                    entity.setPassenger(player);
                    //player.setVelocity(toVelocity);
                }
                entity.setFireTicks(fireTicks);
                entity.setVelocity(toVelocity);
                break;
            case POWERED_MINECART:
                entity.setFireTicks(fireTicks);
                entity.setVelocity(toVelocity);
                break;
            case STORAGE_MINECART:
                entity.setFireTicks(fireTicks);
                entity.setVelocity(toVelocity);
                if ((inventory != null) && ((toGateLocal == null) || toGateLocal.getReceiveInventory())) {
                    StorageMinecart mc = (StorageMinecart)entity;
                    org.bukkit.inventory.Inventory inv = mc.getInventory();
                    for (int slot = 0; slot <  inventory.length; slot++)
                        inv.setItem(slot, inventory[slot]);
                }
                break;
            case BOAT:
                if ((player != null) && (entity.getPassenger() != player)) {
                    entity.setPassenger(player);
                    //player.setVelocity(toVelocity);
                }
                entity.setFireTicks(fireTicks);
                entity.setVelocity(toVelocity);
                break;
        }
    }

    private void rollbackTraveler() {
        if (createdEntity)
            entity.remove();
    }

    private void commitTraveler() {
        // TODO: something?
    }

    public String getTraveler() {
        if (entityType == EntityType.PLAYER)
            return String.format("player '%s'", playerName);
        if (playerName == null)
            return entityType.toString();
        return String.format("player '%s' as a passenger on a %s", playerName, entityType);
    }

    public String getDestination() {
        if (toGateName != null)
            return "'" + toGateName + "'";
        String dst;
        if (toServer != null)
            dst = String.format("server '%s'", toServer.getName());
        else if (toWorld != null)
            dst = String.format("world '%s'", toWorld.getName());
        else
            dst = "unknown";
        if (toLocation != null)
            dst += String.format(" @ %s,%s,%s", toLocation.getBlockX(), toLocation.getBlockY(), toLocation.getBlockZ());
        return dst;
    }

    private enum EntityType {
        PLAYER,
        MINECART,
        POWERED_MINECART,
        STORAGE_MINECART,
        BOAT
    }

}
