package arcana.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;

import java.util.Comparator;

public final class PropertyCodecs{
	
	public static final MapCodec<IntProperty> INT_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("name").forGetter(Property::getName),
			Codec.INT.fieldOf("min").forGetter(x -> x.getValues().stream().min(Comparator.naturalOrder()).orElseThrow()),
			Codec.INT.fieldOf("max").forGetter(x -> x.getValues().stream().max(Comparator.naturalOrder()).orElseThrow())
	).apply(i, IntProperty::of));
}