package arcana.mixin;

import arcana.ArcanaRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static arcana.Arcana.arcId;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin{
	
	@Shadow
	@Final
	private ItemModels models;
	
	@WrapOperation(method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
	               at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;"))
	BakedModel overrideInventoryTextures(ItemRenderer instance,
	                                     ItemStack stack,
	                                     World world,
	                                     @Nullable LivingEntity entity,
	                                     int seed,
	                                     Operation<BakedModel> original,
										 //
	                                     @Nullable LivingEntity _entity,
	                                     ItemStack item,
	                                     ModelTransformationMode renderMode,
	                                     boolean leftHanded,
	                                     MatrixStack matrices,
	                                     VertexConsumerProvider vertexConsumers,
	                                     @Nullable World _world,
	                                     int light,
	                                     int overlay,
	                                     int _seed){
		if(entity != null){
			boolean isInventory = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
			Arm arm = renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND || renderMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND ? Arm.LEFT
					: renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND || renderMode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND ? Arm.RIGHT
					: null;
			Hand hand = entity.getMainArm() == arm ? Hand.MAIN_HAND : arm != null ? Hand.OFF_HAND : null;
			if(!isInventory)
				if(stack.isOf(ArcanaRegistry.CRIMSON_LEECH) && entity.handSwinging && entity.preferredHand == hand)
					return models.getModelManager().getModel(new ModelIdentifier(arcId("crimson_leech_attacking"), "inventory"));
		}
		return original.call(instance, stack, world, entity, seed);
	}
}