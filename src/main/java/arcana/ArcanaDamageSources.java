package arcana;

import net.minecraft.entity.damage.DamageSource;

public class ArcanaDamageSources{
	
	public static final DamageSource PUTREFACTION = new DamageSource("arcana.putrefaction")
			.setBypassesArmor()
			.setUsesMagic();
	
	public static final DamageSource HUNGRY_NODE = new DamageSource("arcana.hungry_node");
}