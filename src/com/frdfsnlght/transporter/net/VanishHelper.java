package com.frdfsnlght.transporter.net;

import org.kitteh.vanish.staticaccess.VanishNoPacket;
import org.kitteh.vanish.staticaccess.VanishNotLoadedException;

public class VanishHelper {

    public static boolean isVanished(String playerName) {
        try {
            return VanishNoPacket.isVanished(playerName);
        } catch (VanishNotLoadedException e) {
            return false;
        }
    }
}
