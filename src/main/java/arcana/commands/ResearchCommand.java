package arcana.commands;

import arcana.datagen.ArcanaDocsProvider;
import arcana.legacy_components.Researcher;
import arcana.research.Addendum;
import arcana.research.Entry;
import arcana.research.Puzzle;
import arcana.research.Research;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ResearchCommand{
	
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_ENTRIES =
			(context, builder) -> ArcanaCommands.suggestIdentifiers(Research.streamEntries().map(Entry::id), builder);
	
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_PUZZLES =
			(context, builder) -> ArcanaCommands.suggestIdentifiers(Research.streamPuzzles().map(Puzzle::id), builder);
	
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_ADDENDA =
			(context, builder) -> ArcanaCommands.suggestIdentifiers(Research.streamAddenda().map(Addendum::id), builder);
	
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
	                            CommandRegistryAccess registry,
	                            CommandManager.RegistrationEnvironment env){
		// arcana-research <player> reset
		// arcana-research <player> [give|take] [entry|puzzle|addendum] id
		// arcana-research generate-docs
		dispatcher.register(
				literal("arcana-research")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("player", EntityArgumentType.player())
								.then(literal("reset")
										.executes(ResearchCommand::performReset)
								).then(literal("give")
										.then(literal("entry")
												.then(argument("entry", IdentifierArgumentType.identifier())
														.executes(ResearchCommand::performGiveEntry)
														.suggests(SUGGEST_ENTRIES)
												)
										).then(literal("puzzle")
												.then(argument("puzzle", IdentifierArgumentType.identifier())
														.executes(ResearchCommand::performGivePuzzle)
														.suggests(SUGGEST_PUZZLES)
												)
										).then(literal("addendum")
												.then(argument("addendum", IdentifierArgumentType.identifier())
														.executes(ResearchCommand::performGiveAddendum)
														.suggests(SUGGEST_ADDENDA)
												)
										)
								).then(literal("take")
										.then(literal("entry")
												.then(argument("entry", IdentifierArgumentType.identifier())
														.executes(ResearchCommand::performTakeEntry)
														.suggests(SUGGEST_ENTRIES)
												)
										).then(literal("puzzle")
												.then(argument("puzzle", IdentifierArgumentType.identifier())
														.executes(ResearchCommand::performTakePuzzle)
														.suggests(SUGGEST_PUZZLES)
												)
										).then(literal("addendum")
												.then(argument("addendum", IdentifierArgumentType.identifier())
														.executes(ResearchCommand::performTakeAddendum)
														.suggests(SUGGEST_ADDENDA)
												)
										)
						)).then(literal("generate-docs").executes(ResearchCommand::generateDocs))
		);
	}
	
	private static int performReset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		researcher.reset();
		researcher.doSync();
		context.getSource().sendMessage(Text.translatable("message.arcana.command.research.reset", player.getDisplayName()));
		return 1;
	}
	
	private static int performGiveEntry(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		Entry entry = Research.getEntry(IdentifierArgumentType.getIdentifier(context, "entry"));
		int prevStage = researcher.entryStage(entry);
		researcher.completeEntry(entry);
		researcher.doSync();
		context.getSource().sendMessage(Text.translatable(
				"message.arcana.command.research.give.entry",
				Text.translatable(entry.name()),
				player.getDisplayName()));
		return researcher.entryStage(entry) - prevStage;
	}
	
	private static int performGivePuzzle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		Puzzle puzzle = Research.getPuzzle(IdentifierArgumentType.getIdentifier(context, "puzzle"));
		boolean had = researcher.isPuzzleComplete(puzzle);
		researcher.completePuzzle(puzzle);
		researcher.doSync();
		context.getSource().sendMessage(Text.translatable(
				"message.arcana.command.research.give.puzzle",
				Text.literal(puzzle.id().toString()),
				player.getDisplayName()));
		return had ? 0 : 1;
	}
	
	private static int performGiveAddendum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		Addendum puzzle = Research.getAddendum(IdentifierArgumentType.getIdentifier(context, "addendum"));
		boolean had = researcher.isAddendumComplete(puzzle);
		researcher.completeAddendum(puzzle);
		researcher.doSync();
		context.getSource().sendMessage(Text.translatable(
				"message.arcana.command.research.give.addendum",
				Text.literal(puzzle.id().toString()),
				player.getDisplayName()));
		return had ? 0 : 1;
	}
	
	private static int performTakeEntry(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		Entry entry = Research.getEntry(IdentifierArgumentType.getIdentifier(context, "entry"));
		int stage = researcher.entryStage(entry);
		researcher.resetEntry(entry);
		researcher.doSync();
		context.getSource().sendMessage(Text.translatable(
				"message.arcana.command.research.take.entry",
				Text.translatable(entry.name()),
				player.getDisplayName()));
		return stage;
	}
	
	private static int performTakePuzzle(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		Puzzle puzzle = Research.getPuzzle(IdentifierArgumentType.getIdentifier(context, "puzzle"));
		boolean had = researcher.isPuzzleComplete(puzzle);
		researcher.uncompletePuzzle(puzzle);
		researcher.doSync();
		context.getSource().sendMessage(Text.translatable(
				"message.arcana.command.research.take.puzzle",
				Text.literal(puzzle.id().toString()),
				player.getDisplayName()));
		return had ? 1 : 0;
	}
	
	private static int performTakeAddendum(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
		var player = EntityArgumentType.getPlayer(context, "player");
		Researcher researcher = Researcher.from(player);
		Addendum puzzle = Research.getAddendum(IdentifierArgumentType.getIdentifier(context, "addendum"));
		boolean had = researcher.isAddendumComplete(puzzle);
		researcher.uncompleteAddendum(puzzle);
		researcher.doSync();
		context.getSource().sendMessage(Text.translatable(
				"message.arcana.command.research.take.addendum",
				Text.literal(puzzle.id().toString()),
				player.getDisplayName()));
		return had ? 1 : 0;
	}
	
	private static int generateDocs(CommandContext<ServerCommandSource> context){
		try{
			ArcanaDocsProvider.booksToDocs();
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
		return 0;
	}
}