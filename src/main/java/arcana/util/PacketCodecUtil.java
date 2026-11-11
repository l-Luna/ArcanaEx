package arcana.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public final class PacketCodecUtil{
	
	public static final PacketCodec<PacketByteBuf, Vec3d> VEC3D = PacketCodec.ofStatic(PacketByteBuf::writeVec3d, PacketByteBuf::readVec3d);
	
	public static <B extends ByteBuf, V> PacketCodec<B, V> descriptive(String name, PacketCodec<B, V> inner){
		return PacketCodec.of((value, buf) -> {
			try{
				inner.encode(buf, value);
			}catch(Exception e){
				throw new RuntimeException("Failed to encode \"%s\" using PacketCodec \"%s\"".formatted(value, name), e);
			}
		}, buf -> {
			try{
				return inner.decode(buf);
			}catch(Exception e){
				throw new RuntimeException("Failed to decode using PacketCodec \"%s\"".formatted(name), e);
			}
		});
	}
	
	public static <B extends ByteBuf, V> PacketCodec<B, V> nullable(PacketCodec<B, V> inner){
		return PacketCodecs.optional(inner).xmap(
				x -> x.orElse(null),
				Optional::ofNullable
		);
	}
	
	public static <V extends Enum<V>> PacketCodec<ByteBuf, V> enumCodec(V[] values){
		return PacketCodecs.VAR_INT.xmap(it -> values[it], Enum::ordinal);
	}
}