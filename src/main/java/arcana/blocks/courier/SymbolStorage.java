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
		
		boolean valid();
	}
	
	long version();
	
	List<? extends ResourceHandle<ItemVariant>> getItems();
	
	List<? extends ResourceHandle<FluidVariant>> getFluids();
	
	default long insert(ResourceAmount<?> stack, boolean simulate){
		if(stack.resource() instanceof ItemVariant)
			return insertItem((ResourceAmount<ItemVariant>)stack, simulate);
		else if(stack.resource() instanceof FluidVariant)
			return insertFluid((ResourceAmount<FluidVariant>)stack, simulate);
		throw new IllegalArgumentException();
	}
	
	long insertItem(ResourceAmount<ItemVariant> stack, boolean simulate);
	
	long insertFluid(ResourceAmount<FluidVariant> stack, boolean simulate);
}