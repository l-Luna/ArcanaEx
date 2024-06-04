package arcana.aspects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AspectStack(@NotNull Aspect type, int amount){
	
	public static final Codec<AspectStack> CODEC = RecordCodecBuilder.create(
			i -> i.group(
					Aspect.CODEC.fieldOf("type").forGetter(AspectStack::type),
					Codec.INT.fieldOf("amount").forGetter(AspectStack::amount)
			).apply(i, AspectStack::new)
	);
	
	public Pair<@NotNull AspectStack /* this */, @Nullable AspectStack /* residual */> splitWithCapacity(int capacity){
		if(amount <= capacity)
			return new Pair<>(this, null);
		int overflow = amount - capacity;
		return new Pair<>(new AspectStack(type, capacity), overflow > 0 ? new AspectStack(type, overflow) : null);
	}
	
	public static Pair<@NotNull AspectStack /* this */, @Nullable AspectStack /* residual */> mergeWithCapacity(@Nullable AspectStack first, AspectStack other, int capacity){
		if(first == null)
			return other.splitWithCapacity(capacity);
		if(first.type != other.type)
			return new Pair<>(first, other);
		return new AspectStack(first.type, first.amount + other.amount).splitWithCapacity(capacity);
	}
	
	public static Pair<@Nullable AspectStack /* this */, @Nullable AspectStack /* drawn */> draw(@Nullable AspectStack self, int max){
		if(self == null)
			return new Pair<>(null, null);
		if(self.amount > max)
			return new Pair<>(new AspectStack(self.type, self.amount - max), new AspectStack(self.type, max));
		else
			return new Pair<>(null, self);
	}
	
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