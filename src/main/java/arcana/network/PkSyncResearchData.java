package arcana.network;

import arcana.Networking;
import arcana.ReflectivelyUtilized;
import arcana.research.Book;
import arcana.research.Research;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;

public class PkSyncResearchData extends S2CMessage{
	
	@MarshalledAs("nbt-list")
	List<NbtCompound> bookNbts = new ArrayList<>();
	
	@ReflectivelyUtilized
	public PkSyncResearchData(NetworkContext ctx){
		super(ctx);
	}
	
	public PkSyncResearchData(){
		super(Networking.arcCtx);
		bookNbts = Research.books.values().stream().map(Book::toNbt).toList();
	}
	
	@Environment(EnvType.CLIENT)
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		Research.books.clear();
		for(NbtCompound nbt : bookNbts){
			Book book = Book.fromNbt(nbt);
			Research.books.put(book.id(), book);
		}
	}
}
