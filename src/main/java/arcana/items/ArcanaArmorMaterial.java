package arcana.items;

import arcana.Registerable;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record ArcanaArmorMaterial(Registerable<ArmorMaterial> inner, int durabilityMultiplier, Optional<Identifier> setBonus){

	public ArcanaArmorMaterial(ArmorMaterial inner, String id, int durabilityMultiplier, Optional<Identifier> setBonus){
		this(new Registerable<>(inner), durabilityMultiplier, setBonus);
		this.inner.register(Registries.ARMOR_MATERIAL, id);
	}
}