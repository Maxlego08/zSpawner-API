package fr.maxlego08.zspawner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import fr.maxlego08.zspawner.api.Board;
import fr.maxlego08.zspawner.api.FactionListener;
import fr.maxlego08.zspawner.api.PlayerSpawner;
import fr.maxlego08.zspawner.api.Spawner;
import fr.maxlego08.zspawner.api.SpawnerManager;
import fr.maxlego08.zspawner.api.enums.InventoryType;
import fr.maxlego08.zspawner.api.event.SpawnerAddEvent;
import fr.maxlego08.zspawner.api.event.SpawnerOpenInventoryEvent;
import fr.maxlego08.zspawner.api.event.SpawnerPlaceEvent;
import fr.maxlego08.zspawner.api.event.SpawnerRegisterEvent;
import fr.maxlego08.zspawner.api.event.SpawnerRemoveEvent;
import fr.maxlego08.zspawner.depends.NoFaction;
import fr.maxlego08.zspawner.save.Config;
import fr.maxlego08.zspawner.zcore.enums.Inventory;
import fr.maxlego08.zspawner.zcore.enums.Message;
import fr.maxlego08.zspawner.zcore.logger.Logger;
import fr.maxlego08.zspawner.zcore.logger.Logger.LogType;
import fr.maxlego08.zspawner.zcore.utils.ZUtils;
import fr.maxlego08.zspawner.zcore.utils.storage.Persist;

public class ZSpawnerManager extends ZUtils implements SpawnerManager {

	private final transient Board board;
	private final transient ZSpawnerPlugin plugin;
	private transient FactionListener factionListener;
	private transient Map<UUID, PlayerSpawner> players = new HashMap<UUID, PlayerSpawner>();

	private static Map<UUID, List<Spawner>> spawners = new HashMap<>();

	protected ZSpawnerManager(Board board, ZSpawnerPlugin plugin) {
		super();
		this.board = board;
		this.plugin = plugin;

		factionListener = new NoFaction();
		if (factionListener instanceof NoFaction)
			Logger.info("No faction plugin was detected.", LogType.SUCCESS);

		SpawnerRegisterEvent event = new SpawnerRegisterEvent(factionListener);
		Bukkit.getPluginManager().callEvent(event);
		factionListener = event.getFactionListener();

	}

	@Override
	public void save(Persist persist) {
		spawners = new HashMap<>();
		players.forEach((uuid, player) -> spawners.put(uuid, player.getSpawners()));
		persist.save(this, "spawners");
	}

	@Override
	public void load(Persist persist) {
		persist.loadOrSaveDefault(this, ZSpawnerManager.class, "spawners");
		players = new HashMap<UUID, PlayerSpawner>();
		spawners.forEach((uuid, list) -> players.put(uuid, new PlayerObject(uuid, list)));
		players.values().forEach(player -> player.getSpawners().forEach(spawner -> {
			if (spawner.isPlace())
				board.placeSpawner(spawner.getLocation(), spawner);
		}));
	}

	@Override
	public PlayerSpawner getPlayer(UUID uuid) {
		if (!players.containsKey(uuid)) {
			PlayerSpawner playerSpawner = new PlayerObject(uuid);
			players.put(uuid, playerSpawner);
		}
		return players.get(uuid);
	}

	@Override
	public boolean exit(UUID uuid) {
		return players.containsKey(uuid);
	}

	@Override
	public boolean hasSpawner(UUID uuid) {
		return exit(uuid) && getPlayer(uuid).spawnerSize() > 0;
	}

	@Override
	public Board getBoard() {
		return board;
	}

	@Override
	public void openInventory(Player player) {

		UUID uuid = player.getUniqueId();

		if (!exit(uuid))
			message(player, Message.NO_SPAWNER);
		else if (!hasSpawner(uuid))
			message(player, Message.NO_SPAWNER);
		else {

			PlayerSpawner playerSpawner = getPlayer(uuid);
			if (playerSpawner.isPlacing())
				message(player, Message.PLACING_SPAWNER);
			else {

				Inventory inventory = Config.type == InventoryType.PAGINATE ? Inventory.INVENTORY_SPAWNER_PAGINATE
						: Inventory.INVENTORY_SPAWNER;

				SpawnerOpenInventoryEvent event = new SpawnerOpenInventoryEvent(inventory, playerSpawner, player);
				event.callEvent();

				if (event.isCancelled())
					return;

				createInventory(player, inventory, 1, playerSpawner);
			}

		}

	}

	@Override
	public void openSendInventory(Player player, Player target) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendSpawner(Player player, Player target, Spawner spawner) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addSpawner(CommandSender sender, Player target, EntityType type, int number) {

		PlayerSpawner playerSpawner = getPlayer(target.getUniqueId());

		SpawnerAddEvent event = new SpawnerAddEvent(sender, target, playerSpawner, type, number);
		event.callEvent();

		if (event.isCancelled())
			return;

		type = event.getType();
		number = event.getAmount();

		if (number < 0)
			return;

		for (int a = 0; a < number; a++) {
			Spawner spawner = new SpawnerObject(target.getUniqueId(), type);
			playerSpawner.addSpawner(spawner);
		}

		String message = Message.ADD_SPAWNER_SENDER.getMessage();
		message = message.replace("%how%", String.valueOf(number));
		message = message.replace("%type%", name(type.name()));
		message = message.replace("%player%", target.getName());

		message(sender, message);

		message = Message.ADD_SPAWNER_RECEIVER.getMessage();
		message = message.replace("%how%", String.valueOf(number));
		message = message.replace("%type%", name(type.name()));

		message(target, message);

	}

	@Override
	public void giveSpawner(CommandSender sender, Player target, EntityType type, int number) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSpawner(CommandSender sender, Player target, EntityType type, int number) {

		UUID uuid = target.getUniqueId();

		if (!hasSpawner(uuid)) {
			message(sender, Message.NO_SPAWNER_TARGET, target.getName());
			return;
		}

		PlayerSpawner playerSpawner = getPlayer(uuid);

		SpawnerRemoveEvent event = new SpawnerRemoveEvent(sender, target, playerSpawner, type, number);
		event.callEvent();

		if (event.isCancelled())
			return;

		type = event.getType();
		number = event.getAmount();

		if (number < 0)
			return;

		playerSpawner.removeSpawner(board, type, number);

		String message = Message.REMOVE_SPAWNER_SENDER.getMessage();
		message = message.replace("%how%", String.valueOf(number));
		message = message.replace("%type%", name(type.name()));
		message = message.replace("%player%", target.getName());

		message(sender, message);

		message = Message.REMOVE_SPAWNER_RECEIVER.getMessage();
		message = message.replace("%how%", String.valueOf(number));
		message = message.replace("%type%", name(type.name()));

		message(target, message);

	}

	@Override
	public void placeSpawner(BlockBreakEvent event, Block block, Player player) {

		// Verif de la permission de casser ici

		UUID uuid = player.getUniqueId();
		if (!hasSpawner(uuid))
			return;

		PlayerSpawner playerSpawner = getPlayer(uuid);
		if (playerSpawner.isPlacing()) {

			event.setCancelled(true);
			if (!factionListener.preBuild(player, block.getLocation())) {
				message(player, Message.PLACE_SPAWNER_ERROR);
				return;
			}

			if (isBlacklist(block)) {
				message(player, Message.PLACE_SPAWNER_ERROR_BLACKLIST);
				return;
			}

			Spawner spawner = playerSpawner.getCurrentPlacingSpawner();
			SpawnerPlaceEvent placeEvent = new SpawnerPlaceEvent(player, playerSpawner, spawner, block.getLocation());

			if (placeEvent.isCancelled())
				return;

			Location finalLocation = placeEvent.getLocation() == null ? block.getLocation() : placeEvent.getLocation();
			spawner.place(finalLocation);
			playerSpawner.placeSpawner();
			board.placeSpawner(finalLocation, spawner);

			message(player, Message.PLACE_SPAWNER_SUCCESS);

		}

	}

	@Override
	public int count() {
		AtomicInteger amount = new AtomicInteger();
		players.values().forEach(player -> amount.getAndAdd(player.spawnerSize()));
		return amount.get();
	}

	@Override
	public FactionListener getFactionListener() {
		return factionListener;
	}

	@Override
	public void setFactionListener(FactionListener factionListener) {
		this.factionListener = factionListener;
	}

	@Override
	public boolean isBlacklist(Block block) {
		return Config.blacklistMaterial.contains(block.getType());
	}

}
