package arcana.mixin;

import arcana.ArcanaDamageSources;
import arcana.ArcanaRegistry;
import arcana.cca_components.RunicShielding;
import arcana.duck.ArcanaLivingEntity;
import arcana.items.BootsOfTheTravellerItem;
import arcana.network.PkEntityStatusEx;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockState;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ArcanaLivingEntity{
	
	@Unique
	private boolean diedToPutrefaction;
	
	public LivingEntityMixin(EntityType<?> type, World world){
		super(type, world);
		throw new UnsupportedOperationException();
	}
	
	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);
	
	public boolean arcana$diedToPutrefaction(){
		return diedToPutrefaction;
	}
	
	public void arcana$markDiedToPutrefaction(){
		diedToPutrefaction = true;
	}
	
	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	private void canWalkOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir){
		if(shouldWalkOnWater() && state.isIn(FluidTags.WATER))
			cir.setReturnValue(true);
	}
	
	@Unique
	private boolean shouldWalkOnWater(){
		return getEquippedStack(EquipmentSlot.FEET).getItem() == ArcanaRegistry.BOOTS_OF_THE_SAILOR
				&& !isSneaky()
				&& !getWorld().getFluidState(getBlockPos().up()).isIn(FluidTags.WATER);
	}
	
	@Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
	private void hasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir){
		if(effect == StatusEffects.JUMP_BOOST && shouldJumpBoost())
			cir.setReturnValue(true);
	}
	
	@ModifyReturnValue(method = "getStatusEffect", at = @At("RETURN"))
	private StatusEffectInstance getStatusEffect(@Nullable StatusEffectInstance original, RegistryEntry<StatusEffect> effect){
		if(effect == StatusEffects.JUMP_BOOST && (original == null || original.getAmplifier() < 3) && shouldJumpBoost())
			return new StatusEffectInstance(StatusEffects.JUMP_BOOST, 0, 3);
		return original;
	}
	
	@Unique
	private boolean shouldJumpBoost(){
		return getEquippedStack(EquipmentSlot.FEET).getItem() instanceof BootsOfTheTravellerItem && isSneaky();
	}
	
	// make trapdoors work right with metal ladders
	
	@Inject(method = "canEnterTrapdoor", at = @At("HEAD"), cancellable = true)
	private void canEnterTrapdoor(BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir){
		if(state.get(TrapdoorBlock.OPEN)){
			BlockState ladderState = getWorld().getBlockState(pos.down());
			if(ladderState.isOf(ArcanaRegistry.METAL_LADDER) && ladderState.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING))
				cir.setReturnValue(true);
		}
	}
	
	// add default value for runic shielding attribute (see reach-entity-attributes)
	
	@Inject(
			method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
			require = 1, allow = 1, at = @At("RETURN"))
	private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info){
		info.getReturnValue().add(RunicShielding.MAX_SHIELDING.entry());
	}
	
	// putrefaction: mark on death, reduce iframes, drop with fortune 1, and drop as a player kill
	// TODO: handle looting/player kills properly (see RandomChanceWithLootingLootCondition)
	//       (add a dummy LivingEntity to the context that EnchantmentHelper.getLooting responds to?)
	
	@Inject(method = "onDeath", at = @At("HEAD"))
	void onDeath(DamageSource source, CallbackInfo ci){
		if(source.isOf(ArcanaDamageSources.PUTREFACTION_KEY)){
			diedToPutrefaction = true;
			PkEntityStatusEx.sendStatus(this, PkEntityStatusEx.STATUS_DIED_TO_PUTREFACTION);
		}
	}
	
	@Inject(method = "damage",
	        at = @At(value = "FIELD",
	                 target = "Lnet/minecraft/entity/LivingEntity;timeUntilRegen:I",
	                 opcode = Opcodes.PUTFIELD,
	                 shift = At.Shift.AFTER))
	void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
		if(source.isOf(ArcanaDamageSources.PUTREFACTION_KEY))
			timeUntilRegen = 18;
	}
}