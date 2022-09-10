package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.screens.KnowledgeableDropperScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class KnowledgeableDropperBlockEntity extends DispenserBlockEntity{
	
	private final Inventory tomeSlot = new SimpleInventory(1);
	
	public KnowledgeableDropperBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.KNOWLEDGEABLE_DROPPER_BE, pos, state);
	}
	
	protected Text getContainerName(){
		return Text.translatable("container.arcana.knowledgeable_dropper");
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		NbtCompound tomeNbt = new NbtCompound();
		nbt.put("tome", tomeSlot.getStack(0).writeNbt(tomeNbt));
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		if(nbt.contains("tome"))
			tomeSlot.setStack(0, ItemStack.fromNbt(nbt.getCompound("tome")));
	}
	
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory){
		return new KnowledgeableDropperScreenHandler(syncId, playerInventory, this, tomeSlot);
	}
	
	public Inventory getTomeSlot(){
		return tomeSlot;
	}
}