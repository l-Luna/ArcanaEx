package arcana.blocks.courier;

import arcana.ArcanaRegistry;
import arcana.blocks.courier.SymbolConfig.SymbolDirection;
import arcana.blocks.courier.symbols.EmptySymbolStorage;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourierBlock extends BlockWithEntity{
	
	private static final int STATE_TIME = 14;
	
	private static final MapCodec<CourierBlock> CODEC = createCodec(CourierBlock::new);
	
	public CourierBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	protected BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new BE(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return BlockWithEntity.validateTicker(type, ArcanaRegistry.COURIER_BE, (_1, _2, _3, be) -> be.tick());
	}
	
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		super.onStateReplaced(state, world, pos, newState, moved);
		if(!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof BE be)
			be.onRemoved();
	}
	
	public static class BE extends BlockEntity{
		
		// symbols
		private List<SymbolInstance> symbols = new ArrayList<>();
		// reloaded from `symbols` whenever symbol configs are updated
		private List<SymbolInstance> sourceSymbols = new ArrayList<>();
		private List<SymbolInstance> sinkSymbols = new ArrayList<>();
		
		// transfer progress
		private QueuedTransfer<?> currentTransfer;
		private CourierState state = CourierState.SLEEP;
		private int stateTicks;
		
		// reloading
		private List<BlockPos> delayedSymbols = new ArrayList<>();
		private int delayedTransferSourceIdx = -1, delayedTransferSinkIdx, delayedTransferHandleIdx;
		private boolean delayedTransferIsFluid;
		private ResourceAmount<?> delayedTransferStack;
		
		public BE(BlockPos pos, BlockState state){
			super(ArcanaRegistry.COURIER_BE, pos, state);
		}
		
		public CourierState getState(){
			return state;
		}
		
		public int getStateTicks(){
			return stateTicks;
		}
		
		public QueuedTransfer<?> getCurrentTransfer(){
			return currentTransfer;
		}
		
		public SymbolInstance addSymbolFromBlock(ControlSymbolBlock.BE symbolBe, BlockState symbolState){
			BlockPos pos = symbolBe.getPos();
			SymbolInstance symInst = new SymbolInstance();
			symInst.pos = pos;
			SymbolStorage storage = symbolBe.symbolType.storage().apply(new SymbolContext(world, symbolState.get(ControlSymbolBlock.FACING), symbolBe.relativeVolume.offset(pos.getX(), pos.getY(), pos.getZ())));
			symInst.storage = storage != null ? storage : new EmptySymbolStorage();
			symInst.config = symbolBe.config;
			symInst.lastVersion = -1;
			addSymbol(symInst);
			return symInst;
		}
		
		// could be made public for addons, but would need extra work in persistence
		private void addSymbol(SymbolInstance symbol){
			symbols.add(symbol);
			updateSymbol(symbol);
		}
		
		public void updateSymbol(SymbolInstance symbol){
			sourceSymbols.remove(symbol);
			sinkSymbols.remove(symbol);
			(symbol.config.direction == SymbolDirection.SOURCE ? sourceSymbols : sinkSymbols).add(symbol);
			markDirtyAndSync();
		}
		
		public void removeSymbol(SymbolInstance symbol){
			symbols.remove(symbol);
			sourceSymbols.remove(symbol);
			sinkSymbols.remove(symbol);
			markDirtyAndSync();
			// TODO: handle removing symbol involved in transfer
		}
		
		public List<SymbolInstance> getSymbols(){
			return symbols;
		}
		
		//
		
		protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
			super.writeNbt(nbt, registryLookup);
			nbt.putLongArray("symbol_positions", symbols.stream().mapToLong(x -> x.pos.asLong()).toArray());
			if(currentTransfer != null){
				NbtCompound transferTag = new NbtCompound();
				transferTag.putInt("source_idx", symbols.indexOf(currentTransfer.source()));
				transferTag.putInt("sink_idx", symbols.indexOf(currentTransfer.sink()));
				transferTag.putInt("handle_idx", currentTransfer.sourceHandleIdx());
				TransferVariant<?> resource = currentTransfer.stack().resource();
				transferTag.putBoolean("is_fluid", resource instanceof FluidVariant);
				// in case the handle doesn't exist (on client, or already picked up)
				if(resource instanceof ItemVariant iv){
					transferTag.put("stack", iv.toStack((int)currentTransfer.stack().amount()).encode(registryLookup));
				}else if(resource instanceof FluidVariant fv){
					transferTag.putString("fluid_type", fv.getRegistryEntry().getIdAsString());
					transferTag.putLong("fluid_amount", currentTransfer.stack().amount());
				}
				nbt.put("delayed_transfer", transferTag);
			}
			nbt.putString("state", state.name().toLowerCase(Locale.ROOT));
			nbt.putInt("state_ticks", stateTicks);
		}
		
		protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
			super.readNbt(nbt, registryLookup);
			boolean syncing = nbt.getBoolean("syncing");
			
			delayedSymbols.clear();
			for(long posLong : nbt.getLongArray("symbol_positions"))
				delayedSymbols.add(BlockPos.fromLong(posLong));
			
			// if reloading symbols on client, avoid recreating symbol instances unless actually changed
			// the server should send the same order if they haven't
			boolean symbolsUnchanged = symbols.size() == delayedSymbols.size();
			if(symbolsUnchanged)
				for(int i = 0; i < symbols.size(); i++)
					if(!symbols.get(i).pos.equals(delayedSymbols.get(i))){
						symbolsUnchanged = false;
						break;
					}
			// but do load them immediately (instead of waiting for the first tick) if there ever was any
			if(!symbolsUnchanged){
				symbols.clear();
				if(syncing)
					loadDelayedSymbols();
			}else
				delayedSymbols.clear();
			
			if(nbt.contains("delayed_transfer", NbtElement.COMPOUND_TYPE)){
				NbtCompound transferTag = nbt.getCompound("delayed_transfer");
				delayedTransferSourceIdx = transferTag.getInt("source_idx");
				delayedTransferSinkIdx = transferTag.getInt("sink_idx");
				delayedTransferHandleIdx = transferTag.getInt("handle_idx");
				delayedTransferIsFluid = transferTag.getBoolean("is_fluid");
				if(!delayedTransferIsFluid){
					ItemStack stack = ItemStack.fromNbtOrEmpty(registryLookup, transferTag.getCompound("stack"));
					delayedTransferStack = new ResourceAmount<>(ItemVariant.of(stack), stack.getCount());
				}else{
					delayedTransferStack = new ResourceAmount<>(
							FluidVariant.of(Registries.FLUID.get(Identifier.of(transferTag.getString("fluid_type")))),
							transferTag.getLong("fluid_amount")
					);
				}
				if(syncing)
					loadDelayedTransfer(true);
			}else
				delayedTransferSourceIdx = -1;
			state = CourierState.valueOf(nbt.getString("state").toUpperCase(Locale.ROOT));
			stateTicks = nbt.getInt("state_ticks");
		}
		
		private void markDirtyAndSync(){
			markDirty();
			if(world instanceof ServerWorld sw)
				sw.getChunkManager().markForUpdate(pos);
		}
		
		public Packet<ClientPlayPacketListener> toUpdatePacket(){
			return BlockEntityUpdateS2CPacket.create(this, (__, registryLookup) -> toClientUpdateNbt(registryLookup));
		}
		
		public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup){
			return createNbt(registryLookup);
		}
		
		private NbtCompound toClientUpdateNbt(RegistryWrapper.WrapperLookup registryLookup){
			NbtCompound nbt = createNbt(registryLookup);
			nbt.putBoolean("syncing", true);
			return nbt;
		}
		
		//
		
		private void tick(){
			stateTicks++;
			markDirty();
			if(world.isClient)
				return;
			
			// wait until first tick to load symbols
			loadDelayedSymbols();
			loadDelayedTransfer(false);
			
			// check after a transfer is made, or if versions update
			boolean shouldCheck = false, justFinished = false;
			
			// continue current transfer
			if(stateTicks >= STATE_TIME && state != CourierState.SLEEP){
				switch(state){
					case SPIN_UP -> state = CourierState.MOVE_TO_SOURCE;
					case MOVE_TO_SOURCE -> {
						if(!currentTransfer.sourceValid())
							justFinished = true;
						else
							currentTransfer.sourceHandle().take(currentTransfer.stack().amount(), false);
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
				markDirtyAndSync();
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
			
			CourierState oldState = state;
			if(currentTransfer == null)
				state = CourierState.SLEEP;
			else if(justFinished)
				state = CourierState.MOVE_TO_SOURCE;
			else if(state == CourierState.SLEEP)
				state = CourierState.SPIN_UP;
			if(state != oldState)
				markDirtyAndSync();
		}
		
		private void loadDelayedSymbols(){
			if(!delayedSymbols.isEmpty()){
				for(BlockPos blockPos : delayedSymbols)
					if(world.getBlockEntity(blockPos) instanceof ControlSymbolBlock.BE symbolBe){
						symbolBe.instance = addSymbolFromBlock(symbolBe, world.getBlockState(blockPos));
						symbolBe.linkedCourierPos = pos;
					}
				delayedSymbols.clear();
				markDirtyAndSync();
			}
		}
		
		@SuppressWarnings({"rawtypes", "unchecked"}) // avoid splitting the method in two for items/fluids, when everything becomes ? anyways
		private void loadDelayedTransfer(boolean client){
			if(delayedTransferSourceIdx != -1){
				SymbolInstance source = symbols.get(delayedTransferSourceIdx), sink = symbols.get(delayedTransferSinkIdx);
				boolean getHandle = !client && (state == CourierState.SPIN_UP || state == CourierState.MOVE_TO_SOURCE);
				SymbolStorage.ResourceHandle handle = getHandle
						? (delayedTransferIsFluid ? source.storage.getFluids() : source.storage.getItems()).get(delayedTransferHandleIdx)
						: new SymbolStorage.DummyResourceHandle(delayedTransferStack);
				currentTransfer = new QueuedTransfer(source, handle, delayedTransferHandleIdx, sink, delayedTransferStack);
				delayedTransferSourceIdx = -1;
				if(!client)
					markDirtyAndSync();
			}
		}
		
		private void onRemoved(){
			for(SymbolInstance symbol : symbols)
				if(world.getBlockEntity(symbol.pos) instanceof ControlSymbolBlock.BE symbolBe){
					symbolBe.instance = null;
					symbolBe.linkedCourierPos = null;
					symbolBe.markDirty();
				}
		}
		
		//
		
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
			return tryQueueTransferFrom(source, sink, source.storage.getItems()) || tryQueueTransferFrom(source, sink, source.storage.getFluids());
		}
		
		private <T extends TransferVariant<?>> boolean tryQueueTransferFrom(SymbolInstance source, SymbolInstance sink, List<? extends SymbolStorage.ResourceHandle<T>> from){
			for(int i = 0; i < from.size(); i++){
				SymbolStorage.ResourceHandle<T> handle = from.get(i);
				ResourceAmount<T> stack = handle.getStack();
				long inserted = sink.storage.insert(stack, true);
				if(inserted == stack.amount()){
					// transfer the full amount
					currentTransfer = new QueuedTransfer<>(source, handle, i, sink, stack);
					return true;
				}else if(inserted > 0){
					// otherwise, try to extract the max permitted amount
					// which might not be possible with e.g. non-splittable fluid containers like buckets
					if(handle.take(inserted, true) == inserted)
						currentTransfer = new QueuedTransfer<>(source, handle, i, sink, new ResourceAmount<>(stack.resource(), inserted));
				}
			}
			return false;
		}
	}
}