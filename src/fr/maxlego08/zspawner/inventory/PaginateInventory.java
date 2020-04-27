package fr.maxlego08.zspawner.inventory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.maxlego08.zspawner.ZSpawnerPlugin;
import fr.maxlego08.zspawner.exceptions.InventoryOpenException;
import fr.maxlego08.zspawner.save.Config;
import fr.maxlego08.zspawner.zcore.utils.inventory.Button;
import fr.maxlego08.zspawner.zcore.utils.inventory.InventoryResult;
import fr.maxlego08.zspawner.zcore.utils.inventory.InventorySize;
import fr.maxlego08.zspawner.zcore.utils.inventory.ItemButton;
import fr.maxlego08.zspawner.zcore.utils.inventory.Pagination;

public abstract class PaginateInventory<T> extends VInventory {

	protected List<T> collections;
	protected final String inventoryName;
	protected final int inventorySize;
	protected int paginationSize = 45;
	protected int nextSlot = 50;
	protected int previousSlot = 48;
	protected int infoSlot = 49;
	protected int removeAllSlot = 45;
	protected int defaultSlot = 0;
	protected boolean isReverse = false;
	protected boolean disableDefaultClick = false;
	protected Material previousMaterial = Material.ARROW;
	protected int maxPage;

	/**
	 * 
	 * @param inventoryName
	 * @param inventorySize
	 */
	public PaginateInventory(String inventoryName, int inventorySize) {
		super();

		inventorySize = inventorySize < 18 ? 18 : inventorySize > 54 ? 54 : inventorySize;
		inventorySize = inventorySize >= 0 && inventorySize < 9 ? 9 : inventorySize;
		inventorySize = inventorySize > 9 && inventorySize < 18 ? 18 : inventorySize;
		inventorySize = inventorySize > 18 && inventorySize < 27 ? 27 : inventorySize;
		inventorySize = inventorySize > 27 && inventorySize < 36 ? 36 : inventorySize;
		inventorySize = inventorySize > 36 && inventorySize < 45 ? 45 : inventorySize;
		inventorySize = inventorySize > 45 && inventorySize < 54 ? 54 : inventorySize;

		this.inventoryName = inventoryName;
		this.inventorySize = inventorySize;

		this.paginationSize = inventorySize - 9;
		this.nextSlot = inventorySize - 4;
		this.previousSlot = inventorySize - 6;
		this.infoSlot = inventorySize - 5;
		this.removeAllSlot = inventorySize - 8;
	}

	public PaginateInventory(String inventoryName, InventorySize inventorySize) {
		this.inventoryName = inventoryName;
		this.inventorySize = inventorySize.getSize();
		this.paginationSize = inventorySize.getPaginationSize();
		this.nextSlot = inventorySize.getNextSlot();
		this.previousSlot = inventorySize.getPreviousSlot();
		this.defaultSlot = inventorySize.getDefaultSlot();
	}

	@Override
	public InventoryResult openInventory(ZSpawnerPlugin main, Player player, int page, Object... args)
			throws InventoryOpenException {

		if (defaultSlot > inventorySize || nextSlot > inventorySize || previousSlot > inventorySize
				|| paginationSize > inventorySize)
			throw new InventoryOpenException("Une erreur est survenue avec la gestion des slots !");

		collections = preOpenInventory();

		maxPage = getMaxPage(collections, paginationSize);

		super.createInventory(
				inventoryName.replace("%mp%", String.valueOf(maxPage)).replace("%p%", String.valueOf(page)),
				inventorySize);

		Pagination<T> pagination = new Pagination<>();
		AtomicInteger slot = new AtomicInteger(defaultSlot);

		List<T> tmpList = isReverse ? pagination.paginateReverse(collections, paginationSize, page)
				: pagination.paginate(collections, paginationSize, page);

		tmpList.forEach(tmpItem -> {
			ItemButton button = addItem(slotChange(slot.getAndIncrement()), buildItem(tmpItem));
			ItemButton itemButton = createButton(button);
			if (!disableDefaultClick)
				itemButton.setClick((event) -> onClick(tmpItem, itemButton));
		});

		if (getPage() != 1) {
			Button button = Config.buttonPrevious;
			int slot1 = button.getSlot() > inventorySize ? previousSlot : button.getSlot();
			addItem(slot1, button.toItemStack())
					.setClick(event -> createInventory(player, getId(), getPage() - 1, args));
		}
		if (getPage() != maxPage) {
			Button button = Config.buttonNext;
			int slot1 = button.getSlot() > inventorySize ? nextSlot : button.getSlot();
			addItem(slot1, button.toItemStack())
					.setClick(event -> createInventory(player, getId(), getPage() + 1, args));
		}

		postOpenInventory();

		return InventoryResult.SUCCESS;
	}

	/**
	 * 
	 * @param slot
	 * @return
	 */
	protected int slotChange(int slot) {
		return slot;
	}

	/**
	 * 
	 * @param button
	 * @return
	 */
	protected ItemButton createButton(ItemButton button) {
		return button;
	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	public abstract ItemStack buildItem(T object);

	/**
	 * 
	 * @param object
	 * @param button
	 */
	public abstract void onClick(T object, ItemButton button);

	/**
	 * 
	 * @return
	 */
	public abstract List<T> preOpenInventory();

	/**
	 * Called after create inventory
	 */
	public abstract void postOpenInventory();
}
