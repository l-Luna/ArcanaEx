package arcana.blocks.be;

import arcana.ArcanaRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class ResearchTableBlockEntity extends BlockEntity{
	
	public SimpleInventory scribingTools = new SimpleInventory(1), note = new SimpleInventory(1);
	
	public ResearchTableBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.RESEARCH_TABLE_BE, pos, state);
		scribingTools.addListener(__ -> markDirty());
		note.addListener(__ -> markDirty());
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("scribingTools", scribingTools.getStack(0).writeNbt(new NbtCompound()));
		nbt.put("note", note.getStack(0).writeNbt(new NbtCompound()));
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		scribingTools.setStack(0, ItemStack.fromNbt(nbt.getCompound("scribingTools")));
		note.setStack(0, ItemStack.fromNbt(nbt.getCompound("note")));
	}
}