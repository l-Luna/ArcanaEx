package arcana.blocks.courier;

import arcana.ArcanaRegistry;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.List;

public class CourierBlock extends BlockWithEntity{
	
	private static final int STATE_TIME = 14;
	
	private static final MapCodec<CourierBlock> CODEC = createCodec(CourierBlock::new);
	
	public CourierBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new BE(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return BlockWithEntity.validateTicker(type, ArcanaRegistry.COURIER_BE, (_1, _2, _3, be) -> be.tick());
	}
	
	public static class BE extends BlockEntity{
		
		// TODO: populate & serialise
		private List<SymbolInstance> sourceSymbols = new ArrayList<>();
		private List<SymbolInstance> sinkSymbols = new ArrayList<>();
		
		private QueuedTransfer<?> currentTransfer;
		private CourierState state = CourierState.SLEEP;
		private int stateTicks;
		
		public BE(BlockPos pos, BlockState state){
			super(ArcanaRegistry.COURIER_BE, pos, state);
		}
		
		public CourierState getState(){
			return state;
		}
		
		//
		
		protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
			super.readNbt(nbt, registryLookup);
		}
		
		protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
			super.writeNbt(nbt, registryLookup);
		}
		
		//
		
		private void tick(){
			// check after a transfer is made, or if versions update
			boolean shouldCheck = false, justFinished = false;
			
			// continue current transfer
			stateTicks++;
			if(stateTicks >= STATE_TIME){
				switch(state){
					case SPIN_UP -> state = CourierState.MOVE_TO_SOURCE;
					case MOVE_TO_SOURCE -> {
						if(!currentTransfer.sourceValid())
							justFinished = true;
						else
							currentTransfer.source().take(currentTransfer.stack().amount(), false);
						state = CourierState.PICKUP;
					}
					case PICKUP -> state = CourierState.MOVE_TO_SINK;
					case MOVE_TO_SINK -> state = CourierState.DROP;
					case DROP -> {
						// try to insert
						if(currentTransfer.stack().resource() instanceof ItemVariant iv){
							long inserted = currentTransfer.sink().storage.insertItem((ResourceAmount<ItemVariant>)currentTransfer.stack(), false);
							long rest = currentTransfer.stack().amount() - inserted;
							// drop the rest
							while(rest > 0){
								BlockPos toPos = currentTransfer.sink().pos;
								int dropped = (int)Math.min(rest, iv.toStack().getMaxCount());
								ItemEntity entity = new ItemEntity(world, toPos.getX(), toPos.getY(), toPos.getZ(), iv.toStack(dropped));
								world.spawnEntity(entity);
								world.emitGameEvent(entity, GameEvent.ENTITY_PLACE, toPos);
								rest -= dropped;
							}
						}else if(currentTransfer.stack().resource() instanceof FluidVariant){
							currentTransfer.sink().storage.insertFluid((ResourceAmount<FluidVariant>)currentTransfer.stack(), false);
							// rest is voided, no sensible way to drop it
						}
						// immediately check for a next transfer
						justFinished = true;
					}
				}
				stateTicks = 0;
			}
			
			if(justFinished){
				currentTransfer = null;
				shouldCheck = true;
			}
			
			// no point checking if in the middle of a transfer
			if(state == CourierState.SLEEP){
				for(SymbolInstance symbol : sourceSymbols){
					long newVersion = symbol.storage.version();
					if(newVersion != symbol.lastVersion){
						symbol.lastVersion = newVersion;
						shouldCheck = true;
					}
				}
				for(SymbolInstance symbol : sinkSymbols){
					long newVersion = symbol.storage.version();
					if(newVersion != symbol.lastVersion){
						symbol.lastVersion = newVersion;
						shouldCheck = true;
					}
				}
			}
			
			if(shouldCheck)
				check();
			
			if(currentTransfer == null)
				state = CourierState.SLEEP;
			else if(justFinished)
				state = CourierState.MOVE_TO_SOURCE;
			else if(state == CourierState.SLEEP)
				state = CourierState.SPIN_UP;
		}
		
		private void check(){
			// scan other sources/sinks to check if a new transfer is possible
			// if forceful, scan *all* others, otherwise just scan forceful others
			for(SymbolInstance source : sourceSymbols)
				for(SymbolInstance sink : sinkSymbols)
					if(source.config.forceful || sink.config.forceful)
						if(tryQueueTransfer(source, sink))
							return;
			
			for(SymbolInstance sink : sinkSymbols)
				for(SymbolInstance source : sourceSymbols)
					if(sink.config.forceful || source.config.forceful)
						if(tryQueueTransfer(source, sink))
							return;
		}
		
		private boolean tryQueueTransfer(SymbolInstance source, SymbolInstance sink){
			return tryQueueTransferFrom(sink, source.storage.getItems()) || tryQueueTransferFrom(sink, source.storage.getFluids());
		}
		
		private <T extends TransferVariant<?>> boolean tryQueueTransferFrom(SymbolInstance sink, List<? extends SymbolStorage.ResourceHandle<T>> from){
			for(SymbolStorage.ResourceHandle<T> handle : from){
				ResourceAmount<T> stack = handle.getStack();
				long inserted = sink.storage.insert(stack, true);
				if(inserted == stack.amount()){
					// transfer the full amount
					currentTransfer = new QueuedTransfer<>(handle, sink, stack);
					return true;
				}else if(inserted > 0){
					// otherwise, try to extract the max permitted amount
					// which might not be possible with e.g. non-splittable fluid containers like buckets
					if(handle.take(inserted, true) == inserted)
						currentTransfer = new QueuedTransfer<>(handle, sink, new ResourceAmount<>(stack.resource(), inserted));
				}
			}
			return false;
		}
	}
}