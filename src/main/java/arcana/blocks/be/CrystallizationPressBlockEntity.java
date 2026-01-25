package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.api.AspectIo;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.items.CrystalItem;
import arcana.screens.CrystallizationPressScreen;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CrystallizationPressBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, AspectIo{
	
	public static final int maxProgress = 20 * 6;
	public static final int maxQuartzLevel = 12;
	public static final int capacity = 40;
	
	public SimpleInventory quartz = new SimpleInventory(1), output = new SimpleInventory(1);
	public AspectStack stored = null;
	public int quartzLevel;
	public int progress;
	
	public final PropertyDelegate propertyDelegate = new PropertyDelegate(){
		// [quartz amount, essentia amount, essentia colour, progress]
		public int get(int index){
			return switch(index){
				case 0 -> quartzLevel;
				case 1 -> stored != null ? stored.amount() : 0;
				case 2 -> stored != null ? stored.type().colour() : 0;
				case 3 -> progress;
				default -> -1;
			};
		}
		
		public void set(int index, int value){
			switch(index){
				case 0 -> quartzLevel = value;
				case 3 -> progress = value;
				// can't set 1/2...
			}
		}
		
		public int size(){
			return 4;
		}
	};
	
	public CrystallizationPressBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.CRYSTALLIZATION_PRESS_BE, pos, state);
		quartz.addListener(__ -> markDirty());
		output.addListener(__ -> markDirty());
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, CrystallizationPressBlockEntity press){
		// do we have quartz (item) to consume?
		ItemStack quartzStack = press.quartz.getStack(0);
		if(press.quartzLevel <= 0 && !quartzStack.isEmpty() && quartzStack.isOf(Items.QUARTZ)){
			quartzStack.decrement(1);
			press.quartzLevel = maxQuartzLevel;
			
			press.markDirty();
		}
		
		// do we have *both* essentia and quartz (substance) to consume, and space in the output?
		AspectStack aspects = press.stored;
		ItemStack outputStack = press.output.getStack(0);
		if(aspects != null
			&& aspects.amount() >= 2
			&& press.quartzLevel > 0
			&& (outputStack.isEmpty()
				|| (outputStack.getItem() instanceof CrystalItem ci
					&& ci.getAspect().equals(aspects.type())
					&& outputStack.getCount() < outputStack.getMaxCount()))){
			press.progress++;
			if(press.progress >= maxProgress){
				press.progress = 0;
				press.stored = AspectStack.draw(aspects, 2).getLeft();
				press.quartzLevel--;
				if(outputStack.isEmpty())
					press.output.setStack(0, new ItemStack(Aspects.crystals.get(aspects.type())));
				else
					outputStack.increment(1);
			}
			
			press.markDirty();
		}else if(press.progress > 0){
			press.progress -= 3;
			if(press.progress < 0)
				press.progress = 0;
			
			press.markDirty();
		}
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		
		nbt.put("quartz", quartz.getStack(0).writeNbt(new NbtCompound()));
		nbt.put("output", output.getStack(0).writeNbt(new NbtCompound()));
		
		if(stored != null)
			nbt.put("stored", stored.toNbt());
		
		nbt.putInt("quartzLevel", quartzLevel);
		nbt.putInt("progress", progress);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		
		quartz.setStack(0, ItemStack.fromNbt(nbt.getCompound("quartz")));
		output.setStack(0, ItemStack.fromNbt(nbt.getCompound("output")));
		
		if(nbt.contains("stored"))
			stored = AspectStack.fromNbt(nbt.getCompound("stored"));
		else stored = null;
		
		quartzLevel = nbt.getInt("quartzLevel");
		progress = nbt.getInt("progress");
	}
	
	public Text getDisplayName(){
		return Text.translatable("block.arcana.crystallization_press");
	}
	
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player){
		return new CrystallizationPressScreen.Handler(syncId, inv, quartz, output, propertyDelegate);
	}
	
	public @Nullable AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		// standard jar code
		var result = AspectStack.mergeWithCapacity(stored, stack, capacity);
		if(!result.getLeft().equals(stored)){
			stored = result.getLeft();
			markDirty();
		}
		return result.getRight();
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return null; // no returns, sorry!
	}
}