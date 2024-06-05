package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.screens.ArcaneFurnaceScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class ArcaneFurnaceBlockEntity extends BlockEntity implements NamedScreenHandlerFactory{
	
	private static final Text title = Text.translatable("container.crafting");
	
	public SimpleInventory material = new SimpleInventory(1),
			fuel = new SimpleInventory(1),
			substrate = new SimpleInventory(1),
			husks = new SimpleInventory(1);
	public AspectMap aspects = new AspectMap();
	
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
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		material.setStack(0, ItemStack.fromNbt(nbt.getCompound("material")));
		fuel.setStack(0, ItemStack.fromNbt(nbt.getCompound("fuel")));
		substrate.setStack(0, ItemStack.fromNbt(nbt.getCompound("substrate")));
		husks.setStack(0, ItemStack.fromNbt(nbt.getCompound("husks")));
		aspects = AspectMap.fromNbt(nbt.getCompound("aspects"));
	}
	
	public Text getDisplayName(){
		return title;
	}
	
	@Nullable
	public ScreenHandler createMenu(int syncId, PlayerInventory pInv, PlayerEntity player){
		return new ArcaneFurnaceScreenHandler(syncId, pInv, material, fuel, substrate, husks);
	}
}