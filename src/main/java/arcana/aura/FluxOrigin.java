package arcana.aura;

import arcana.ArcanaRegistry;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public enum FluxOrigin{
	CRUCIBLE_EMPTYING(ArcanaRegistry.CRUCIBLE, "arcana.flux_origin.crucible_empty"),
	CRUCIBLE_BOILOFF(ArcanaRegistry.CRUCIBLE, "arcana.flux_origin.crucible_boiloff"),
	ARCANE_LEVITATOR(ArcanaRegistry.ARCANE_LEVITATOR),
	
	DISTILLERY_FAILURE(ArcanaRegistry.DISTILLERY_PATHFINDER, "arcana.flux_origin.distillery_failure"),
	
	TAINT_IN_A_BOTTLE(ArcanaRegistry.TAINT_IN_A_BOTTLE)
	;
	
	public final Identifier sprite;
	public final String translationKey;
	
	FluxOrigin(Identifier sprite, String key){
		this.sprite = sprite;
		translationKey = key;
	}
	
	FluxOrigin(ItemConvertible sprite, String key){
		this(Registry.ITEM.getId(sprite.asItem()), key);
	}
	
	FluxOrigin(ItemConvertible i){
		this(i, i.asItem().getTranslationKey());
	}
}