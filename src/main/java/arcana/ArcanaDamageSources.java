package arcana;

import arcana.entities.FlameOrbEntity;
import arcana.entities.PrismaticOrbEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public class ArcanaDamageSources{
	
	public static final RegistryKey<DamageType> PUTREFACTION_KEY = typeKey("putrefaction");
	public static final RegistryKey<DamageType> HUNGRY_NODE_KEY = typeKey("hungry_node");
	
	public static final RegistryKey<DamageType> PRISMATIC_LIGHT_KEY = typeKey("prismatic_light");
	public static final RegistryKey<DamageType> FLAME_ORB_KEY = typeKey("flame_orb");
	
	public static DamageSource putrefaction(World world){
		return world.getDamageSources().create(PUTREFACTION_KEY);
	}
	
	public static DamageSource hungryNode(World world){
		return world.getDamageSources().create(HUNGRY_NODE_KEY);
	}
	
	public static DamageSource prismaticLight(World world, PrismaticOrbEntity projectile){
		return createProjectile(world, PRISMATIC_LIGHT_KEY, projectile);
	}
	
	public static DamageSource flameOrb(World world, FlameOrbEntity projectile){
		return createProjectile(world, FLAME_ORB_KEY, projectile);
	}
	
	private static DamageSource createProjectile(World world, RegistryKey<DamageType> type, ProjectileEntity projectile){
		Entity owner = projectile.getOwner();
		return owner != null ? world.getDamageSources().create(type, projectile, owner) : world.getDamageSources().create(type, projectile);
	}
	
	private static RegistryKey<DamageType> typeKey(String id){
		return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, arcId(id));
	}
}