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

import com.frdfsnlght.transporter.command.APICommand;
import com.frdfsnlght.transporter.command.CommandProcessor;
import com.frdfsnlght.transporter.command.DebugCommand;
import com.frdfsnlght.transporter.command.DesignCommand;
import com.frdfsnlght.transporter.command.GateCommand;
import com.frdfsnlght.transporter.command.GlobalCommands;
import com.frdfsnlght.transporter.command.HelpCommand;
import com.frdfsnlght.transporter.command.NetworkCommand;
import com.frdfsnlght.transporter.command.PinCommand;
import com.frdfsnlght.transporter.command.RealmCommand;
import com.frdfsnlght.transporter.command.ReloadCommand;
import com.frdfsnlght.transporter.command.SaveCommand;
import com.frdfsnlght.transporter.command.ServerCommand;
import com.frdfsnlght.transporter.command.WorldCommand;
import com.frdfsnlght.transporter.compatibility.Compatibility;
import com.frdfsnlght.transporter.test.TestCommand;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Global {

    public static final int DEFAULT_PLUGIN_PORT = 25555;
    public static final int DEFAULT_MC_PORT = 25565;

    public static Thread mainThread = null;
    public static boolean enabled = false;
    public static Transporter plugin = null;
    public static String pluginName;
    public static String pluginVersion;
    public static boolean started = false;
    public static Compatibility compatibility;
    
    public static final List<CommandProcessor> commands = new ArrayList<CommandProcessor>();

    static {
        commands.add(new HelpCommand());
        commands.add(new ReloadCommand());
        commands.add(new SaveCommand());
        commands.add(new PinCommand());
        commands.add(new GlobalCommands());
        commands.add(new DesignCommand());
        commands.add(new GateCommand());
        commands.add(new ServerCommand());
        commands.add(new NetworkCommand());
        commands.add(new WorldCommand());
        commands.add(new RealmCommand());
        commands.add(new APICommand());
        commands.add(new DebugCommand());

        if (isTesting()) {
            System.out.println("**** Transporter testing mode is enabled! ****");
            commands.add(new TestCommand());
        }
    }

    public static boolean isTesting() {
        return System.getenv("TRANSPORTER_TEST") != null;
    }

}
