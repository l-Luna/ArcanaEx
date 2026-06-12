package arcana.client.particles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record CubeParticleEffect(ParticleType<CubeParticleEffect> particleType, CubeParticleStyle style) implements ParticleEffect{
	
	public static MapCodec<CubeParticleEffect> createCodec(ParticleType<CubeParticleEffect> type){
		return Codec.STRING.xmap(x -> new CubeParticleEffect(type, CubeParticleStyle.valueOf(x)), x -> x.style().name()).fieldOf("style");
	}
	
	public static PacketCodec<? super RegistryByteBuf, CubeParticleEffect> createPacketCodec(ParticleType<CubeParticleEffect> type){
		return PacketCodecs.VAR_INT.xmap(x -> new CubeParticleEffect(type, CubeParticleStyle.values()[x]), x -> x.style().ordinal());
	}
	
	public ParticleType<?> getType(){
		return particleType;
	}
	
	public String asString(){
		return style.name();
	}
}