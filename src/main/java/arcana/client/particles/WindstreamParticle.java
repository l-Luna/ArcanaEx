package arcana.client.particles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class WindstreamParticle extends SimpleSpriteParticle{
	
	protected WindstreamParticle(ClientWorld world, double x, double y, double z, double vX, double vY, double vZ, SpriteProvider spr){
		super(world, x, y, z, vX, vY, vZ, spr);
		updateSprite();
	}
	
	public void tick(){
		super.tick();
		updateSprite();
	}
	
	private void updateSprite(){
		// set sprite based on normal & player view, too
		Vec3d playerLook = MinecraftClient.getInstance().player.getRotationVec(0);
		double diff = Math.abs(playerLook.dotProduct(new Vec3d(velocityX, velocityY, velocityZ).normalize()));
		int spriteAge = Math.max(age, (int)Math.round(maxAge * diff));
		setSprite(spr.getSprite(spriteAge, maxAge));
	}
	
	public static class Factory extends SimpleSpriteParticle.Factory{
		
		public Factory(SpriteProvider spr, float gravity, float drag){
			super(spr, gravity, drag);
		}
		
		protected SimpleSpriteParticle createParticle(ClientWorld world, double x, double y, double z, double vX, double vY, double vZ){
			return new WindstreamParticle(world, x, y, z, vX, vY, vZ, spr);
		}
	}
}