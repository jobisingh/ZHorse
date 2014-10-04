package eu.reborn_minecraft.zhorse.commands;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import eu.reborn_minecraft.zhorse.ZHorse;

public class ZHere extends Command {

	public ZHere(ZHorse zh, CommandSender s, String[] a) {
		super(zh, a, s);
		idAllow = true;
		targetAllow = false;
		if (isPlayer()) {
			if (analyseArguments()) {
				if (hasPermission()) {
					if (isWorldEnabled()) {
						if (idMode) {
							if (isRegistered(targetUUID, userID)) {
								horse = zh.getUM().getHorse(targetUUID, userID);
								if (isHorseLoaded()) {
									execute();
								}
							}
						}
						else if (displayConsole) {
							sendCommandUsage();
						}
					}
				}
			}
		}
	}
	
	private void execute() {
		if (isOwner()) {
			if (isOnSameWorld()) {
				if (isNotOnHorse()) {
					if (isHorseEmpty()) {
						if (zh.getEM().isReadyToPay(p, command)) {
							Block block = p.getWorld().getHighestBlockAt(p.getLocation());
							horse.teleport(new Location(p.getWorld(), block.getX(), block.getY(), block.getZ()));
							if (displayConsole) {
								s.sendMessage(String.format(zh.getLM().getCommandAnswer(zh.getLM().horseTeleported), horseName));
							}
							zh.getEM().payCommand(p, command);
						}
					}
				}
			}
		}
	}

}