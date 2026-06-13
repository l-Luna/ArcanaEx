package arcana.warp;

import arcana.ArcanaRegistry;
import arcana.components.Researcher;
import arcana.network.PkTriggerWarpEvent;
import arcana.warp.events.FrailEvent;
import arcana.warp.events.PassEvent;
import arcana.warp.events.PeekToastEvent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.List;

import static arcana.Arcana.arcId;

public final class WarpEvents{

	public static final BiMap<Identifier, WarpEvent> EVENTS = HashBiMap.create();
	
	public static void setup(){
		EVENTS.put(arcId("peek_toast"), new PeekToastEvent());
		
		EVENTS.put(arcId("pass"), new PassEvent());
		EVENTS.put(arcId("frail"), new FrailEvent());
		
		ServerTickEvents.END_WORLD_TICK.register(WarpEvents::tickWarp);
	}
	
	public static void tickWarp(ServerWorld world){
		for(ServerPlayerEntity player : world.getPlayers()){
			if(player.isSpectator() || player.hasStatusEffect(RegistryEntry.of(ArcanaRegistry.WARP_WARD)))
				continue;
			Researcher researcher = Researcher.from(player);
			long elapsed = world.getTime() - researcher.getLastWarpEventTime();
			// TODO: scale frequency with warp amount
			if((elapsed >= 20 * 60 * 12) || (researcher.wasLastWarpEventPrecursor() && elapsed >= 20 * 18))
				triggerEligible(player);
		}
	}
	
	public static void triggerEligible(PlayerEntity player){
		WarpEvent event = eligible(player);
		if(event != null)
			triggerEvent(player, event);
	}
	
	public static void triggerEvent(PlayerEntity player, WarpEvent event){
		var researcher = Researcher.from(player);
		var hadPrecursor = researcher.wasLastWarpEventPrecursor();
		event.perform(player, hadPrecursor);
		new PkTriggerWarpEvent(event, hadPrecursor).sendTo(player);
		researcher.setLastWarpEvent(player.getWorld().getTime(), event.isPrecursor());
	}
	
	public static WarpEvent eligible(PlayerEntity player){
		Researcher researcher = Researcher.from(player);
		int significantWarp = Researcher.bonusWarp(player);
		int warp = researcher.getWarp() + significantWarp;
		boolean hadPrecursor = researcher.wasLastWarpEventPrecursor();
		Random random = player.getWorld().random;
		List<WarpEvent> choices = EVENTS.values().stream()
				.filter(x -> x.minWarp() <= warp)
				.filter(x -> x.applicableTo(player, significantWarp > 0))
				.filter(x -> (hadPrecursor || !x.requiresPrecursor()) && (!x.isPrecursor() || !hadPrecursor || random.nextBoolean()))
				.toList();
		if(choices.isEmpty())
			return null;
		return choices.get(random.nextInt(choices.size()));
	}
}