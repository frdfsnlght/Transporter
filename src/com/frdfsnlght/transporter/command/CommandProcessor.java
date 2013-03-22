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
package com.frdfsnlght.transporter.command;

import com.frdfsnlght.transporter.Context;
import com.frdfsnlght.transporter.api.TransporterException;
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public abstract class CommandProcessor {

    public abstract boolean matches(Context ctx, Command cmd, List<String> args);
    public abstract void process(Context ctx, Command cmd, List<String> args) throws TransporterException;
    public abstract List<String> getUsage(Context ctx);

    /*
    public boolean isHidden() { return false; }
    public boolean requiresPlayer() { return false; }
    public boolean requiresOp() { return false; }
    public boolean requiresConsole() { return false; }
    public abstract String getUsage(Context ctx);
     *
     */

    protected String rebuildCommandArgs(List<String> args) {
        StringBuilder b = new StringBuilder();
        for (String arg : args) {
            if (arg.contains(" "))
                b.append("\"").append(arg).append("\"");
            else
                b.append(arg);
            b.append(" ");
        }
        return b.toString().trim();
    }

}
