package arcana.util;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StreamUtil{
	
	public static <X, Z> Stream<X> partialReduce(Stream<X> in, Function<? super X, ? extends Z> categorizer, BinaryOperator<X> merger){
		return in.collect(Collectors.groupingBy(categorizer)).values().stream()
				.map(elements -> elements.stream().reduce(merger))
				.filter(Optional::isPresent)
				.map(Optional::get);
	}
	
	public static <X extends NbtElement, Z> Stream<Z> streamAndApply(NbtList list, Class<X> filterType, Function<X, Z> applicator){
		return list.stream().filter(filterType::isInstance).map(filterType::cast).map(applicator);
	}
}