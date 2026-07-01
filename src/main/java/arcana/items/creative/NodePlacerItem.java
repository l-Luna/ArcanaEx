package arcana.items.creative;

import arcana.aura.AuraWorld;
import arcana.aura.Node;
import arcana.aura.NodeType;
import arcana.aura.NodeTypes;
import arcana.items.components.ArcanaDataComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NodePlacerItem extends Item{
	
	public NodePlacerItem(Settings settings){
		super(settings);
	}
	
	public ActionResult useOnBlock(ItemUsageContext ctx){
		ItemStack stack = ctx.getStack();
		NodeType type = typeFor(stack);
		PlayerEntity player = ctx.getPlayer();
		if(player != null && player.isSneaking()){
			cycleType(player, stack);
		}else{
			BlockPos targetPos = ctx.getBlockPos().offset(ctx.getSide());
			AuraWorld.from(ctx.getWorld()).addNode(new Node(type, Vec3d.ofCenter(targetPos), type.randomCap(ctx.getWorld().getRandom())));
		}
		return ActionResult.SUCCESS;
	}
	
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand){
		if(user.isSneaking()){
			ItemStack stack = user.getStackInHand(hand);
			cycleType(user, stack);
			return TypedActionResult.success(stack);
		}
		return super.use(world, user, hand);
	}
	
	public void appendTooltip(ItemStack stack, @Nullable TooltipContext ctx, List<Text> tooltip, TooltipType type){
		super.appendTooltip(stack, ctx, tooltip, type);
		tooltip.add(typeFor(stack).name());
	}
	
	private static void setTypeFor(ItemStack stack, NodeType type){
		stack.set(ArcanaDataComponents.NODE_TYPE, type);
	}
	
	private static NodeType typeFor(ItemStack stack){
		return stack.getOrDefault(ArcanaDataComponents.NODE_TYPE, NodeTypes.NORMAL);
	}
	
	private static void cycleType(PlayerEntity user, ItemStack stack){
		NodeType ty = NodeTypes.cycle(typeFor(stack));
		setTypeFor(stack, ty);
		user.sendMessage(ty.name(), true);
	}
}