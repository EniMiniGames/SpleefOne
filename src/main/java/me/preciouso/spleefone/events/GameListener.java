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

import me.preciouso.spleefone.Game;
import me.preciouso.spleefone.Main;
import me.preciouso.spleefone.minigame.GamePlayer;
import me.preciouso.spleefone.minigame.SpleefBlockStore;
import me.preciouso.spleefone.utils.PlayerState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;

public class GameListener implements Listener {
    SpleefBlockStore blockStore = new SpleefBlockStore(Main.getProvidingPlugin(Main.class));
    Game game = Main.game;

    // https://bukkit.org/threads/get-blocks-under-player-feet.168727/
    private ArrayList<Block> getBlocksBelow(Player player) {
        ArrayList<Block> blocksBelow = new ArrayList<Block>();
        BoundingBox boundingBox = player.getBoundingBox();
        World world = player.getWorld();
        double yBelow = player.getLocation().getY() - 0.0001;
        Block northEast = new Location(world, boundingBox.getMaxX(), yBelow, boundingBox.getMaxZ()).getBlock();
        Block northWest = new Location(world, boundingBox.getMinX(), yBelow, boundingBox.getMaxZ()).getBlock();
        Block southEast = new Location(world, boundingBox.getMaxX(), yBelow, boundingBox.getMinZ()).getBlock();
        Block southWest = new Location(world, boundingBox.getMinX(), yBelow, boundingBox.getMinZ()).getBlock();
        Block[] blocks = {northEast, northWest, southEast, southWest};
        for (Block block : blocks) {
            if (!blocksBelow.isEmpty()) {
                boolean duplicateExists = false;
                for (Block value : blocksBelow) {
                    if (value.equals(block)) {
                        duplicateExists = true;
                        break; // intellij's idea
                    }
                }
                if (!duplicateExists) {
                    blocksBelow.add(block);
                }
            } else {
                blocksBelow.add(block);
            }
        }
        return blocksBelow;
    }

    @EventHandler
    public void onPlayerStep(PlayerMoveEvent event) {
        if (Main.game.getPlayerCount() <= 1 || game.getGameTimeMS() >= 30*1000) { // or clause only here bc I test by myself
            // Stop game if there's a winner
            Main.game.stopGame();
        }

        // Get player, and get GamePlayer attributes
        Player player = event.getPlayer();
        GamePlayer gamePlayer = Main.game.getPlayerByUUID(player.getUniqueId());

        if (gamePlayer != null) {
            if (gamePlayer.getPlayerState() == PlayerState.PLAYING) {
                // If Player is playing, get block player is stepping on and remove it.
                ArrayList<Block> steppedOn = getBlocksBelow(player);
                blockStore.removeBlocksAfter(steppedOn, game.getRemoveBlockAfterMS());
            } else {
                System.out.print(gamePlayer.getPlayerState()); // TODO err
            }
        }
    }

    @EventHandler
    public void onPlayerFall(PlayerMoveEvent event) {
        // Get GamePlayer
        Player player = event.getPlayer();
        GamePlayer gamePlayer = Main.game.getPlayerByUUID(player.getUniqueId());

        if (gamePlayer != null) {
            if (gamePlayer.getPlayerState() == PlayerState.PLAYING) {
                // If player y is less than void level, set player to spectator and tp to game location
                if (player.getLocation().getY() < game.getVoidLevel()) {
                    gamePlayer.updatePlayerState(PlayerState.SPECTATING);
                    player.teleport(game.getGameSpawn());
                    Main.game.decreasePlayerCount();  // TODO this func, setPlayerState, addPlayer new void removePlayer need to consolidated
                }
            }
        }
    }


}
