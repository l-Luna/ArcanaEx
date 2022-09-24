package arcana.client.particles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;

// similar to BlockDustParticle, but with ItemStacks instead
public class InfusionItemParticle extends SpriteBillboardParticle{
	
	private final float sampleU;
	private final float sampleV;
	
	protected InfusionItemParticle(ClientWorld world, double x, double y, double z, double dx, double dy, double dz, ItemStack stack){
		super(world, x, y, z, dx, dy, dz);
		setSprite(MinecraftClient.getInstance().getItemRenderer().getModel(stack, world, null, 413).getParticleSprite());
		gravityStrength = 0;
		collidesWithWorld = false;
		red = green = blue = .6f;
		scale /= 2;
		sampleU = random.nextFloat() * 3;
		sampleV = random.nextFloat() * 3;
		maxAge = 11;
		velocityX = dx;
		velocityY = dy;
		velocityZ = dz;
	}
	
	public ParticleTextureSheet getType(){
		return ParticleTextureSheet.TERRAIN_SHEET;
	}
	
	protected float getMinU() {
		return sprite.getFrameU((sampleU + 1) / 4 * 16);
	}
	
	protected float getMaxU() {
		return sprite.getFrameU(sampleU / 4 * 16);
	}
	
	protected float getMinV() {
		return sprite.getFrameV(sampleV / 4 * 16);
	}
	
	protected float getMaxV() {
		return sprite.getFrameV((sampleV + 1) / 4 * 16);
	}
	
	public static class Factory implements ParticleFactory<ItemStackParticleEffect>{
		
		public Particle createParticle(ItemStackParticleEffect parameters,
		                               ClientWorld world,
		                               double x,
		                               double y,
		                               double z,
		                               double dx,
		                               double dy,
		                               double dz){
			return new InfusionItemParticle(world, x, y, z, dx, dy, dz, parameters.getItemStack());
		}
	}
}