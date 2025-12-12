package arcana.aspects;

import java.util.Arrays;
import java.util.List;

/**
 * The primal aspects. Should be used where a primal aspect is used as a variant/type/key/etc thematically,
 * *not* as an actual stored aspect.
 */
public enum Primal{
	AIR(Aspects.AIR),
	FIRE(Aspects.FIRE),
	WATER(Aspects.WATER),
	EARTH(Aspects.EARTH),
	ORDER(Aspects.ORDER),
	ENTROPY(Aspects.ENTROPY);
	
	public static final List<Primal> entries = Arrays.asList(values());
	
	private final Aspect aspect;
	
	Primal(Aspect aspect){
		this.aspect = aspect;
	}
	
	public Aspect asAspect(){
		return aspect;
	}
	
	public int colour(){
		return aspect.colour();
	}
	
	public String id(){
		return switch(this){
			case AIR -> "air";
			case FIRE -> "fire";
			case WATER -> "water";
			case EARTH -> "earth";
			case ORDER -> "order";
			case ENTROPY -> "entropy";
		};
	}
	
	public static Primal byId(String id){
		return switch(id){
			case "air" -> AIR;
			case "fire" -> FIRE;
			case "water" -> WATER;
			case "earth" -> EARTH;
			case "order" -> ORDER;
			case "entropy" -> ENTROPY;
			default -> null;
		};
	}
}