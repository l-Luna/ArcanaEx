package arcana.items;

import arcana.ArcanaRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.Nullable;

public class ArcanaItemSettings extends Item.Settings{

	@Nullable
	private FragileComponent fragileComponent;
	
	//
	
	public ArcanaItemSettings fragile(int colour){
		fragileComponent = new FragileComponent(colour);
		return this;
	}
	
	public ArcanaItemSettings fragile(int colour, RegistryEntry<StatusEffect> impactEffect){
		fragileComponent = new FragileComponent(colour, impactEffect);
		return this;
	}
	
	//
	
	public ArcanaItemSettings group(ArcanaRegistry.Tab tab){
		// TODO
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
	
	//
	
	public @Nullable FragileComponent getFragileComponent(){
		return fragileComponent;
	}
}