package arcana.fluids;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.TagKey;
import net.minecraft.world.WorldView;

import java.util.Optional;

public class TaintGooFluid extends ArcanaFluid{
	
	public TaintGooFluid(boolean isStillVariant){
		super(isStillVariant);
	}
	
	public Fluid getFlowing(){
		return ArcanaRegistry.FLOWING_TAINT_GOO;
	}
	
	public Fluid getStill(){
		return ArcanaRegistry.STILL_TAINT_GOO;
	}
	
	public TagKey<Fluid> getTag(){
		return ArcanaTags.TAINT_GOO_FLUID;
	}
	
	public int getMaxLevel(){
		return 4;
	}
	
	public String getTexturePath(){
		return "fluid/taint_goo";
	}
	
	public Optional<BlockState> interact(FluidState self, FluidState other){
		return other.isIn(FluidTags.LAVA) ? Optional.of(ArcanaRegistry.TAINT_CRUST.getDefaultState()) : Optional.empty();
	}
	
	public float getEntityPushStrength(Entity entity){
		return 0.001f;
	}
	
	public float getEntityDragFactor(Entity entity){
		return 0.1f;
	}
	
	public float getEntitySpeedFactor(Entity entity){
		return 0.017f;
	}
	
	public void onEntityInteractTick(Entity entity){
		if(entity instanceof LivingEntity lem && (lem.world.getTime() % 80 == 0 || !lem.hasStatusEffect(ArcanaRegistry.TAINTED)))
			lem.addStatusEffect(new StatusEffectInstance(ArcanaRegistry.TAINTED, 5 * 20));
	}
	
	protected boolean isInfinite(){
		return false;
	}
	
	protected int getFlowSpeed(WorldView world){
		return 2;
	}
	
	protected int getLevelDecreasePerBlock(WorldView world){
		return 1;
	}
	
	public Item getBucketItem(){
		return ArcanaRegistry.TAINT_GOO_BUCKET;
	}
	
	public int getTickRate(WorldView world){
		return 40;
	}
	
	protected BlockState toBlockState(FluidState state){
		return ArcanaRegistry.TAINT_GOO.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(state));
	}
}