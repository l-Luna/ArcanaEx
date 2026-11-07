package arcana.items.foci.follow_me;

import arcana.research.Parent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static arcana.Arcana.arcId;

public record Locator(Ingredient input, boolean consumed, Destination target, @Nullable Parent requiredResearch){
	
	public static final RegistryKey<Registry<Locator>> REGISTRY_KEY = RegistryKey.ofRegistry(arcId("locator"));
	
	public static final Codec<Locator> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(
			Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("input").forGetter(Locator::input),
			Codec.BOOL.optionalFieldOf("consumed", true).forGetter(Locator::consumed),
			Destination.TYPE_CODEC.fieldOf("destination").forGetter(Locator::target),
			Parent.SIMPLE_CODEC.optionalFieldOf("required_research").forGetter(it -> Optional.ofNullable(it.requiredResearch))
	).apply(i, (input, consumed, target, requiredResearch) -> new Locator(input, consumed, target, requiredResearch.orElse(null))));
	
	public static Registry<Locator> getRegistry(World world){
		return world.getRegistryManager().get(REGISTRY_KEY);
	}
}