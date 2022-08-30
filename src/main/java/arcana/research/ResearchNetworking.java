package arcana.research;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class ResearchNetworking{
	
	public static final Identifier syncPacketId = arcId("sync_research");
	
	public static void setup(){
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, didJoin) ->
				ServerPlayNetworking.send(player, syncPacketId, serializeResearch()));
	}
	
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
}
