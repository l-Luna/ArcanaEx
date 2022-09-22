package arcana.warp;

import arcana.components.Researcher;
import arcana.network.PkTriggerWarpEvent;
import arcana.warp.events.NauseaEvent;
import arcana.warp.events.PassEvent;
import arcana.warp.events.PeekToastEvent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.List;

import static arcana.Arcana.arcId;

public final class WarpEvents{

	public static final BiMap<Identifier, WarpEvent> events = HashBiMap.create();
	
	public static void setup(){
		events.put(arcId("peek_toast"), new PeekToastEvent());
		
		events.put(arcId("nausea"), new NauseaEvent());
		events.put(arcId("pass"), new PassEvent());
		
		ServerTickEvents.END_WORLD_TICK.register(WarpEvents::tickWarp);
	}
	
	public static void tickWarp(ServerWorld world){
		for(ServerPlayerEntity player : world.getPlayers()){
			Researcher researcher = Researcher.from(player);
			long elapsed = world.getTime() - researcher.getLastWarpEventTime();
			if((elapsed >= 20 * 60 * 9) || (researcher.wasLastWarpEventPrecursor() && elapsed >= 20 * 13))
				triggerEligible(player);
		}
	}
	
	public static void triggerEligible(PlayerEntity player){
		Researcher researcher = Researcher.from(player);
		WarpEvent event = eligible(researcher.getEffectiveWarp(), researcher.wasLastWarpEventPrecursor(), player.world.random);
		if(event != null)
			triggerEvent(player, event);
	}
	
	public static void triggerEvent(PlayerEntity player, WarpEvent event){
		var researcher = Researcher.from(player);
		var hadPrecursor = researcher.wasLastWarpEventPrecursor();
		event.perform(player, hadPrecursor);
		new PkTriggerWarpEvent(event, hadPrecursor).sendTo(player);
		researcher.setLastWarpEvent(player.world.getTime(), event.isPrecursor());
	}
	
	public static WarpEvent eligible(int warp, boolean hadPrecursor, Random random){
		List<WarpEvent> choices = events.values().stream()
				.filter(x -> x.minWarp() <= warp)
				.filter(x -> (hadPrecursor || !x.requiresPrecursor()) && (!x.isPrecursor() || !hadPrecursor || random.nextBoolean()))
				.toList();
		// TODO: select based on items and prefer more severe ones if precursor is present
		if(choices.size() == 0)
			return null;
		return choices.get(random.nextInt(choices.size()));
	}
}