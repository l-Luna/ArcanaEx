package arcana.blocks.courier.symbols;

import arcana.blocks.courier.InventoryListenerHooks;
import arcana.blocks.courier.SymbolContext;
import arcana.blocks.courier.SymbolStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventorySymbolStorage implements SymbolStorage, InventoryChangedListener{
	
	private final Inventory inventory;
	private final Direction insertDirection;
	private int version;
	
	public InventorySymbolStorage(SimpleInventory inventory, Direction insertDirection){
		this.inventory = inventory;
		this.insertDirection = insertDirection;
		InventoryListenerHooks.addHook(inventory, this);
	}
	
	public InventorySymbolStorage(BlockEntity inventory, Direction insertDirection){
		this.inventory = (Inventory)inventory;
		this.insertDirection = insertDirection;
		InventoryListenerHooks.addHook(inventory, this);
	}
	
	public static @Nullable InventorySymbolStorage fromContext(SymbolContext ctx){
		return ctx.getSingleBlock().flatMap(pos -> {
			Inventory there = HopperBlockEntity.getInventoryAt(ctx.world(), pos);
			if(there instanceof SimpleInventory si)
				return Optional.of(new InventorySymbolStorage(si, ctx.symbolFacing().getOpposite()));
			else if(there instanceof BlockEntity be)
				return Optional.of(new InventorySymbolStorage(be, ctx.symbolFacing().getOpposite()));
			return Optional.empty();
		}).orElse(null);
	}
	
	public long version(){
		return version;
	}
	
	public List<? extends ResourceHandle<ItemVariant>> getItems(){
		List<SlotStorageHandle> slots = new ArrayList<>();
		for(int i = 0; i < inventory.size(); i++)
			if(!inventory.getStack(i).isEmpty())
				slots.add(new SlotStorageHandle(inventory, i));
		return slots;
	}
	
	public long insertItem(ResourceAmount<ItemVariant> stack, boolean simulate){
		// slightly lazy, uses fabric-transfer just for easy simulate mode
		long amountInserted;
		try(Transaction transaction = Transaction.openOuter()){
			amountInserted = InventoryStorage.of(inventory, insertDirection).insert(stack.resource(), stack.amount(), transaction);
			if(!simulate)
				transaction.commit();
		}
		return amountInserted;
	}
	
	// "vanilla" containers don't support fluids; modded fluid containers should go through FabricBackedSymbolStorage instead
	public List<ResourceHandle<FluidVariant>> getFluids(){
		return List.of();
	}
	
	public long insertFluid(ResourceAmount<FluidVariant> stack, boolean simulate){
		return 0;
	}
	
	public void onInventoryChanged(Inventory sender){
		if(sender == inventory)
			version++;
	}
	
	private record SlotStorageHandle(Inventory inventory, int slot) implements ResourceHandle<ItemVariant>{
		
		public ResourceAmount<ItemVariant> getStack(){
			ItemStack stack = inventory.getStack(slot);
			return new ResourceAmount<>(ItemVariant.of(stack), stack.getCount());
		}
		
		public long take(long amount, boolean simulate){
			return inventory.removeStack(slot, (int)amount).getCount();
		}
		
		public boolean valid(){
			// an inventory slot won't stop existing
			return true;
		}
	}
}