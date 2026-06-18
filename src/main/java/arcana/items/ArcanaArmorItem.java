package arcana.items;

import net.minecraft.item.ArmorItem;

public class ArcanaArmorItem extends ArmorItem{
	
	public final ArcanaArmorMaterial material;
	
	public ArcanaArmorItem(ArcanaArmorMaterial material, Type type, Settings settings){
		super(material.inner().entry(), type, settings.maxDamage(type.getMaxDamage(material.durabilityMultiplier())));
		this.material = material;
	}
}