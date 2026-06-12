package arcana.client.particles;

import arcana.aspects.Aspect;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class AspectParticleEffect implements ParticleEffect{
	
	private final ParticleType<AspectParticleEffect> type;
	
	private final Aspect aspect;
	
	public static MapCodec<AspectParticleEffect> createCodec(ParticleType<AspectParticleEffect> type){
		return Aspect.CODEC.xmap(aspect -> new AspectParticleEffect(type, aspect), AspectParticleEffect::getAspect).fieldOf("aspect");
	}
	
	public static PacketCodec<? super RegistryByteBuf, AspectParticleEffect> createPacketCodec(ParticleType<AspectParticleEffect> type){
		return Aspect.PACKET_CODEC.xmap(aspect -> new AspectParticleEffect(type, aspect), AspectParticleEffect::getAspect);
	}
	
	public AspectParticleEffect(ParticleType<AspectParticleEffect> type, Aspect aspect){
		this.type = type;
		this.aspect = aspect;
	}
	
	public Aspect getAspect(){
		return aspect;
	}
	
	public ParticleType<?> getType(){
		return type;
	}
}