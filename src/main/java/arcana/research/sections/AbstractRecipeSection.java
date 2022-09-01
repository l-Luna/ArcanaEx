package arcana.research.sections;

import arcana.research.Entry;
import arcana.research.EntrySection;
import arcana.research.Icon;
import arcana.research.Pin;
import arcana.util.NbtUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
	
	public Stream<Pin> pins(int idx, World world, Entry entry){
		Optional<? extends Recipe<?>> recipe = world.getRecipeManager().get(recipeId);
		if(recipe.isPresent()){
			ItemStack output = recipe.get().getOutput();
			return Stream.of(new Pin(new Icon(output), entry, idx, output.getItem()));
		}
		return super.pins(idx, world, entry);
	}
}