package arcana.nodes;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.Arcana.arcId;

public class NodeTypes{
	
	public static final BiMap<Identifier, NodeType> NODE_TYPES = HashBiMap.create();
	
	public static final NodeType
			NORMAL = create("normal", 43 * 20, 20),
			BRIGHT = create("bright", 33 * 20, 35),
			FADING = create("fading", 65 * 20, 13),
	
			HUNGRY = create("hungry", 40 * 20, 22),
			ELDRITCH = create("eldritch", 49 * 20, 18);
	
	public static final List<NodeType> normalTypes = List.of(NORMAL, BRIGHT, FADING);
	public static final List<NodeType> specialTypes = List.of(HUNGRY, ELDRITCH);
	
	private static NodeType create(String id, int rechargeTime, int aspectCap){
		Identifier identifier = arcId(id);
		NodeType type = new NodeType(identifier, rechargeTime, aspectCap);
		NODE_TYPES.put(identifier, type);
		return type;
	}
	
	public static NodeType byName(Identifier id){
		return NODE_TYPES.get(id);
	}
}