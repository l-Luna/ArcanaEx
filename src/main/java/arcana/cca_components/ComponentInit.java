package arcana.cca_components;

import arcana.aura.AuraChunk;
import arcana.aura.AuraWorld;
import arcana.aura.InfestedChunk;
import arcana.aura.WardedChunk;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.chunk.ChunkComponentInitializer;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

public class ComponentInit implements EntityComponentInitializer, WorldComponentInitializer, ChunkComponentInitializer{
	
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry){
		registry.registerForPlayers(Researcher.KEY, Researcher::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(Caster.KEY, Caster::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(RunicShielding.KEY, RunicShielding::new, RespawnCopyStrategy.LOSSLESS_ONLY);
		registry.registerFor(ItemEntity.class, KdItem.KEY, __ -> new KdItem());
		registry.registerFor(ArrowEntity.class, CaArrow.KEY, __ -> new CaArrow());
	}
	
	public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry){
		registry.register(AuraWorld.KEY, AuraWorld::new);
		registry.register(MagicMirrorQueue.KEY, __ -> new MagicMirrorQueue());
	}
	
	public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry){
		registry.register(AuraChunk.KEY, AuraChunk::new);
		registry.register(InfestedChunk.KEY, InfestedChunk::new);
		registry.register(WardedChunk.KEY, WardedChunk::new);
	}
}