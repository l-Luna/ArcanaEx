package arcana.items;

import arcana.ArcanaRegistry;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

import java.util.function.Supplier;

public enum ArcanaToolMaterials implements ToolMaterial{
	ARCANIUM(MiningLevels.IRON, 855, 7, 2.5f, 25, () -> Ingredient.ofItems(ArcanaRegistry.ARCANIUM_INGOT))
	;
	
	private final int miningLevel;
	private final int durability;
	private final int miningSpeed;
	private final float attackDamage;
	private final int enchantability;
	private final Supplier<Ingredient> repairMaterial;
	
	ArcanaToolMaterials(int miningLevel,
	                    int durability,
	                    int miningSpeed,
	                    float attackDamage,
	                    int enchantability,
	                    Supplier<Ingredient> repairMaterial){
		this.miningLevel = miningLevel;
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
	
	public int getMiningLevel(){
		return miningLevel;
	}
	
	public int getEnchantability(){
		return enchantability;
	}
	
	public Ingredient getRepairIngredient(){
		return repairMaterial.get();
	}
}
