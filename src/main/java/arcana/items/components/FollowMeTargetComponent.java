package arcana.items.components;

import arcana.util.PacketCodecUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.Optional;

public record FollowMeTargetComponent(Vec3d start, Vec3d end, long timeCreated, Optional<Heightmap.Type> snapToHeightmap){

	public static final Codec<FollowMeTargetComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
			Vec3d.CODEC.fieldOf("start").forGetter(FollowMeTargetComponent::start),
			Vec3d.CODEC.fieldOf("target").forGetter(FollowMeTargetComponent::end),
			Codec.LONG.fieldOf("time_created").forGetter(FollowMeTargetComponent::timeCreated),
			Heightmap.Type.CODEC.optionalFieldOf("snap_to_heightmap").forGetter(FollowMeTargetComponent::snapToHeightmap)
	).apply(i, FollowMeTargetComponent::new));
	
	public static final PacketCodec<PacketByteBuf, FollowMeTargetComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecUtil.VEC3D, FollowMeTargetComponent::start,
			PacketCodecUtil.VEC3D, FollowMeTargetComponent::end,
			PacketCodecs.VAR_LONG, FollowMeTargetComponent::timeCreated,
			PacketCodecs.optional(PacketCodecUtil.enumCodec(Heightmap.Type.values())), FollowMeTargetComponent::snapToHeightmap,
			FollowMeTargetComponent::new
	);
}