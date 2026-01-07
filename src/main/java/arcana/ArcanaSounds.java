package arcana;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.registry.Registry;

import static arcana.Arcana.arcId;

public class ArcanaSounds{
	
	public static final SoundEvent RUNIC_SHIELDING_HIT = new SoundEvent(arcId("runic_shielding_hit"));
	
	public static void setup(){
		register(RUNIC_SHIELDING_HIT);
	}
	
	private static void register(SoundEvent event){
		Registry.register(Registry.SOUND_EVENT, event.getId(), event);
	}
}