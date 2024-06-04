package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AlembicBlockEntity extends BlockEntity implements AspectIo{
	
	@Nullable
	private AspectStack stored;
	
	public AlembicBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.ALEMBIC_BE, pos, state);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		if(nbt.contains("stored"))
			stored = AspectStack.fromNbt(nbt.getCompound("stored"));
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		if(stored != null)
			nbt.put("stored", stored.toNbt());
	}
	
	// we do not accept returns thank you
	public AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		return stack;
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return draw(max);
	}
	
	public @Nullable AspectStack draw(int max){
		var result = AspectStack.draw(stored, max);
		if(!Objects.equals(result.getLeft(), stored)){
			markDirty();
			stored = result.getLeft();
		}
		return result.getRight();
	}
}