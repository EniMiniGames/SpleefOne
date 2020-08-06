/*
   SpleefOne - A spleef plugin for Minecraft servers via the Spigot API
   Copyright (C) 2020 Precious Oyetunji

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package me.preciouso.spleefone.commands;

import me.preciouso.spleefone.Main;
import me.preciouso.spleefone.utils.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartGame implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (Main.game.getGameState() == GameState.ENQUEUE && sender instanceof Player) { // Get G
            Main.game.startGame();
            return true;
        }

        return false;
    }
}
