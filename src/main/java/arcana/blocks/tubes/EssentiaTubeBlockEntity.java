package arcana.blocks.tubes;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectIo;
import arcana.aspects.AspectSpeck;
import arcana.util.NbtUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EssentiaTubeBlockEntity extends BlockEntity{
	
	// specks currently passing through
	public List<AspectSpeck> specks = new ArrayList<>();
	
	public EssentiaTubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}
	
	public EssentiaTubeBlockEntity(BlockPos pos, BlockState state){
		this(ArcanaRegistry.ESSENTIA_TUBE_BE, pos, state);
	}
	
	public static void tick(World world, BlockPos pos, BlockState state, EssentiaTubeBlockEntity tube){
		List<AspectSpeck> specks = tube.specks;
		if(!specks.isEmpty())
			tube.markDirty();
		for(int i = specks.size() - 1; i >= 0; i--){ // reverse loop to allow removal
			AspectSpeck speck = specks.get(i);
			Direction dir = speck.direction;
			speck.progress += speck.speed;
			if(speck.progress >= 1){
				speck.progress %= 1;
				BlockPos there = pos.offset(dir);
				// try insert there,
				if(world.getBlockEntity(there) instanceof EssentiaTubeBlockEntity otherTube && otherTube.enabled()){
					otherTube.insert(speck);
					specks.remove(speck);
				}else if(world.getBlockState(there).getBlock() instanceof AspectIo aio
						&& aio.accept(speck.payload, world, there, dir.getOpposite())){
					specks.remove(speck);
				}else{
					// hit a wall, try to recover
					if(dir.getAxis().isHorizontal()){
						// horizontally-moving specks will prefer to move randomly left or right, then backwards
						Direction first = world.random.nextBoolean() ? dir.rotateYCounterclockwise() : dir.rotateYClockwise(),
							second = first.getOpposite();
						if(EssentiaTubeBlock.connectsTo(world.getBlockState(pos.offset(first)).getBlock()))
							speck.direction = first;
						else if(EssentiaTubeBlock.connectsTo(world.getBlockState(pos.offset(second)).getBlock()))
							speck.direction = second;
						else
							speck.direction = dir.getOpposite();
					}else vertical: {
						// it'll randomly pick a possible direction
						for(Direction hd : Direction.shuffle(world.random))
							if(EssentiaTubeBlock.connectsTo(world.getBlockState(pos.offset(hd)).getBlock())){
								speck.direction = hd;
								break vertical;
							}
						// or it'll turn back in doubt
						speck.direction = dir.getOpposite();
					}
				}
			}
		}
	}
	
	public void insert(AspectSpeck speck){
		specks.add(speck);
		markDirty();
		// specks obey gravity, but only once
		if(speck.direction != Direction.UP
				&& speck.direction != Direction.DOWN
				&& EssentiaTubeBlock.connectsTo(world.getBlockState(pos.down()).getBlock()))
			speck.direction = Direction.DOWN;
	}
	
	public boolean enabled(){
		return true;
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("specks", specks.stream().map(AspectSpeck::toNbt).collect(NbtUtil.toNbtList()));
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		for(NbtElement entryElem : nbt.getList("specks", NbtElement.COMPOUND_TYPE))
			specks.add(AspectSpeck.fromNbt((NbtCompound)entryElem));
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(){
		return createNbt();
	}
}