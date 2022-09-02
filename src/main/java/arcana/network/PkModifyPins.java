package arcana.network;

import arcana.Networking;
import arcana.ReflectivelyUtilized;
import arcana.components.Researcher;
import arcana.research.Pin;
import com.unascribed.lib39.tunnel.api.C2SMessage;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PkModifyPins extends C2SMessage{
	
	String entry = "";
	boolean add;
	@MarshalledAs("varint")
	int stage;
	
	@ReflectivelyUtilized
	public PkModifyPins(NetworkContext ctx){
		super(ctx);
	}
	
	public PkModifyPins(Pin pin, boolean add){
		super(Networking.arcCtx);
		entry = pin.entry().id().toString();
		stage = pin.stage();
		this.add = add;
	}
	
	protected void handle(ServerPlayerEntity player){
		Researcher researcher = Researcher.from(player);
		if(add)
			researcher.addPinned(new Identifier(entry), stage);
		else
			researcher.removePinned(new Identifier(entry), stage);
	}
}