package arcana.recipes.alchemy;

import arcana.aspects.AspectMap;
import arcana.blocks.be.CrucibleBlockEntity;
import arcana.research.Research;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AlchemyInput implements RecipeInput{
	
	private final ItemStack reagent;
	private final CrucibleBlockEntity crucible;
	// mirror Researcher and TomeOfSharingItem
	private final Map<Identifier, Integer> stages;
	
	public AlchemyInput(CrucibleBlockEntity crucible, ItemStack reagent, Map<Identifier, Integer> stages){
		this.crucible = crucible;
		this.reagent = reagent;
		this.stages = stages;
	}
	
	public ItemStack getStackInSlot(int slot){
		return slot == 0 ? reagent : null;
	}
	
	public int getSize(){
		return 1;
	}
	
	public boolean isEmpty(){
		return RecipeInput.super.isEmpty() && crucible.getAspects().isEmpty();
	}
	
	public AspectMap getAspects(){
		return crucible.getAspects();
	}
	
	public ItemStack getReagent(){
		return reagent;
	}
	
	public int entryStage(Identifier entryId){
		return stages.getOrDefault(entryId, 0);
	}
	
	public boolean complete(Identifier entryId, int stage){
		return entryStage(entryId) >= (stage == -1 ? Research.getEntry(entryId).sections().size() : stage);
	}
}