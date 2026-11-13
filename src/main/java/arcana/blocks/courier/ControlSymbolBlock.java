package arcana.blocks.courier;

import arcana.ArcanaRegistry;
import arcana.blocks.courier.SymbolConfig.SymbolDirection;
import arcana.blocks.courier.symbols.EmptySymbolStorage;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static arcana.Arcana.arcId;

public class ControlSymbolBlock extends BlockWithEntity{
	
	private static final MapCodec<ControlSymbolBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			createSettingsCodec(),
			SymbolType.CODEC.fieldOf("symbol_type").forGetter(x -> x.symbolType)
	).apply(i, ControlSymbolBlock::new));
	
	public static final DirectionProperty FACING = Properties.FACING;
	
	public final SymbolType symbolType;
	
	public ControlSymbolBlock(Settings settings, SymbolType symbolType){
		super(settings);
		this.symbolType = symbolType;
		setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.UP));
	}
	
	protected MapCodec<? extends BlockWithEntity> getCodec(){
		return CODEC;
	}
	
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder){
		builder.add(FACING);
	}
	
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(FACING, ctx.getSide().getOpposite());
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		BE be = new BE(pos, state);
		be.symbolType = symbolType;
		return be;
	}
	
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit){
		if(world.getBlockEntity(pos) instanceof BE be){
			if(player.isSneaking()){
				// temp flip direction
				be.config.direction = be.config.direction == SymbolDirection.SOURCE ? SymbolDirection.SINK : SymbolDirection.SOURCE;
			}else if(world instanceof ServerWorld sw){
				// temp link to courier
				getNearestCourier(pos, sw).ifPresent(courierBe -> {
					List<SymbolInstance> symbolList = be.config.direction == SymbolDirection.SOURCE ? courierBe.sourceSymbols : courierBe.sinkSymbols;
					SymbolInstance symInst = new SymbolInstance();
					symInst.pos = pos;
					SymbolStorage storage = be.symbolType.storage().apply(new SymbolContext(world, state.get(FACING), be.relativeVolume.offset(pos.getX(), pos.getY(), pos.getZ())));
					symInst.storage = storage != null ? storage : new EmptySymbolStorage();
					symInst.config = be.config;
					symInst.lastVersion = 0;
					symbolList.add(symInst);
				});
			}
			return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hit);
	}
	
	private static @NotNull Optional<CourierBlock.BE> getNearestCourier(BlockPos pos, ServerWorld sw){
		return sw.getPointOfInterestStorage().getInCircle(
						poiTy -> poiTy.matchesId(arcId("courier")),
						pos,
						24,
						PointOfInterestStorage.OccupationStatus.ANY
				)
				.min(Comparator.comparing(it -> it.getPos().getSquaredDistance(pos)))
				.map(PointOfInterest::getPos)
				.flatMap(p -> Optional.ofNullable(sw.getBlockEntity(p)))
				.flatMap(be -> be instanceof CourierBlock.BE c ? Optional.of(c) : Optional.empty());
	}
	
	public static class BE extends BlockEntity{
		
		SymbolType symbolType;
		SymbolConfig config = new SymbolConfig();
		BlockBox relativeVolume = new BlockBox(0, 0, 0, 1, 1, 1);
		// linked courier...
		
		public BE(BlockPos pos, BlockState state){
			super(ArcanaRegistry.CONTROL_SYMBOL_BE, pos, state);
			config.forceful = true;
		}
	}
}