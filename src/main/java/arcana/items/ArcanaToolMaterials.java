package arcana.items;

import arcana.ArcanaRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Supplier;

public enum ArcanaToolMaterials implements ToolMaterial{
	ARCANIUM(BlockTags.INCORRECT_FOR_IRON_TOOL, 655, 13, 2.5f, 25, () -> Ingredient.ofItems(ArcanaRegistry.ARCANIUM_INGOT)),
	VOID_METAL(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 312, 9, 3.5f, 6, () -> Ingredient.ofItems(ArcanaRegistry.VOID_METAL_INGOT)),
	SILVERLEAF(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1100, 7, 2.5f, 27, () -> Ingredient.ofItems(ArcanaRegistry.SILVERLEAF_AMALGAMATE)),
	CRIMSON(BlockTags.INCORRECT_FOR_IRON_TOOL, 400, 7, 2.5f, 1, () -> Ingredient.ofItems(ArcanaRegistry.ALCHEMICAL_IRON)),
	PRIMAL(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2727, 13, 3.5f, 10, () -> Ingredient.ofItems(ArcanaRegistry.ARCANIUM_INGOT)),
	;
	
	private final TagKey<Block> incorrectTag;
	private final int durability;
	private final int miningSpeed;
	private final float attackDamage;
	private final int enchantability;
	private final Supplier<Ingredient> repairMaterial;
	
	ArcanaToolMaterials(TagKey<Block> incorrectTag,
	                    int durability,
	                    int miningSpeed,
	                    float attackDamage,
	                    int enchantability,
	                    Supplier<Ingredient> repairMaterial){
		this.incorrectTag = incorrectTag;
		this.durability = durability;
		this.miningSpeed = miningSpeed;
		this.attackDamage = attackDamage;
		this.enchantability = enchantability;
		this.repairMaterial = repairMaterial;
	}
	
	public int getDurability(){
		return durability;
	}
	
	public float getMiningSpeedMultiplier(){
		return miningSpeed;
	}
	
	public float getAttackDamage(){
		return attackDamage;
	}
	
	public TagKey<Block> getInverseTag(){
		return incorrectTag;
	}
	
	public int getEnchantability(){
		return enchantability;
	}
	
	public Ingredient getRepairIngredient(){
		return repairMaterial.get();
	}
}