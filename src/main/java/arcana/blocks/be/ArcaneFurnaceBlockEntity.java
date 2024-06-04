package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

public class ArcaneFurnaceBlockEntity extends BlockEntity{
	
	// [material, fuel, substrate, husks]
	public SimpleInventory inventory = new SimpleInventory(4);
	public AspectMap aspects = new AspectMap();
	
	public ArcaneFurnaceBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.ARCANE_FURNACE_BE, pos, state);
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("inventory", inventory.toNbtList());
		nbt.put("aspects", aspects.toNbt());
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE));
		aspects = AspectMap.fromNbt(nbt.getCompound("aspects"));
	}
}