package arcana.research.sections;

import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class CraftingRecipeSection extends AbstractRecipeSection{
	
	public static final Identifier TYPE = arcId("crafting");
	
	public CraftingRecipeSection(Identifier id){
		super(id);
	}
	
	public Identifier type(){
		return TYPE;
	}
}