package arcana.aspects;

import java.util.HashSet;
import java.util.function.Function;

/**
 * An AspectMap scaled by a constant.
 */
public record ScaledAspectMap(AspectMap underlying, float scale){
	
	public float get(Aspect aspect){
		return underlying.get(aspect) * scale;
	}
	
	public void add(Aspect aspect, float amount){
		underlying.add(aspect, (int)Math.floor(amount / scale));
	}
	
	public void multiply(Function<Aspect, Float> multiplier){
		for(Aspect aspect : new HashSet<>(underlying.aspectSet()))
			underlying.set(aspect, (int)Math.floor((get(aspect) * multiplier.apply(aspect)) / scale));
	}
	
	// everything else is implemented in terms of the above
	
	public void add(AspectStack stack){
		add(stack.type(), stack.amount());
	}
	
	public void add(AspectMap aspects){
		aspects.asStacks().forEach(this::add);
	}
	
	public void take(Aspect aspect, float amount){
		add(aspect, -amount);
	}
	
	public void take(AspectStack stack){
		add(stack.type(), -stack.amount());
	}
	
	public void take(AspectMap map){
		map.asStacks().forEach(this::take);
	}
	
	public void take(ScaledAspectMap map){
		map.underlying.aspectSet().forEach(x -> take(x, map.get(x)));
	}
	
	public boolean contains(Aspect aspect, float amount){
		return get(aspect) >= amount;
	}
	
	public boolean contains(Aspect aspect){
		return contains(aspect, 1);
	}
	
	public boolean contains(AspectStack stack){
		return contains(stack.type(), stack.amount());
	}
	
	public boolean contains(AspectMap other){
		return other.asStacks().stream().allMatch(this::contains);
	}
	
	public boolean contains(ScaledAspectMap other){
		return other.underlying.aspectSet().stream().allMatch(x -> contains(x, other.get(x)));
	}
	
	public void addCapped(Aspect aspect, float amount, float cap){
		add(aspect, Math.max(0, amount + Math.min(0, cap - get(aspect) - amount)));
	}
	
	public void addCapped(AspectStack stack, int cap){
		addCapped(stack.type(), stack.amount(), cap);
	}
}