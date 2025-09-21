package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.util.SearchUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
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
				mist.randomSearch(2, (__, b) -> b.isIn(BlockTags.CROPS), (cPos, cState) -> {
					if(cState.getBlock() instanceof Fertilizable f){
						f.grow((ServerWorld)world, rng, cPos, cState);
						world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, cPos, 0);
					}
				});
				// TODO: keep track of MM AoE so that farmland and fire can both treat it like rain
				mist.randomSearch(24, (__, b) -> b.isIn(BlockTags.FIRE), (fPos, fState) ->
						world.removeBlock(fPos, false));
				mist.randomSearch(24, (__, b) -> b.getBlock() instanceof FarmlandBlock, (fPos, fState) ->
						world.setBlockState(fPos, fState.with(FarmlandBlock.MOISTURE, 7), Block.NOTIFY_LISTENERS));
				// TODO: fill crucibles... implement on crucible end using AoE
			}
			case 1 /* fire */ -> {
				mist.randomSearch(1, (__, b) -> b.isOf(Blocks.NETHER_WART), (cPos, cState) -> {
					if(cState.getBlock() instanceof NetherWartBlock){
						int value = cState.get(NetherWartBlock.AGE) + 1;
						if(value < 3){
							world.setBlockState(cPos, cState.with(NetherWartBlock.AGE, value), Block.NOTIFY_LISTENERS);
							world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, cPos, 0);
						}
					}
				});
				// TODO: fire infiniburn
			}
			case 2 /* energy */ -> {
				// TODO: static electricity around lightning rods?
				// TODO: also extinguish fire
				// roughly continuous every 5 seconds, with a minimum of half a second delay
				if(world.getTime() % 10 == 0 && rng.nextInt(9) == 0){
					mist.randomSearch(1,
							(there, b) -> world.getBlockState(there.up()).isAir() && !b.isAir(),
							(there, __) -> {
								// prefer to hit lightning rods
								Optional<BlockPos> rod = ((ServerWorld)world).getPointOfInterestStorage().getNearestPosition(
										ty -> ty.matchesKey(PointOfInterestTypes.LIGHTNING_ROD),
										posx -> world.getBlockState(posx.up()).isAir(),
										there,
										16,
										PointOfInterestStorage.OccupationStatus.ANY
								);
								if(rod.isPresent())
									there = rod.get();
								
								LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(world);
								lightningEntity.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(there.up()));
								world.spawnEntity(lightningEntity);
							});
				}
			}
			case 3 /* ice */ ->
					mist.randomSearch(1,
							(there, b) -> world.getBlockState(there.up()).isAir() && b.isOpaqueFullCube(world, there),
							(there, __) -> world.setBlockState(there.up(), Blocks.SNOW.getDefaultState()));
			case 4 /* aura */ -> {
				// no-op
			}
		}
		
		// time ticks, but you get more for the more expensive aspects
		mist.timer++;
		if(mist.timer > baseAspectTime * Math.sqrt(idx + 1)){
			mist.stored = stored.decrement();
			mist.timer = 0;
		}
		mist.markDirty();
	}
	
	private void randomSearch(int rolls, BiPredicate<BlockPos, BlockState> predicate, BiConsumer<BlockPos, BlockState> then){
		SearchUtil.vRandomSearch(world, pos, radius, vspace, rolls, predicate, then);
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