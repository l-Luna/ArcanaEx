package arcana.research.sections;

import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class AlchemyRecipeSection extends AbstractRecipeSection{
	
	public static final Identifier TYPE = arcId("alchemy");
	
	public AlchemyRecipeSection(Identifier id){
		super(id);
	}
	
	public Identifier type(){
		return TYPE;
	}
}