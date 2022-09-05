package arcana.network;

import arcana.Networking;
import arcana.ReflectivelyUtilized;
import arcana.warp.WarpEvent;
import arcana.warp.WarpEvents;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

public class PkTriggerWarpEvent extends S2CMessage{
	
	String eventId;
	boolean hadPrecursor;
	
	@ReflectivelyUtilized
	public PkTriggerWarpEvent(NetworkContext ctx){
		super(ctx);
	}
	
	public PkTriggerWarpEvent(WarpEvent eventId, boolean hadPrecursor){
		super(Networking.arcCtx);
		this.eventId = eventId.id().toString();
		this.hadPrecursor = hadPrecursor;
	}
	
	@Environment(EnvType.CLIENT)
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		WarpEvents.events.get(new Identifier(eventId)).performOnClient(player, hadPrecursor);
	}
}