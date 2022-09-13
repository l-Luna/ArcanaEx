package arcana.commands;

import arcana.components.AuraWorld;
import arcana.nodes.Node;
import arcana.nodes.NodeTypes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.command.argument.Vec3ArgumentType.getVec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class NodeCommand{
	
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_NODE_TYPES =
			(context, builder) -> CommandSource.suggestIdentifiers(NodeTypes.NODE_TYPES.keySet().stream(), builder);
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
	                            CommandRegistryAccess registry,
	                            CommandManager.RegistrationEnvironment env){
		// arcana nodes [add|remove|list]
		// arcana nodes add <type> <x> <y> <z>
		// arcana nodes remove <nodes>
		// arcana nodes list <nodes>?
		// <nodes> = @n (nearest) or @i[x1,y1,z1,x2,y2,z2] (in AABB)
		dispatcher.register(
				literal("arcana-nodes")
						.requires(source -> source.hasPermissionLevel(2))
						.then(literal("add")
								.then(argument("type", identifier())
										.then(argument("position", Vec3ArgumentType.vec3())
												.executes(NodeCommand::performAdd)
										)
										.suggests(SUGGEST_NODE_TYPES)
								)
						).then(literal("remove") // TODO: use NodesArgumentType
								.then(argument("nodes", EntityArgumentType.entities())
										.executes(NodeCommand::performRemove)
								)
						).then(literal("list")
								.executes(NodeCommand::performList)
						)
		);
	}
	
	private static int performAdd(CommandContext<ServerCommandSource> context){
		World world = context.getSource().getWorld();
		AuraWorld aura = world.getComponent(AuraWorld.KEY);
		
		aura.addNode(new Node(NodeTypes.byName(getIdentifier(context, "type")), world, getVec3(context, "position")));
		return 1;
	}
	
	private static int performRemove(CommandContext<ServerCommandSource> context){
		return 0;
	}
	
	private static int performList(CommandContext<ServerCommandSource> context){
		World world = context.getSource().getWorld();
		AuraWorld aura = world.getComponent(AuraWorld.KEY);
		context.getSource().sendMessage(Text.literal(aura.getNodes().toString()));
		return 1;
	}
}