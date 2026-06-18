package arcana.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Optional;

public final class PacketCodecUtil{
	
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
}