package arcana.research.puzzles;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.research.Puzzle;
import arcana.util.StreamUtil;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static arcana.Arcana.arcId;

public class Chemistry extends Puzzle{

	public static final Identifier TYPE = arcId("chemistry");
	
	private final List<Aspect> nodes;
	private final int size, flux;
	
	public Chemistry(List<Aspect> nodes, int size, int flux){
		this.nodes = nodes;
		this.size = size;
		this.flux = flux;
	}
	
	public Chemistry(NbtCompound data){
		nodes = StreamUtil.streamAndApply(
				data.getList("nodes", NbtElement.STRING_TYPE), NbtString.class,
				x -> Aspects.byName(x.asString())).toList();
		size = data.getInt("size");
		flux = data.getInt("flux");
	}
	
	public Chemistry(JsonObject obj){
		nodes = new ArrayList<>();
		for(JsonElement nodeElem : obj.getAsJsonArray("nodes"))
			nodes.add(Aspects.byName(nodeElem.getAsString()));
		
		size = JsonHelper.getInt(obj, "size", 4);
		flux = JsonHelper.getInt(obj, "flux", 0);
	}
	
	public NbtCompound getInitialNoteTag(){
		NbtCompound tag = new NbtCompound();
		AspectMap aspects = new AspectMap();
		Random rng = new Random();
		for(Aspect primal : Aspects.primals)
			aspects.add(primal, rng.nextInt(9, 17));
		tag.put("stored_aspects", aspects.toNbt());
		return tag;
	}
	
	public List<Aspect> getNodes(){
		return nodes;
	}
	
	public int getSize(){
		return size;
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound data(){
		NbtCompound data = new NbtCompound();
		
		NbtList nodeList = new NbtList();
		for(Aspect node : nodes)
			nodeList.add(NbtString.of(node.id().toString()));
		data.put("nodes", nodeList);
		
		data.putInt("size", size);
		data.putInt("flux", flux);
		
		return data;
	}
	
	// validation
	
	@SuppressWarnings("UnstableApiUsage")
	public boolean validate(NbtCompound puzzleData){
		// transform the hex grid NBT into a graph
		int edgeHexes = (size - 1) * 6;
		int nodeGap = edgeHexes / getNodes().size();
		NbtCompound gridTag = puzzleData.getCompound("grid_aspects");
		// fill grid
		Map<HexOffset, AspectGraphNode> grid = new HashMap<>(gridTag.getKeys().size() + getNodes().size());
		AtomicReference<AspectGraphNode> firstNode = new AtomicReference<>();
		processHexes(getSize(), 0, 0, (_x, _y, turn, rx, ry) -> {
			if(turn % nodeGap == 0){
				var value = new AspectGraphNode(getNodes().get(turn / nodeGap), true);
				firstNode.set(value);
				grid.put(new HexOffset(rx, ry), value);
				return false;
			}
			String hexId = rx + "," + ry;
			if(gridTag.contains(hexId))
				grid.put(new HexOffset(rx, ry), new AspectGraphNode(Aspects.byName(gridTag.getString(hexId)), false));
			return false;
		});
		// find connections
		MutableGraph<AspectGraphNode> graph = GraphBuilder.undirected().build();
		processHexes(getSize(), 0, 0, (xPos, yPos, turn, rx, ry) -> {
			var pos = new HexOffset(rx, ry);
			AspectGraphNode self = grid.get(pos);
			if(self != null){
				Aspect aspect = self.aspect;
				for(HexOffset offset : neighborsByRow(ry)){
					HexOffset neighborPos = pos.add(offset);
					AspectGraphNode neighbor = grid.get(neighborPos);
					if(neighbor != null){
						Aspect nAspect = neighbor.aspect;
						if(aspect.equals(nAspect.left()) || aspect.equals(nAspect.right())
							|| nAspect.equals(aspect.left()) || nAspect.equals(aspect.right()))
							graph.putEdge(self, neighbor);
					}
				}
			}
			return false;
		});
		
		// export the graph to Mermaid and print to stdout for debugging
		if(false){
			StringBuilder sb = new StringBuilder();
			for(EndpointPair<AspectGraphNode> edge : graph.edges()){
				var u = edge.nodeU();
				var v = edge.nodeV();
				sb.append(System.identityHashCode(u)).append("::").append(u.aspect.id())
						.append(" --> ")
						.append(System.identityHashCode(v)).append("::").append(v.aspect.id())
						.append("\n");
			}
			System.out.println(sb);
		}
		
		// traverse the graph to count reachable connections
		if(firstNode.get() != null && graph.nodes().contains(firstNode.get())){
			int found = 0;
			for(AspectGraphNode node : Traverser.forGraph(graph).depthFirstPreOrder(firstNode.get())){
				if(node.isPuzzleNode)
					found++;
			}
			return found == getNodes().size();
		}
		return false; // if it doesn't even have the first node (i.e. has no edges), it's definitely incomplete
	}
	
	// utilities used by rendering and validation
	
	public static boolean processHexes(int size, int x, int y, HexConsumer application){
		for(int ry = 0; ry < size; ry++){
			for(int side : List.of(1, -1)){
				if(ry == 0 && side == -1) continue; // don't process middle row twice
				int rowWidth = size * 2 - ry - 1;
				int yOffset = side * (side - ry) * 17;
				for(int rx = 0; rx < rowWidth; rx++){
					var xPos = x + 195 + (int)((rowWidth / 2f - rx) * 22);
					var yPos = y + 60 + yOffset;
					
					int turn = -1;
					if(ry == size - 1 || rx == 0 || rx == rowWidth - 1){
						if(ry == size - 1)
							turn = side == -1 ? rx : (size - 1) * 4 - rx;
						else if(rx == 0)
							turn = (size - 1) * 5 + ry * side;
						else
							turn = (size - 1) * 2 + ry * side;
					}
					
					if(application.accept(xPos, yPos, turn, rx, ry * side))
						return true;
				}
			}
		}
		return false;
	}
	
	// a "vertical" line changes direction in the middle with this "amazing" coordinate system
	public static List<HexOffset> neighborsByRow(int ry){
		return switch((int)Math.signum(ry)){
			case 1 -> List.of(
					new HexOffset(1, 0, 3),
					new HexOffset(-1, 0, 0),
					new HexOffset(0, 1, 4),
					new HexOffset(0, -1, 1),
					new HexOffset(1, -1, 2),
					new HexOffset(-1, 1, 5)
			);
			case -1 -> List.of(
					new HexOffset(1, 0, 3),
					new HexOffset(-1, 0, 0),
					new HexOffset(0, 1, 5),
					new HexOffset(0, -1, 2),
					new HexOffset(1, 1, 4),
					new HexOffset(-1, -1, 1)
			);
			case 0 -> List.of(
					new HexOffset(1, 0, 3),
					new HexOffset(-1, 0, 0),
					new HexOffset(0, 1, 4),
					new HexOffset(0, -1, 2),
					new HexOffset(-1, 1, 5),
					new HexOffset(-1, -1, 1)
			);
			default -> throw new IllegalStateException("how");
		};
	}
	
	@FunctionalInterface
	public interface HexConsumer{
		boolean accept(int xPos, int yPos, int turn, int rx, int ry);
	}
	
	public record HexOffset(int x, int y, int turn /*from right*/){
		public HexOffset(int x, int y){
			this(x, y, -1);
		}
		public HexOffset add(HexOffset other){
			return new HexOffset(other.x + x, other.y + y, other.turn);
		}
		
		public boolean equals(Object obj){ // don't check turn for equality
			return obj instanceof HexOffset hx && hx.x == x && hx.y == y;
		}
		
		public int hashCode(){
			return Objects.hash(x, y);
		}
	}
	
	// we rely on identity comparisons in the graph
	// we could add the hex position as an additional record component,
	// or override equals/hashCode to use identity,
	// but that would be more complicated than just Not
	@SuppressWarnings("ClassCanBeRecord")
	private static final class AspectGraphNode{
		private final Aspect aspect;
		private final boolean isPuzzleNode;
		
		private AspectGraphNode(Aspect aspect, boolean isPuzzleNode){
			this.aspect = aspect;
			this.isPuzzleNode = isPuzzleNode;
		}
		
		public String toString(){
			return "AspectGraphNode[" +
					"aspect=" + aspect + ", " +
					"isPuzzleNode=" + isPuzzleNode + ']';
		}
	}
}