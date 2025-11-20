package arcana.client;

public class BlockLayerRenderer{
	
	/*private final Object2LongMap<BlockPos> lastVisibleTimes = new Object2LongOpenHashMap<>();
	
	private final int fadeInTime = 10, fadeTime = 30;
	private final float opacity;
	private final Identifier texture;
	private boolean translucent = false;
	
	public BlockLayerRenderer(float opacity, Identifier texture){
		this.opacity = opacity;
		this.texture = texture;
	}
	
	public void render(WorldRenderContext context){
		VertexConsumerProvider vcp = context.consumers();
		VertexConsumer vc = vcp.getBuffer(translucent ? RenderLayer.getTranslucent() : RenderLayer.getCutout());
		long now = context.world().getTime();
		for(BlockPos pos : new ArrayList<>(lastVisibleTimes.keySet())){
			long ageL = now - lastVisibleTimes.getLong(pos);
			if(ageL > fadeTime)
				lastVisibleTimes.remove(pos);
			float age = (float)ageL + context.tickDelta();
			
		}
	}*/
}