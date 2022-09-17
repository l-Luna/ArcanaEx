package arcana.nodes;

import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public record NodeType(Identifier id, int rechargeTime, int aspectCap, Consumer<Node> ticker){}