package arcana.commands;

import arcana.components.Researcher;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class WarpCommand{
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
	                            CommandRegistryAccess registry,
	                            CommandManager.RegistrationEnvironment env){
		// arcana-warp <player> show
		// arcana-warp <player> add <amount>
		// arcana-warp <player> set <amount>
		dispatcher.register(
				literal("arcana-warp")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("player", EntityArgumentType.player())
								.then(literal("show")
										.executes(WarpCommand::performShow)
								).then(literal("add")
										.then(argument("amount", IntegerArgumentType.integer())
												.executes(WarpCommand::performAdd)
										)
								).then(literal("set")
										.then(argument("amount", IntegerArgumentType.integer(0))
												.executes(WarpCommand::performSet)
										)
								)
						)
		);
	}
	
	private static int performShow(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		context.getSource().sendMessage(Text.translatable("message.arcana.warp.show", player.getDisplayName(), researcher.getWarp()));
		return researcher.getWarp();
	}
	
	private static int performAdd(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Researcher researcher = Researcher.from(player);
		int had = researcher.getWarp();
		var now = Math.max(0, had + amount);
		researcher.setWarp(now);
		context.getSource().sendMessage(Text.translatable("message.arcana.warp.add", player.getDisplayName(), amount, had, now));
		return now != had ? 1 : 0;
	}
	
	private static int performSet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		int amount = IntegerArgumentType.getInteger(context, "amount");
		Researcher researcher = Researcher.from(player);
		int had = researcher.getWarp();
		researcher.setWarp(amount);
		context.getSource().sendMessage(Text.translatable("message.arcana.warp.set", player.getDisplayName(), amount, had));
		return amount != had ? 1 : 0;
	}
}