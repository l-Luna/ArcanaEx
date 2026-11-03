package arcana.blocks.courier.symbols;

import arcana.blocks.courier.SymbolContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import java.util.Optional;

// for interfacing with modded fluid storage or weirder containers
public class InventoryStorageProvider implements FabricBackedSymbolStorage.StorageProvider{
	
	public Optional<Storage<ItemVariant>> getItemStorage(SymbolContext ctx){
		return ctx.getSingleBlock()
				.flatMap(pos -> Optional.ofNullable(ItemStorage.SIDED.find(ctx.world(), pos, ctx.symbolFacing().getOpposite())));
	}
	
	public Optional<Storage<FluidVariant>> getFluidStorage(SymbolContext ctx){
		return ctx.getSingleBlock()
				.flatMap(pos -> Optional.ofNullable(FluidStorage.SIDED.find(ctx.world(), pos, ctx.symbolFacing().getOpposite())));
	}
}