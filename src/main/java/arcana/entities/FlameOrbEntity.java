package arcana.entities;

import arcana.ArcanaDamageSources;
import arcana.ArcanaRegistry;
import arcana.util.SearchUtil;
import net.minecraft.block.AbstractFireBlock;
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
		// randomly placed fire
		for(int i = 0; i < 2; i++){
			SearchUtil.vRandomSearch(sw, getBlockPos(), 3, 3, 6, (where, what) -> {
				BlockPos up = where.up();
				if(what.isAir() || !what.getFluidState().isEmpty() || !sw.getBlockState(up).isAir())
					return false;
				sw.setBlockState(up, AbstractFireBlock.getState(sw, up));
				return true;
			});
		}
		super.burst();
	}
	
	protected void damageEntity(LivingEntity target, Entity owner){
		// 2 base damage + 2 charged damage
		target.damage(ArcanaDamageSources.flameOrb(getEntityWorld(), this), 2 + 2 * getSize());
		target.setOnFireFor(3);
	}
}