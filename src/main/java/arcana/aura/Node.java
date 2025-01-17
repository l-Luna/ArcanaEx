package arcana.aura;

import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class Node implements Position{

	private static final double HALF_NODE = .7;
	
	private NodeType type;
	private double x, y, z;
	
	private int ticksUntilRecharge;
	private AspectMap aspects, aspectCap;
	private NbtCompound tag;
	
	private World world;
	private UUID uuid;
	
	public Node(NodeType type, World world, Position pos){
		this.type = type;
		this.world = world;
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		aspects = new AspectMap();
		aspectCap = new AspectMap();
		tag = new NbtCompound();
		uuid = UUID.randomUUID();
	}
	
	public Node(NodeType type, World world, Position pos, Random random){
		this.type = type;
		this.world = world;
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		aspects = new AspectMap();
		aspectCap = new AspectMap();
		tag = new NbtCompound();
		uuid = UUID.randomUUID();
		
		// it's a bit weird to put random generation here, but `fromNbt` would overwrite this immediately anyways
		randomiseCap(random);
	}
	
	public Node(Node node){
		type = node.type;
		world = node.world;
		x = node.x;
		y = node.y;
		z = node.z;
		ticksUntilRecharge = node.ticksUntilRecharge;
		aspects = node.aspects.copy();
		aspectCap = node.aspectCap.copy();
		tag = node.tag.copy();
		uuid = UUID.randomUUID();
	}
	
	public void tick(){
		if(ticksUntilRecharge <= 0){
			ticksUntilRecharge = type.rechargeTime() + world.random.nextBetween(-3 * 20, 3 * 20);
			doRecharge();
		}
		ticksUntilRecharge--;
		
		if(type.ticker() != null)
			type.ticker().accept(this);
	}
	
	protected void doRecharge(){
		// add 2-5 of 3 aspects in our cap
		var rng = world.random;
		for(int i = 0; i < 3; i++){
			Aspect aspect = Util.getRandom(aspectCap.aspectSet().stream().toList(), rng);
			aspects.addCapped(aspect, rng.nextBetween(2, 5), aspectCap.get(aspect));
		}
	}
	
	protected void randomiseCap(Random random){
		// at least 1 aspect at full capacity; 2 at half-full; 3 at 0-half
		// with a 1/7 chance of an extra non-primal aspect
		int cap = type.aspectCap();
		List<Aspect> primals = Util.copyShuffled(Aspects.primals.stream(), random);
		aspectCap.add(primals.get(0), cap);
		aspectCap.add(primals.get(1), random.nextBetween(cap/2, cap));
		aspectCap.add(primals.get(2), random.nextBetween(cap/2, cap));
		aspectCap.add(primals.get(3), random.nextBetween(0, cap/2));
		aspectCap.add(primals.get(4), random.nextBetween(0, cap/2));
		aspectCap.add(primals.get(5), random.nextBetween(0, cap/2));
		if(random.nextInt(7) == 0)
			aspectCap.add(Util.getRandom(Aspects.aspects.values().stream().toList(), random), random.nextBetween(cap/2, cap));
	}
	
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
	
	public static Node fromNbt(NbtCompound nbt, World world){
		var pos = new Vec3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
		NodeType nodeType = NodeTypes.byName(new Identifier(nbt.getString("type")));
		var node = new Node(nodeType, world, pos);
		node.ticksUntilRecharge = nbt.getInt("ticksUntilRecharge");
		node.uuid = nbt.getUuid("uuid");
		node.aspects = AspectMap.fromNbt(nbt.getCompound("aspects"));
		if(nbt.contains("aspectCap"))
			node.aspectCap = AspectMap.fromNbt(nbt.getCompound("aspectCap"));
		else
			node.randomiseCap(world.random); // possibly a race condition??
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
	
	public World getWorld(){
		return world;
	}
	
	public UUID getUuid(){
		return uuid;
	}
	
	public AspectMap getAspects(){
		return aspects;
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
	
	public int hashCode(){
		return uuid.hashCode();
	}
	
	public boolean equals(Object obj){
		return obj instanceof Node other && other.uuid.equals(uuid);
	}
}