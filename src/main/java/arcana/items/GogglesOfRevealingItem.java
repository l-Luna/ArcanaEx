package arcana.items;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class GogglesOfRevealingItem extends ArmorItem{
	
	public GogglesOfRevealingItem(Settings settings){
		super(Material.instance, EquipmentSlot.HEAD, settings);
	}
	
	public static class Material implements ArmorMaterial{
		
		public static Material instance = new Material();
		
		public int getDurability(EquipmentSlot slot){
			return 100;
		}
		
		public int getProtectionAmount(EquipmentSlot slot){
			return 2;
		}
		
		public int getEnchantability(){
			return 20;
		}
		
		public SoundEvent getEquipSound(){
			return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
		}
		
		public Ingredient getRepairIngredient(){
			return Ingredient.ofItems(Items.LEATHER);
		}
		
		public String getName(){
			return "arcana/goggles";
		}
		
		public float getToughness(){
			return 0;
		}
		
		public float getKnockbackResistance(){
			return 0;
		}
	}
}