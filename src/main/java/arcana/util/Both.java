package arcana.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// codec broth
public record Both<A, B>(A fst, B snd){
	
	public static <A, B> Both<A, B> of(A fst, B snd){
		return new Both<>(fst, snd);
	}
	
	public static <A, B> Codec<Both<A, B>> codec(Codec<A> fstCodec, Codec<B> sndCodec){
		return RecordCodecBuilder.create(i -> i.group(
				fstCodec.fieldOf("fst").forGetter(Both::fst),
				sndCodec.fieldOf("snd").forGetter(Both::snd)
		).apply(i, Both::new));
	}
}