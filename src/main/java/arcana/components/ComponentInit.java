package arcana.components;

import arcana.aura.AuraChunk;
import arcana.aura.AuraWorld;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.chunk.ChunkComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ArrowEntity;

public class ComponentInit implements EntityComponentInitializer, WorldComponentInitializer, ChunkComponentInitializer{
	
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry){
		registry.registerForPlayers(Researcher.KEY, Researcher::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerFor(ItemEntity.class, KdItem.KEY, __ -> new KdItem());
		registry.registerFor(ArrowEntity.class, CaArrow.KEY, __ -> new CaArrow());
	}
	
	public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry){
		registry.register(AuraWorld.KEY, AuraWorld::new);
	}
	
	public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry){
		registry.register(AuraChunk.KEY, AuraChunk::new);
	}
}