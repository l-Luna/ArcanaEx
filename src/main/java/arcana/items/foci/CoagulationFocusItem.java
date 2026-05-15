package arcana.items.foci;

import arcana.aspects.AspectMap;
import arcana.aspects.AspectStack;
import arcana.aspects.Aspects;
import arcana.entities.wisps.CoagulationEntity;
import arcana.items.FocusItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CoagulationFocusItem extends FocusItem{
	
	public CoagulationFocusItem(Settings settings){
		super(settings);
	}
	
	public AspectMap deciCastCost(@Nullable ItemStack wand, ItemStack focus, PlayerEntity user){
		return AspectMap.fromAspectStacks(new AspectStack(Aspects.FIRE, 300), new AspectStack(Aspects.ORDER, 300));
	}
	
	public boolean isContinuous(){
		return true;
	}
	
	public void tickContinuousCast(ContinuousCastContext ccc){
		PlayerEntity user = ccc.user;
		World w = user.world;
		Vec3d target = PrismaticLightFocusItem.hoverPosition(user);
		if(ccc.castTime >= 30){
			ccc.stop();
			CoagulationEntity ce = new CoagulationEntity(w, user.getUuid());
			ce.setPosition(target);
			w.spawnEntity(ce);
		}
	}
}