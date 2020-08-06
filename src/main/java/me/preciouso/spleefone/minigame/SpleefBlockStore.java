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

package me.preciouso.spleefone.minigame;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


// Block store. Stores blocks to remove later
public class SpleefBlockStore {
    Plugin plugin;

    public SpleefBlockStore(Plugin plugin) {
        // Plugin needed for scheduler
        this.plugin = plugin;
    }

    void removeBlockNow(@NotNull Block block) {
        // Set as air
        block.setType(Material.AIR, false);
    }

    public void removeBlockAfter(@NotNull Block block, long expiryTimeMS) {
        // Converts MS to ticks
        long ticks = expiryTimeMS / 50L;

        // Run timer for block removal
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeBlockNow(block);
        }, ticks);
    }

    public void removeBlocksAfter(@NotNull ArrayList<Block> blocks, long expiryTimeMS) {
        // Converts MS to ticks
        long ticks = expiryTimeMS / 50L;

        // Run timer for block removal
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for(Block bl: blocks) {
                removeBlockNow(bl);
            }
        }, ticks);
    }
}
