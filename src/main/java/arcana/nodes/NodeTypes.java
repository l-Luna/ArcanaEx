package arcana.nodes;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.util.Identifier;

import static arcana.Arcana.arcId;

public class NodeTypes{
	
	public static final BiMap<Identifier, NodeType> NODE_TYPES = HashBiMap.create();
	
	public static final NodeType
			NORMAL = create("normal", 18 * 20, 20),
			BRIGHT = create("bright", 10 * 20, 35),
			FADING = create("fading", 26 * 20, 13);
	
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