package arcana.research;

import arcana.components.Researcher;
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

public class ResearchNetworking{
	
	public static final Identifier syncPacketId = arcId("sync_research");
	public static final Identifier tryAdvanceId = arcId("try_advance");
	
	public static void setup(){
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, didJoin) ->
				ServerPlayNetworking.send(player, syncPacketId, serializeResearch()));
		
		ServerPlayNetworking.registerGlobalReceiver(tryAdvanceId, ResearchNetworking::receiveTryAdvance);
	}
	
	// server -> client
	private static PacketByteBuf serializeResearch(){
		var buf = PacketByteBufs.create();
		buf.writeVarInt(Research.books.size());
		for(Book book : Research.books.values())
			buf.writeNbt(book.toNbt());
		return buf;
	}
	
	public static void deserializeResearch(PacketByteBuf buf){
		Research.books.clear();
		int size = buf.readVarInt();
		for(int i = 0; i < size; i++){
			Book book = Book.fromNbt(buf.readNbt());
			Research.books.put(book.id(), book);
		}
	}
	
	// client -> server
	public static PacketByteBuf serializeTryAdvance(Entry entry){
		return PacketByteBufs.create().writeIdentifier(entry.id());
	}
	
	private static void receiveTryAdvance(MinecraftServer server,
	                                      ServerPlayerEntity player,
	                                      ServerPlayNetworkHandler handler,
	                                      PacketByteBuf buf,
	                                      PacketSender responseSender){
		Entry entry = Research.getEntry(buf.readIdentifier());
		var researcher = Researcher.from(player);
		server.execute(() -> researcher.tryAdvance(entry));
	}
}