package arcana.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class SuspensionEngineEntity extends Entity implements IAnimatable{
	
	protected static final AnimationBuilder floatAnim = new AnimationBuilder().addAnimation("engine.float");
	
	private final AnimationFactory animFactory = GeckoLibUtil.createFactory(this);
	
	public SuspensionEngineEntity(EntityType<?> type, World world){
		super(type, world);
	}
	
	protected void initDataTracker(){}
	protected void readCustomDataFromNbt(NbtCompound nbt){}
	protected void writeCustomDataToNbt(NbtCompound nbt){}
	public Packet<?> createSpawnPacket(){
		return new EntitySpawnS2CPacket(this);
	}
	
	public void registerControllers(AnimationData data){
		data.addAnimationController(new AnimationController<>(this, "float", 20, event -> {
			event.getController().setAnimation(floatAnim);
			return PlayState.CONTINUE;
		}));
	}
	
	public AnimationFactory getFactory(){
		return animFactory;
	}
}