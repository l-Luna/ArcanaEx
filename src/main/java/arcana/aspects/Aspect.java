package arcana.aspects;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record Aspect(Identifier id){

	public Text name(){
		return Text.translatable("aspect." + id.getNamespace() + "." + id.getPath());
	}
}