package arcana.recipes;

import arcana.aspects.AspectMap;
import arcana.blocks.CrucibleBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class AlchemyInventory extends SimpleInventory{
	
	private final CrucibleBlockEntity crucible;
	private final PlayerEntity alchemist;
	
	public AlchemyInventory(CrucibleBlockEntity crucible, PlayerEntity alchemist, ItemStack stack){
		super(stack);
		this.crucible = crucible;
		this.alchemist = alchemist;
	}
	
	public boolean isEmpty(){
		return super.isEmpty() && crucible.getAspects().isEmpty();
	}
	
	public void clear(){
		super.clear();
		crucible.getAspects().clear();
	}
	
	public void markDirty(){
		super.markDirty();
		crucible.markDirty();
	}
	
	public PlayerEntity getAlchemist(){
		return alchemist;
	}
	
	public AspectMap getAspects(){
		return crucible.getAspects();
	}
}