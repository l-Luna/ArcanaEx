package arcana.items;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record ArcanaArmorMaterial(ArmorMaterial inner, int durabilityMultiplier, Optional<Identifier> setBonus){

}