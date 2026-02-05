package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.ItemAspectRegistry;
import arcana.blocks.ArcaneFurnaceBlock;
import arcana.screens.ArcaneFurnaceScreen;
import arcana.util.SidedArrayInventory;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArcaneFurnaceBlockEntity extends BlockEntity implements NamedScreenHandlerFactory{
	
	private static final Text title = Text.translatable("container.crafting");
	
	public static final int capacity = 100;
	public static final int maxAlembics = 4;
	
	public SidedArrayInventory inventory = new SidedArrayInventory(4)
			.withSidedSlots(Direction.SOUTH, 0)
			.withSidedSlots(Direction.WEST, 1)
			.withSidedSlots(Direction.EAST, 2)
			.withSidedSlots(Direction.DOWN, 3)
			.withNonInsertableSlots(3);
	public AspectMap aspects = new AspectMap();
	public int burnTime, maxBurnTime, progress, maxProgress, substrateAmount, maxSubstrateAmount, substrateColour;
	public float substrateResidualBurn;
	
	public final PropertyDelegate propertyDelegate = new PropertyDelegate(){
		// [burn time, max burn time, substrate amount, max substrate amount, substrate colour, progress, max progress, aspect total]
		public int get(int index){
			return switch(index){
				case 0 -> burnTime;
				case 1 -> maxBurnTime;
				case 2 -> substrateAmount;
				case 3 -> maxSubstrateAmount;
				case 4 -> substrateColour;
				case 5 -> progress;
				case 6 -> maxProgress;
				case 7 -> aspects.total();
				default -> -1;
			};
		}
		
		public void set(int index, int value){
			switch(index){
				case 0 -> burnTime = value;
				case 1 -> maxBurnTime = value;
				case 2 -> substrateAmount = value;
				case 3 -> maxSubstrateAmount = value;
				case 4 -> substrateColour = value;
				case 5 -> progress = value;
				case 6 -> maxProgress = value;
				// can't set 7...
			}
		}
		
		public int size(){
			return 8;
		}
	};
	
	public ArcaneFurnaceBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.ARCANE_FURNACE_BE, pos, state);
		inventory.addListener(sender -> markDirty());
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("material", inventory.toNbtList());
		nbt.put("aspects", aspects.toNbt());
		
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("maxBurnTime", maxBurnTime);
		nbt.putInt("progress", progress);
		nbt.putInt("substrateAmount", substrateAmount);
		nbt.putInt("maxSubstrateAmount", substrateAmount);
		nbt.putInt("substrateColour", substrateColour);
		nbt.putFloat("substrateResidualBurn", substrateResidualBurn);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE));
		aspects = AspectMap.fromNbt(nbt.getCompound("aspects"));
		
		burnTime = nbt.getInt("burnTime");
		maxBurnTime = nbt.getInt("maxBurnTime");
		progress = nbt.getInt("progress");
		substrateAmount = nbt.getInt("substrateAmount");
		maxSubstrateAmount = nbt.getInt("maxSubstrateAmount");
		substrateColour = nbt.getInt("substrateColour");
		substrateResidualBurn = nbt.getFloat("substrateResidualBurn");
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, ArcaneFurnaceBlockEntity furnace){
		// no material? no problem
		ItemStack material = furnace.inventory.getStack(0);
		ItemStack husks = furnace.inventory.getStack(3);
		AspectMap mAspects = ItemAspectRegistry.get(material);
		AspectMap fAspects = furnace.aspects;
		boolean canActivate = !material.isEmpty()
				&& !material.isIn(ArcanaTags.ARCANE_FURNACE_BLACKLIST)
				&& !mAspects.isEmpty()
				&& husks.getCount() < husks.getMaxCount()
				&& mAspects.total() + fAspects.total() <= capacity;
		if(!canActivate)
			furnace.progress = 0;
		else{
			int total = mAspects.total();
			furnace.maxProgress = total;
			// if we have enough progress and space, consume the item and add the aspects
			if(furnace.progress >= total){
				material.decrement(1);
				if(husks.isEmpty())
					furnace.inventory.setStack(3, new ItemStack(ArcanaRegistry.SHATTERED_HUSK));
				else
					husks.increment(1);
				furnace.progress = 0;
				fAspects.add(mAspects);
			}
			// if we have burn time and substrate, increase progress
			if(furnace.burnTime > 0 && furnace.substrateAmount > 0){
				// for items with few aspects (<20), set burn rate to 0.5
				float burnRate = Math.max(0.5f, ((float)total)/40f) + furnace.substrateResidualBurn;
				furnace.substrateResidualBurn = burnRate % 1;
				int actualRate = Math.min((int)burnRate, furnace.substrateAmount);
				furnace.progress += actualRate;
				furnace.substrateAmount -= actualRate;
			}
		}
		// got burn time? no problem
		boolean wasBurning = furnace.burnTime > 0;
		if(wasBurning)
			furnace.burnTime--;
		// try to use new fuel...
		if(furnace.burnTime <= 0 && canActivate){
			ItemStack fuel = furnace.inventory.getStack(1);
			if(!fuel.isEmpty() && FuelRegistry.INSTANCE.get(fuel.getItem()) != null){
				furnace.burnTime = furnace.maxBurnTime = FuelRegistry.INSTANCE.get(fuel.getItem());
				fuel.decrement(1);
			}else if(furnace.progress > 0)
				furnace.progress--;
		}
		boolean isBurning = furnace.burnTime > 0;
		// look the part!
		if(wasBurning != isBurning){
			state = state.with(ArcaneFurnaceBlock.ON, isBurning);
			world.setBlockState(pos, state, Block.NOTIFY_ALL);
		}
		// substrates are similar; it's depleted with progress and not with time though
		if(furnace.substrateAmount <= 0 && canActivate){
			ItemStack substrate = furnace.inventory.getStack(2);
			if(!substrate.isEmpty() && ArcaneFurnaceBlock.substrateTimes.containsKey(substrate.getItem())){
				ArcaneFurnaceBlock.SubstrateData data = ArcaneFurnaceBlock.substrateTimes.get(substrate.getItem());
				furnace.substrateAmount = furnace.maxSubstrateAmount = data.amount();
				furnace.substrateColour = data.colour();
				substrate.decrement(1);
			}else if(furnace.progress > 0)
				furnace.progress--;
		}
		
		// and while we're here, *we'll* handle alembics ourselves
		if(!fAspects.isEmpty()){
			// when giving out a new aspect, prefer to give the largest aspect that hasn't been seen
			List<Aspect> preferred = fAspects.asStacks()
					.stream().sorted(Comparator.comparingInt(AspectStack::amount).reversed())
					.map(AspectStack::type)
					.collect(Collectors.toCollection(ArrayList::new));
			for(int i = 0; i < maxAlembics; i++){
				BlockEntity there = world.getBlockEntity(pos.up(i + 1));
				if(there instanceof AlembicBlockEntity alembic){
					// if it already has a stack, just try to add to it from what we have
					if(alembic.stored != null){
						Aspect type = alembic.stored.type();
						preferred.remove(type);
						if(fAspects.contains(type) && alembic.stored.amount() < AlembicBlockEntity.capacity){
							fAspects.take(type, 1);
							alembic.stored = new AspectStack(type, alembic.stored.amount() + 1);
							alembic.markDirty();
						}
					}else{
						Aspect toGive;
						if(!preferred.isEmpty())
							toGive = preferred.remove(0);
						else if(!fAspects.isEmpty())
							toGive = Objects.requireNonNull(fAspects.aspectByIndex(0));
						else break; // we have no more aspects to even give out
						
						// we definitely have this aspect in non-zero capacity, don't worry
						fAspects.take(toGive, 1);
						alembic.stored = new AspectStack(toGive, 1);
						alembic.markDirty();
					}
				}else break; // gap in the line of alembics
			}
		}
		
		// basically everything changes the thing's state
		// TODO: don't do this, maybe use setters?
		furnace.markDirty();
	}
	
	public Text getDisplayName(){
		return title;
	}
	
	@Nullable
	public ScreenHandler createMenu(int syncId, PlayerInventory pInv, PlayerEntity player){
		return new ArcaneFurnaceScreen.Handler(syncId, pInv, inventory, propertyDelegate);
	}
	
	public void markDirty(){
		super.markDirty();
		if(world instanceof ServerWorld sw)
			sw.getChunkManager().markForUpdate(pos);
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(){
		return createNbt();
	}
}