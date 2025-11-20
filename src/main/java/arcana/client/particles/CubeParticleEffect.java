package arcana.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public record CubeParticleEffect(ParticleType<CubeParticleEffect> particleType, CubeParticleStyle style) implements ParticleEffect{
	
	public static final Factory<CubeParticleEffect> PARAMETERS_FACTORY = new Factory<>(){
		public CubeParticleEffect read(ParticleType<CubeParticleEffect> type, StringReader reader)
				throws CommandSyntaxException{
			return new CubeParticleEffect(type, CubeParticleStyle.valueOf(reader.readString()));
		}
		
		public CubeParticleEffect read(ParticleType<CubeParticleEffect> type, PacketByteBuf buf){
			return new CubeParticleEffect(type, CubeParticleStyle.valueOf(buf.readString()));
		}
	};
	
	public ParticleType<?> getType(){
		return particleType;
	}
	
	public void write(PacketByteBuf buf){
		buf.writeString(style.name());
	}
	
	public String asString(){
		return style.name();
	}
}