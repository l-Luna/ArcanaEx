package arcana;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

import static arcana.Arcana.arcId;

public class ArcanaSounds{
	
	public static final SoundEvent RUNIC_SHIELDING_HIT = SoundEvent.of(arcId("runic_shielding_hit"));
	
	public static void setup(){
		register(RUNIC_SHIELDING_HIT);
	}
	
	private static void register(SoundEvent event){
		Registry.register(Registries.SOUND_EVENT, event.getId(), event);
	}
}