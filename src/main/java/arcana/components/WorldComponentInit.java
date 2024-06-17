package arcana.components;

import arcana.aura.AuraWorld;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import org.jetbrains.annotations.NotNull;

public final class WorldComponentInit implements WorldComponentInitializer{
	
	public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry){
		registry.register(AuraWorld.KEY, AuraWorld::new);
	}
}