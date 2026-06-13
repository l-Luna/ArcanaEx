package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.blocks.ResearchTableBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ResearchTableBlockEntity extends BlockEntity{
	
	// TODO: use ArrayInventory
	public SimpleInventory scribingTools = new SimpleInventory(1), note = new SimpleInventory(1);
	
	public ResearchTableBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.RESEARCH_TABLE_BE, pos, state);
		scribingTools.addListener(inv -> {
			World w = getWorld();
			if(w != null && w.getBlockState(getPos()).isOf(ArcanaRegistry.RESEARCH_TABLE))
				w.setBlockState(getPos(), w.getBlockState(getPos()).with(ResearchTableBlock.hasInk, !inv.isEmpty()));
			markDirty();
		});
		note.addListener(__ -> markDirty());
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.writeNbt(nbt, registryLookup);
		nbt.put("scribingTools", scribingTools.getStack(0).encode(registryLookup));
		nbt.put("note", note.getStack(0).encode(registryLookup));
	}
	
	public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
		super.readNbt(nbt, registryLookup);
		scribingTools.setStack(0, ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("scribingTools")));
		note.setStack(0, ItemStack.fromNbtOrEmpty(registryLookup, nbt.getCompound("note")));
	}
}