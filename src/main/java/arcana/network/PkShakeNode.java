package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.aura.Node;
import arcana.aura.NodeReference;
import arcana.client.renderers.NodeRenderer;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class PkShakeNode extends S2CMessage{
	
	NodeReference node;
	@MarshalledAs("varint")
	int ticks;
	
	@ReflectivelyUtilized
	public PkShakeNode(NetworkContext ctx){
		super(ctx);
	}
	
	public PkShakeNode(Node node, int ticks){
		this(Networking.context);
		this.node = NodeReference.ref(node);
		this.ticks = ticks;
	}
	
	@Environment(EnvType.CLIENT)
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		node.deref(player.getWorld()).ifPresent(node -> NodeRenderer.shakeNode(node, ticks));
	}
}