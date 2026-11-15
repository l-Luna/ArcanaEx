package arcana.blocks.courier;

import arcana.ArcanaRegistry;
import arcana.blocks.courier.SymbolConfig.SymbolDirection;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
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
				if(be.linkedCourierPos != null && world.getBlockEntity(be.linkedCourierPos) instanceof CourierBlock.BE courierBe)
					courierBe.updateSymbol(be.instance);
			}else if(world instanceof ServerWorld sw){
				// temp link to courier
				getNearestCourier(pos, sw).ifPresent(courierBe -> {
					if(be.linkedCourierPos != null
							&& !be.linkedCourierPos.equals(courierBe.getPos())
							&& world.getBlockEntity(be.linkedCourierPos) instanceof CourierBlock.BE oldCourierBe)
						oldCourierBe.removeSymbol(be.instance);
					
					be.instance = courierBe.addSymbolFromBlock(be, state);
					be.linkedCourierPos = courierBe.getPos();
				});
			}
			return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hit);
	}
	
	protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved){
		super.onStateReplaced(state, world, pos, newState, moved);
		if(!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof BE be)
			be.onRemoved();
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
		
		// saved data
		SymbolType symbolType;
		SymbolConfig config = new SymbolConfig();
		BlockBox relativeVolume = new BlockBox(0, 0, 0, 1, 1, 1);
		
		// supplied by courier BE on linkage
		@Nullable
		BlockPos linkedCourierPos;
		@Nullable
		SymbolInstance instance;
		
		public BE(BlockPos pos, BlockState state){
			super(ArcanaRegistry.CONTROL_SYMBOL_BE, pos, state);
			config.forceful = true;
		}
		
		public void onRemoved(){
			if(linkedCourierPos != null && world.getBlockEntity(linkedCourierPos) instanceof CourierBlock.BE courierBe)
				courierBe.removeSymbol(instance);
		}
		
		protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
			super.writeNbt(nbt, registryLookup);
			nbt.putString("symbol_type", symbolType.id().toString());
			SymbolConfig.CODEC.encodeStart(NbtOps.INSTANCE, config).ifSuccess(it -> nbt.put("config", it));
			BlockBox.CODEC.encodeStart(NbtOps.INSTANCE, relativeVolume).ifSuccess(it -> nbt.put("relative_volume", it));
		}
		
		protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup){
			super.readNbt(nbt, registryLookup);
			symbolType = SymbolType.TYPES.get(Identifier.of(nbt.getString("symbol_type")));
			if(nbt.contains("config", NbtElement.COMPOUND_TYPE))
				config = SymbolConfig.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("config")).getOrThrow().getFirst();
			if(nbt.contains("relative_volume", NbtElement.COMPOUND_TYPE))
				relativeVolume = BlockBox.CODEC.decode(NbtOps.INSTANCE, nbt.getCompound("relative_volume")).getOrThrow().getFirst();
		}
	}
}