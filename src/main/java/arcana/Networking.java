package arcana;

import arcana.network.*;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import static arcana.Arcana.arcId;

public final class Networking{
	
	public static final NetworkContext arcCtx = NetworkContext.forChannel(arcId("network"));
	
	public static void setup(){
		arcCtx.register(PkSyncResearchData.class);
		arcCtx.register(PkTryAdvance.class);
		arcCtx.register(PkModifyPins.class);
		arcCtx.register(PkGetNote.class);
		arcCtx.register(PkChemistryClick.class);
		arcCtx.register(PkChemistryCombineAspects.class);
		arcCtx.register(PkSwapFocus.class);
		arcCtx.register(PkTriggerWarpEvent.class);
		
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, didJoin) -> new PkSyncResearchData().sendTo(player));
	}
}