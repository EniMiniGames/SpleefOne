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

package me.preciouso.spleefone;

import me.preciouso.spleefone.commands.EndGame;
import me.preciouso.spleefone.commands.StartGame;
import me.preciouso.spleefone.events.VanillaListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    // New Game instance for all classes to draw from
    public static Game game = new Game();

    @Override
    public void onEnable() {
        // Save config if not exists, and load options into game
        final File check = new File(this.getDataFolder(), "config.yml");
        boolean firstTimeStarting = false;
        if (!check.exists()) {
            firstTimeStarting = true;
            this.saveDefaultConfig();
        }

        game.loadConfigOpts(this.getConfig());

        // Register Listener
        Bukkit.getServer().getPluginManager().registerEvents(new VanillaListener(), this);

        // Register Commands
        this.getCommand("spleef").setExecutor(new StartGame());
        this.getCommand("nospleef").setExecutor(new EndGame());

        // Begin
        Bukkit.getLogger().info("Started SpleefOne");
    }

    @Override
    public void onDisable() {
        // End
        Bukkit.getLogger().info("Stopping SpleefOne");
    }

}