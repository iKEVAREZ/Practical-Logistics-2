package sonar.logistics.common.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.InventoryHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.common.multiparts.nodes.ArrayPart;

public class ContainerArray extends ContainerMultipartSync {

	public ContainerArray(EntityPlayer player, ArrayPart part) {
		super(part);
		for (int k = 0; k < 8; ++k) {
			this.addSlotToContainer(new ArraySlot(part, k, 17 + k * 18, 20));
		}

		for (int j = 0; j < 3; ++j) {
			for (int k = 0; k < 9; ++k) {
				this.addSlotToContainer(new Slot(player.inventory, k + j * 9 + 9, 8 + k * 18, 51 + j * 18));
			}
		}

		for (int j = 0; j < 9; ++j) {
			this.addSlotToContainer(new Slot(player.inventory, j, 8 + j * 18, 109));
		}
	}

	public ItemStack transferStackInSlot(EntityPlayer player, int slotID) {
		ItemStack itemstack = InventoryHelper.EMPTY;
		Slot slot = (Slot) this.inventorySlots.get(slotID);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotID < 8) {
				if (!this.mergeItemStack(itemstack1, 8, this.inventorySlots.size(), true)) {
					return InventoryHelper.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, 8, false)) {
				return InventoryHelper.EMPTY;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack(InventoryHelper.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}

}