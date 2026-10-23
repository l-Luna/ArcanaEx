package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.items.TomeOfSharingItem;
import arcana.recipes.alchemy.AlchemyInput;
import arcana.screens.AwareCrucibleScreen;
import arcana.util.ArrayInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class AwareCrucibleBlockEntity extends CrucibleBlockEntity implements NamedScreenHandlerFactory{
	
	public final ArrayInventory bookInventory = new ArrayInventory(1);
	private int signalTicks = 0;
	
	public AwareCrucibleBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.AWARE_CRUCIBLE_BE, pos, state);
	}
	
	public void tick(){
		super.tick();
		if(signalTicks > 0){
			signalTicks--;
			markDirty();
		}
	}
	
	protected boolean isValidCrafter(@Nullable PlayerEntity thrower){
		ItemStack bookStack = bookInventory.getStack(0);
		return !bookStack.isEmpty() && bookStack.isOf(ArcanaRegistry.TOME_OF_SHARING);
	}
	
	protected AlchemyInput createInput(ItemStack reagent, @Nullable PlayerEntity thrower){
		return new AlchemyInput(this, reagent, TomeOfSharingItem.getBoundResearch(bookInventory.getStack(0)));
	}
	
	protected void outputStack(ItemStack result, @Nullable PlayerEntity thrower){
		signalTicks = 4;
		markDirty();
		
		Random rng = getWorld().getRandom();
		// first, try to push to inventories directly
		for(Direction dir : Direction.shuffle(rng))
			if(dir.getAxis().isHorizontal()){
				BlockPos there = getPos().offset(dir);
				Inventory target = HopperBlockEntity.getInventoryAt(getWorld(), there);
				if(target != null)
					result = HopperBlockEntity.transfer(null, target, result, null);
				if(result.isEmpty())
					return;
			}
		// then, throw the excess randomly
		ItemEntity product = new ItemEntity(getWorld(), getPos().getX() + .5, getPos().getY() + 1, getPos().getZ() + .5, result);
		product.setVelocity(rng.nextGaussian() / 10, 0.2, rng.nextGaussian() / 10);
		for(Direction dir : Direction.shuffle(rng))
			if(dir.getAxis().isHorizontal()){
				BlockPos offset = getPos().offset(dir).up();
				if(!getWorld().getBlockState(offset).isSolidBlock(getWorld(), offset)){
					product.setVelocity(dir.getOffsetX() * 0.1, 0.2, dir.getOffsetZ() * 0.1);
					break;
				}
			}
		getWorld().spawnEntity(product);
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		super.writeNbt(nbt, lookup);
		nbt.put("inventory", bookInventory.toNbtList(lookup));
		nbt.putInt("signal_ticks", signalTicks);
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		super.readNbt(nbt, lookup);
		bookInventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE), lookup);
		signalTicks = nbt.getInt("signal_ticks");
	}
	
	public Text getDisplayName(){
		return Text.empty();
	}
	
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player){
		return new AwareCrucibleScreen.Handler(syncId, playerInventory, bookInventory);
	}
	
	public int getSignalTicks(){
		return signalTicks;
	}
}