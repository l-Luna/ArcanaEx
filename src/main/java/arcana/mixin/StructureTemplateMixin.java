package arcana.mixin;

import arcana.Arcana;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeType;
import arcana.aura.NodeTypes;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin{
	
	@Shadow
	@Final
	private List<StructureTemplate.StructureEntityInfo> entities;
	
	// to restore them later
	@Unique
	private List<StructureTemplate.StructureEntityInfo> stashedEntities;
	
	@Inject(method = "spawnEntities", at = @At("HEAD"))
	private void addStructureBlockNodes(ServerWorldAccess world,
	                                    BlockPos pos,
	                                    BlockMirror mirror,
	                                    BlockRotation rotation,
	                                    BlockPos pivot,
	                                    BlockBox area,
	                                    boolean initializeMobs,
	                                    CallbackInfo ci){
		// stash removed entries for later
		stashedEntities = new ArrayList<>();
		// hijack entries with "arcana:actually_a_node": true
		for(int i = entities.size() - 1; i >= 0; i--){
			var e = entities.get(i);
			if(e.nbt.getBoolean("arcana:actually_a_node")){
				// add the respective node
				String id = e.nbt.getString("id");
				NodeType ty = NodeTypes.byName(new Identifier(id));
				if(ty != null){
					Vec3d transformed = StructureTemplate.transformAround(e.pos, mirror, rotation, pivot);
					Vec3d offset = transformed.add(pos.getX(), pos.getY(), pos.getZ());
					World w = world.toServerWorld();
					AuraWorld.from(w).addNode(new Node(ty, offset, ty.randomCap(world.getRandom())));
				}else
					Arcana.LOGGER.error("Found node in structure with invalid node type {}", id);
				
				// and remove them from the list, so it doesn't try to add them as entities anyways
				stashedEntities.add(e);
				entities.remove(e);
			}
		}
	}
	
	@Inject(method = "spawnEntities", at = @At("TAIL"))
	private void restoreStructureBlockNodeEntities(ServerWorldAccess world,
	                                               BlockPos pos,
	                                               BlockMirror mirror,
	                                               BlockRotation rotation,
	                                               BlockPos pivot,
	                                               BlockBox area,
	                                               boolean initializeMobs,
	                                               CallbackInfo ci){
		// restore stashed entities
		entities.addAll(stashedEntities);
	}
}