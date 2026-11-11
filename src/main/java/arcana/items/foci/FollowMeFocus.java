package arcana.items.foci;

import arcana.ArcanaRegistry;
import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.cca_components.Researcher;
import arcana.items.FocusItem;
import arcana.items.components.ArcanaDataComponents;
import arcana.items.components.FollowMeTargetComponent;
import arcana.items.foci.follow_me.Locator;
import arcana.research.Parent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FollowMeFocus extends FocusItem{
	
	public FollowMeFocus(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return AspectMap.fromAspectStacks(new AspectStack(Aspects.WATER, 120), new AspectStack(Aspects.EARTH, 65));
	}
	
	public ActionResult cast(ItemStack wand, ItemStack focus, PlayerEntity user){
		World world = user.getWorld();
		// find the item being clicked on
		double reach = user.getAttributeValue(EntityAttributes.PLAYER_ENTITY_INTERACTION_RANGE);
		Vec3d from = user.getEyePos();
		Vec3d to = from.add(user.getRotationVector().multiply(reach));
		EntityHitResult entityRaycast = ProjectileUtil.raycast(user, from, to, new Box(from, to), ItemEntity.class::isInstance, reach*reach);
		if(entityRaycast != null && entityRaycast.getEntity() instanceof ItemEntity itemEntity){
			ItemStack stack = itemEntity.getStack();
			Researcher researcher = Researcher.from(user);
			// find a matching locator
			for(Locator locator : Locator.getRegistry(world)){
				Parent research = locator.requiredResearch();
				if(locator.input().test(stack) && (research == null || researcher.isEntryPartComplete(research))){
					if(world instanceof ServerWorld sw){
						Pair<BlockPos, Optional<Heightmap.Type>> target = locator.destination().getLocation(stack, sw, user.getBlockPos());
						// only consume item if locating was successful, may take many tries for e.g. rare biomes
						if(target != null){
							user.setAttached(ArcanaDataComponents.FOLLOW_ME_TARGET, new FollowMeTargetComponent(itemEntity.getPos(), target.getLeft().toCenterPos(), world.getTime(), target.getRight()));
							if(locator.consumed()){
								stack.decrement(1);
								if(stack.getCount() > 1)
									itemEntity.setStack(stack);
								else
									itemEntity.discard();
							}
						}
						// add particles on the server, since other clients should see them too
						sw.spawnParticles(
								ArcanaRegistry.FLAME,
								itemEntity.getX(),
								itemEntity.getY(),
								itemEntity.getZ(),
								8,
								0.1, 0.1, 0.1,
								0.01
						);
					}
					// locating happens on the server, but casting is successful on both sides
					return ActionResult.SUCCESS;
				}
			}
		}
		
		// try to get target, & if it exists, create pointing visual
		// if it exists and is consumable, well, you know
		return super.cast(wand, focus, user);
	}
}