package arcana.research.sections;

import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class ArcaneCraftingRecipeSection extends AbstractRecipeSection{
	
	public static final Identifier TYPE = arcId("arcane_crafting");
	
	public ArcaneCraftingRecipeSection(Identifier id){
		super(id);
	}
	
	public Identifier type(){
		return TYPE;
	}
}