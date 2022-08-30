package arcana.components;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class EntityComponentInit implements EntityComponentInitializer{
	
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry){
		registry.registerForPlayers(Researcher.KEY, Researcher::new, RespawnCopyStrategy.ALWAYS_COPY);
	}
}