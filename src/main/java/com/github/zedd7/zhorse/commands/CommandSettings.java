package com.github.zedd7.zhorse.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractHorse;

import com.github.zedd7.zhorse.ZHorse;
import com.github.zedd7.zhorse.enums.KeyWordEnum;
import com.github.zedd7.zhorse.enums.LocaleEnum;
import com.github.zedd7.zhorse.enums.SettingsSubCommandEnum;
import com.github.zedd7.zhorse.utils.CallbackListener;
import com.github.zedd7.zhorse.utils.CallbackResponse;
import com.github.zedd7.zhorse.utils.MessageConfig;

import net.md_5.bungee.api.ChatColor;

public class CommandSettings extends AbstractCommand {

	private String fullCommand;
	private String subCommand;

	public CommandSettings(ZHorse zh, CommandSender s, String[] a) {
		super(zh, s, a);
		if (isPlayer() && zh.getEM().canAffordCommand(p, command) && parseArguments() && hasPermission() && isCooldownElapsed() && isWorldEnabled()) {
			if (!idMode) {
				if (isOnHorse(true)) { // select the horse w/ or w/o target
					horse = (AbstractHorse) p.getVehicle();
					if (isOwner(targetUUID, false, true, true)) {
						idMode = true;
						Integer horseIDInt = zh.getDM().getHorseID(horse.getUniqueId(), true, null);
						horseID = horseIDInt != null ? horseIDInt.toString() : null;
					}
				}
				execute();
			}
			else if (isRegistered(targetUUID, horseID)) {
				execute();
			}
		}
	}

	private void execute() {
		if ((!targetMode || samePlayer || hasPermissionAdmin(false))) {
			if (!args.isEmpty()) {
				subCommand = args.get(0);
				if (subCommand.equalsIgnoreCase(SettingsSubCommandEnum.FAVORITE.getName())) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + SettingsSubCommandEnum.LANGUAGE.getName();
					setFavorite();
				}
				else if (subCommand.equalsIgnoreCase(SettingsSubCommandEnum.LANGUAGE.getName())) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + SettingsSubCommandEnum.FAVORITE.getName();
					setLanguage();
				}
				else if (subCommand.equalsIgnoreCase(SettingsSubCommandEnum.STATS.getName())) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + SettingsSubCommandEnum.STATS.getName();
					setStatsDisplay();
				}
				else if (subCommand.equalsIgnoreCase(SettingsSubCommandEnum.SWAP.getName())) {
					fullCommand = command + KeyWordEnum.DOT.getValue() + SettingsSubCommandEnum.SWAP.getName();
					swapIDs();
				}
				else {
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.UNKNOWN_SUB_COMMAND) {{ setValue(subCommand); setValue(command); }});
					sendSubCommandDescriptionList(SettingsSubCommandEnum.class);
				}
			}
			else {
				sendSubCommandDescriptionList(SettingsSubCommandEnum.class);
			}
		}
	}

	private void setFavorite() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (args.size() >= 2) {
				idMode = true;
				horseID = args.get(1);
			}
			if (idMode) {
				if (isRegistered(targetUUID, horseID)) {
					if (!zh.getDM().getPlayerFavoriteHorseID(targetUUID, true, null).toString().equals(horseID)) {
						zh.getDM().updatePlayerFavoriteHorseID(targetUUID, Integer.parseInt(horseID), false, new CallbackListener<Boolean>() {

							@Override
							public void callback(CallbackResponse<Boolean> response) {
								if (response.getResult()) {
									if (samePlayer) {
										zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_SET) {{ setHorseName(horseName); }});
									}
									else {
										zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_SET_OTHER) {{ setHorseName(horseName); setPlayerName(targetName); }});
									}
									zh.getCmdM().updateCommandHistory(s, command);
									zh.getEM().payCommand(p, command);
								}
							}

						});
					}
					else {
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_ALREADY_SET) {{ setHorseName(horseName); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.FAVORITE_ALREADY_SET_OTHER) {{ setHorseName(horseName); setPlayerName(targetName); }});
						}
					}
				}
			}
			else {
				zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.MISSING_HORSE_ID));
				sendCommandUsage(subCommand, true, true);
			}
		}
	}

	private void setLanguage() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (args.size() >= 2) {
				String language = args.get(1).toUpperCase();
				if (zh.getCM().isLanguageAvailable(language)) {
					if (!zh.getDM().getPlayerLanguage(targetUUID, true, null).equals(language)) {
						zh.getDM().updatePlayerLanguage(targetUUID, language, false, new CallbackListener<Boolean>() {

							@Override
							public void callback(CallbackResponse<Boolean> response) {
								if (response.getResult()) {
									if (samePlayer) {
										zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_SET) {{ setLanguage(language); }});
									}
									else {
										zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_SET_OTHER) {{ setLanguage(language); setPlayerName(targetName); }});
									}
									zh.getCmdM().updateCommandHistory(s, command);
									zh.getEM().payCommand(p, command);
								}
							}

						});
					}
					else {
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_ALREADY_USED) {{ setLanguage(language); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.LANGUAGE_ALREADY_USED_OTHER) {{ setLanguage(language); setPlayerName(targetName); }});
						}
					}
				}
				else {
					displayAvailableLanguages(LocaleEnum.UNKNOWN_LANGUAGE, language);
				}
			}
			else {
				displayAvailableLanguages(LocaleEnum.MISSING_LANGUAGE);
				sendCommandUsage(subCommand, true, true);
			}
		}
	}

	private void setStatsDisplay() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (args.size() >= 2) {
				String displayMode = args.get(1);
				boolean validDisplayMode = false;
				Boolean shouldDisplayExactStats = null;
				if (displayMode.equalsIgnoreCase(StatsDisplayModeEnum.EXACT.getName())) {
					validDisplayMode = true;
					shouldDisplayExactStats = true;
				}
				else if (displayMode.equalsIgnoreCase(StatsDisplayModeEnum.ROUNDED.getName())) {
					validDisplayMode = true;
					shouldDisplayExactStats = false;
				}
				if (validDisplayMode) {
					if (shouldDisplayExactStats ^ zh.getDM().isPlayerDisplayingExactStats(targetUUID, true, null)) { // XOR
						zh.getDM().updatePlayerDisplayExactStats(targetUUID, shouldDisplayExactStats, false, new CallbackListener<Boolean>() {

							@Override
							public void callback(CallbackResponse<Boolean> response) {
								if (response.getResult()) {
									if (samePlayer) {
										zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_SET) {{ setValue(displayMode); }});
									}
									else {
										zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_SET_OTHER) {{ setValue(displayMode); setPlayerName(targetName); }});
									}
									zh.getCmdM().updateCommandHistory(s, command);
									zh.getEM().payCommand(p, command);
								}
							}

						});
					}
					else {
						if (samePlayer) {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_ALREADY_USED) {{ setValue(displayMode); }});
						}
						else {
							zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.STATS_DISPLAY_MODE_ALREADY_USED_OTHER) {{ setValue(displayMode); setPlayerName(targetName); }});
						}
					}
				}
				else {
					displayAvailableStatsDisplayMode(LocaleEnum.UNKNOWN_STATS_DISPLAY_MODE);
				}

			}
			else {
				displayAvailableStatsDisplayMode(LocaleEnum.MISSING_STATS_DISPLAY_MODE);
				sendCommandUsage(subCommand, true, true);
			}
		}
	}

	private void swapIDs() {
		if (hasPermission(s, fullCommand , true, false)) {
			if (args.size() >= 3) {
				String horseID1 = args.get(1);
				String horseID2 = args.get(2);
				if (isRegistered(targetUUID, horseID1) && isRegistered(targetUUID, horseID2)) {
					int favoriteHorseID = zh.getDM().getPlayerFavoriteHorseID(targetUUID, true, null);
					if (favoriteHorseID == Integer.parseInt(horseID1)) {
						zh.getDM().updatePlayerFavoriteHorseID(targetUUID, Integer.parseInt(horseID2), false, null);
					}
					else if (favoriteHorseID == Integer.parseInt(horseID2)) {
						zh.getDM().updatePlayerFavoriteHorseID(targetUUID, Integer.parseInt(horseID1), false, null);
					}
					UUID horseUUID1 = zh.getDM().getHorseUUID(targetUUID, Integer.parseInt(horseID1), true, null);
					UUID horseUUID2 = zh.getDM().getHorseUUID(targetUUID, Integer.parseInt(horseID2), true, null);
					zh.getDM().updateHorseID(horseUUID1, Integer.parseInt(horseID2), false, null);
					zh.getDM().updateHorseID(horseUUID2, Integer.parseInt(horseID1), false, null);
					zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.IDS_SWAPPED));
					zh.getCmdM().updateCommandHistory(s, command);
					zh.getEM().payCommand(p, command);
				}
			}
			else {
				zh.getMM().sendMessage(s, new MessageConfig(LocaleEnum.MISSING_HORSE_IDS));
				sendCommandUsage(subCommand, true, true);
			}
		}
	}

	private void displayAvailableLanguages(LocaleEnum index) {
		displayAvailableLanguages(index, null);
	}

	private void displayAvailableLanguages(LocaleEnum index, String language) {
		List<String> availableLanguageList = zh.getCM().getAvailableLanguages();
		List<String> availableLanguageMessageList = new ArrayList<>();
		for (String availableLanguage : availableLanguageList) {
			String availableLanguageMessage = zh.getMM().getMessage(s, new MessageConfig(LocaleEnum.AVAILABLE_OPTION_FORMAT) {{ setValue(availableLanguage); }}, true);
			availableLanguageMessageList.add(availableLanguageMessage);
		}
		String availableLanguagesMessage = String.join(", ", availableLanguageMessageList) + ChatColor.RESET;
		if (language != null) {
			zh.getMM().sendMessage(s, new MessageConfig(index) {{ setLanguage(language); setValue(availableLanguagesMessage); }});
		}
		else {
			zh.getMM().sendMessage(s, new MessageConfig(index) {{ setValue(availableLanguagesMessage); }});
		}
	}

	private void displayAvailableStatsDisplayMode(LocaleEnum index) {
		List<String> availableStatsDisplayModeMessageList = new ArrayList<>();
		for (StatsDisplayModeEnum statsDisplayMode : StatsDisplayModeEnum.values()) {
			String availableStatsDisplayModeMessage = zh.getMM().getMessage(s, new MessageConfig(LocaleEnum.AVAILABLE_OPTION_FORMAT) {{ setValue(statsDisplayMode.getName()); }}, true);
			availableStatsDisplayModeMessageList.add(availableStatsDisplayModeMessage);
		}
		String availableStatsDisplayModesMessage = String.join(", ", availableStatsDisplayModeMessageList) + ChatColor.RESET;
		zh.getMM().sendMessage(s, new MessageConfig(index) {{ setValue(availableStatsDisplayModesMessage); }});
	}

	private enum StatsDisplayModeEnum {

		EXACT("exact"),
		ROUNDED("rounded");

		private String name;

		private StatsDisplayModeEnum(String name) {
			this.name = name;
		}

		private String getName() {
			return name;
		}

	}

}