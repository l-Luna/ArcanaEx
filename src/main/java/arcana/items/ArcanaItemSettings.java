package arcana.items;

import arcana.ArcanaRegistry;
import arcana.items.components.ArcanaDataComponents;
import arcana.items.components.FragileComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Rarity;

public class ArcanaItemSettings extends Item.Settings{
	
	public ArcanaItemSettings fragile(int colour){
		component(ArcanaDataComponents.FRAGILE, new FragileComponent(colour));
		return this;
	}
	
	public ArcanaItemSettings fragile(int colour, RegistryEntry<StatusEffect> impactEffect){
		component(ArcanaDataComponents.FRAGILE, new FragileComponent(colour, impactEffect));
		return this;
	}
	
	//
	
	public ArcanaItemSettings group(ArcanaRegistry.Tab tab){
		component(ArcanaDataComponents.SUBTAB, tab);
		return this;
	}
	
	public ArcanaItemSettings maxCount(int maxCount){
		super.maxCount(maxCount);
		return this;
	}
	
	public ArcanaItemSettings rarity(Rarity rarity){
		super.rarity(rarity);
		return this;
	}
}