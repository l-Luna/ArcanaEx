package arcana.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Optional;

public final class PacketCodecUtil{
	
	public static <B extends ByteBuf, V> PacketCodec<B, V> nullable(PacketCodec<B, V> inner){
		return PacketCodecs.optional(inner).xmap(
				x -> x.orElse(null),
				Optional::ofNullable
		);
	}
}