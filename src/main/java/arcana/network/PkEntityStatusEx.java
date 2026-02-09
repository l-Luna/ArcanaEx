package arcana.network;

import arcana.ReflectivelyUtilized;
import arcana.duck.ArcanaLivingEntity;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;

public class PkEntityStatusEx extends S2CMessage{
	
	public static final byte STATUS_DIED_TO_PUTREFACTION = 0;
	
	public static void sendStatus(Entity entity, byte status){
		new PkEntityStatusEx(entity, status).sendToAllWatching(entity);
	}
	
	@MarshalledAs("int32")
	int entityId;
	@MarshalledAs("int8")
	byte status;
	
	@ReflectivelyUtilized
	public PkEntityStatusEx(NetworkContext ctx){
		super(ctx);
	}
	
	public PkEntityStatusEx(Entity entity, byte status){
		super(Networking.context);
		this.entityId = entity.getId();
		this.status = status;
	}
	
	@Environment(EnvType.CLIENT)
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		Entity entity = client.world.getEntityById(entityId);
		switch(status){
			case STATUS_DIED_TO_PUTREFACTION -> {
				if(entity instanceof ArcanaLivingEntity ale)
					ale.arcana$markDiedToPutrefaction();
			}
			default -> {}
		}
	}
}