package arcana.nodes;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class Node implements Position{

	private NodeType type;
	private double x, y, z;
	
	private int ticksUntilRecharge;
	private NbtCompound tag;
	
	private World world;
	private UUID uuid;
	
	public Node(NodeType type, World world, Position pos){
		this.type = type;
		this.world = world;
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		tag = new NbtCompound();
		uuid = UUID.randomUUID();
	}
	
	public Node(Node node){
		type = node.type;
		world = node.world;
		x = node.x;
		y = node.y;
		z = node.z;
		ticksUntilRecharge = node.ticksUntilRecharge;
		tag = node.tag.copy();
		uuid = UUID.randomUUID();
	}
	
	public void tick(){
		if(ticksUntilRecharge <= 0){
			ticksUntilRecharge = type.rechargeTime() + world.random.nextBetween(-3 * 20, 3 * 20);
			doRecharge();
		}
		ticksUntilRecharge--;
	}
	
	protected void doRecharge(){
		// ...
	}
	
	public NbtCompound toNbt(){
		NbtCompound c = new NbtCompound();
		c.putString("type", type.id().toString());
		c.putDouble("x", x);
		c.putDouble("y", y);
		c.putDouble("z", z);
		
		c.putInt("ticksUntilRecharge", ticksUntilRecharge);
		if(tag != null)
			c.put("tag", tag);
		
		c.putUuid("uuid", uuid);
		
		return c;
	}
	
	public static Node fromNbt(NbtCompound nbt, World world){
		var pos = new Vec3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
		var node = new Node(NodeTypes.byName(new Identifier(nbt.getString("type"))), world, pos);
		node.ticksUntilRecharge = nbt.getInt("ticksUntilRecharge");
		node.uuid = nbt.getUuid("uuid");
		if(nbt.contains("tag"))
			node.tag = nbt.getCompound("tag");
		return node;
	}
	
	public BlockPos asBlockPos(){
		return new BlockPos(x, y, z);
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
}