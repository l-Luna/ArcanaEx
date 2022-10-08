package arcana.items;

import arcana.ArcanaRegistry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.function.Supplier;

public enum ArcanaArmourMaterials implements ArmorMaterial{
	// TODO: move GogglesOfRevealingItem.Material?
	
	ARCANIUM("arcanium", 27, new int[]{3, 5, 7, 3}, 30, SoundEvents.ITEM_ARMOR_EQUIP_IRON, () -> Ingredient.ofItems(ArcanaRegistry.ARCANIUM_INGOT)),
	BOOTS_OF_THE_TRAVELLER("boots_of_the_traveller", 20, new int[]{3,3,3,3}, 20, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.LEATHER)),
	;
	
	private static final int[] baseDurability = new int[]{13, 15, 16, 11};
	
	private final String name;
	private final int durabilityModifier;
	private final int[] protection;
	private final int enchantability;
	private final SoundEvent equipSound;
	private final Supplier<Ingredient> repairMaterial;
	
	ArcanaArmourMaterials(String name,
	                      int durabilityModifier,
	                      int[] protection,
	                      int enchantability,
	                      SoundEvent equipSound,
	                      Supplier<Ingredient> repairMaterial){
		this.name = name;
		this.durabilityModifier = durabilityModifier;
		this.protection = protection;
		this.enchantability = enchantability;
		this.equipSound = equipSound;
		this.repairMaterial = repairMaterial;
	}
	
	public int getDurability(EquipmentSlot slot){
		return baseDurability[slot.getEntitySlotId()] * durabilityModifier;
	}
	
	public int getProtectionAmount(EquipmentSlot slot){
		return protection[slot.getEntitySlotId()];
	}
	
	public int getEnchantability(){
		return enchantability;
	}
	
	public SoundEvent getEquipSound(){
		return equipSound;
	}
	
	public Ingredient getRepairIngredient(){
		return repairMaterial.get();
	}
	
	public String getName(){
		return "arcana:" + name;
	}
	
	public float getToughness(){
		return 0;
	}
	
	public float getKnockbackResistance(){
		return 0;
	}
}