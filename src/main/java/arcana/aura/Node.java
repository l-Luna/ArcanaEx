package arcana.aura;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.util.UUID;

public class Node implements Position{
	
	private static final Logger logger = LogUtils.getLogger();
	public static final double HALF_NODE = .7;
	
	private NodeType type;
	private double x, y, z;
	
	private int ticksUntilRecharge;
	private AspectMap aspects, aspectCap;
	private NbtCompound tag;
	
	private UUID uuid;
	
	private AuraChunk chunk;
	
	public Node(NodeType type, Position pos, AspectMap aspectCap){
		this.type = type;
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		aspects = new AspectMap();
		this.aspectCap = aspectCap;
		tag = new NbtCompound();
		uuid = UUID.randomUUID();
	}
	
	public Node(Node node){
		type = node.type;
		x = node.x;
		y = node.y;
		z = node.z;
		ticksUntilRecharge = node.ticksUntilRecharge;
		aspects = node.aspects.copy();
		aspectCap = node.aspectCap.copy();
		tag = node.tag.copy();
		chunk = node.chunk;
		uuid = UUID.randomUUID();
	}
	
	//
	
	public void tick(World world){
		if(ticksUntilRecharge <= 0){
			ticksUntilRecharge = type.rechargeTime() + world.random.nextBetween(-3 * 20, 3 * 20);
			doRecharge(world.random);
		}
		ticksUntilRecharge--;
		
		if(type.ticker() != null)
			type.ticker().accept(this, world);
	}
	
	public void damage(boolean degrade, Random rng){
		for(int i = 0; i < 2; i++){
			Aspect aspect = Util.getRandom(aspectCap.aspectSet().stream().toList(), rng);
			aspectCap.take(aspect, rng.nextBetween(2, 5));
		}
		
		if(degrade || aspectCap.isEmpty()){
			NodeType nextType = NodeTypes.weakerType(type);
			if(nextType == null){
				destroy(true);
				return;
			}else
				type = nextType;
		}
		
		markDirty();
	}
	
	public void enhance(boolean upgrade, Random rng){
		if(upgrade){
			type = NodeTypes.strongerType(type);
			for(int i = 0; i < 4; i++)
				aspectCap.add(Util.getRandom(aspectCap.aspectSet().stream().toList(), rng), rng.nextBetween(1, 4));
		}
		
		doRecharge(rng);
		if(rng.nextInt(20) == 0)
			aspectCap.addCapped(Util.getRandom(Aspects.primals, rng), rng.nextBetween(1, 3), type.aspectCap());
		
		markDirty();
	}
	
	public void destroy(boolean effects){
		chunk.removeNode(this);
		if(effects){
			// particles...
		}
	}
	
	//
	
	private void doRecharge(Random rng){
		// add 2-5 of 3 aspects in our cap
		for(int i = 0; i < 3; i++){
			Aspect aspect = Util.getRandom(aspectCap.aspectSet().stream().toList(), rng);
			aspects.addCapped(aspect, rng.nextBetween(2, 5), aspectCap.get(aspect));
		}
		markDirty();
	}
	
	//
	
	public NbtCompound toNbt(){
		NbtCompound c = new NbtCompound();
		c.putString("type", type.id().toString());
		c.putDouble("x", x);
		c.putDouble("y", y);
		c.putDouble("z", z);
		
		c.putInt("ticksUntilRecharge", ticksUntilRecharge);
		c.put("aspects", aspects.toNbt());
		c.put("aspectCap", aspectCap.toNbt());
		if(tag != null)
			c.put("tag", tag);
		
		c.putUuid("uuid", uuid);
		
		return c;
	}
	
	public static Node fromNbt(NbtCompound nbt){
		var pos = new Vec3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
		NodeType nodeType = NodeTypes.byName(new Identifier(nbt.getString("type")));
		var node = new Node(nodeType, pos, AspectMap.fromNbt(nbt.getCompound("aspectCap")));
		node.ticksUntilRecharge = nbt.getInt("ticksUntilRecharge");
		node.uuid = nbt.getUuid("uuid");
		node.aspects = AspectMap.fromNbt(nbt.getCompound("aspects"));
		if(nbt.contains("tag"))
			node.tag = nbt.getCompound("tag");
		return node;
	}
	
	public BlockPos asBlockPos(){
		return new BlockPos(x, y, z);
	}
	
	public Vec3d asVec3d(){
		return new Vec3d(x, y, z);
	}
	
	public Box bounds(){
		return new Box(x - HALF_NODE, y - HALF_NODE, z - HALF_NODE, x + HALF_NODE, y + HALF_NODE, z + HALF_NODE);
	}
	
	public NodeType getType(){
		return type;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double getZ(){
		return z;
	}
	
	public UUID getUuid(){
		return uuid;
	}
	
	public AspectMap getAspects(){
		return aspects;
	}
	
	public AspectMap getAspectCap(){
		return aspectCap;
	}
	
	public NbtCompound getOrCreateTag(){
		if(tag == null)
			tag = new NbtCompound();
		return tag;
	}
	
	public NbtCompound getTag(){
		return tag;
	}
	
	public void setTag(NbtCompound tag){
		this.tag = tag;
	}
	
	public void setChunk(AuraChunk chunk){
		this.chunk = chunk;
	}
	
	public void markDirty(){
		if(chunk != null)
			chunk.markDirty();
		else
			logger.error("Tried to mark dirty node with no set AuraChunk (UUID {})", uuid);
	}
	
	public int hashCode(){
		return uuid.hashCode();
	}
	
	public boolean equals(Object obj){
		return obj instanceof Node other && other.uuid.equals(uuid);
	}
}