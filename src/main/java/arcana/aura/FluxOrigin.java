package arcana.aura;

import arcana.ArcanaRegistry;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public enum FluxOrigin{
	CRUCIBLE_EMPTYING(ArcanaRegistry.CRUCIBLE, "arcana.flux_origin.crucible_empty"),
	CRUCIBLE_BOILOFF(ArcanaRegistry.CRUCIBLE, "arcana.flux_origin.crucible_boiloff"),
	;
	
	public final Identifier sprite;
	public final String translationKey;
	
	FluxOrigin(Identifier sprite, String key){
		this.sprite = sprite;
		translationKey = key;
	}
	
	FluxOrigin(Block sprite, String key){
		this(Registry.BLOCK.getId(sprite), key);
	}
	
	FluxOrigin(Block block){
		this(block, block.getTranslationKey());
	}
}