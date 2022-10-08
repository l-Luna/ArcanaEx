package arcana.items;

import arcana.ArcanaRegistry;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.Nullable;

public class GogglesOfRevealingItem extends ArmorItem{
	
	public GogglesOfRevealingItem(Settings settings){
		super(Material.instance, EquipmentSlot.HEAD, settings);
	}
	
	public static boolean hasRevealing(@Nullable PlayerEntity player){
		return player == null
			|| player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof GogglesOfRevealingItem
			|| TrinketsApi.getTrinketComponent(player).map(x -> x.isEquipped(ArcanaRegistry.MONOCLE_OF_REVEALING)).orElse(false);
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
			return "arcana:goggles";
		}
		
		public float getToughness(){
			return 0;
		}
		
		public float getKnockbackResistance(){
			return 0;
		}
	}
}