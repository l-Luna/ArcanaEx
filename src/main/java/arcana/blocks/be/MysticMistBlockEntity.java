package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class MysticMistBlockEntity extends BlockEntity implements AspectIo{
	
	public static final int
			capacity = 6,
			baseAspectTime = 40,
			radius = 16;
	public static final List<Aspect> weatherAspects = List.of(
			Aspects.WATER,
			Aspects.FIRE,
			Aspects.ENERGY,
			Aspects.ICE,
			Aspects.AURA
	);
	
	public @Nullable AspectStack stored;
	public int timer = 0, vspace;
	
	// transient
	// public Aspect lastAspect;
	// public float lerp;
	
	public MysticMistBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.MYSTIC_MIST_BE, pos, state);
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		if(stored != null)
			nbt.put("stored", stored.toNbt());
		nbt.putInt("timer", timer);
		nbt.putInt("vspace", vspace);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		if(nbt.contains("stored"))
			stored = AspectStack.fromNbt(nbt.getCompound("stored"));
		else stored = null;
		timer = nbt.getInt("timer");
		vspace = nbt.getInt("vspace");
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, MysticMistBlockEntity mist){
		if(world.isClient)
			return;
		
		// update vertical free space
		int newVspace = 0;
		for(int i = 1; i < 6; i++){
			if(world.getBlockState(pos.up(i)).isOpaque())
				break;
			newVspace = i;
		}
		if(mist.vspace != newVspace){
			mist.vspace = newVspace;
			mist.markDirty();
		}
		
		Random rng = world.random;
		AspectStack stored = mist.stored;
		if(stored == null)
			return; // no essentia? no problem!
		
		int idx = weatherAspects.indexOf(stored.type());
		if(idx < 0) idx = 0;
		
		switch(idx){
			case 0 /* water */ -> {
				mist.randomSearch((__, b) -> b.isIn(BlockTags.CROPS), 2, (cPos, cState) -> {
					if(cState.getBlock() instanceof Fertilizable f){
						f.grow((ServerWorld)world, rng, cPos, cState);
						world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, cPos, 0);
					}
				});
				// TODO: keep track of MM AoE so that farmland and fire can both treat it like rain
				mist.randomSearch((__, b) -> b.isIn(BlockTags.FIRE), 24, (fPos, fState) ->
						world.removeBlock(fPos, false));
				mist.randomSearch((__, b) -> b.getBlock() instanceof FarmlandBlock, 24, (fPos, fState) ->
						world.setBlockState(fPos, fState.with(FarmlandBlock.MOISTURE, 7), Block.NOTIFY_LISTENERS));
				// TODO: fill crucibles... implement on crucible end using AoE
			}
			case 1 /* fire */ -> {
				mist.randomSearch((__, b) -> b.isOf(Blocks.NETHER_WART), 3, (cPos, cState) -> {
					if(cState.getBlock() instanceof NetherWartBlock){
						world.setBlockState(cPos, cState.with(NetherWartBlock.AGE, cState.get(NetherWartBlock.AGE) + 1), Block.NOTIFY_LISTENERS);
						world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, cPos, 0);
					}
				});
				// TODO: fire infiniburn
			}
			case 2 /* energy */ -> {
				// TODO: energy lightning effects
			}
			case 3 /* ice */ ->
					mist.randomSearch(
							(there, b) -> world.getBlockState(there.up()).isAir() && b.isOpaqueFullCube(world, there),
							1,
							(there, __) -> world.setBlockState(there.up(), Blocks.SNOW.getDefaultState()));
			case 4 /* aura */ -> {
				// no-op
			}
		}
		
		// time ticks, but you get more for the more expensive aspects
		mist.timer++;
		if(mist.timer > baseAspectTime * Math.sqrt(idx + 1)){
			mist.stored = stored.amount() <= 1 ? null : new AspectStack(stored.type(), stored.amount() - 1);
			mist.timer = 0;
		}
		mist.markDirty();
	}
	
	private void randomSearch(BiPredicate<BlockPos, BlockState> predicate, int rolls, BiConsumer<BlockPos, BlockState> then){
		for(int i = 0; i < rolls; i++){
			int x = pos.getX() + world.random.nextInt(radius * 2 + 1) - radius,
				z = pos.getZ() + world.random.nextInt(radius * 2 + 1) - radius;
			for(int yOff = vspace; yOff >= -2; yOff--){
				BlockPos there = new BlockPos(x, getPos().getY() + yOff, z);
				BlockState state = world.getBlockState(there);
				if(predicate.test(there, state)){
					then.accept(there, state);
					break;
				}
			}
		}
	}
	
	public @Nullable AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		// only accept at all if it's something we can actually use
		if(!weatherAspects.contains(stack.type()))
			return stack;
		// standard jar code
		var result = AspectStack.mergeWithCapacity(stored, stack, capacity);
		if(!result.getLeft().equals(stored)){
			stored = result.getLeft();
			markDirty();
		}
		return result.getRight();
	}
	
	public @Nullable AspectStack draw(int max, World world, BlockPos pos, Direction from){
		return null; // no returns, sorry!
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(){
		return createNbt();
	}
	
	public void markDirty(){
		super.markDirty();
		if(world instanceof ServerWorld sw)
			sw.getChunkManager().markForUpdate(pos);
	}
}