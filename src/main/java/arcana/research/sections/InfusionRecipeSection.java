package arcana.research.sections;

import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class InfusionRecipeSection extends AbstractRecipeSection{
	
	public static final Identifier TYPE = arcId("infusion");
	
	public InfusionRecipeSection(Identifier id){
		super(id);
	}
	
	public Identifier type(){
		return TYPE;
	}
}