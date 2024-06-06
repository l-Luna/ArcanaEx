package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.aspects.ItemAspectRegistry;
import arcana.blocks.ArcaneFurnaceBlock;
import arcana.screens.ArcaneFurnaceScreenHandler;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ArcaneFurnaceBlockEntity extends BlockEntity implements NamedScreenHandlerFactory{
	
	private static final Text title = Text.translatable("container.crafting");
	
	public static final int capacity = 100;
	
	public SimpleInventory material = new SimpleInventory(1),
			fuel = new SimpleInventory(1),
			substrate = new SimpleInventory(1),
			husks = new SimpleInventory(1);
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
		material.addListener(sender -> markDirty());
		fuel.addListener(sender -> markDirty());
		substrate.addListener(sender -> markDirty());
		husks.addListener(sender -> markDirty());
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("material", material.getStack(0).writeNbt(new NbtCompound()));
		nbt.put("fuel", fuel.getStack(0).writeNbt(new NbtCompound()));
		nbt.put("substrate", substrate.getStack(0).writeNbt(new NbtCompound()));
		nbt.put("husks", husks.getStack(0).writeNbt(new NbtCompound()));
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
		material.setStack(0, ItemStack.fromNbt(nbt.getCompound("material")));
		fuel.setStack(0, ItemStack.fromNbt(nbt.getCompound("fuel")));
		substrate.setStack(0, ItemStack.fromNbt(nbt.getCompound("substrate")));
		husks.setStack(0, ItemStack.fromNbt(nbt.getCompound("husks")));
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
		ItemStack material = furnace.material.getStack(0);
		ItemStack husks = furnace.husks.getStack(0);
		AspectMap mAspects = ItemAspectRegistry.get(material);
		boolean canActivate = !material.isEmpty()
				&& !mAspects.isEmpty()
				&& husks.getCount() < husks.getMaxCount()
				&& mAspects.total() + furnace.aspects.total() <= capacity;
		if(!canActivate)
			furnace.progress = 0;
		else{
			int total = mAspects.total();
			furnace.maxProgress = total;
			// if we have enough progress and space, consume the item and add the aspects
			if(furnace.progress >= total){
				material.decrement(1);
				if(husks.isEmpty())
					furnace.husks.setStack(0, new ItemStack(ArcanaRegistry.SHATTERED_HUSK));
				else
					husks.increment(1);
				furnace.progress = 0;
				furnace.aspects.add(mAspects);
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
		if(furnace.burnTime > 0)
			furnace.burnTime--;
		// try to use new fuel...
		if(furnace.burnTime <= 0 && canActivate){
			ItemStack fuel = furnace.fuel.getStack(0);
			if(!fuel.isEmpty()){
				furnace.burnTime = furnace.maxBurnTime = FuelRegistry.INSTANCE.get(fuel.getItem());
				fuel.decrement(1);
			}else if(furnace.progress > 0)
				furnace.progress--;
		}
		// substrates are similar; it's depleted with progress and not with time though
		if(furnace.substrateAmount <= 0 && canActivate){
			ItemStack substrate = furnace.substrate.getStack(0);
			if(!substrate.isEmpty() && ArcaneFurnaceBlock.substrateTimes.containsKey(substrate.getItem())){
				ArcaneFurnaceBlock.SubstrateData data = ArcaneFurnaceBlock.substrateTimes.get(substrate.getItem());
				furnace.substrateAmount = furnace.maxSubstrateAmount = data.amount();
				furnace.substrateColour = data.colour();
				substrate.decrement(1);
			}else if(furnace.progress > 0)
				furnace.progress--;
		}
		
		// basically everything changes the thing's state
		furnace.markDirty();
	}
	
	public Text getDisplayName(){
		return title;
	}
	
	@Nullable
	public ScreenHandler createMenu(int syncId, PlayerInventory pInv, PlayerEntity player){
		return new ArcaneFurnaceScreenHandler(syncId, pInv, material, fuel, substrate, husks, propertyDelegate);
	}
}