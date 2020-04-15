package com.github.zedd7.zhorse.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;

import com.eqn.jobisingh.addon.StatsHandler;
import com.github.zedd7.zhorse.ZHorse;
import com.github.zedd7.zhorse.enums.GenderSubCommandEnum;
import com.github.zedd7.zhorse.enums.KeyWordEnum;
import com.github.zedd7.zhorse.enums.LocaleEnum;
import com.github.zedd7.zhorse.utils.MessageConfig;
/**
Author: jobisingh
**/

public class CommandGender extends AbstractCommand {
	
	private String fullCommand;
	private String subCommand;

	public CommandGender(ZHorse zh, CommandSender s, String[] a) {
		super(zh, s, a);
		

		
		if (isPlayer() && zh.getEM().canAffordCommand(p, command) && parseArguments() && hasPermission() && isCooldownElapsed() && isWorldEnabled()
				&& parseArgument(ArgumentEnum.HORSE_NAME, ArgumentEnum.PLAYER_NAME)) {
			if(s.isOp()) {
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
			} else s.sendMessage(ChatColor.GREEN + "[ZHorse] " + ChatColor.RED +"You must be OP to use this command.");
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
				if (subCommand.equalsIgnoreCase("Stallion")) {
					
					fullCommand = command + KeyWordEnum.DOT.getValue() + "Stallion";
					StatsHandler.setHorseGender(horse.getUniqueId(), "Stallion");
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.HORSE_GENDERED) {{ setHorseName(horseName); }});
				}
				else if (subCommand.equalsIgnoreCase("Mare")) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + "Mare";
					StatsHandler.setHorseGender(horse.getUniqueId(), "Mare");
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.HORSE_GENDERED) {{ setHorseName(horseName); }});
				}
				else if (subCommand.equalsIgnoreCase("Gelding")) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + "Gelding";
					StatsHandler.setHorseGender(horse.getUniqueId(), "Gelding");
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.HORSE_GENDERED) {{ setHorseName(horseName); }});
				}
				else {
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.UNKNOWN_SUB_COMMAND) {{ setValue(subCommand); setValue(command); }});
					sendSubCommandDescriptionList(GenderSubCommandEnum.class);
				}
			}
			else {
				sendSubCommandDescriptionList(GenderSubCommandEnum.class);
			}
			


			zh.getCmdM().updateCommandHistory(s, command);
			zh.getEM().payCommand(p, command);
		}
	}

}