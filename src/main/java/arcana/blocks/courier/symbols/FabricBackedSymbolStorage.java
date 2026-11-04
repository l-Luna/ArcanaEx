package arcana.blocks.courier.symbols;

import arcana.blocks.courier.SymbolContext;
import arcana.blocks.courier.SymbolStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// general fallback for anything fabric-transfer based
public class FabricBackedSymbolStorage implements SymbolStorage{
	
	private final @Nullable Storage<ItemVariant> itemStorage;
	private final @Nullable Storage<FluidVariant> fluidStorage;
	
	public FabricBackedSymbolStorage(StorageProvider provider, SymbolContext ctx){
		itemStorage = provider.getItemStorage(ctx).orElse(null);
		fluidStorage = provider.getFluidStorage(ctx).orElse(null);
	}
	
	public long version(){
		return (itemStorage == null ? 0 : itemStorage.getVersion()) + (fluidStorage == null ? 0 : fluidStorage.getVersion());
	}
	
	public List<ResourceHandle<ItemVariant>> getItems(){
		if(itemStorage == null)
			return List.of();
		// doesn't return a stream oddly
		List<ResourceHandle<ItemVariant>> items = new ArrayList<>();
		for(StorageView<ItemVariant> view : itemStorage.nonEmptyViews())
			items.add(new FabricBackedResourceHandle<>(view));
		return items;
	}
	
	public List<ResourceHandle<FluidVariant>> getFluids(){
		if(fluidStorage == null)
			return List.of();
		List<ResourceHandle<FluidVariant>> items = new ArrayList<>();
		for(StorageView<FluidVariant> view : fluidStorage.nonEmptyViews())
			items.add(new FabricBackedResourceHandle<>(view));
		return items;
	}
	
	public long insertItem(ResourceAmount<ItemVariant> stack, boolean simulate){
		if(itemStorage == null)
			return 0;
		long amountInserted;
		try(Transaction transaction = Transaction.openOuter()){
			amountInserted = itemStorage.insert(stack.resource(), stack.amount(), transaction);
			if(!simulate)
				transaction.commit();
		}
		return amountInserted;
	}
	
	public long insertFluid(ResourceAmount<FluidVariant> stack, boolean simulate){
		if(fluidStorage == null)
			return 0;
		long amountInserted;
		try(Transaction transaction = Transaction.openOuter()){
			amountInserted = fluidStorage.insert(stack.resource(), stack.amount(), transaction);
			if(!simulate)
				transaction.commit();
		}
		return amountInserted;
	}
	
	public interface StorageProvider{
		
		default Optional<Storage<ItemVariant>> getItemStorage(SymbolContext ctx){
			return Optional.empty();
		}
		
		default Optional<Storage<FluidVariant>> getFluidStorage(SymbolContext ctx){
			return Optional.empty();
		}
	}
	
	private record FabricBackedResourceHandle<T extends TransferVariant<?>>(StorageView<T> backing) implements ResourceHandle<T>{
		
		public ResourceAmount<T> getStack(){
			return new ResourceAmount<>(backing.getResource(), backing.getAmount());
		}
		
		public long take(long amount, boolean simulate){
			long amountExtracted;
			try(Transaction transaction = Transaction.openOuter()){
				amountExtracted = backing.extract(backing.getResource(), amount, transaction);
				if(!simulate)
					transaction.commit();
			}
			return amountExtracted;
		}
		
		public boolean valid(){
			// assume it won't stop existing
			return true;
		}
	}
}