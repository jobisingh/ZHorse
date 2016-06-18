package eu.reborn_minecraft.zhorse.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LeashHitch;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import eu.reborn_minecraft.zhorse.ZHorse;
import eu.reborn_minecraft.zhorse.utils.AsyncChunckLoad;
import net.md_5.bungee.api.ChatColor;

public class HorseManager {
	
	public static final double MIN_HEALTH = 1.0;
	public static final double MIN_JUMP_STRENGTH = 0.0;
	public static final double MIN_SPEED = 0.0;
	
	public static final double MAX_HEALTH = 30.0;
	public static final double MAX_JUMP_STRENGTH = 1.2;
	public static final double MAX_SPEED = 1.0;
	
	private static final int TICK_PER_SECOND = 20;
	
	private ZHorse zh;
	private Map<UUID, Horse> loadedHorses = new HashMap<UUID, Horse>();
	
	public HorseManager(ZHorse zh) {
		this.zh = zh;
		loadHorses();
	}
	
	public Horse getFavoriteHorse(UUID playerUUID) {
		return getHorse(playerUUID, zh.getUM().getFavoriteUserID(playerUUID));
	}
	
	public Horse getHorse(UUID playerUUID, String userID) {
		Horse horse = null;
		if (playerUUID != null && userID != null) {
			UUID horseUUID = zh.getUM().getHorseUUID(playerUUID, userID);
			if (horseUUID != null) {
				horse = getLoadedHorse(horseUUID);
				if (horse == null) {
					Location location = zh.getUM().getLocation(playerUUID, userID);
					horse = getHorseFromLocation(horseUUID, location);
				}
			}
		}
		return horse;
	}
	
	private Horse getHorseFromLocation(UUID horseUUID, Location location) {
		Horse horse = null;
		if (location != null) {
			horse = getHorseInChunk(horseUUID, location.getChunk());
			if (horse == null) {
				List<Chunk> neighboringChunks = getChunksInRegion(location, 2);
				for (Chunk chunk : neighboringChunks) {
					horse = getHorseInChunk(horseUUID, chunk);
					if (horse != null) {
						break;
					}
				}
			}
		}
		return horse;
	}

	private Horse getHorseInChunk(UUID horseUUID, Chunk chunk) {
		for (Entity entity : chunk.getEntities()) {
			if (entity.getUniqueId().equals(horseUUID)) {
				return (Horse) entity;
			}
		}
		return null;
	}
	
	private List<Chunk> getChunksInRegion(Location center, int chunkRange) {
		World world = center.getWorld();
		Location NWCorner = new Location(world, center.getX() - 16 * chunkRange, 0, center.getZ() - 16 * chunkRange);
		Location SECorner = new Location(world, center.getX() + 16 * chunkRange, 0, center.getZ() + 16 * chunkRange);
		List<Chunk> chunkList = new ArrayList<Chunk>();
		for (int x = NWCorner.getBlockX(); x <= SECorner.getBlockX(); x += 16) {
			for (int z = NWCorner.getBlockZ(); z <= SECorner.getBlockZ(); z += 16) {
				// WARN w.getChunkAt(x, z) uses chunk coordinates (loc % 16)
				chunkList.add(world.getChunkAt(new Location(world, x, 0, z)));
			}
		}
		return chunkList;
	}

	public Horse getLoadedHorse(UUID horseUUID) {
		return loadedHorses.get(horseUUID);
	}
	
	public Map<UUID, Horse> getLoadedHorses() {
		return loadedHorses;
	}
	
	public void loadHorse(Horse horse) {
		UUID horseUUID = horse.getUniqueId();
		if (!loadedHorses.containsKey(horseUUID)) {
			loadedHorses.put(horseUUID, horse);
		}
	}
	
	public void loadHorses() {
		for (World world : zh.getServer().getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				new AsyncChunckLoad(zh, chunk);
			}
		}
	}
	
	public void unloadHorse(Horse horse) {
		unloadHorse(horse.getUniqueId());
	}
	
	public void unloadHorse(UUID horseUUID) {
		if (loadedHorses.containsKey(horseUUID)) {
			loadedHorses.remove(horseUUID);
		}
	}
	
	public void unloadHorses() {
		Iterator<Entry<UUID, Horse>> loadedHorsesItr = loadedHorses.entrySet().iterator();
		while (loadedHorsesItr.hasNext()) {
			Horse horse = loadedHorsesItr.next().getValue();
			zh.getUM().saveLocation(horse);
			loadedHorsesItr.remove();
		}
	}
	
	public Horse teleport(Horse sourceHorse, Location destination) {
		Horse copyHorse = (Horse) destination.getWorld().spawnEntity(destination, EntityType.HORSE);
		if (copyHorse != null) {
			UUID playerUUID = zh.getUM().getPlayerUUID(sourceHorse);
			String userID = zh.getUM().getUserID(playerUUID, sourceHorse);
			copyAttributes(sourceHorse, copyHorse);
			copyInventory(sourceHorse, copyHorse);
			removeLeash(sourceHorse);
			unloadHorse(sourceHorse);
			loadHorse(copyHorse);
			removeHorse(sourceHorse);
			zh.getUM().updateHorse(playerUUID, userID, copyHorse);
		}
		return copyHorse;
	}

	private void removeLeash(Horse horse) {
		if (horse.isLeashed()) {
			Entity leashHolder = horse.getLeashHolder();
			if (leashHolder instanceof LeashHitch) {
				leashHolder.remove();
			}
			ItemStack leash = new ItemStack(Material.LEASH);
			horse.getWorld().dropItem(horse.getLocation(), leash);
		}
	}
	
	private void removeHorse(Horse horse) {
		World world = horse.getWorld();
		int effectDuration = 3;
		horse.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, effectDuration * TICK_PER_SECOND, 0));
		horse.remove();
		new Thread() {
			public void run() {
				try {
					sleep(effectDuration * 1000);
					if (world.getEntitiesByClass(horse.getClass()).contains(horse)) {
						Location location = horse.getLocation();
						int x = (int) location.getX();
						int y = (int) location.getY();
						int z = (int) location.getZ();
						String warning = "A horse was duplicated at location %s:%s:%s in world %s, killing it.";
						zh.getServer().broadcast(ChatColor.RED + String.format(warning, x, y, z, world.getName()), "zh.admin");
						horse.setHealth(0);
					}
				} catch (Exception e) {}
			}
		}.start();
	}

	private void copyAttributes(Horse sourceHorse, Horse copyHorse) {	
		// Define maximum of value before actual value to keep it in valid range
		copyHorse.setMaxDomestication(sourceHorse.getMaxDomestication());
		copyHorse.setMaxHealth(sourceHorse.getMaxHealth());
		copyHorse.setMaximumAir(sourceHorse.getMaximumAir());
		copyHorse.setMaximumNoDamageTicks(sourceHorse.getMaximumNoDamageTicks());
		
		copyHorse.addPotionEffects(sourceHorse.getActivePotionEffects());
		copyHorse.setAge(sourceHorse.getAge());
		copyHorse.setAgeLock(sourceHorse.getAgeLock());
		copyHorse.setBreed(sourceHorse.canBreed());
		copyHorse.setCanPickupItems(sourceHorse.getCanPickupItems());
		copyHorse.setColor(sourceHorse.getColor());
		copyHorse.setCustomName(sourceHorse.getCustomName());
		copyHorse.setCustomNameVisible(sourceHorse.isCustomNameVisible());
		copyHorse.setDomestication(sourceHorse.getDomestication());
		copyHorse.setFallDistance(sourceHorse.getFallDistance());
		copyHorse.setFireTicks(sourceHorse.getFireTicks());
		copyHorse.setGlowing(sourceHorse.isGlowing());
		copyHorse.setHealth(sourceHorse.getHealth());
		copyHorse.setJumpStrength(sourceHorse.getJumpStrength());
		copyHorse.setLastDamage(sourceHorse.getLastDamage());
		copyHorse.setLastDamageCause(sourceHorse.getLastDamageCause());
		copyHorse.setNoDamageTicks(sourceHorse.getNoDamageTicks());
		copyHorse.setOwner(sourceHorse.getOwner());
		copyHorse.setRemainingAir(sourceHorse.getRemainingAir());
		copyHorse.setRemoveWhenFarAway(sourceHorse.getRemoveWhenFarAway());
		copyHorse.setStyle(sourceHorse.getStyle());
		copyHorse.setTamed(sourceHorse.isTamed());
		copyHorse.setTicksLived(sourceHorse.getTicksLived());
		copyHorse.setVariant(sourceHorse.getVariant());
		
		double speed = sourceHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
		copyHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
	}
	
	private void copyInventory(Horse sourceHorse, Horse copyHorse) {
		copyHorse.setCarryingChest(sourceHorse.isCarryingChest());
		copyHorse.getInventory().setContents(sourceHorse.getInventory().getContents());
	}

}
