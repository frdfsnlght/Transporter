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
import java.util.List;
import org.bukkit.command.Command;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public abstract class TrpCommandProcessor extends CommandProcessor {

    @Override
    public boolean matches(Context ctx, Command cmd, List<String> args) {
        if (! cmd.getName().toLowerCase().equals("trp")) return false;
        if (args.isEmpty()) return false;
        return true;
    }

    protected String getPrefix(Context ctx) {
        return (ctx.isPlayer() ? "/" : "") + "trp ";
    }

}
