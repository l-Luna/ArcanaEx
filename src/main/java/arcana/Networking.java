package arcana;

import arcana.components.Researcher;
import arcana.network.PkSyncResearchData;
import arcana.network.PkTryAdvance;
import arcana.research.Pin;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public final class Networking{
	
	public static final NetworkContext arcCtx = NetworkContext.forChannel(arcId("network"));
	
	public static final Identifier modifyPinsId = arcId("modify_pins");
	
	public static void setup(){
		arcCtx.register(PkSyncResearchData.class);
		arcCtx.register(PkTryAdvance.class);
		
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, didJoin) -> new PkSyncResearchData().sendTo(player));
		
		ServerPlayNetworking.registerGlobalReceiver(modifyPinsId, Networking::receiveModifyPins);
	}
	
	// client -> server
	public static PacketByteBuf serializeModifyPins(Pin pin, boolean add){
		var buf = PacketByteBufs.create().writeIdentifier(pin.entry().id()).writeVarInt(pin.stage());
		buf.writeBoolean(add); // me when ByteBuf
		return buf;
	}
	
	private static void receiveModifyPins(MinecraftServer server,
	                                      ServerPlayerEntity player,
	                                      ServerPlayNetworkHandler handler,
	                                      PacketByteBuf buf,
	                                      PacketSender responseSender){
		var researcher = Researcher.from(player);
		Identifier entry = buf.readIdentifier();
		int stage = buf.readVarInt();
		boolean add = buf.readBoolean();
		server.execute(() -> {
			if(add)
				researcher.addPinned(entry, stage);
			else
				researcher.removePinned(entry, stage);
			// pinning/unpinning UI keeps track on the client side
		});
	}
}