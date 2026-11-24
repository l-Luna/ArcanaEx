package arcana.mixin.warding;

import arcana.world.WardedChunk;
import arcana.worldgen.structure.WardStructureProcessor;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin{
	
	@Inject(method = "place",
	        at = @At("HEAD"))
	private void place_start(ServerWorldAccess world,
	                         BlockPos pos,
	                         BlockPos pivot,
	                         StructurePlacementData placementData,
	                         Random random,
	                         int flags,
	                         CallbackInfoReturnable<Boolean> cir,
	                         @Share("ward") LocalRef<WardStructureProcessor> wardRef){
		for(StructureProcessor processor : placementData.getProcessors())
			if(processor instanceof WardStructureProcessor ward){
				wardRef.set(ward);
				break;
			}
	}
	
	@Inject(method = "place",
	        at = @At(value = "FIELD",
	                 target = "Lnet/minecraft/structure/StructureTemplate$StructureBlockInfo;pos:Lnet/minecraft/util/math/BlockPos;",
	                 opcode = Opcodes.GETFIELD))
	private void place_block(ServerWorldAccess world,
	                         BlockPos pos,
	                         BlockPos pivot,
	                         StructurePlacementData placementData,
	                         Random random,
	                         int flags,
	                         CallbackInfoReturnable<Boolean> cir,
	                         @Local StructureTemplate.StructureBlockInfo where,
	                         @Share("ward") LocalRef<WardStructureProcessor> wardRef){
		WardStructureProcessor ward = wardRef.get();
		if(ward != null){
			BlockBox box = placementData.getBoundingBox();
			if((box == null || box.contains(where.pos())) && !where.state().isAir() && ward.check(where.state()))
				WardedChunk.setWarded(world.toServerWorld(), where.pos(), true);
		}
	}
}