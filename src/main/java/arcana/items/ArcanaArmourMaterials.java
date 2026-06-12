package arcana.items;

import net.minecraft.item.ArmorMaterial;

public final class ArcanaArmourMaterials{
	// TODO: set bonuses
	
	public static final ArmorMaterial ARCANIUM = new ArmorMaterial();
	public static final ArmorMaterial VOID_METAL = new ArmorMaterial();
	public static final ArmorMaterial SILVERLEAF = new ArmorMaterial();
	
	public static final ArmorMaterial GOGGLES_OF_REVEALING = new ArmorMaterial();
	public static final ArmorMaterial BOOTS_OF_THE_TRAVELLER = new ArmorMaterial();
	public static final ArmorMaterial BOOTS_OF_THE_SAILOR = new ArmorMaterial();
	public static final ArmorMaterial BOOTS_OF_THE_REAPER = new ArmorMaterial();
	
	/*ARCANIUM("arcanium", 21, new int[]{2, 5, 7, 3}, 30, SoundEvents.ITEM_ARMOR_EQUIP_IRON, () -> Ingredient.ofItems(ArcanaRegistry.ARCANIUM_INGOT), 0, () -> ArcanaRegistry.ARCANE_AURA),
	VOID_METAL("void_metal", 20, new int[]{3, 6, 8, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, () -> Ingredient.ofItems(ArcanaRegistry.VOID_METAL_INGOT), 1, null),
	SILVERLEAF("silverleaf", 34, new int[]{4, 5, 7, 4}, 32, SoundEvents.ITEM_ARMOR_EQUIP_IRON, () -> Ingredient.ofItems(ArcanaRegistry.SILVERLEAF_AMALGAMATE), 0, null),
	BOOTS_OF_THE_TRAVELLER("boots_of_the_traveller", 20, new int[]{3,3,3,3}, 20, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.LEATHER), 0, null),
	BOOTS_OF_THE_SAILOR("boots_of_the_sailor", 22, new int[]{3,3,3,4}, 18, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.LEATHER), 0, null),
	BOOTS_OF_THE_REAPER("boots_of_the_reaper", 22, new int[]{3,3,3,4}, 22, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.LEATHER), 0, null),
	;
	
	private static final int[] baseDurability = new int[]{13, 15, 16, 11};
	
	private final String name;
	private final int durabilityModifier;
	private final int[] protection;
	private final int enchantability;
	private final SoundEvent equipSound;
	private final Supplier<Ingredient> repairMaterial;
	private final int toughness;
	private final @Nullable Supplier<StatusEffect> setBonusEffect;
	
	ArcanaArmourMaterials(String name,
	                      int durabilityModifier,
	                      int[] protection,
	                      int enchantability,
	                      SoundEvent equipSound,
	                      Supplier<Ingredient> repairMaterial, int toughness, @Nullable Supplier<StatusEffect> effect){
		this.name = name;
		this.durabilityModifier = durabilityModifier;
		this.protection = protection;
		this.enchantability = enchantability;
		this.equipSound = equipSound;
		this.repairMaterial = repairMaterial;
		this.toughness = toughness;
		setBonusEffect = effect;
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
		return toughness;
	}
	
	public float getKnockbackResistance(){
		return 0;
	}
	
	public @Nullable StatusEffect getSetBonusEffect(){
		return setBonusEffect == null ? null : setBonusEffect.get();
	}*/
}