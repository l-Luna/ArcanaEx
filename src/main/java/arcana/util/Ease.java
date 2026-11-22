package arcana.util;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;

public enum Ease{
	LINEAR(x -> x),
	QUAD_IN(x -> x * x),
	QUAD_OUT(flip(QUAD_IN)),
	QUAD_IN_OUT(inOut(QUAD_IN)),
	CUBE_IN(x -> x * x * x),
	CUBE_OUT(flip(CUBE_IN)),
	CUBE_IN_OUT(inOut(CUBE_IN));
	
	public static final Codec<Ease> CODEC = CodecUtil.enumCodec(values());
	
	private final FloatUnaryOperator func;
	
	Ease(FloatUnaryOperator func){
		this.func = func;
	}
	
	public float apply(float x){
		return func.apply(x);
	}
	
	private static FloatUnaryOperator flip(Ease e){
		return x -> 1 - e.func.apply(1 - x);
	}
	
	private static FloatUnaryOperator inOut(Ease e){
		float adj = 0.5f / e.func.apply(0.5f);
		return x -> x <= 0.5f ? adj * e.func.apply(x) : 1 - adj * (e.func.apply(1 - x));
	}
}