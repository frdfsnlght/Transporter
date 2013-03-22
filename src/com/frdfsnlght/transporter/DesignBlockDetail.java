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

import com.frdfsnlght.transporter.api.SpawnDirection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class DesignBlockDetail {

    private BuildableBlock buildBlock = null;
    private BuildableBlock openBlock = null;
    private boolean isScreen = false;
    private boolean isPortal = false;
    private boolean isTrigger = false;
    private boolean isSwitch = false;
    private boolean isInsert = false;
    private SpawnDirection spawn = null;
    private LightningMode sendLightningMode = LightningMode.NONE;
    private LightningMode receiveLightningMode = LightningMode.NONE;
    private RedstoneMode triggerOpenMode = RedstoneMode.HIGH;
    private RedstoneMode triggerCloseMode = RedstoneMode.LOW;
    private RedstoneMode switchMode = RedstoneMode.HIGH;

    public DesignBlockDetail(DesignBlockDetail src, BlockFace direction) {
        if (src.buildBlock != null)
            buildBlock = new BuildableBlock(src.buildBlock, direction);
        if (src.openBlock != null)
            openBlock = new BuildableBlock(src.openBlock, direction);
        isScreen = src.isScreen;
        isPortal = src.isPortal;
        isTrigger = src.isTrigger;
        isSwitch = src.isSwitch;
        isInsert = src.isInsert;
        if (src.spawn != null)
            spawn = src.spawn.rotate(direction);
        sendLightningMode = src.sendLightningMode;
        receiveLightningMode = src.receiveLightningMode;
        triggerOpenMode = src.triggerOpenMode;
        triggerCloseMode = src.triggerCloseMode;
        switchMode = src.switchMode;
    }

    public DesignBlockDetail(String blockType) throws BlockException {
        buildBlock = new BuildableBlock(blockType);
        if (! buildBlock.hasType()) buildBlock = null;
    }

    public DesignBlockDetail(TypeMap map) throws BlockException {
        TypeMap subMap;
        String str;

        subMap = map.getMap("build");
        if (subMap == null) {
            str = map.getString("build");
            if (str != null)
                buildBlock = new BuildableBlock(str);
        } else
            buildBlock = new BuildableBlock(subMap);
        if ((buildBlock != null) && (! buildBlock.hasType())) buildBlock = null;

        subMap = map.getMap("open");
        if (subMap == null) {
            str = map.getString("open");
            if (str != null)
                openBlock = new BuildableBlock(str);
        } else
            openBlock = new BuildableBlock(subMap);
        if ((openBlock != null) && (! openBlock.hasType())) openBlock = null;

        isScreen = map.getBoolean("screen", false);
        isPortal = map.getBoolean("portal", false);
        isTrigger = map.getBoolean("trigger", false);
        isSwitch = map.getBoolean("switch", false);
        isInsert = map.getBoolean("insert", false);

        str = map.getString("spawn");
        if (str != null) {
            try {
                spawn = Utils.valueOf(SpawnDirection.class, str);
            } catch (IllegalArgumentException iae) {
                throw new BlockException(iae.getMessage() + " spawn '%s'", str);
            }
        }

        str = map.getString("sendLightningMode", "NONE");
        try {
            sendLightningMode = Utils.valueOf(LightningMode.class, str);
        } catch (IllegalArgumentException iae) {
            throw new BlockException(iae.getMessage() + " sendLightningMode '%s'", str);
        }

        str = map.getString("receiveLightningMode", "NONE");
        try {
            receiveLightningMode = Utils.valueOf(LightningMode.class, str);
        } catch (IllegalArgumentException iae) {
            throw new BlockException(iae.getMessage() + " receiveLightningMode '%s'", str);
        }

        // backwards compatibility in v7.8, remove someday
        str = map.getString("lightningMode");
        if (str != null) {
            str = str.toUpperCase();
            if (! str.equals("NONE"))
                Utils.warning("Outdated 'lightningMode' block option found! Please update to use new lightning mode options!");
            if (str.equals("SEND"))
                sendLightningMode = LightningMode.NORMAL;
            else if (str.equals("RECEIVE"))
                receiveLightningMode = LightningMode.NORMAL;
            else if (str.equals("BOTH")) {
                sendLightningMode = LightningMode.NORMAL;
                receiveLightningMode = LightningMode.NORMAL;
            }
        }

        str = map.getString("triggerOpenMode", "HIGH");
        try {
            triggerOpenMode = Utils.valueOf(RedstoneMode.class, str);
        } catch (IllegalArgumentException iae) {
            throw new BlockException(iae.getMessage() + " triggerOpenMode '%s'", str);
        }

        str = map.getString("triggerCloseMode", "LOW");
        try {
            triggerCloseMode = Utils.valueOf(RedstoneMode.class, str);
        } catch (IllegalArgumentException iae) {
            throw new BlockException(iae.getMessage() + " triggerCloseMode '%s'", str);
        }

        str = map.getString("switchMode", "HIGH");
        try {
            switchMode = Utils.valueOf(RedstoneMode.class, str);
        } catch (IllegalArgumentException iae) {
            throw new BlockException(iae.getMessage() + " switchMode '%s'", str);
        }

        if (isScreen && ((buildBlock == null) || (! buildBlock.isSign())))
            throw new BlockException("screen blocks must be wall signs or sign posts");

    }

    public Map<String,Object> encode() {
        Map<String,Object> node = new HashMap<String,Object>();
        if (buildBlock != null) node.put("build", buildBlock.encode());
        if (openBlock != null) node.put("open", openBlock.encode());
        if (isScreen) node.put("screen", isScreen);
        if (isPortal) node.put("portal", isPortal);
        if (isTrigger) node.put("trigger", isTrigger);
        if (isSwitch) node.put("switch", isSwitch);
        if (isInsert) node.put("insert", isInsert);
        if (spawn != null) node.put("spawn", spawn.toString());
        if (sendLightningMode != LightningMode.NONE) node.put("sendLightningMode", sendLightningMode.toString());
        if (receiveLightningMode != LightningMode.NONE) node.put("receiveLightningMode", receiveLightningMode.toString());
        if (triggerOpenMode != RedstoneMode.HIGH) node.put("triggerOpenMode", triggerOpenMode.toString());
        if (triggerCloseMode != RedstoneMode.LOW) node.put("triggerCloseMode", triggerCloseMode.toString());
        if (switchMode != RedstoneMode.HIGH) node.put("switchMode", switchMode.toString());
        return node;
    }

    public BuildableBlock getBuildBlock() {
        return buildBlock;
    }

    public BuildableBlock getOpenBlock() {
        return openBlock;
    }

    public boolean isScreen() {
        return isScreen;
    }

    public boolean isPortal() {
        return isPortal;
    }

    public boolean isTrigger() {
        return isTrigger;
    }

    public boolean isSwitch() {
        return isSwitch;
    }

    public boolean isInsert() {
        return isInsert;
    }

    public boolean isSpawn() {
        return spawn != null;
    }

    public SpawnDirection getSpawn() {
        return spawn;
    }

    public RedstoneMode getTriggerOpenMode() {
        return triggerOpenMode;
    }

    public RedstoneMode getTriggerCloseMode() {
        return triggerCloseMode;
    }

    public RedstoneMode getSwitchMode() {
        return switchMode;
    }

    public LightningMode getSendLightningMode() {
        return sendLightningMode;
    }

    public LightningMode getReceiveLightningMode() {
        return receiveLightningMode;
    }

    public boolean isInventory() {
        return (buildBlock != null) && (buildBlock.getType() != Material.AIR.getId());
    }

    public boolean isBuildable() {
        return buildBlock != null;
    }

    public boolean isOpenable() {
        return openBlock != null;
    }

    public boolean isMatchable() {
        return (buildBlock != null) && (buildBlock.getMaterial() != Material.AIR);
    }

    @Override
    public int hashCode() {
        return
                ((buildBlock != null) ? buildBlock.hashCode() : 0) +
                ((openBlock != null) ? openBlock.hashCode() : 0) +
                (isScreen ? 10 : 0) +
                (isPortal ? 100 : 0) +
                (isTrigger ? 1000 : 0) +
                (isSwitch ? 10000 : 0) +
                (isInsert ? 100000 : 0) +
                sendLightningMode.hashCode() +
                receiveLightningMode.hashCode() +
                ((spawn != null) ? spawn.hashCode() : 0) +
                triggerOpenMode.hashCode() +
                triggerCloseMode.hashCode() +
                switchMode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (! (obj instanceof DesignBlockDetail)) return false;
        DesignBlockDetail other = (DesignBlockDetail)obj;

        if ((buildBlock == null) && (other.buildBlock != null)) return false;
        if ((buildBlock != null) && (other.buildBlock == null)) return false;
        if ((buildBlock != null) && (other.buildBlock != null) &&
            (! buildBlock.equals(other.buildBlock))) return false;

        if ((openBlock == null) && (other.openBlock != null)) return false;
        if ((openBlock != null) && (other.openBlock == null)) return false;
        if ((openBlock != null) && (other.openBlock != null) &&
            (! openBlock.equals(other.openBlock))) return false;

        if (isScreen != other.isScreen) return false;
        if (isPortal != other.isPortal) return false;
        if (isTrigger != other.isTrigger) return false;
        if (isSwitch != other.isSwitch) return false;
        if (isInsert != other.isInsert) return false;
        if (spawn != other.spawn) return false;
        if (sendLightningMode != other.sendLightningMode) return false;
        if (receiveLightningMode != other.receiveLightningMode) return false;
        if (triggerOpenMode != other.triggerOpenMode) return false;
        if (triggerCloseMode != other.triggerCloseMode) return false;
        if (switchMode != other.switchMode) return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Detail[");
        buf.append("scr=").append(isScreen).append(",");
        buf.append("prt=").append(isPortal).append(",");
        buf.append("trg=").append(isTrigger).append(",");
        buf.append("swt=").append(isSwitch).append(",");
        buf.append("ins=").append(isInsert).append(",");
        buf.append("spw=").append(spawn).append(",");
        buf.append("slng=").append(sendLightningMode).append(",");
        buf.append("rlng=").append(receiveLightningMode).append(",");
        buf.append("trgOpnMod=").append(triggerOpenMode).append(",");
        buf.append("trgClsMod=").append(triggerOpenMode).append(",");
        buf.append("swtMod=").append(switchMode).append(",");
        buf.append(buildBlock).append(",");
        buf.append(openBlock);
        buf.append("]");
        return buf.toString();
    }
}
