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

package me.preciouso.spleefone.events;

import me.preciouso.spleefone.Main;
import me.preciouso.spleefone.minigame.GamePlayer;
import me.preciouso.spleefone.utils.GameState;
import me.preciouso.spleefone.utils.PlayerState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanillaListener implements Listener {
    @EventHandler
    public void onPLayerJoin(PlayerJoinEvent event) {
        GameState gameState = Main.game.getGameState();
        boolean send = Main.game.addPlayer(event.getPlayer());
        if (!send) {
            event.getPlayer().kickPlayer("A problem occured while trying to add to game!");
        } else {
            Main.game.tryStart();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        GamePlayer gamePlayer = Main.game.getGamePlayers().get(player.getUniqueId());  // TODO This goes on Bungee, Game can be static and global

        // Set player to offline.
        gamePlayer.updatePlayerState(PlayerState.OFFLINE);
        Main.game.decreasePlayerCount(); // See other TODO
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER){
            e.setCancelled(true);
        }
    }
}
