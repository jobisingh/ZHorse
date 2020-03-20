package com.github.zedd7.zhorse.jobisingh.addon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Damageable;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.zedd7.zhorse.ZHorse;

/**
Author: jobisingh
**/
public class StatsHandler {
	
	private static HashMap<UUID, String> horseGenders = new HashMap<UUID, String>();
	private static HashMap<UUID, Double> horseAges = new HashMap<UUID, Double>();
	private static HashMap<UUID, String> horseBreeds = new HashMap<UUID, String>();
	private static ArrayList<String> listOfBreeds = new ArrayList<String>();
	
	
	//GENDER

	public static String getHorseGender(UUID horseID) {
		String gender = "Unknown";
		if(horseGenders.containsKey(horseID)) gender = horseGenders.get(horseID);
		return gender;
		
	}
	
	public static Boolean setHorseGender(UUID horseID, String gender) {
		if(gender == "Male" || gender == "Female") {
			horseGenders.put(horseID, gender);
			return true;
		}
		else return false;
	}
	
	public static void saveGenders() {
		try {
			SLAPI.save(horseGenders, "plugins/ZHorse/HorseGenders");
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}
	
	public static void loadGenders() {
		try {
			horseGenders = (HashMap<UUID, String>) SLAPI.load("plugins/ZHorse/HorseGenders");
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}
	
	
	//AGE
	
	public static Integer getHorseAge(UUID horseID) {
		Double age = 0.0;
		if(horseAges.containsKey(horseID)) age = horseAges.get(horseID);
		return (int) Math.round(age/11.2);
		
	}
	
	public static Boolean hasAge(UUID horseID) {
		
		if(horseAges.containsKey(horseID)) return true;
		
		return false;
	}
	
	public static Boolean setHorseAge(UUID horseID, Double age) {
		if(age < 31 && age > -1) {
			
			Double finalAge = age*11.2;
			
			horseAges.put(horseID, finalAge);
			return true;
		}
		else return false;
	}
	
	//Updates Horse Ages Every Hour.
	public static void startAgingHandler() {
	    new BukkitRunnable(){
	        @Override
	        public void run() {	
	        	
	        	HashMap<UUID, Double> tempMap = new HashMap<UUID, Double>();
	        	     	
	        	for(Entry<UUID, Double> entry : horseAges.entrySet()) {
	        		
	        		if(entry.getValue() >= 672 ) {
	        			Damageable horse = (Damageable) Bukkit.getEntity(entry.getKey());
	        			horse.damage(10000.99);
	        			
	        			AbstractHorse abHorse = (AbstractHorse) horse;
	        			if(Bukkit.getOfflinePlayer(abHorse.getOwner().getUniqueId()).isOnline()) {
	        				Bukkit.getPlayer(abHorse.getOwner().getUniqueId()).sendMessage(ChatColor.GREEN + "[ZHorse] " + ChatColor.GOLD + horse.getCustomName() + ChatColor.YELLOW + " has died of old age");
	        			}
	        			
	        		}
	        		else tempMap.put(entry.getKey(), entry.getValue()+1);
	        		
	        	}
	        	horseAges = tempMap;
	        	
	        } 
	    }.runTaskTimer(ZHorse.plugin, 0, 72000);
		
	}
	
	//Saving The Genders to a File
	public static void saveAges() {
		try {
			SLAPI.save(horseAges, "plugins/ZHorse/HorseAges");
		} catch (Exception e) {
		}
	}
	
	//Loading the Genders from a File
	public static void loadAges() {
		try {
			horseAges = (HashMap<UUID, Double>) SLAPI.load("plugins/ZHorse/HorseAges");
		} catch (Exception e) {
		}
	}
	
	//A method to test if a string is an Integer.
	public static boolean isInteger(String s)
	{
	    try
	    {
	        Integer.parseInt(s);
	        return true;
	    } catch (NumberFormatException ex)
	    {
	        return false;
	    }
	}
	
	
	//BREED
	
	public static String getHorseBreed(UUID horseID) {
		if(hasBreed(horseID) == false) return "Unknown";
		if(isBreed(horseBreeds.get(horseID)) == false) return "Unknown"; 
		return horseBreeds.get(horseID);
	}
	
	public static Boolean hasBreed(UUID horseID) {
		if(horseBreeds.containsKey(horseID)) return true;
		return false;
	}
	
	public static Boolean isBreed(String breedName) {
		if(listOfBreeds.contains(breedName)) return true;
		return false;
	}
	
	public static Boolean setHorseBreed(UUID horseID, String breed) {
		
		if(isBreed(breed) == false) return false;
		
		horseBreeds.put(horseID, breed);
		
		return false;
	}
	
	public static void updateListOfBreedsFromConfig() {
		
		List<String> tempList = (List<String>) Config.getData().getList("Breeds");
		
		listOfBreeds = new ArrayList<String>(tempList);
		
	}
	
	public static void addToHorseBreeds(String breed) {
		listOfBreeds.add(breed);
	}
	
	public static void removeFromHorseBreeds(String breed) {
		listOfBreeds.remove(breed);
	}
	
	
	public static void saveListOfBreeds() {
		Config.getData().set("Breeds", listOfBreeds);
		Config.saveData();
	}
	
	public static void saveBreeds() {
		try {
			SLAPI.save(horseBreeds, "plugins/ZHorse/HorseBreeds");
		} catch (Exception e) {
		}
	}
	
	//Loading the Genders from a File
	public static void loadBreeds() {
		try {
			horseBreeds = (HashMap<UUID, String>) SLAPI.load("plugins/ZHorse/HorseBreeds");
		} catch (Exception e) {
		}
	}
	
}
