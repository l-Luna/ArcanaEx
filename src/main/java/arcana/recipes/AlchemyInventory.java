package arcana.recipes;

import arcana.aspects.AspectMap;
import arcana.blocks.CrucibleBlockEntity;
import arcana.research.Parent;
import arcana.research.Research;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AlchemyInventory extends SimpleInventory{
	
	private final CrucibleBlockEntity crucible;
	// mirror Researcher and TomeOfSharingItem
	private final Map<Identifier, Integer> stages;
	
	public AlchemyInventory(CrucibleBlockEntity crucible, ItemStack stack, Map<Identifier, Integer> stages){
		super(stack);
		this.crucible = crucible;
		this.stages = stages;
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
	
	public AspectMap getAspects(){
		return crucible.getAspects();
	}
	
	public int entryStage(Identifier entryId){
		return stages.getOrDefault(entryId, 0);
	}
	
	public boolean complete(Parent parent){
		return entryStage(parent.id()) >= (parent.stage() == -1 ? Research.getEntry(parent.id()).sections().size() : parent.stage());
	}
}