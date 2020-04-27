package fr.maxlego08.zspawner.api;

import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import fr.maxlego08.zspawner.zcore.utils.storage.Saveable;

public interface SpawnerManager extends Saveable{

	/**
	 * 
	 * @param uuid
	 * @return
	 */
	PlayerSpawner getPlayer(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	boolean exit(UUID uuid);
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	boolean hasSpawner(UUID uuid);
	
	/**
	 * 
	 * @return
	 */
	Board getBoard();
	
	/**
	 * 
	 * @param player
	 */
	void openInventory(Player player);

	/**
	 * 
	 * @param player
	 * @param target
	 */
	void openSendInventory(Player player, Player target);
	
	/**
	 * 
	 * @param player
	 * @param target
	 * @param spawner
	 */
	void sendSpawner(Player player, Player target, Spawner spawner);
	
	/**
	 * 
	 * @param sender
	 * @param target
	 * @param type
	 * @param number
	 */
	void addSpawner(CommandSender sender, Player target, EntityType type, int number);
	
	/**
	 * 
	 * @param sender
	 * @param target
	 * @param type
	 * @param number
	 */
	void giveSpawner(CommandSender sender, Player target, EntityType type, int number);
	
	/**
	 * 
	 * @param sender
	 * @param target
	 * @param type
	 * @param number
	 */
	void removeSpawner(CommandSender sender, Player target, EntityType type, int number);
	
	/**
	 * 
	 * @param event
	 * @param block
	 * @param player
	 */
	void placeSpawner(BlockBreakEvent event, Block block, Player player);

	/**
	 * 
	 * @return count spawner
	 */
	int count();
	
	/**
	 * 
	 * @return
	 */
	FactionListener getFactionListener();
	
	/**
	 * 
	 * @param factionListener
	 */
	void setFactionListener(FactionListener factionListener);
	
	/**
	 * 
	 * @param block
	 * @return
	 */
	boolean isBlacklist(Block block);
	
	/**
	 * 
	 * @return nsm
	 */
	NMS getNMS();
	
	/**
	 * 
	 * @param event
	 * @param player
	 * @param itemInHand
	 * @param block
	 */
	void placeSpawner(BlockPlaceEvent event, Player player, ItemStack itemInHand, Block block);
	
}
