package arcana.research.sections;

import arcana.research.EntrySection;
import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Map;

public abstract class AbstractRecipeSection extends EntrySection{

	protected final Identifier recipeId;
	
	protected AbstractRecipeSection(Identifier id){
		recipeId = id;
	}
	
	public Identifier getRecipeId(){
		return recipeId;
	}
	
	public NbtCompound data(){
		return NbtUtil.from(Map.of("recipe", getRecipeId()));
	}
	
	// pins...
}