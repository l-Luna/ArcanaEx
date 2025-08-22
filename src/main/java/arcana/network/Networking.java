package arcana.network;

import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import static arcana.Arcana.arcId;

public final class Networking{
	
	public static final NetworkContext context = NetworkContext.forChannel(arcId("network"));
	
	public static void setup(){
		context.register(PkSyncResearchData.class);
		context.register(PkTryAdvance.class);
		context.register(PkModifyPins.class);
		context.register(PkGetNote.class);
		context.register(PkChemistryClick.class);
		context.register(PkChemistryCombineAspects.class);
		context.register(PkSwapFocus.class);
		context.register(PkTriggerWarpEvent.class);
		context.register(PkShakeNode.class);
		
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, didJoin) -> new PkSyncResearchData().sendTo(player));
	}
}