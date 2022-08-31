package arcana.research.sections;

import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class CookingRecipeSection extends AbstractRecipeSection{
	
	public static final Identifier TYPE = arcId("cooking");
	
	public CookingRecipeSection(Identifier id){
		super(id);
	}
	
	public Identifier type(){
		return TYPE;
	}
}