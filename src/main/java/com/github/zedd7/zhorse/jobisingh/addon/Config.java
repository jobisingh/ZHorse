package com.github.zedd7.zhorse.jobisingh.addon;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class Config {

    Plugin p;
    
    static FileConfiguration config;
    static File cfile;
   
    static FileConfiguration data;
    static File dfile;
   
    public static void setup(Plugin p) {
            //config.options().copyDefaults(true);
            //saveConfig();
           
            if (!p.getDataFolder().exists()) {
                    p.getDataFolder().mkdir();
            }
           
            dfile = new File(p.getDataFolder(), "Breeds.yml");
           
            if (!dfile.exists()) {
                    try {
                            dfile.createNewFile();
                    }
                    catch (IOException e) {
                            Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create Breeds.yml!");
                    }
            }
           
            data = YamlConfiguration.loadConfiguration(dfile);
    }
   
    public static FileConfiguration getData() {
            return data;
    }
   
    public static void saveData() {
            try {
                    data.save(dfile);
            }
            catch (IOException e) {
                    Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save Breeds.yml!");
            }
    }
   
    public static void reloadData() {
            data = YamlConfiguration.loadConfiguration(dfile);
    }
   
    public FileConfiguration getConfig() {
            return config;
    }
   
    public static void saveConfig() {
            try {
                    config.save(cfile);
            }
            catch (IOException e) {
                    Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save config.yml!");
            }
    }
   
    public void reloadConfig() {
            config = YamlConfiguration.loadConfiguration(cfile);
    }
   
    public PluginDescriptionFile getDesc() {
            return p.getDescription();
    }
	
}