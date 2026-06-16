package arcana.items;

import net.minecraft.item.ArmorItem;
import net.minecraft.registry.entry.RegistryEntry;

public class ArcanaArmorItem extends ArmorItem{
	
	public final ArcanaArmorMaterial material;
	
	public ArcanaArmorItem(ArcanaArmorMaterial material, Type type, Settings settings){
		super(RegistryEntry.of(material.inner()), type, settings.maxDamage(type.getMaxDamage(material.durabilityMultiplier())));
		this.material = material;
	}
}