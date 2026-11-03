package arcana.blocks.courier.symbols;

import arcana.blocks.be.PedestalBlockEntity;
import arcana.blocks.courier.SymbolContext;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import java.util.Optional;

public class PedestalItemSymbolStorage extends FabricBackedSymbolStorage{
	
	public PedestalItemSymbolStorage(SymbolContext ctx){
		super(new PedestalItemStorageProvider(), ctx);
	}
	
	private static class PedestalItemStorageProvider implements StorageProvider{
		
		public Optional<Storage<ItemVariant>> getItemStorage(SymbolContext ctx){
			return getContainerContext(ctx).map(it -> it.find(ItemStorage.ITEM));
		}
		
		public Optional<Storage<FluidVariant>> getFluidStorage(SymbolContext ctx){
			return getContainerContext(ctx).map(it -> it.find(FluidStorage.ITEM));
		}
		
		private static Optional<ContainerItemContext> getContainerContext(SymbolContext ctx){
			return ctx.getSingleBlock()
					.flatMap(pos -> Optional.ofNullable(ctx.world().getBlockEntity(pos)))
					.flatMap(be -> be instanceof PedestalBlockEntity pd ? Optional.of(pd) : Optional.empty())
					.map(pd -> ContainerItemContext.ofSingleSlot(InventoryStorage.of(pd.getInventory(), null).getSlot(0)));
		}
	}
}