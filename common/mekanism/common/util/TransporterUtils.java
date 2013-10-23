package mekanism.common.util;

import java.util.Arrays;

import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.tileentity.TileEntityLogisticalTransporter;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public final class TransporterUtils 
{
    /**
     * Gets all the transporters around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of TileEntities
     */
    public static TileEntity[] getConnectedTransporters(TileEntity tileEntity)
    {
    	TileEntity[] transporters = new TileEntity[] {null, null, null, null, null, null};
    	
    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity transporter = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(TransmissionType.checkTransmissionType(transporter, TransmissionType.ITEM))
			{
				transporters[orientation.ordinal()] = transporter;
			}
    	}
    	
    	return transporters;
    }

    /**
     * Gets all the adjacent connections to a TileEntity.
     * @param tileEntity - center TileEntity
     * @return boolean[] of adjacent connections
     */
    public static boolean[] getConnections(TileEntity tileEntity)
    {
		boolean[] connectable = new boolean[] {false, false, false, false, false, false};
		
		TileEntity[] connectedTransporters = getConnectedTransporters(tileEntity);
		IInventory[] connectedInventories = getConnectedInventories(tileEntity);
		
		for(IInventory inventory : connectedInventories)
		{
			if(inventory != null)
			{
				int side = Arrays.asList(connectedInventories).indexOf(inventory);
				ForgeDirection forgeSide = ForgeDirection.getOrientation(side).getOpposite();
				
				if(inventory.getSizeInventory() > 0)
				{
					if(inventory instanceof ISidedInventory)
					{
						ISidedInventory sidedInventory = (ISidedInventory)inventory;
						
						if(sidedInventory.getAccessibleSlotsFromSide(forgeSide.ordinal()) != null)
						{
							if(sidedInventory.getAccessibleSlotsFromSide(forgeSide.ordinal()).length > 0)
							{
								connectable[side] = true;
							}
						}
					}
					else {
						connectable[side] = true;
					}
				}
			}
		}
		
		for(TileEntity tile : connectedTransporters)
		{
			if(tile != null)
			{
				int side = Arrays.asList(connectedTransporters).indexOf(tile);
				
				connectable[side] = true;
			}
		}
		
		return connectable;
    }
    
    /**
     * Gets all the inventories around a tile entity.
     * @param tileEntity - center tile entity
     * @return array of IInventories
     */
    public static IInventory[] getConnectedInventories(TileEntity tileEntity)
    {
    	IInventory[] inventories = new IInventory[] {null, null, null, null, null, null};

    	for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
    	{
			TileEntity inventory = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(tileEntity.worldObj);
			
			if(inventory instanceof IInventory && !(inventory instanceof ITransmitter))
			{
				inventories[orientation.ordinal()] = (IInventory)inventory;
			}
    	}
    	
    	return inventories;
    }
    
    public static boolean insert(TileEntity outputter, TileEntityLogisticalTransporter tileEntity, ItemStack itemStack)
    {
    	return tileEntity.insert(Object3D.get(outputter), itemStack);
    }
    
    public static boolean canInsert(TileEntity tileEntity, ItemStack itemStack)
    {
    	if(!(tileEntity instanceof IInventory))
    	{
    		return false;
    	}
    	
    	IInventory inventory = (IInventory)tileEntity;
    		
    	return true;
    }
    
	public static ItemStack putStackInInventory(IInventory inventory, ItemStack itemStack, int side) 
	{
		if (!(inventory instanceof ISidedInventory)) {
			for (int i = 0; i <= inventory.getSizeInventory() - 1; i++)
			{
				if (inventory.isItemValidForSlot(i, itemStack)) {
					ItemStack inSlot = inventory.getStackInSlot(i);

					if (inSlot == null) {
						inventory.setInventorySlotContents(i, itemStack);
						return null;
					} 
					else if (inSlot.isItemEqual(itemStack)
							&& inSlot.stackSize < inSlot.getMaxStackSize()) 
					{
						if (inSlot.stackSize + itemStack.stackSize <= inSlot
								.getMaxStackSize()) {
							ItemStack toSet = itemStack.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(i, toSet);
							return null;
						} 
						else {
							int rejects = (inSlot.stackSize + itemStack.stackSize) - inSlot.getMaxStackSize();

							ItemStack toSet = itemStack.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = itemStack.copy();
							remains.stackSize = rejects;

							inventory.setInventorySlotContents(i, toSet);
							return remains;
						}
					}
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory) inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

			for (int get = 0; get <= slots.length - 1; get++) 
			{
				int slotID = slots[get];

				if (sidedInventory.isItemValidForSlot(slotID, itemStack)
						&& sidedInventory
								.canInsertItem(slotID, itemStack, side)) 
				{
					ItemStack inSlot = inventory.getStackInSlot(slotID);

					if (inSlot == null) {
						inventory.setInventorySlotContents(slotID, itemStack);
						return null;
					} 
					else if (inSlot.isItemEqual(itemStack) && inSlot.stackSize < inSlot.getMaxStackSize())
					{
						if (inSlot.stackSize + itemStack.stackSize <= inSlot
								.getMaxStackSize()) {
							ItemStack toSet = itemStack.copy();
							toSet.stackSize += inSlot.stackSize;

							inventory.setInventorySlotContents(slotID, toSet);
							return null;
						} 
						else {
							int rejects = (inSlot.stackSize + itemStack.stackSize)
									- inSlot.getMaxStackSize();

							ItemStack toSet = itemStack.copy();
							toSet.stackSize = inSlot.getMaxStackSize();

							ItemStack remains = itemStack.copy();
							remains.stackSize = rejects;

							inventory.setInventorySlotContents(slotID, toSet);
							return remains;
						}
					}
				}
			}
		}

		return itemStack;
	}

	public static ItemStack takeTopItemFromInventory(IInventory inventory, int side) 
	{
		if (!(inventory instanceof ISidedInventory)) 
		{
			for (int i = inventory.getSizeInventory() - 1; i >= 0; i--) 
			{
				if (inventory.getStackInSlot(i) != null) {
					ItemStack toSend = inventory.getStackInSlot(i).copy();
					toSend.stackSize = 1;

					inventory.decrStackSize(i, 1);

					return toSend;
				}
			}
		} 
		else {
			ISidedInventory sidedInventory = (ISidedInventory) inventory;
			int[] slots = sidedInventory.getAccessibleSlotsFromSide(side);

			if (slots != null) 
			{
				for (int get = slots.length - 1; get >= 0; get--) 
				{
					int slotID = slots[get];

					if (sidedInventory.getStackInSlot(slotID) != null) 
					{
						ItemStack toSend = sidedInventory
								.getStackInSlot(slotID);
						toSend.stackSize = 1;

						if (sidedInventory.canExtractItem(slotID, toSend, side)) 
						{
							sidedInventory.decrStackSize(slotID, 1);

							return toSend;
						}
					}
				}
			}
		}

		return null;
	}
}