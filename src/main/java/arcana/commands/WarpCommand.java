package arcana.commands;

import arcana.components.Researcher;
import arcana.warp.WarpEvents;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class WarpCommand{
	
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_WARP_EVENTS =
			(context, builder) -> CommandSource.suggestIdentifiers(WarpEvents.events.keySet().stream(), builder);
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
	                            CommandRegistryAccess registry,
	                            CommandManager.RegistrationEnvironment env){
		// arcana-warp <player> show
		// arcana-warp <player> add <amount>
		// arcana-warp <player> set <amount>
		// arcana-warp <player> trigger <event>?
		dispatcher.register(
				literal("arcana-warp")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("player", EntityArgumentType.player())
								.then(literal("show")
										.executes(WarpCommand::performShow)
								).then(literal("show-effective")
										.executes(WarpCommand::performShowEffective)
								).then(literal("add")
										.then(argument("amount", IntegerArgumentType.integer())
												.executes(WarpCommand::performAdd)
										)
								).then(literal("set")
										.then(argument("amount", IntegerArgumentType.integer(0))
												.executes(WarpCommand::performSet)
										)
								).then(literal("trigger")
										.executes(WarpCommand::performTriggerRandom)
										.then(argument("event", IdentifierArgumentType.identifier())
												.executes(WarpCommand::performTriggerSpecific)
												.suggests(SUGGEST_WARP_EVENTS)
										)
								)
						)
		);
	}
	
	private static int performShow(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		context.getSource().sendMessage(Text.translatable("message.arcana.command.warp.show", player.getDisplayName(), researcher.getWarp()));
		return researcher.getWarp();
	}
	
	private static int performShowEffective(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		context.getSource().sendMessage(Text.translatable("message.arcana.command.warp.show.effective", player.getDisplayName(), researcher.getEffectiveWarp()));
		return researcher.getWarp();
	}
	
	private static int performAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Researcher researcher = Researcher.from(player);
		int had = researcher.getWarp();
		var now = Math.max(0, had + amount);
		researcher.setWarp(now);
		context.getSource().sendMessage(Text.translatable("message.arcana.command.warp.add", player.getDisplayName(), amount, had, now));
		return now != had ? 1 : 0;
	}
	
	private static int performSet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Researcher researcher = Researcher.from(player);
		int had = researcher.getWarp();
		researcher.setWarp(amount);
		context.getSource().sendMessage(Text.translatable("message.arcana.command.warp.set", player.getDisplayName(), amount, had));
		return amount != had ? 1 : 0;
	}
	
	private static int performTriggerRandom(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		WarpEvents.triggerEligible(player);
		context.getSource().sendMessage(Text.translatable("message.arcana.command.warp.trigger", player.getDisplayName()));
		return 1;
	}
	
	private static int performTriggerSpecific(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		var id = IdentifierArgumentType.getIdentifier(context, "event");
		WarpEvents.triggerEvent(player, WarpEvents.events.get(id));
		context.getSource().sendMessage(Text.translatable("message.arcana.command.warp.trigger.specific", id, player.getDisplayName()));
		return 1;
	}
}