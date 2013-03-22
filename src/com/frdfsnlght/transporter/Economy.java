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

import com.nijikokun.register.payment.Methods;
import com.nijikokun.register.payment.Method;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Economy {

    private static net.milkbowl.vault.economy.Economy vaultPlugin = null;
    private static Method registerPlugin = null;
//    private static BOSEconomy boseconomyPlugin = null;

    public static boolean isAvailable() {
        return vaultAvailable() ||
               registerAvailable();

//        return  boseconomyAvailable();
    }

    public static boolean vaultAvailable() {
        if (! Config.getUseVaultEconomy()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("Vault");
        if ((p == null) || (! p.isEnabled())) return false;
        if (vaultPlugin != null) return true;
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                Global.plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) return false;
        vaultPlugin = rsp.getProvider();
        if (vaultPlugin == null) return false;
        Utils.info("Initialized Vault for Economy");
        return true;
    }

    public static boolean registerAvailable() {
        if (! Config.getUseRegisterEconomy()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("Register");
        if ((p == null) || (! p.isEnabled())) return false;
        if (registerPlugin != null) return true;
        registerPlugin = Methods.getMethod();
        Utils.info("Initialized Register for Economy");
        return true;
    }

    /*
    public static boolean boseconomyAvailable() {
        if (! Config.getUseBOSEconomy()) return false;
        Plugin p = Global.plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
        if ((p == null) || (! p.isEnabled())) return false;
        if (boseconomyPlugin != null) return true;
        boseconomyPlugin = (BOSEconomy)p;
        Utils.info("Initialized BOSEconomy for Economy");
        return true;
    }
*/

    public static String format(double funds) {
        if (vaultAvailable())
            return vaultPlugin.format(funds);
        if (registerAvailable())
            return registerPlugin.format(funds);
//        if (boseconomyAvailable())
//            return boseconomyPlugin.getMoneyFormatted(funds);

        // default
        return String.format("$%1.2f", funds);
    }

    public static boolean requireFunds(Player player, double amount) throws EconomyException {
        if (player == null) return false;
        return requireFunds(player.getName(), amount);
    }

    public static boolean requireFunds(String accountName, double amount) throws EconomyException {
        if (accountName == null) return false;
        if (amount <= 0) return false;
        if (vaultAvailable()) {
            double balance = vaultPlugin.getBalance(accountName);
            if (balance < amount)
                throw new EconomyException("insufficient funds");
            return true;
        }
        if (registerAvailable()) {
            Method.MethodAccount account = registerPlugin.getAccount(accountName);
            if (! account.hasEnough(amount))
                throw new EconomyException("insufficient funds");
            return true;
        }
        /*
        if (boseconomyAvailable()) {
            double balance = boseconomyPlugin.getPlayerMoneyDouble(accountName);
            if (balance < amount)
                throw new EconomyException("insufficient funds");
            return true;
        }
        */

        // default
        return false;
    }

    public static boolean deductFunds(Player player, double amount) throws EconomyException {
        if (player == null) return false;
        return deductFunds(player.getName(), amount);
    }

    public static boolean deductFunds(String accountName, double amount) throws EconomyException {
        if (accountName == null) return false;
        if (amount <= 0) return false;

        if (vaultAvailable()) {
            double balance = vaultPlugin.getBalance(accountName);
            if (balance < amount)
                throw new EconomyException("insufficient funds");
            EconomyResponse r = vaultPlugin.withdrawPlayer(accountName, amount);
            if (r.transactionSuccess()) return true;
            throw new EconomyException("economy error: %s", r.errorMessage);
        }
        if (registerAvailable()) {
            Method.MethodAccount account = registerPlugin.getAccount(accountName);
            if (! account.hasEnough(amount))
                throw new EconomyException("insufficient funds");
            if (account.subtract(amount)) return true;
            throw new EconomyException("economy error");
        }
        /*
        if (boseconomyAvailable()) {
            double balance = boseconomyPlugin.getPlayerMoneyDouble(accountName);
            if (balance < amount)
                throw new EconomyException("insufficient funds");
            if (boseconomyPlugin.addPlayerMoney(accountName, -amount, false)) return true;
            throw new EconomyException("economy error");
        }
        */

        // default
        return false;
    }

}
