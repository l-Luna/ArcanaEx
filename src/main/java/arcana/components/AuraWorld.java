package arcana.components;

import arcana.nodes.Node;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

@ParametersAreNonnullByDefault
public class AuraWorld implements Component, CommonTickingComponent{
	
	public static final ComponentKey<AuraWorld> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(arcId("aura_world"), AuraWorld.class);
	
	private final List<Node> nodes = new ArrayList<>();
	private World world;
	
	public AuraWorld(World world){
		this.world = world;
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
	
	public List<Node> getNodes(){
		return nodes;
	}
	
	public void tick(){
		for(Node node : getNodes())
			node.tick();
	}
}