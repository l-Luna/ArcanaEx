package arcana.items;

import arcana.ArcanaTags;
import arcana.mixin.MiningToolItemAccessor;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;

public class EarthmoverShovelItem extends ShovelItem{
	
	public EarthmoverShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings){
		super(material, attackDamage, attackSpeed, settings);
		((MiningToolItemAccessor)this).arcana$setEffectiveBlocks(ArcanaTags.EARTHMOVER_MINEABLE);
	}
}