package arcana.util;

import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

public record TintingVertexConsumerProvider(VertexConsumerProvider inner, float r, float g, float b, float a) implements VertexConsumerProvider{
	
	public VertexConsumer getBuffer(RenderLayer layer){
		VertexConsumer inner = this.inner.getBuffer(layer);
		return new FixedColorVertexConsumer(){
			public VertexConsumer vertex(double x, double y, double z){
				return inner.vertex(x, y, z);
			}
			
			public VertexConsumer color(int red, int green, int blue, int alpha){
				return inner.color((int)(red * r), (int)(green * g), (int)(blue * b), (int)(alpha * a));
			}
			
			public VertexConsumer texture(float u, float v){
				return inner.texture(u, v);
			}
			
			public VertexConsumer overlay(int u, int v){
				return inner.overlay(u, v);
			}
			
			public VertexConsumer light(int u, int v){
				return inner.light(u, v);
			}
			
			public VertexConsumer normal(float x, float y, float z){
				return inner.normal(x, y, z);
			}
			
			public void next(){
				inner.next();
			}
		};
	}
}