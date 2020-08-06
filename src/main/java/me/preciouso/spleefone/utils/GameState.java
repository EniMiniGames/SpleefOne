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

package me.preciouso.spleefone.utils;

public enum GameState {
    // State of the game
    // TODO Make comments IDE friendly
    ENQUEUE(1), // Game is queued
    STARTING(2), // Queue is over, game isn't started yet
    STARTED(3),  // Game processes have started
    ENDING(4);  // Game processes have ended, players are now in limbo

    // Game code for ordering purposes
    private final int code;

    GameState(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }
}
