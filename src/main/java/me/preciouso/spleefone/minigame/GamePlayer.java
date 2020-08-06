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


import me.preciouso.spleefone.Main;
import me.preciouso.spleefone.utils.GameState;
import me.preciouso.spleefone.utils.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

// TODO Comment
public class GamePlayer {
    Player player;
    PlayerState state;

    public GamePlayer(Player player) {
        this(player, PlayerState.QUEUED);
    }

    public GamePlayer(Player player, PlayerState state) {
        this.player = player;
        this.state = state;

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
    }

    // TODO .getMinecraftPlayer()
    public Player getPlayer() {
        return this.player;
    }

    // TODD .playerState ?
    public PlayerState getPlayerState() {
        return state;
    }

    private void setPlayerState(PlayerState state) {
        this.state = state;
    } // TODO refresh playerState, gives effects etc

    public boolean updatePlayerState(PlayerState newState) {
        if (newState == this.state) {
            Bukkit.getLogger().warning("Tried to set same PlayerState");
            return false;
        }
        boolean change = false;

        switch (newState) {
            case OFFLINE:
                change = takeOffline();
                break;
            case PLAYING:
                change = addToGame();
                break;
            case SPECTATING:
                change = turnSpectator();
                break;
            case QUEUED:
                Bukkit.getLogger().warning("This should never fire.");
            default:
                Bukkit.getLogger().warning("PlayerState not recognized");
                break;
        }

        return change;
    }

    private boolean takeOffline() {
        state = PlayerState.OFFLINE;
        Bukkit.broadcastMessage(player.getDisplayName() + " has left the game!");
        return true;
    }

    private boolean addToGame() {
        boolean status = false;

        switch (state) {
            case QUEUED:
                state = PlayerState.PLAYING;
                status = true;
                break;
            case OFFLINE:
                Bukkit.getLogger().warning("Tried to turn an offline user into a playing user.");
                break;
            case SPECTATING:
                state = PlayerState.PLAYING;
                status = true;
                Bukkit.getLogger().warning("Tried to turn a spectating player into an in game player.");
                break;
            default:
                Bukkit.getLogger().warning("PlayerState not recognized");
                break;
        }
        if (status) {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
        }
        return status;

    }

    private boolean turnSpectator() {
        boolean status = false;

        switch (state) {
            case PLAYING:
                GameState gameState = Main.game.getGameState();
                if (gameState != GameState.ENDING) {
                    Bukkit.broadcastMessage(player.getDisplayName() + " has died!");
                }
            case OFFLINE:
                // TODO disappear from player list
                // TODO Prevent Offline players from sending packets to other player
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*60*120, 3));
                state = PlayerState.SPECTATING;
                status = true;
                break;
            case QUEUED:
                Bukkit.getLogger().warning("Tried to turn a spectating player into an in game player.");
                break;
            default:
                Bukkit.getLogger().warning("PlayerState not recognized");
                break;
        }

        return status;
    }

    public boolean equals(@NotNull GamePlayer gamePlayer) {
        return this.player.getUniqueId() == gamePlayer.player.getUniqueId();
    }
}