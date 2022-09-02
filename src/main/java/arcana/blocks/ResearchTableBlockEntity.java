package arcana.blocks;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.BlockPos;

public class ResearchTableBlockEntity extends BlockEntity{
	
	public SimpleInventory inventory = new SimpleInventory(2);
	
	public ResearchTableBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.RESEARCH_TABLE_BE, pos, state);
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("inventory", inventory.toNbtList());
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE));
	}
}