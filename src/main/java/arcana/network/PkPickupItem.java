package arcana.network;

import arcana.ReflectivelyUtilized;
import com.unascribed.lib39.tunnel.api.NetworkContext;
import com.unascribed.lib39.tunnel.api.S2CMessage;
import com.unascribed.lib39.tunnel.api.annotation.field.MarshalledAs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class PkPickupItem extends S2CMessage{
	
	ItemStack stack;
	float x, y, z;
	@MarshalledAs("varint")
	int collectorId;
	
	public PkPickupItem(ItemStack stack, int collectorId, Vec3d from){
		super(Networking.context);
		this.stack = stack;
		this.collectorId = collectorId;
		x = (float)from.getX();
		y = (float)from.getY();
		z = (float)from.getZ();
	}
	
	@ReflectivelyUtilized
	public PkPickupItem(NetworkContext ctx){
		super(ctx);
	}
	
	// see ClientPlayNetworkHandler.onItemPickupAnimation
	@Environment(EnvType.CLIENT)
	protected void handle(MinecraftClient client, ClientPlayerEntity player){
		ClientWorld world = client.world;
		Random random = world.random;
		Entity entity = world.getEntityById(collectorId);
		if(entity == null)
			entity = player;
		if(!(entity instanceof LivingEntity le))
			return;
		world.playSound(
				x, y, z,
				SoundEvents.ENTITY_ITEM_PICKUP,
				SoundCategory.PLAYERS,
				0.2f,
				(random.nextFloat() - random.nextFloat()) * 1.4f + 2,
				false
		);
		ItemEntity substitute = new ItemEntity(world, x, y, z, stack);
		client.particleManager.addParticle(new ItemPickupParticle(client.getEntityRenderDispatcher(), client.getBufferBuilders(), world, substitute, le));
	}
}