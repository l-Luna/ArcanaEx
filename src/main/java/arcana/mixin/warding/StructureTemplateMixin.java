package arcana.mixin.warding;

import arcana.aura.WardedChunk;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin{
	
	@Unique
	private boolean isWarded;
	
	@Inject(method = "readNbt", at = @At("HEAD"))
	void readNbt(RegistryEntryLookup<Block> blockLookup, NbtCompound nbt, CallbackInfo ci){
		isWarded = nbt.getBoolean("arcana:warded");
	}
	
	@Inject(method = "place",
	        at = @At(value = "FIELD",
	                 target = "Lnet/minecraft/structure/StructureTemplate$StructureBlockInfo;pos:Lnet/minecraft/util/math/BlockPos;",
	                 opcode = Opcodes.GETFIELD))
	private void place(ServerWorldAccess world,
	                   BlockPos pos,
	                   BlockPos pivot,
	                   StructurePlacementData placementData,
	                   Random random,
	                   int flags,
	                   CallbackInfoReturnable<Boolean> cir,
	                   @Local StructureTemplate.StructureBlockInfo where){
		if(isWarded){
			BlockBox box = placementData.getBoundingBox();
			if((box == null || box.contains(where.pos())) && !where.state().isAir())
				WardedChunk.setWarded(world.toServerWorld(), where.pos(), true);
		}
	}
}