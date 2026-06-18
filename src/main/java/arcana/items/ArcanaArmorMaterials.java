package arcana.items;

import arcana.ArcanaRegistry;
import arcana.Registerable;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static arcana.Arcana.arcId;

public final class ArcanaArmorMaterials{
	
	public static final ArcanaArmorMaterial ARCANIUM = new ArcanaArmorMaterial(
			new ArmorMaterial(Map.of(
					ArmorItem.Type.HELMET, 3,
					ArmorItem.Type.CHESTPLATE, 7,
					ArmorItem.Type.LEGGINGS, 5,
					ArmorItem.Type.BOOTS, 2,
					ArmorItem.Type.BODY, 9
			), 21, SoundEvents.ITEM_ARMOR_EQUIP_IRON, () -> Ingredient.ofItems(ArcanaRegistry.ARCANIUM_INGOT), List.of(new ArmorMaterial.Layer(arcId("arcanium"))), 0, 0),
			"arcanium",
			30,
			Optional.of(arcId("arcane_aura")));
	public static final ArcanaArmorMaterial VOID_METAL = new ArcanaArmorMaterial(
			new ArmorMaterial(Map.of(
					ArmorItem.Type.HELMET, 3,
					ArmorItem.Type.CHESTPLATE, 8,
					ArmorItem.Type.LEGGINGS, 6,
					ArmorItem.Type.BOOTS, 3,
					ArmorItem.Type.BODY, 9
			), 20, SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, () -> Ingredient.ofItems(ArcanaRegistry.VOID_METAL_INGOT), List.of(new ArmorMaterial.Layer(arcId("void_metal"))), 1, 0),
			"void_metal",
			10,
			Optional.empty());
	public static final ArcanaArmorMaterial SILVERLEAF = new ArcanaArmorMaterial(
			new ArmorMaterial(Map.of(
					ArmorItem.Type.HELMET, 4,
					ArmorItem.Type.CHESTPLATE, 7,
					ArmorItem.Type.LEGGINGS, 5,
					ArmorItem.Type.BOOTS, 4,
					ArmorItem.Type.BODY, 10
			), 34, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, () -> Ingredient.ofItems(ArcanaRegistry.SILVERLEAF_AMALGAMATE), List.of(new ArmorMaterial.Layer(arcId("silverleaf"))), 0, 0),
			"silverleaf",
			36,
			Optional.empty());
	
	public static final Registerable<ArmorMaterial> GOGGLES_OF_REVEALING = new Registerable<>(new ArmorMaterial(Map.of(
			ArmorItem.Type.HELMET, 2
	), 20, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.GOLD_INGOT), List.of(new ArmorMaterial.Layer(arcId("goggles_of_revealing"))), 0, 0)).register(Registries.ARMOR_MATERIAL, "goggles_of_revealing");
	public static final Registerable<ArmorMaterial> BOOTS_OF_THE_TRAVELLER = new Registerable<>(new ArmorMaterial(Map.of(
			ArmorItem.Type.BOOTS, 3
	), 20, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.LEATHER), List.of(new ArmorMaterial.Layer(arcId("boots_of_the_traveller"))), 0, 0)).register(Registries.ARMOR_MATERIAL, "boots_of_the_traveller");
	public static final Registerable<ArmorMaterial> BOOTS_OF_THE_SAILOR = new Registerable<>(new ArmorMaterial(Map.of(
			ArmorItem.Type.BOOTS, 4
	), 22, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.LEATHER), List.of(new ArmorMaterial.Layer(arcId("boots_of_the_sailor"))), 0, 0)).register(Registries.ARMOR_MATERIAL, "boots_of_the_sailor");
	public static final Registerable<ArmorMaterial> BOOTS_OF_THE_REAPER = new Registerable<>(new ArmorMaterial(Map.of(
			ArmorItem.Type.BOOTS, 4
	), 22, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, () -> Ingredient.ofItems(Items.LEATHER), List.of(new ArmorMaterial.Layer(arcId("boots_of_the_reaper"))), 0, 0)).register(Registries.ARMOR_MATERIAL, "boots_of_the_reaper");
}