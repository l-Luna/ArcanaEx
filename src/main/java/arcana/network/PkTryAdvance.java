package arcana.network;

import arcana.Networking;
import arcana.ReflectivelyUtilized;
import arcana.components.Researcher;
import arcana.research.Entry;
import arcana.research.Research;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PkTryAdvance extends C2SMessage{
	
	Identifier entryId;
	
	@ReflectivelyUtilized
	public PkTryAdvance(NetworkContext ctx){
		super(ctx);
	}
	
	public PkTryAdvance(Identifier entryId){
		super(Networking.arcCtx);
		this.entryId = entryId;
	}
	
	public PkTryAdvance(Entry entry){
		this(entry.id());
	}
	
	protected void handle(ServerPlayerEntity player){
		Researcher.from(player).tryAdvance(Research.getEntry(entryId));
	}
}