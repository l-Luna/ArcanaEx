package arcana.items;

import arcana.ArcanaRegistry;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public class GogglesOfRevealingItem extends ArmorItem{
	
	public GogglesOfRevealingItem(Settings settings){
		super(RegistryEntry.of(ArcanaArmorMaterials.GOGGLES_OF_REVEALING), Type.HELMET, settings);
	}
	
	public static boolean hasRevealing(@Nullable PlayerEntity player){
		return player == null
			|| player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof GogglesOfRevealingItem
			|| TrinketsApi.getTrinketComponent(player).map(x -> x.isEquipped(ArcanaRegistry.MONOCLE_OF_REVEALING)).orElse(false);
	}
}