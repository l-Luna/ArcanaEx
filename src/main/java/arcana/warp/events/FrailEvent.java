package arcana.warp.events;

import arcana.ArcanaRegistry;
import arcana.cca_components.RunicShielding;
import arcana.util.InventoryUtil;
import arcana.warp.WarpEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class FrailEvent extends WarpEvent{
	
	@Environment(EnvType.CLIENT)
	public void performOnClient(PlayerEntity player, boolean hadPrecursor){
		player.sendMessage(Text.translatable("message.arcana.warp.frail"), true);
	}
	
	public void perform(PlayerEntity player, boolean hadPrecursor){
		player.addStatusEffect(new StatusEffectInstance(RegistryEntry.of(ArcanaRegistry.WARP_FRAIL), 60 * 20, 0, true, false, true));
	}
	
	public boolean isPrecursor(){
		return false;
	}
	
	public int minWarp(){
		return 7;
	}
	
	public boolean applicableTo(PlayerEntity player, boolean hasSignificantWarp){
		return hasSignificantWarp
				&& InventoryUtil.hasTrinket(player, ArcanaRegistry.RING_OF_TWIN_HEARTBEATS)
				&& RunicShielding.from(player).getHalfPoints() > 2;
	}
}