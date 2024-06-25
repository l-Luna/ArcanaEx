package arcana.entities;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspects;
import arcana.aura.AuraChunk;
import arcana.aura.FluxOrigin;
import arcana.aura.Taint;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

public class ThrownTaintBottleEntity extends ThrownItemEntity{
	
	// used by entity type
	public ThrownTaintBottleEntity(EntityType<? extends ThrownItemEntity> entityType, World world){
		super(entityType, world);
	}
	
	// used by dispenser behaviour
	public ThrownTaintBottleEntity(double d, double e, double f, World world){
		super(ArcanaRegistry.THROWN_TAINT_BOTTLE, d, e, f, world);
	}
	
	// used by item
	public ThrownTaintBottleEntity(LivingEntity livingEntity, World world){
		super(ArcanaRegistry.THROWN_TAINT_BOTTLE, livingEntity, world);
	}
	
	protected void onEntityHit(EntityHitResult entityHitResult){
		super.onEntityHit(entityHitResult);
		collide();
	}
	
	protected void onCollision(HitResult hitResult){
		super.onCollision(hitResult);
		collide();
	}
	
	private void collide(){
		if(!world.isClient){
			Random rng = world.random;
			int tainted = 0;
			// aim to taint 6 blocks within a 5x3x5 area, fail after 12 attempts
			BlockPos.Mutable pos = new BlockPos.Mutable();
			for(int tries = 0; tries < 12 && tainted < 6; tries++){
				pos.set(getBlockPos()).move(rng.nextInt(5) - 2, rng.nextInt(3) - 1, rng.nextInt(5) - 2);
				// TODO: pure node protection
				var newState = Taint.taintBlock(world.getBlockState(pos));
				if(newState.isPresent()){
					world.setBlockState(pos, newState.get());
					tainted++;
				}
			}
			
			// add flux
			AuraChunk.at(world, getBlockPos()).incrementFlux(rng.nextInt(3) + 3 + (6 - tainted), FluxOrigin.TAINT_IN_A_BOTTLE);
			// add particles
			int i = WorldEvents.INSTANT_SPLASH_POTION_SPLASHED;
			world.syncWorldEvent(i, getBlockPos(), Aspects.TAINT.colour());
			// and disappear
			discard();
		}
	}
	
	protected Item getDefaultItem(){
		return ArcanaRegistry.TAINT_IN_A_BOTTLE;
	}
}