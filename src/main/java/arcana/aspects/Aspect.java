package arcana.aspects;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record Aspect(Identifier id, Aspect left, Aspect right) implements Comparable<Aspect>{

	public static final Codec<Aspect> CODEC = Identifier.CODEC.xmap(Aspects::byName, Aspect::id);
	
	public Text name(){
		return Text.translatable("aspect." + id.getNamespace() + "." + id.getPath());
	}
	
	public int compareTo(@NotNull Aspect o){
		return Aspects.orderedAspects.indexOf(this) - Aspects.orderedAspects.indexOf(o);
	}
	
	public boolean equals(Object obj){
		return obj instanceof Aspect other && other.id().equals(id());
	}
	
	public int hashCode(){
		return id().hashCode();
	}
}