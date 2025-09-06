package arcana.items.foci;

import arcana.entities.CoagulationEntity;
import arcana.items.FocusItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CoagulationFocusItem extends FocusItem{
	
	public CoagulationFocusItem(Settings settings){
		super(settings);
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