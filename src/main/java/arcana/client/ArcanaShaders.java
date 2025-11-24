package arcana.client;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

import static net.minecraft.client.render.VertexFormats.*;

public class ArcanaShaders{
	
	public static final VertexFormatElement LOCAL_UV_ELEMENT = new VertexFormatElement(3, VertexFormatElement.ComponentType.FLOAT, VertexFormatElement.Type.UV, 2);
	
	public static final VertexFormat FX = new VertexFormat(
			ImmutableMap.<String, VertexFormatElement>builder()
					.put("Position", POSITION_ELEMENT)
					.put("UV0", TEXTURE_ELEMENT)
					.put("Color", COLOR_ELEMENT)
					.put("UV2", LIGHT_ELEMENT)
					.put("LocalUV", LOCAL_UV_ELEMENT)
					.build()
	);
	
	/* package-private */ static Shader fxTurbulent;
	
	public static Shader getFxTurbulentShader(){
		return fxTurbulent;
	}
}