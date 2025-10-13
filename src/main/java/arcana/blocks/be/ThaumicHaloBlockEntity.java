package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.ArcanaTags;
import arcana.aspects.Aspect;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.util.SearchUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ThaumicHaloBlockEntity extends BlockEntity{
	
	public static final int capacity = 50, maxTime = 30 * 20;
	public static final List<Aspect> boostAspects = List.of(
			Aspects.MAGIC,
			Aspects.AURA
	);
	public static final List<Block> magicMushrooms = List.of(
			ArcanaRegistry.VISHROOM,
			ArcanaRegistry.CORDISPORA
	);
	public static final List<Block> magicFlowers = List.of(
			ArcanaRegistry.SNOWDROP,
			ArcanaRegistry.FIREWHEEL,
			ArcanaRegistry.LILIUM
	);
	
	@Nullable
	public AspectStack stored;
	public int fuelTimer, workTimer;
	public boolean isBoosted;
	
	public ThaumicHaloBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.THAUMIC_HALO_BE, pos, state);
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, ThaumicHaloBlockEntity halo){
		if(world.isClient)
			return;
		
		// consume fuel
		if(halo.stored != null && halo.fuelTimer <= 0){
			halo.fuelTimer = maxTime;
			halo.workTimer = 0;
			halo.isBoosted = halo.stored.type() == Aspects.AURA;
			halo.stored = halo.stored.decrement();
			halo.markDirty();
		}
		
		if(halo.fuelTimer > 0){
			halo.fuelTimer--;
			
			if(halo.workTimer <= 0){
				// boost nodes in range
				for(Node node : AuraWorld.from(world).getNodesInBounds(new Box(pos).expand(10)))
					node.enhance(halo.isBoosted && world.random.nextInt(18) == 0, world.random);
				// convert small plants into magical plants
				SearchUtil.vRandomSearch(world, pos, 10, 5, 4,
						(p, st) -> {
							boolean isFlower = st.isIn(ArcanaTags.HALO_CONVERTIBLE_FLOWERS);
							boolean isMushroom = st.isIn(ArcanaTags.HALO_CONVERTIBLE_MUSHROOMS);
							if((isFlower || isMushroom) && !st.isIn(ArcanaTags.HALO_CONVERTED)){
								((ServerWorld)world).spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getY(), p.getZ(), 12, 1, 1, 1, 0);
								world.setBlockState(p, isFlower ? Util.getRandom(magicFlowers, world.random).getDefaultState() : Util.getRandom(magicMushrooms, world.random).getDefaultState());
								return true;
							}
							return false;
						}
				);
				
				halo.workTimer = halo.isBoosted ? world.random.nextBetween(7 * 20, 10 * 20) : world.random.nextBetween(13 * 20, 17 * 20);
			}else
				halo.workTimer--;
			
			halo.markDirty();
		}
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		if(stored != null)
			nbt.put("stored", stored.toNbt());
		nbt.putInt("fuelTimer", fuelTimer);
		nbt.putInt("workTimer", workTimer);
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		stored = nbt.contains("stored") ? AspectStack.fromNbt(nbt.getCompound("stored")) : null;
		fuelTimer = nbt.getInt("fuelTimer");
		workTimer = nbt.getInt("workTimer");
	}
	
	@Nullable
	public AspectStack accept(AspectStack stack, World world, BlockPos pos, Direction from){
		// only accept at all if it's something we can actually use
		if(!boostAspects.contains(stack.type()))
			return stack;
		// standard jar code
		var result = AspectStack.mergeWithCapacity(stored, stack, capacity);
		if(!result.getLeft().equals(stored)){
			stored = result.getLeft();
			markDirty();
		}
		return result.getRight();
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