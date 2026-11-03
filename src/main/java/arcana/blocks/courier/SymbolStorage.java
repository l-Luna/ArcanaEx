package arcana.blocks.courier;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

import java.util.List;

public interface SymbolStorage{
	
	interface ResourceHandle<T extends TransferVariant<?>>{
		
		ResourceAmount<T> getStack();
		
		long take(long amount, boolean simulate);
	}
	
	long version();
	
	List<? extends ResourceHandle<ItemVariant>> getItems();
	
	List<? extends ResourceHandle<FluidVariant>> getFluids();
	
	long insertItem(ResourceAmount<ItemVariant> stack, boolean simulate);
	
	long insertFluid(ResourceAmount<FluidVariant> stack, boolean simulate);
}