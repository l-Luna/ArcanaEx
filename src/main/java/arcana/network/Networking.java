package arcana.network;

import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public final class Networking{
	
	public static final NetworkContext context = NetworkContext.forChannel(arcId("network"));
	
	public static void setup(){
		context.register(PkSyncResearchData.class);
		context.register(PkSyncTaintData.class);
		context.register(PkTryAdvance.class);
		context.register(PkModifyPins.class);
		context.register(PkGetNote.class);
		context.register(PkChemistryClick.class);
		context.register(PkChemistryCombineAspects.class);
		context.register(PkSwapFocus.class);
		context.register(PkTriggerWarpEvent.class);
		context.register(PkShakeNode.class);
		context.register(PkPickupItem.class);
		
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, didJoin) -> {
			new PkSyncResearchData().sendTo(player);
			new PkSyncTaintData().sendTo(player);
		});
	}
	
	public static void sendToNearbyPlayers(World sw, S2CMessage message, Vec3d pos){
		for(PlayerEntity pe : sw.getPlayers())
			if(pe.getPos().isInRange(pos, 32))
				message.sendTo(pe);
	}
}