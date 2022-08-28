package arcana.items;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.WandAspectsTooltipData;
import arcana.components.AuraWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Consumer;

public class WandItem extends Item{
	
	public WandItem(Settings settings){
		super(settings);
	}
	
	public static ItemStack withCapAndCore(Cap cap, Core core){
		ItemStack stack = new ItemStack(ArcanaRegistry.WAND);
		NbtCompound tag = stack.getOrCreateNbt();
		tag.putString("cap_id", cap.id().toString());
		tag.putString("core_id", core.id().toString());
		return stack;
	}
	
	public static ItemStack basicWand(){
		return withCapAndCore(ArcanaRegistry.IRON_WAND_CAP, ArcanaRegistry.STICK_CORE);
	}
	
	public Text getName(ItemStack stack){
		return Text.translatable(
				"item.arcana.wand",
				Text.translatable(capFrom(stack).translationKey()),
				Text.translatable(coreFrom(stack).translationKey()),
				Text.translatable("wand.variant.arcana.wand"));
	}
	
	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks){
		if(isIn(group)){
			stacks.add(withCapAndCore(ArcanaRegistry.IRON_WAND_CAP, ArcanaRegistry.STICK_CORE));
			stacks.add(withCapAndCore(ArcanaRegistry.GOLD_WAND_CAP, ArcanaRegistry.BLAZE_WAND_CORE));
		}
	}
	
	public ActionResult useOnBlock(ItemUsageContext context){
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		if(state.getBlock() == Blocks.CAULDRON){
			world.setBlockState(pos, ArcanaRegistry.CRUCIBLE.getDefaultState());
			world.playSound(player, pos, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1, 1);
			for(int i = 0; i < 20; i++)
				world.addParticle(ParticleTypes.END_ROD, pos.getX() + world.random.nextDouble(), pos.getY() + world.random.nextDouble(), pos.getZ() + world.random.nextDouble(), 0, 0, 0);
			return ActionResult.SUCCESS;
		}
		if(state.getBlock() == Blocks.CRAFTING_TABLE){
			world.setBlockState(pos, ArcanaRegistry.ARCANE_CRAFTING_TABLE.getDefaultState());
			world.playSound(player, pos, SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1, 1);
			for(int i = 0; i < 20; i++)
				world.addParticle(ParticleTypes.END_ROD, (pos.getX() - .1f) + world.random.nextDouble() * 1.2f, (pos.getY() - .1f) + world.random.nextDouble() * 1.2f, (pos.getZ() - .1f) + world.random.nextDouble() * 1.2f, 0, 0, 0);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
	
	public int getMaxUseTime(ItemStack stack){
		return 72000;
	}
	
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks){
		// TODO: use reach-entity-attributes to check player's true range
		world.getComponent(AuraWorld.KEY).raycast(user.getEyePos(), 4.5, false, user).ifPresent(node -> {
			AspectMap aspects = node.getAspects();
			if(!aspects.aspectSet().isEmpty()){
				Aspect aspect = aspects.aspectByIndex(world.random.nextInt(aspects.size()));
				int aspectDrainWait = 8;
				int aspectDrainAmount = 3 + world.random.nextInt(3);
				if(world.getTime() % aspectDrainWait == 0){
					int realDrainAmount = Math.min(aspects.get(aspect), aspectDrainAmount);
					aspects.take(aspect, realDrainAmount);
					updateAspects(stack, map -> map.add(aspect, realDrainAmount));
				}
			}
		});
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}
	
	public Optional<TooltipData> getTooltipData(ItemStack stack){
		return Optional.of(new WandAspectsTooltipData(stack));
	}
	
	// TODO: NBT-backed aspect map?
	
	public static void updateAspects(ItemStack stack, Consumer<AspectMap> updater){
		var map = aspectsFrom(stack);
		updater.accept(map);
		putAspects(stack, map);
	}
	
	public static AspectMap aspectsFrom(ItemStack stack){
		return AspectMap.fromNbt(stack.getSubNbt("aspects"));
	}
	
	public static void putAspects(ItemStack stack, AspectMap aspects){
		stack.getOrCreateNbt().put("aspects", aspects.toNbt());
	}
	
	public static Cap capFrom(ItemStack stack){
		return capFrom(stack.getOrCreateNbt());
	}
	
	public static Cap capFrom(NbtCompound nbt){
		return Cap.CAPS.get(new Identifier(nbt.getString("cap_id")));
	}
	
	public static Core coreFrom(ItemStack stack){
		return coreFrom(stack.getOrCreateNbt());
	}
	
	public static Core coreFrom(NbtCompound nbt){
		return Core.CORES.get(new Identifier(nbt.getString("core_id")));
	}
}