package arcana.items;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public record FragileComponent(int colour, @Nullable RegistryEntry<StatusEffect> effect){
	
	public FragileComponent(int colour){
		this(colour, null);
	}
}