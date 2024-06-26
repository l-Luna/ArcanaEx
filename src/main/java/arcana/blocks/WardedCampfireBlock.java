package arcana.blocks;

import arcana.ArcanaRegistry;
import arcana.blocks.be.WardedCampfireBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;

import static arcana.Arcana.arcId;

public class WardedCampfireBlock extends CampfireBlock{
	
	public WardedCampfireBlock(Settings settings){
		super(false, 0, settings);
		setDefaultState(getDefaultState().with(LIT, false));
	}
	
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext ctx){
		return super.getPlacementState(ctx).with(LIT, false);
	}
	
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state){
		return new WardedCampfireBlockEntity(pos, state);
	}
	
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type){
		if(world.isClient)
			return state.get(LIT) ? checkType(type, ArcanaRegistry.WARDED_CAMPFIRE_BE, CampfireBlockEntity::clientTick) : null;
		else
			return state.get(LIT)
					? checkType(type, ArcanaRegistry.WARDED_CAMPFIRE_BE, WardedCampfireBlockEntity::litServerTick)
					: checkType(type, ArcanaRegistry.WARDED_CAMPFIRE_BE, CampfireBlockEntity::unlitServerTick);
	}
	
	public static boolean canBeLit(BlockState state){
		return state.isOf(ArcanaRegistry.WARDED_CAMPFIRE) && !state.get(WATERLOGGED) && !state.get(LIT);
	}
	
	public static boolean isProtected(ServerWorld w, BlockPos pos){
		return w.getPointOfInterestStorage().getInCircle(
				poiTy -> poiTy.matchesId(arcId("warded_campfire")),
				pos,
				24,
				PointOfInterestStorage.OccupationStatus.ANY
		).findAny().isPresent();
	}
	
	public static boolean isProtected(Entity e){
		return e.world instanceof ServerWorld sw && isProtected(sw, e.getBlockPos());
	}
	
	public static void handleTime(ServerWorld sw){
		if(sw.isDay() || !sw.shouldTickTime || !sw.getLevelProperties().getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE))
			return;
		int psum = 0;
		for(ServerPlayerEntity player : sw.getPlayers())
			if(isProtected(player))
				psum++;
		if(psum >= Math.ceil(sw.getPlayers().size() / 2f))
			sw.setTimeOfDay(sw.getTimeOfDay() + 1);
	}
}