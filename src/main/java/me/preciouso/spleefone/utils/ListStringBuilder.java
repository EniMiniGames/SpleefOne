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

import java.util.ArrayList;

public class ListStringBuilder {
    // Array of strings
    private final ArrayList<String> stringArray;

    // Empty array
    public ListStringBuilder() {
        stringArray = new ArrayList<String>();
    }

    // Populate array with one string
    public ListStringBuilder(String one) {
        this();
        stringArray.add(one);
    }

    // Copy array
    public ListStringBuilder(ArrayList<String> strings) {
        stringArray = strings;
    }

    // Append to arraylist and return self
    public ListStringBuilder append(String str) {
        this.stringArray.add(str);
        return this;
    }

    @Override
    public String toString() {
        switch (stringArray.size()) {
            case 0:
                return "";
            case 1:
                // Returns singular string
                return stringArray.get(0);
            case 2:
                // Returns compound of two strings
                return stringArray.get(0) + " and " + stringArray.get(1);
            default:
                // Returns compound sentence with commas
                StringBuilder sb = new StringBuilder();
                for (String str: stringArray.subList(0, stringArray.size() - 1)) {
                    sb.append(str).append(", ");
                }
                sb.append("and ").append(stringArray.get(stringArray.size() -1));
                return sb.toString();
        }
    }
}
