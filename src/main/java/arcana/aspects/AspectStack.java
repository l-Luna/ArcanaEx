package arcana.aspects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public record AspectStack(@NotNull Aspect type, int amount){
	
	public static final Codec<AspectStack> CODEC = RecordCodecBuilder.create(
			i -> i.group(
					Aspect.CODEC.fieldOf("type").forGetter(AspectStack::type),
					Codec.INT.fieldOf("amount").forGetter(AspectStack::amount)
			).apply(i, AspectStack::new)
	);
	
	public NbtCompound toNbt(){
		NbtCompound nbt = new NbtCompound();
		nbt.putString("aspect", type.id().toString());
		nbt.putInt("amount", amount);
		return nbt;
	}
	
	public static AspectStack fromNbt(NbtCompound nbt){
		return new AspectStack(Aspects.byName(nbt.getString("aspect")), nbt.getInt("amount"));
	}
}