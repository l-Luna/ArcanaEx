package arcana.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

import java.util.UUID;

public class RunicShieldingEnchantment extends Enchantment{
	
	public static final UUID[] MODIFIER_UUIDS = new UUID[]{
			UUID.fromString("d938c526-0e7d-43eb-b39e-c31b68a62777"),
			UUID.fromString("d9234b3c-9e35-4fbe-8383-a82bb19dda24"),
			UUID.fromString("15c12ffd-73e3-421d-b438-ccd18f36c042"),
			UUID.fromString("b5b3eb30-a86e-4b3d-920c-5e70e0fd6ea4")
	};
	
	public RunicShieldingEnchantment(){
		super(Rarity.VERY_RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{ EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET });
	}
	
	public boolean isAvailableForRandomSelection(){
		return false;
	}
	
	public boolean isAvailableForEnchantedBookOffer(){
		return false;
	}
	
	public boolean isTreasure(){
		return true;
	}
	
	public int getMaxLevel(){
		return 3;
	}
	
	public int getMinPower(int level){
		return level * 25;
	}
	
	public int getMaxPower(int level){
		return getMinPower(level) + 50;
	}
}