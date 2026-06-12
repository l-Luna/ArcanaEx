package arcana.fluids;

import arcana.ArcanaDamageSources;
import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class PutrefactionFluid extends ArcanaFluid{
	
	public PutrefactionFluid(boolean isStillVariant){
		super(isStillVariant);
	}
	
	public Fluid getFlowing(){
		return ArcanaRegistry.FLOWING_PUTREFACTION;
	}
	
	public Fluid getStill(){
		return ArcanaRegistry.STILL_PUTREFACTION;
	}
	
	public TagKey<Fluid> getTag(){
		return ArcanaTags.PUTREFACTION_FLUID;
	}
	
	public int getMaxLevel(){
		return 7;
	}
	
	public String getTexturePath(){
		return "fluid/putrefaction";
	}
	
	public void onEntityInteractTick(Entity entity){
		entity.damage(ArcanaDamageSources.PUTREFACTION, 2);
	}
	
	public float getEntityPushStrength(Entity entity){
		return 0.005f;
	}
	
	public float getEntityDragFactor(Entity entity){
		return 0.1f;
	}
	
	public float getEntitySpeedFactor(Entity entity){
		return 0.1f;
	}
	
	protected boolean isInfinite(World world){
		return false;
	}
	
	protected int getMaxFlowDistance(WorldView world){
		return 4;
	}
	
	protected int getLevelDecreasePerBlock(WorldView world){
		return 2;
	}
	
	public Item getBucketItem(){
		return ArcanaRegistry.PUTREFACTION_BUCKET;
	}
	
	public int getTickRate(WorldView world){
		return 30;
	}
	
	protected BlockState toBlockState(FluidState state){
		return ArcanaRegistry.PUTREFACTION.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
	}
	
	public void randomDisplayTick(World world, BlockPos pos, FluidState state, Random random){
		BlockPos above = pos.up();
		if(world.getBlockState(above).isAir() && !world.getBlockState(above).isOpaqueFullCube(world, above)){
			if(random.nextInt(4) == 0){
				float xO = random.nextFloat();
				float zO = random.nextFloat();
				double yO = FluidUtil.calculateLerpedFluidHeight(world, this, pos, xO, zO);
				world.addParticle(ParticleTypes.CLOUD, pos.getX() + xO, pos.getY() + yO, pos.getZ() + zO, 0, 0, 0);
			}
		}
	}
}