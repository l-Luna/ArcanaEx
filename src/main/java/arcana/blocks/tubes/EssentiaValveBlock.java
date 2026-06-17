package arcana.blocks.tubes;

import arcana.ArcanaRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EssentiaValveBlock extends EssentiaTubeBlock{
	
	private static final MapCodec<EssentiaValveBlock> CODEC = createCodec(EssentiaValveBlock::new);
	
	public EssentiaValveBlock(Settings settings){
		super(settings);
	}
	
	protected MapCodec<? extends ConnectingBlock> getCodec(){
		return CODEC;
	}
	
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
		if(world.getBlockEntity(pos) instanceof EssentiaValveBlockEntity evbe){
			evbe.updateChangedTick();
			evbe.disabledManually = !evbe.disabledManually;
			evbe.markDirty();
			return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hit);
	}
	
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify){
		super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
		if(!world.isClient && world.getBlockEntity(pos) instanceof EssentiaValveBlockEntity evbe){
			evbe.disabledByRedstone = world.isReceivingRedstonePower(pos);
			evbe.markDirty();
		}
	}
	
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random){
		if(world.getBlockEntity(pos) instanceof EssentiaValveBlockEntity evbe && evbe.disabledByRedstone && random.nextFloat() < 0.25f)
			world.addParticle(new DustParticleEffect(DustParticleEffect.RED, 1), pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, 0, 0, 0);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new EssentiaValveBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World _world, BlockState _state, BlockEntityType<T> type){
		BlockEntityTicker<EssentiaValveBlockEntity> ticker = type == ArcanaRegistry.ESSENTIA_VALVE_BE ? EssentiaValveBlockEntity::tick : null;
		return (BlockEntityTicker<T>)ticker;
	}
}
