/*
 * VitalSpawn is a Spigot Plugin that lets you set a spawn point.
 * Copyright © 2022 Leopold Meinel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://github.com/TamrielNetwork/VitalSpawn/blob/main/LICENSE
 */

package com.tamrielnetwork.vitalspawn.storage;

import com.tamrielnetwork.vitalspawn.storage.mysql.SqlManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class SpawnStorageSql extends SpawnStorage {

	@Override
	public Location getSpawn() {
		try (PreparedStatement selectStatement = SqlManager.getConnection().prepareStatement("SELECT * FROM " + main.getPrefix() + "Spawn")) {
			try (ResultSet rs = selectStatement.executeQuery()) {
				if (rs.getString(1) == null) {
					Bukkit.getLogger().severe("VitalSpawn cannot find world in spawn.yml");
					return null;
				}
				World world = Bukkit.getWorld(Objects.requireNonNull(rs.getString(1)));
				int x = rs.getInt(2);
				int y = rs.getInt(3);
				int z = rs.getInt(4);
				int yaw = rs.getInt(5);
				int pitch = rs.getInt(6);

				return new Location(world, x, y, z, yaw, pitch);
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			return null;
		}
	}

	@Override
	public void saveSpawn(@NotNull CommandSender sender) {
		clear();

		Player senderPlayer = (Player) sender;
		Location location = senderPlayer.getLocation();

		try (PreparedStatement insertStatement = SqlManager.getConnection().prepareStatement("INSERT INTO" + main.getPrefix() + "Spawn (`World`, `X`, `Y`, `Z`, `Yaw`, `Pitch`) VALUES (?, ?, ?, ?, ?, ?)")) {
			insertStatement.setString(1, location.getWorld().getName());
			insertStatement.setInt(2, (int) location.getX());
			insertStatement.setInt(3, (int) location.getY());
			insertStatement.setInt(4, (int) location.getZ());
			insertStatement.setInt(5, (int) location.getYaw());
			insertStatement.setInt(6, (int) location.getPitch());
			insertStatement.executeUpdate();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	@Override
	public void clear() {
		try (PreparedStatement truncateStatement = SqlManager.getConnection().prepareStatement("TRUNCATE TABLE " + main.getPrefix() + " Spawn")) {
			truncateStatement.executeUpdate();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
