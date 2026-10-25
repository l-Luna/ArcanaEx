package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.api.RequestableScreenHandler;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import com.unascribed.lib39.tunnel.api.annotation.field.Optional;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

// generic client-to-server int data, parallel to PropertyDelegates
public class PkScreenRequest extends C2SMessage{
	
	@Optional
	String tag;
	@MarshalledAs("varint")
	int screenHandlerId, index, data;
	
	@ReflectivelyUtilized
	public PkScreenRequest(NetworkContext ctx){
		super(ctx);
	}
	
	public PkScreenRequest(ScreenHandler handler, @Nullable String tag, int index, int data){
		super(Networking.context);
		this.screenHandlerId = handler.syncId;
		this.tag = tag;
		this.index = index;
		this.data = data;
	}
	
	protected void handle(ServerPlayerEntity entity){
		ScreenHandler handler = entity.currentScreenHandler;
		if(!entity.isSpectator() && handler != null
				&& handler.syncId == screenHandlerId
				&& handler instanceof RequestableScreenHandler rsh)
			rsh.handleRequest(tag, index, data);
	}
}