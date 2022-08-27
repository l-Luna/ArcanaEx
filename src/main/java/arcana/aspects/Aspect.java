package arcana.aspects;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record Aspect(Identifier id) implements Comparable<Aspect>{

	public Text name(){
		return Text.translatable("aspect." + id.getNamespace() + "." + id.getPath());
	}
	
	public int compareTo(@NotNull Aspect o){
		return Aspects.ORDERED_ASPECTS.indexOf(this) - Aspects.ORDERED_ASPECTS.indexOf(o);
	}
}