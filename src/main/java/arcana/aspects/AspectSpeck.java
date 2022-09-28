package arcana.aspects;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

public final class AspectSpeck{

	public AspectStack payload;
	public float speed; // blocks/sec
	public Direction direction;
	public float progress; // position along tube
	
	public boolean stuck = false;
	
	public AspectSpeck(AspectStack payload, float speed, Direction direction, float progress){
		this.payload = payload;
		this.speed = speed;
		this.direction = direction;
		this.progress = progress;
	}
	
	public NbtCompound toNbt(){
		NbtCompound nbt = new NbtCompound();
		nbt.put("payload", payload.toNbt());
		nbt.putFloat("speed", speed);
		nbt.putInt("direction", direction.getId());
		nbt.putFloat("progress", progress);
		return nbt;
	}
	
	public static AspectSpeck fromNbt(NbtCompound nbt){
		return new AspectSpeck(
				AspectStack.fromNbt(nbt.getCompound("payload")),
				nbt.getFloat("speed"),
				Direction.byId(nbt.getInt("direction")),
				nbt.getFloat("pos")
		);
	}
}