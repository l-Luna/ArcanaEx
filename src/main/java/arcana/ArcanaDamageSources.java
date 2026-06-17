package arcana;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import static arcana.Arcana.arcId;

public class ArcanaDamageSources{
	
	public static final RegistryKey<DamageType> PUTREFACTION_KEY = typeKey("putrefaction");
	public static final RegistryKey<DamageType> HUNGRY_NODE_KEY = typeKey("hungry_node");
	
	public static DamageSource putrefaction(World world){
		return world.getDamageSources().create(PUTREFACTION_KEY);
	}
	
	public static DamageSource hungryNode(World world){
		return world.getDamageSources().create(HUNGRY_NODE_KEY);
	}
	
	private static RegistryKey<DamageType> typeKey(String id){
		return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, arcId(id));
	}
}