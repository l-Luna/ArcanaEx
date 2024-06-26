package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.mixin.BlockEntityAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WardedCampfireBlockEntity extends CampfireBlockEntity{
	
	private int timer = -1;
	
	public WardedCampfireBlockEntity(BlockPos pos, BlockState state){
		super(pos, state);
		// muahaha
		((BlockEntityAccessor)this).setType(ArcanaRegistry.WARDED_CAMPFIRE_BE);
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.putInt("timer", timer);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		timer = nbt.getInt("timer");
	}
	
	public static void litServerTick(World world, BlockPos pos, BlockState state, WardedCampfireBlockEntity campfire){
		CampfireBlockEntity.litServerTick(world, pos, state, campfire);
		
		if(campfire.timer < 0)
			campfire.timer = 20 * 5 * 60 + world.random.nextInt(20 * 27);
		if(campfire.timer == 0){
			world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.9f, 1.5f);
			world.setBlockState(pos, state.with(Properties.LIT, false));
			CampfireBlock.extinguish(null, world, pos, state);
			campfire.timer = 20 * 5 * 60 + world.random.nextInt(20 * 27);
		}else
			campfire.timer--;
	}
}
