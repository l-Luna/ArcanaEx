package arcana.components;

import arcana.nodes.Node;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static arcana.Arcana.arcId;

@ParametersAreNonnullByDefault
public final class AuraWorld implements Component, CommonTickingComponent, AutoSyncedComponent{
	
	public static final ComponentKey<AuraWorld> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("aura_world"), AuraWorld.class);
	
	private final List<Node> nodes = new ArrayList<>();
	private final World world;
	
	public AuraWorld(World world){
		this.world = world;
	}
	
	public static AuraWorld from(World world){
		return world.getComponent(KEY);
	}
	
	public static AuraWorld from(StructureWorldAccess swa){
		if(swa instanceof ServerWorld sw)
			return from((World)sw);
		else if(swa instanceof ChunkRegion cr)
			return from((World)cr.toServerWorld());
		throw new IncompatibleClassChangeError();
	}
	
	public void readFromNbt(NbtCompound tag){
		nodes.clear();
		tag.getList("nodes", NbtElement.COMPOUND_TYPE).forEach(x -> nodes.add(Node.fromNbt((NbtCompound)x, world)));
	}
	
	public void writeToNbt(NbtCompound tag){
		NbtList list = new NbtList();
		for(Node node : nodes)
			list.add(node.toNbt());
		tag.put("nodes", list);
	}
	
	public void tick(){
		for(Node node : getNodes())
			node.tick();
	}
	
	// TODO: methods for only syncing one node, nodes in a chunk...
	public void sync(){
		world.syncComponent(KEY);
	}
	
	// "public" API
	
	public List<Node> getNodes(){
		return nodes;
	}
	
	public World getWorld(){
		return world;
	}
	
	public List<Node> getNodesInBounds(Box bounds){
		return filterBounds(getNodes(), bounds);
	}
	
	public static List<Node> filterBounds(List<Node> nodes, Box bounds){
		List<Node> ret = new ArrayList<>();
		for(Node node : nodes)
			if(bounds.contains(node.asVec3d()))
				ret.add(node);
		return ret;
	}
	
	public Optional<Node> raycast(Position from, double length, boolean ignoreBlocks, Entity viewer){
		return raycast(getNodes(), from, length, ignoreBlocks, viewer);
	}
	
	public static Optional<Node> raycast(List<Node> nodes, Position fromPos, double length, boolean ignoreBlocks, Entity viewer){
		Vec3d from = new Vec3d(fromPos.getX(), fromPos.getY(), fromPos.getZ());
		Vec3d to = from.add(viewer.getRotationVector().multiply(length));
		BlockHitResult bhr = null;
		if(!ignoreBlocks)
			bhr = viewer.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, viewer));
		Box bounds = new Box(from, to);
		Node ret = null;
		double curDist = length;
		for(Node node : filterBounds(nodes, bounds)){
			Optional<Vec3d> hit = node.bounds().raycast(from, to);
			if(hit.isPresent()){ // TODO: use squared distance in comparisons? skip nodes based on block hit?
				double dist = from.distanceTo(hit.get());
				if(dist < curDist){
					ret = node;
					curDist = dist;
				}
			}
		}
		if(!ignoreBlocks)
			if(bhr.getPos().distanceTo(from) < curDist)
				return Optional.empty(); // blocked by a block
		return Optional.ofNullable(ret);
	}
}