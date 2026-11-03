package arcana.blocks.courier.symbols;

import arcana.blocks.courier.SymbolStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

import java.util.List;

public class VoidingSymbolStorage implements SymbolStorage{
	
	public long version(){
		return 0;
	}
	
	public List<? extends ResourceHandle<ItemVariant>> getItems(){
		return List.of();
	}
	
	public List<? extends ResourceHandle<FluidVariant>> getFluids(){
		return List.of();
	}
	
	public long insertItem(ResourceAmount<ItemVariant> stack, boolean simulate){
		return stack.amount();
	}
	
	public long insertFluid(ResourceAmount<FluidVariant> stack, boolean simulate){
		return stack.amount();
	}
}