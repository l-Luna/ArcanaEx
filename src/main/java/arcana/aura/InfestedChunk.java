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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;

import java.util.EnumSet;

import static arcana.Arcana.arcId;

public class InfestedChunk extends ChunkLayer implements ServerTickingComponent{
	
	public static final ComponentKey<InfestedChunk> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("infested_chunk"), InfestedChunk.class);
	
	private boolean dirty = false;
	
	public InfestedChunk(Chunk chunk){
		super(chunk);
	}
	
	public static InfestedChunk from(World world, BlockPos pos){
		if(!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4))
			return null;
		return KEY.getNullable(world.getChunk(pos));
	}
	
	public static boolean isInfested(World world, BlockPos pos){
		InfestedChunk from = InfestedChunk.from(world, pos);
		return from != null && from.isInfested(pos);
	}
	
	public static boolean setInfested(World world, BlockPos pos, boolean infested){
		InfestedChunk from = InfestedChunk.from(world, pos);
		return from != null && from.setInfested(pos, infested);
	}
	
	public static float infestationDensity(World world, Vec3d position){
		Vec3d in = position.subtract(position.multiply(1/16d).floorAlongAxes(EnumSet.allOf(Direction.Axis.class)).multiply(16))
				.subtract(8, 8, 8)
				.multiply(1 / 16d);
		double acc = 0;
		for(int xD = 0; xD < 2; xD++){
			for(int yD = 0; yD < 2; yD++){
				for(int zD = 0; zD < 2; zD++){
					// where 0 = near, 1 = far
					InfestedChunk there = from(world, new BlockPos(position).add(16 * xD * Math.signum(in.x), 0, 16 * zD * Math.signum(in.z)));
					if(there == null)
						continue;
					double contribution =
							(xD*Math.abs(in.x) + (1 - xD)*(1 - Math.abs(in.x))) *
							(yD*Math.abs(in.y) + (1 - yD)*(1 - Math.abs(in.y))) *
							(zD*Math.abs(in.z) + (1 - zD)*(1 - Math.abs(in.z)));
					acc += contribution * there.infestationDensity((int)(position.y + 16 * yD * Math.signum(in.y)));
				}
			}
		}
		return (float)acc;
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
	
	public float infestationDensity(int sectionHeight){
		return countO((sectionHeight - chunk.getBottomY()) / 16) / (float)(16*16*16);
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
					if(state.isAir())
						return false;
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