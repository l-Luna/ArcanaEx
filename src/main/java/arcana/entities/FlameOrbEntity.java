package arcana.entities;

import arcana.ArcanaDamageSources;
import arcana.ArcanaRegistry;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlameOrbEntity extends MagicOrbEntity{
	
	public FlameOrbEntity(EntityType<? extends ProjectileEntity> entityType, World world){
		super(entityType, world);
	}
	
	public void tick(){
		World w = getWorld();
		if(w.isClient && w.random.nextFloat() < 0.4f)
			w.addParticle(ArcanaRegistry.FLAME,
					getPos().x, getPos().y - 0.3, getPos().z,
					getVelocity().x + (w.random.nextDouble() - .5) * 0.04, getVelocity().y + 0.04, getVelocity().z + (w.random.nextDouble() - .5) * 0.04);
		super.tick();
	}
	
	public void burst(){
		// particle burst
		ServerWorld sw = (ServerWorld)getWorld();
		sw.spawnParticles(ArcanaRegistry.FLAME,
				getPos().getX(),
				getPos().getY(),
				getPos().getZ(),
				20, 0.1, 0.1, 0.1, 0.02f);
		// weak fire
		BlockPos here = getBlockPos();
		if(sw.getBlockState(here).isAir()){
			BlockState fireState = AbstractFireBlock.getState(sw, here);
			if(fireState.canPlaceAt(sw, here)){
				if(fireState.isOf(Blocks.FIRE))
					fireState = fireState.with(FireBlock.AGE, 15);
				sw.setBlockState(here, fireState);
			}
		}
		super.burst();
	}
	
	protected void damageEntity(LivingEntity target, Entity owner){
		// 2 base damage + 2 charged damage
		target.damage(ArcanaDamageSources.flameOrb(getEntityWorld(), this), 2 + 2 * getSize());
		target.setOnFireFor(3);
	}
}