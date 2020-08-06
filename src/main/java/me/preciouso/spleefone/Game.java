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

import me.preciouso.spleefone.events.GameListener;
import me.preciouso.spleefone.minigame.GamePlayer;
import me.preciouso.spleefone.utils.GameState;
import me.preciouso.spleefone.utils.ListStringBuilder;
import me.preciouso.spleefone.utils.PlayerState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class Game {
    // Store GamePlayers by UUID
    private HashMap<UUID, GamePlayer> gamePlayers;

    // State of the game, determines what happens next
    private GameState gameState;

    // Just to see how long a game is in queue, for analytics
    private long queueBegin;
    private long queueEnd = 0L;

    // To see how long a game lasts, for analytics
    private long gameBegin;
    private long gameEnd = 0L;

    // Amount of players playing
    private int playerCount = 0;

    // Game options, loaded from a config.yml

    // Also TODO Find other TODO like keywords, like NOTE
    // Player queue location, and game start location
    Location worldSpawn; // TODO rename var/config name to queueLocation/queueSpawn
    Location gameSpawn;

    // Lowest y-level player can drop before being 'voided'
    int voidLevel;

    // Max game length in seconds
    // Should be named maxGameLength. TODO Change var/config name to maxGameLength
    int gameLength;

    // Min amount of players in queue to start game. TODO Change var/config name to minQueuedPlayers
    int queueMin;

    // Max players in queue
    int queueMax;

    // Amount of time to wait for game start after queueMin is hit
    int startDelaySeconds;

    // Amount of time after game end to stop server. TODO Change var/config name to ???
    int shutdownTimer;

    // Amount of time in MS to remove block after player has stepped on it
    long removeBlockAfterMS;

    public Game() {
        // No args, creates blank game
        this(new HashMap<UUID, GamePlayer>());
    }

    public Game(HashMap<UUID, GamePlayer> players) {
        // Loads players into gamePlayers, sets game as queued, and starts timer
        gamePlayers = players;
        gameState = GameState.ENQUEUE;
        queueBegin = System.currentTimeMillis();

        // TODO call loadCfgOpt if bool loaded is not true
    }

    public int getPlayerCount() {
        // Get number of players
        return playerCount;
    }

    public void increasePlayerCount() {
        playerCount++;
    }

    public void decreasePlayerCount() {
        playerCount--;
    }

    public void loadConfigOpts(FileConfiguration config) {
        // Load game options from config

        int spawnX = config.getInt("queue-location.x");
        int spawnY = config.getInt("queue-location.y");
        int spawnZ = config.getInt("queue-location.z");
        this.worldSpawn = new Location(Bukkit.getWorlds().get(0), spawnX, spawnY, spawnZ);

        int gameX = config.getInt("game-location.x");
        int gameY = config.getInt("game-location.y");
        int gameZ = config.getInt("game-location.z");
        this.gameSpawn = new Location(Bukkit.getWorlds().get(0), gameX, gameY, gameZ);

        this.voidLevel = config.getInt("void-level");
        this.gameLength = config.getInt("game-length-seconds");
        this.queueMin = config.getInt("queue-minimum");
        this.queueMax = config.getInt("queue-maximum");
        this.startDelaySeconds = config.getInt("start-delay");

        this.shutdownTimer = config.getInt("shutdown-on-gameover-seconds");  // !

        this.removeBlockAfterMS = config.getLong("remove-block-after-ms");
    }

    public int getVoidLevel() {
        return voidLevel;
    }

    public long getRemoveBlockAfterMS() {
        return removeBlockAfterMS;
    }

    public Location getWorldSpawn() {
        return worldSpawn;
    }

    public Location getGameSpawn() {
        return gameSpawn;
    }

    public void startGame() {
        // TODO Teleport to place
        if (gameState.getCode() > GameState.STARTING.getCode()) {
            // Game is either STARTED or ENDING
            return;
        }

        // Analytics
        queueEnd = gameBegin = System.currentTimeMillis();
        Bukkit.broadcastMessage("Game starting!");

        // Teleport each player to game location, play explosion noise
        for (GamePlayer player: gamePlayers.values()) {
            player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.5F);
            player.getPlayer().teleport(gameSpawn);
        }

        // Give players a preparation countdown, then begin midGame()
        new CountDownDisplay(5, Main.getProvidingPlugin(Main.class), this::midGame) {
            @Override
            public void eachCount(int current) {
                for (GamePlayer pl: gamePlayers.values()) {
                    pl.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new ComponentBuilder("Starting in " + String.valueOf(current)).color(ChatColor.WHITE).create());
                }
            }
        }
        .start();
    }

    public void midGame() {
        // Register game events
        Bukkit.getServer().getPluginManager().registerEvents(new GameListener(), Main.getProvidingPlugin(Main.class));

        // Update gameState. Should I put this in a function?
        gameState = GameState.STARTED;

        // Update each player's state to playing
        for (GamePlayer player: Main.game.gamePlayers.values()) {
            player.updatePlayerState(PlayerState.PLAYING);
        }


        // Start a countdown until game ends.
        new CountDownDisplay(gameLength, Main.getProvidingPlugin(Main.class), this::stopGame) {
            @Override
            public void eachCount(int current) {
                for (GamePlayer pl: gamePlayers.values()) {
                    // When timer reaches certain number, remind players that game ends soon, if game isnt already ended
                    if (current <= 10 && gameState == GameState.STARTED) { // TODO Config countdown sound, time, message, pitch, etc
                        pl.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                new ComponentBuilder("Game ending in " + String.valueOf(current)).color(ChatColor.WHITE).create());
                        pl.getPlayer().getWorld().playSound(pl.getPlayer().getLocation(), Sound.BLOCK_LAVA_POP, 3.0F, 0.5F);
                    }
                }
            }
        }
        .start();
    }

    public void stopGame() {
        if (gameState == GameState.ENDING) {
            // Game has already stopped
            return; // TODO err
        }

        // Analytics
        gameEnd = System.currentTimeMillis();

        // Change GameState to ending
        gameState = GameState.ENDING;

        Bukkit.broadcastMessage("Game over!");

        // Start getting list of winners
        ListStringBuilder winners = new ListStringBuilder();
        for (GamePlayer player: Main.game.gamePlayers.values()) {
            if (player.getPlayerState() == PlayerState.PLAYING) {
                winners.append(player.getPlayer().getName());
            }

            // Turn players to spectators, and play sound
            player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3.0F, 0.5F);
            player.updatePlayerState(PlayerState.SPECTATING);
        }

        if (winners.toString().equals("")) {
            winners.append("No one");
        }

        Bukkit.broadcastMessage(winners.toString() + " won the game!");

        if (shutdownTimer >=0) {
            // Start shutdown timer if exists
            Bukkit.broadcastMessage("Server closing in " + shutdownTimer + " seconds...");
            Bukkit.getScheduler().runTaskLater(Main.getProvidingPlugin(Main.class), Bukkit::shutdown, 20*shutdownTimer);
        } else {
            // ???
            Main.getProvidingPlugin(Main.class).reloadConfig();
        }

    }

    public boolean addPlayer(@NotNull Player player) {
        UUID playerID = player.getUniqueId();

        if (gameState.getCode() <= 2 && playerCount <= queueMax) {
            // If Game is queued or about to start. OR if player joined before and is reconnecting
            if (gamePlayers.containsKey(playerID)) {
                // If player is reconnecting
                GamePlayer gamePlayer = gamePlayers.get(playerID);
                if (gamePlayer.getPlayerState() == PlayerState.OFFLINE) {
                    // If Player was marked offline, no wahala
                    // gamePlayer.updatePlayerState(PlayerState.PLAYING);
                    player.teleport(worldSpawn);

                    gamePlayers.put(playerID, new GamePlayer(player, PlayerState.PLAYING));
                    increasePlayerCount();
                    return true;
                }
                return false;
            }

            // Adding fresh gamePlayer, and to queue location
            GamePlayer gamePlayer = new GamePlayer(player);
            gamePlayers.put(playerID, gamePlayer);
            player.teleport(worldSpawn);
            increasePlayerCount();

        } else { // TODO force join perms
            player.teleport(gameSpawn);

            gamePlayers.put(playerID, new GamePlayer(player, PlayerState.OFFLINE));
            gamePlayers.get(playerID).updatePlayerState(PlayerState.SPECTATING);
        }
        return true;
    }

    public void tryStart() {
        if (gameState == GameState.ENQUEUE) {
            // If game is queued
            if (playerCount >= queueMin) {
                // and more players than queue min
                Bukkit.broadcastMessage("Game starting in " + startDelaySeconds + " seconds!");

                // put game in prelim mode
                gameState = GameState.STARTING;

                // Start countdown
                new CountDownDisplay(startDelaySeconds, Main.getProvidingPlugin(Main.class), this::startGame) {
                    @Override
                    public void eachCount(int countDown) {
                        if (playerCount < queueMin) {
                            Bukkit.broadcastMessage("Not enough players to start!");
                            gameState = GameState.ENQUEUE;
                            task.cancel();
                        }

                        // Show each player countdown until game start
                        for (GamePlayer pl: gamePlayers.values()) {
                            if (pl.getPlayerState() != PlayerState.OFFLINE) {
                                // Ignore offline players
                                pl.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                        new ComponentBuilder(String.valueOf(countDown)).color(ChatColor.WHITE).create());
                                if (countDown <= 10) {
                                    // Play sounds when countdown is close
                                    pl.getPlayer().getWorld().playSound(pl.getPlayer().getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_OFF, 3.0F, 0.1F);
                                }
                            }
                        }
                    }
                }
                .start();
            }
        }
    }
    public HashMap<UUID, GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public GamePlayer getPlayerByUUID(UUID ID) {
        return gamePlayers.getOrDefault(ID, null);
    }

    // TODO comment ?
    public long getQueueTimeMS() {
        if (queueEnd == 0L) {
            return System.currentTimeMillis() - queueBegin;
        }

        return queueEnd - queueBegin;
    }

    // TODO comment?
    public long getGameTimeMS() {
        if (gameState == GameState.ENQUEUE) {
            return 0L;
        } else if (gameState == GameState.STARTED) {
            return System.currentTimeMillis() - gameBegin;
        }

        return queueEnd - queueBegin;
    }

    public GameState getGameState() {
        return gameState;
    }

}

// TODO Comment
abstract class CountDownDisplay{
    private int time;

    protected BukkitTask task;
    protected final Plugin plugin;
    protected Runnable doAfter;

    CountDownDisplay(int time, Plugin plugin) {
        this.plugin = plugin;
        this.time = time;
    }

    CountDownDisplay(int time, Plugin plugin, Runnable after) {
        this.plugin = plugin;
        this.time = time;
        this.doAfter = after;
    }

    public abstract void eachCount(int countDown);

    public final void start() {
        task = new BukkitRunnable() {

            @Override
            public void run() {
                eachCount(time);
                if (time-- <= 0) {
                    cancel();
                    doAfter.run();
                }
            }

        }.runTaskTimer(plugin, 0L, 20L);


    }

}
