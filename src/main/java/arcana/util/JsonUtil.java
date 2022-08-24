package arcana.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class JsonUtil{
	
	public static int getInt(JsonObject object, String name, int fallback){
		var child = object.get(name);
		if(child == null || !child.isJsonPrimitive())
			return fallback;
		JsonPrimitive p = child.getAsJsonPrimitive();
		if(!p.isNumber())
			return fallback;
		return p.getAsInt();
	}
}