package arcana.research.puzzles;

import arcana.aspects.Aspect;
import arcana.aspects.Aspects;
import arcana.research.Puzzle;
import arcana.util.HexPos;
import arcana.util.NbtUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static arcana.Arcana.arcId;

public class Lighthouses extends Puzzle{
	
	public record Obstacle(@Nullable Aspect aspect, HexPos pos, int count, boolean solid, boolean needsNeighbor){
		public NbtCompound toNbt(){
			return NbtUtil.from(Map.of(
					"aspect", aspect == null ? "" : aspect.id(),
					"pos", pos,
					"count", count,
					"solid", solid,
					"needsNeighbor", needsNeighbor
			));
		}
		
		public static Obstacle fromNbt(NbtCompound data){
			return new Obstacle(
					Aspects.byName(data.getString("aspect")),
					HexPos.fromLong(data.getLong("pos")),
					data.getInt("count"),
					data.getBoolean("solid"),
					data.getBoolean("needsNeighbor")
			);
		}
		
		public static Obstacle fromJson(JsonObject data){
			return new Obstacle(
					Aspects.byName(JsonHelper.getString(data, "aspect", "")),
					HexPos.fromString(JsonHelper.getString(data, "pos")),
					JsonHelper.getInt(data, "count", 1),
					JsonHelper.getBoolean(data, "solid", false),
					JsonHelper.getBoolean(data, "needsNeighbor", false)
			);
		}
	}
	
	public static final Identifier TYPE = arcId("lighthouses");
	
	private final List<Obstacle> obstacles;
	private final int size;
	
	public Lighthouses(List<Obstacle> obstacles, int size){
		this.obstacles = obstacles;
		this.size = size;
	}
	
	public Lighthouses(NbtCompound data){
		obstacles = NbtUtil.readList(data, "obstacles", Obstacle::fromNbt);
		size = data.getInt("size");
	}
	
	public Lighthouses(JsonObject data){
		obstacles = new ArrayList<>();
		for(JsonElement elem : data.getAsJsonArray("obstacles"))
			obstacles.add(Obstacle.fromJson(elem.getAsJsonObject()));
		size = JsonHelper.getInt(data, "size", 3);
	}
	
	public Identifier type(){
		return TYPE;
	}
	
	public NbtCompound getInitialNoteTag(ServerPlayerEntity player){
		return super.getInitialNoteTag(player);
	}
	
	public NbtCompound data(){
		NbtCompound data = new NbtCompound();
		
		NbtList obstacleList = new NbtList();
		obstacles.stream().map(Obstacle::toNbt).forEach(obstacleList::add);
		data.put("obstacles", obstacleList);
		
		data.putInt("size", size);
		
		return data;
	}
	
	public boolean validate(NbtCompound noteData){
		return false;
	}
}