package com.github.zedd7.zhorse.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.eqn.jobisingh.addon.StatsHandler;
import com.github.zedd7.zhorse.ZHorse;
import com.github.zedd7.zhorse.enums.LocaleEnum;
import com.github.zedd7.zhorse.utils.MessageConfig;
/**
Author: jobisingh
**/

public class CommandCreatebreed extends AbstractCommand {
	
	private String fullCommand;
	private String subCommand;

	public CommandCreatebreed(ZHorse zh, CommandSender s, String[] a) {
		super(zh, s, a);
		

		
		if (isPlayer() && parseArguments() && hasPermission() && isCooldownElapsed() && isWorldEnabled()
				) {
			if(s.isOp()) {
				
				if (!targetMode) {
					execute();
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

			if (!args.isEmpty()) {
				subCommand = args.get(0);
				
				if(s.isOp() == false) {
					s.sendMessage(ChatColor.GREEN + "[ZHorse] " + ChatColor.YELLOW + subCommand + ChatColor.GOLD + " can only be used by players with OP"); 
					return;
				}
				
				
				//args.remove(0); // Remove sub-command to allow parsing of playerName
				if (StatsHandler.isBreed(subCommand) == false) {
					
					StatsHandler.addToHorseBreeds(subCommand);
					
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.HORSE_CREATEDBREED) {{ }});
				}
				else {
					s.sendMessage(ChatColor.GREEN + "[ZHorse] " + ChatColor.YELLOW + subCommand + ChatColor.GOLD + " is already a breed");
				}
			}
			else {
				s.sendMessage(ChatColor.GREEN + "[ZHorse] " + ChatColor.YELLOW + "Try /zh createbreed (Breed Name)");
			}
			


			zh.getCmdM().updateCommandHistory(s, command);
			zh.getEM().payCommand(p, command);
	}

}