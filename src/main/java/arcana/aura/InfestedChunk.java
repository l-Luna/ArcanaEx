package arcana.aura;

import arcana.Arcana;
import arcana.ArcanaConfig;
import arcana.components.ChunkLayer;
import arcana.util.MathUtil;
import arcana.util.SearchUtil;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import static arcana.Arcana.arcId;

public class InfestedChunk extends ChunkLayer implements ServerTickingComponent{
	
	public static final ComponentKey<InfestedChunk> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("infested_chunk"), InfestedChunk.class);
	
	private boolean dirty = false;
	
	public InfestedChunk(Chunk chunk){
		super(chunk);
	}
	
	public static InfestedChunk from(World world, BlockPos pos){
		return world.getChunk(pos).getComponent(KEY);
	}
	
	public static boolean isInfested(World world, BlockPos pos){
		return InfestedChunk.from(world, pos).isInfested(pos);
	}
	
	public static boolean setInfested(World world, BlockPos pos, boolean infested){
		return InfestedChunk.from(world, pos).setInfested(pos, infested);
	}
	
	public boolean isInfested(BlockPos pos){
		return isMarkedO(MathUtil.toChunkOffset(pos));
	}
	
	public boolean setInfested(BlockPos pos, boolean infested){
		boolean changed = setMarkedO(MathUtil.toChunkOffset(pos), infested);
		if(changed)
			markDirty();
		return changed;
	}
	
	public void markDirty(){
		dirty = true;
		chunk.setNeedsSaving(true);
	}
	
	public void sync(){
		chunk.syncComponent(KEY);
	}
	
	public void serverTick(){
		ArcanaConfig.TaintConfig config = Arcana.CONFIG.taintConfig;
		AuraChunk from = AuraChunk.from(chunk);
		if(chunk instanceof WorldChunk wc
				&& wc.getWorld().getRandom().nextInt(config.taintSpreadInvChance) == 0
				&& from.flux() > config.taintSpreadThreshold){
			World world = wc.getWorld();
			BlockPos offset = sampleO(world.getRandom());
			if(offset != null){
				BlockPos pos = offset.add(chunk.getPos().getStartPos());
				SearchUtil.randomSearch(world, pos, 1, 3, (there, state) -> {
					if(!isInfested(there)){
						setInfested(there, true);
						from.setFlux(from.flux() - config.infestCost);
						return true;
					}else if(!Taint.isBlockProtected(wc.getWorld(), there) && Taint.taintBlock(wc.getWorld(), there)){
						from.setFlux(from.flux() - config.taintCost);
						return true;
					}
					return false;
				});
			}
		}
		
		if(dirty){
			sync();
			dirty = false;
		}
	}
}