package arcana.mixin;

import arcana.Arcana;
import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeType;
import arcana.aura.NodeTypes;
import com.unascribed.lib39.core.mixinsupport.AutoMixinEligible;
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
@AutoMixinEligible(unlessModPresent = "connector")
public class StructureTemplateMixinFabric{
	
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
			StructureTemplate.StructureEntityInfo e = entities.get(i);
			if(e.nbt.getBoolean("arcana:actually_a_node")){
				// add the respective node
				String id = e.nbt.getString("id");
				NodeType ty = NodeTypes.byName(Identifier.of(id));
				if(ty != null){
					Vec3d transformed = StructureTemplate.transformAround(e.pos, mirror, rotation, pivot);
					Vec3d offset = transformed.add(pos.getX(), pos.getY(), pos.getZ());
					if(area == null || area.contains(BlockPos.ofFloored(offset))){
						World w = world.toServerWorld();
						Node toAdd = new Node(ty, offset, ty.randomCap(world.getRandom()));
						toAdd.getAspects().clear();
						toAdd.getAspects().add(toAdd.getAspectCap());
						AuraWorld.from(w).addNode(toAdd);
					}
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