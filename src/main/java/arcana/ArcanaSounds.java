package arcana;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

import static arcana.Arcana.arcId;

public class ArcanaSounds{
	
	public static final SoundEvent RUNIC_SHIELDING_HIT = SoundEvent.of(arcId("runic_shielding_hit"));
	
	public static final SoundEvent HURT_BURNING_POWERFUL = SoundEvent.of(arcId("hurt_burning_powerful"));
	public static final SoundEvent HURT_PRISMATIC_LIGHT = SoundEvent.of(arcId("hurt_prismatic_light"));
	public static final SoundEvent HURT_PUTREFACTION = SoundEvent.of(arcId("hurt_putrefaction"));
	
	public static void setup(){
		register(RUNIC_SHIELDING_HIT);
		register(HURT_BURNING_POWERFUL);
		register(HURT_PRISMATIC_LIGHT);
		register(HURT_PUTREFACTION);
	}
	
	private static void register(SoundEvent event){
		Registry.register(Registries.SOUND_EVENT, event.getId(), event);
	}
}