package arcana.research;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

public abstract class BackgroundLayer{
	
	public static BackgroundLayer makeLayer(Identifier type, JsonObject obj, Identifier file, float speed, float v){
		return null;
	}
}