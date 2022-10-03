package arcana.client.particles;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

public class AspectParticleEffect implements ParticleEffect{
	
	public static final ParticleEffect.Factory<AspectParticleEffect> PARAMETERS_FACTORY = new Factory<>(){
		public AspectParticleEffect read(ParticleType<AspectParticleEffect> type, StringReader reader)
				throws CommandSyntaxException{
			return new AspectParticleEffect(type, Aspects.byName(reader.readString()));
		}
		
		public AspectParticleEffect read(ParticleType<AspectParticleEffect> type, PacketByteBuf buf){
			return new AspectParticleEffect(type, Aspects.byName(buf.readIdentifier()));
		}
	};
	
	private final ParticleType<AspectParticleEffect> type;
	
	private final Aspect aspect;
	
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
	
	public void write(PacketByteBuf buf){
		buf.writeIdentifier(aspect.id());
	}
	
	public String asString(){
		return Registry.PARTICLE_TYPE.getId(getType()) + " " + aspect.id();
	}
	
	public static Codec<AspectParticleEffect> createCodec(ParticleType<AspectParticleEffect> type){
		return Aspect.CODEC.xmap(aspect -> new AspectParticleEffect(type, aspect), AspectParticleEffect::getAspect);
	}
}