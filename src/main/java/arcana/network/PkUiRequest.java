package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.api.RequestableScreenHandler;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

// generic client-to-server int data, parallel to PropertyDelegates
public class PkUiRequest extends C2SMessage{
	
	@MarshalledAs("varint")
	int screenHandlerId, index, data;
	
	@ReflectivelyUtilized
	public PkUiRequest(NetworkContext ctx){
		super(ctx);
	}
	
	public PkUiRequest(ScreenHandler handler, int index, int data){
		super(Networking.context);
		this.screenHandlerId = handler.syncId;
		this.index = index;
		this.data = data;
	}
	
	protected void handle(ServerPlayerEntity entity){
		ScreenHandler handler = entity.currentScreenHandler;
		if(!entity.isSpectator() && handler != null
				&& handler.syncId == screenHandlerId
				&& handler instanceof RequestableScreenHandler rsh)
			rsh.handleRequest(index, data);
	}
}