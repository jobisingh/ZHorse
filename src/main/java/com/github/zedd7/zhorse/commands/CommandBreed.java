package com.github.zedd7.zhorse.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Player;

import com.github.zedd7.zhorse.ZHorse;
import com.github.zedd7.zhorse.commands.AbstractCommand;
import com.github.zedd7.zhorse.enums.FriendSubCommandEnum;
import com.github.zedd7.zhorse.enums.GenderSubCommandEnum;
import com.github.zedd7.zhorse.enums.KeyWordEnum;
import com.github.zedd7.zhorse.enums.LocaleEnum;
import com.github.zedd7.zhorse.jobisingh.addon.StatsHandler;
import com.github.zedd7.zhorse.utils.MessageConfig;
/**
Author: jobisingh
**/

public class CommandBreed extends AbstractCommand {
	
	private String fullCommand;
	private String subCommand;

	public CommandBreed(ZHorse zh, CommandSender s, String[] a) {
		super(zh, s, a);
		

		
		if (isPlayer() && zh.getEM().canAffordCommand(p, command) && parseArguments() && hasPermission() && isCooldownElapsed() && isWorldEnabled()
				&& parseArgument(ArgumentEnum.HORSE_NAME, ArgumentEnum.PLAYER_NAME)) {

				if (!targetMode) {
					boolean ownsHorse = ownsHorse(targetUUID, true);
					if (isOnHorse(ownsHorse)) {
						horse = (AbstractHorse) p.getVehicle();
						if (isRegistered(horse)) {
							execute();
						}
					}
					else if (ownsHorse) {
						
						horseID = zh.getDM().getPlayerFavoriteHorseID(p.getUniqueId(), true, null).toString();
						execute(p.getUniqueId(), horseID);
					}
				}
				else {
					sendCommandUsage();
				}
			
		}
	}

	private void execute(UUID ownerUUID, String horseID) {
		if (isRegistered(ownerUUID, horseID)) {
			horse = zh.getHM().getHorse(ownerUUID, Integer.parseInt(horseID));
			if (isHorseLoaded(true)) {
				execute();
			}
		}
	}

	private void execute() {
		if (isOwner(true)) {

			if (!args.isEmpty()) {
				subCommand = args.get(0);
				
				//args.remove(0); // Remove sub-command to allow parsing of playerName
				if (StatsHandler.isBreed(subCommand)) {
					
					fullCommand = command + KeyWordEnum.DOT.getValue() + subCommand;
					StatsHandler.setHorseBreed(horse.getUniqueId(), subCommand);
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.HORSE_BREEDED) {{ setHorseName(horseName); }});
				}
				else {
					Player player = (Player) s;
					s.sendMessage(ChatColor.GREEN + "[ZHorse] " + ChatColor.YELLOW + subCommand + ChatColor.GOLD + " is not a designated breed. Check the config for a list of breeds.");
				}
			}
			else {
				Player player = (Player) s;
				s.sendMessage(ChatColor.GREEN + "[ZHorse]" + ChatColor.YELLOW + " Try /zh breed (Breed Name)");
			}
			


			zh.getCmdM().updateCommandHistory(s, command);
			zh.getEM().payCommand(p, command);
		}
	}

}