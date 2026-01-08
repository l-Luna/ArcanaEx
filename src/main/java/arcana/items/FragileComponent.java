package arcana.items;

import net.minecraft.entity.effect.StatusEffect;
import org.jetbrains.annotations.Nullable;

public record FragileComponent(int colour, @Nullable StatusEffect effect){
	
	public FragileComponent(int colour){
		this(colour, null);
	}
}