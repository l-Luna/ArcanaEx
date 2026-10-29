package arcana.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;

public class AmbientLightLootCondition implements LootCondition{
	
	public enum LightFilter{
		OVERALL,
		SKY,
		BLOCK
	}
	
	public static final MapCodec<AmbientLightLootCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			CodecUtil.enumCodec(LightFilter.values()).fieldOf("light_filter").forGetter(x -> x.lightFilter),
			Codec.intRange(0, 15).fieldOf("min_inclusive").forGetter(x -> x.minInclusive),
			Codec.intRange(0, 15).fieldOf("max_inclusive").forGetter(x -> x.maxExclusive)
	).apply(i, AmbientLightLootCondition::new));
	
	public static final LootConditionType TYPE = new LootConditionType(CODEC);
	
	private final LightFilter lightFilter;
	private final int minInclusive, maxExclusive;
	
	public AmbientLightLootCondition(LightFilter lightFilter, int minInclusive, int maxExclusive){
		this.lightFilter = lightFilter;
		this.minInclusive = minInclusive;
		this.maxExclusive = maxExclusive;
	}
	
	public LootConditionType getType(){
		return TYPE;
	}
	
	public boolean test(LootContext context){
		ServerWorld world = context.getWorld();
		Vec3d origin = context.get(LootContextParameters.ORIGIN);
		if(origin != null){
			BlockPos pos = BlockPos.ofFloored(origin);
			int light = switch(lightFilter){
				case OVERALL -> world.getLightLevel(pos);
				case SKY -> world.getLightLevel(LightType.SKY, pos);
				case BLOCK -> world.getLightLevel(LightType.BLOCK, pos);
			};
			return light >= minInclusive && light <= maxExclusive;
		}
		
		return false;
	}
}