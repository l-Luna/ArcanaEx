package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.api.ScalpelSlashable;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
import arcana.blocks.be.InfusionMatrixBlockEntity;
import arcana.blocks.be.InfusionPillarBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class InfusionMatrixBlock extends BlockWithEntity implements ScalpelSlashable{
	
	public InfusionMatrixBlock(Settings settings){
		super(settings);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new InfusionMatrixBlockEntity(pos, state);
	}
	
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		return checkType(type, ArcanaRegistry.INFUSION_MATRIX_BE, (_1, _2, _3, be) -> be.tick());
	}
	
	public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data){
		super.onSyncedBlockEvent(state, world, pos, type, data);
		BlockEntity be = world.getBlockEntity(pos);
		return be != null && be.onSyncedBlockEvent(type, data);
	}
	
	public void onScalpelSlash(World world, PlayerEntity user, BlockPos pos){
		world.addBlockBreakParticles(pos, world.getBlockState(pos));
		world.setBlockState(pos, ArcanaRegistry.TAINT_GOO.getDefaultState());
		AuraWorld.from(world).incrementFlux(13, FluxOrigin.SCALPEL_TAMPERING, pos);
	}
	
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		if(!state.isOf(newState.getBlock())){
			for(Direction direction : Direction.Type.HORIZONTAL){
				BlockPos pillarPos = pos.down(2).offset(direction).offset(direction.rotateYClockwise());
				if(world.getBlockEntity(pillarPos) instanceof InfusionPillarBlockEntity pillar)
					pillar.setMatrixPosition(null);
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
}