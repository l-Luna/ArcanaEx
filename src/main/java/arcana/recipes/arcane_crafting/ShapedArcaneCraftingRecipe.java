package arcana.recipes.arcane_crafting;

import arcana.api.RenamableRecipe;
import arcana.aspects.AspectMap;
import arcana.recipes.ArcanaRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.Optional;

import static arcana.Arcana.arcId;

public class ShapedArcaneCraftingRecipe extends ShapedRecipe implements ArcaneCraftingRecipe, RenamableRecipe, ArcanaRecipe{
	
	public static RecipeType<ArcaneCraftingRecipe> TYPE;
	public static Serializer SERIALIZER;
	
	public static void setup(){
		TYPE = Registry.register(
				Registries.RECIPE_TYPE,
				arcId("arcane_crafting"),
				new RecipeType<>(){
					public String toString(){
						return "arcana:arcane_crafting";
					}
				}
		);
		SERIALIZER = Registry.register(
				Registries.RECIPE_SERIALIZER,
				arcId("arcane_crafting"),
				new Serializer()
		);
	}
	
	private final AspectMap aspects;
	private final String translationKey;
	
	public ShapedArcaneCraftingRecipe(String group, RawShapedRecipe raw, ItemStack result, AspectMap aspects, String translationKey){
		super(group, CraftingRecipeCategory.MISC, raw, result, false);
		this.aspects = aspects;
		this.translationKey = translationKey;
	}
	
	public RecipeType<?> getType(){
		return TYPE;
	}
	
	public RecipeSerializer<?> getSerializer(){
		return SERIALIZER;
	}
	
	public AspectMap aspects(){
		return aspects;
	}
	
	public Optional<String> getTranslationKey(){
		return Optional.ofNullable(translationKey);
	}
	
	public boolean isIgnoredInRecipeBook(){
		return true;
	}
	
	public static class Serializer implements RecipeSerializer<ShapedArcaneCraftingRecipe>{
		public static final MapCodec<ShapedArcaneCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
						Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
						RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw),
						ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
						AspectMap.CODEC.fieldOf("aspects").forGetter(ShapedArcaneCraftingRecipe::aspects),
						Codec.STRING.optionalFieldOf("name", null).forGetter(recipe -> recipe.translationKey)
				)
				.apply(i, ShapedArcaneCraftingRecipe::new));
		public static final PacketCodec<RegistryByteBuf, ShapedArcaneCraftingRecipe> PACKET_CODEC = PacketCodec.ofStatic(
				ShapedArcaneCraftingRecipe.Serializer::write, ShapedArcaneCraftingRecipe.Serializer::read
		);
		
		@Override
		public MapCodec<ShapedArcaneCraftingRecipe> codec(){
			return CODEC;
		}
		
		@Override
		public PacketCodec<RegistryByteBuf, ShapedArcaneCraftingRecipe> packetCodec(){
			return PACKET_CODEC;
		}
		
		private static ShapedArcaneCraftingRecipe read(RegistryByteBuf buf){
			String string = buf.readString();
			RawShapedRecipe rawShapedRecipe = RawShapedRecipe.PACKET_CODEC.decode(buf);
			ItemStack itemStack = ItemStack.PACKET_CODEC.decode(buf);
			AspectMap aspects = AspectMap.PACKET_CODEC.decode(buf);
			String translationKey = buf.readBoolean() ? buf.readString() : null;
			return new ShapedArcaneCraftingRecipe(string, rawShapedRecipe, itemStack, aspects, translationKey);
		}
		
		private static void write(RegistryByteBuf buf, ShapedArcaneCraftingRecipe recipe){
			buf.writeString(recipe.getGroup());
			RawShapedRecipe.PACKET_CODEC.encode(buf, recipe.raw);
			ItemStack.PACKET_CODEC.encode(buf, recipe.result);
			AspectMap.PACKET_CODEC.encode(buf, recipe.aspects);
			boolean bl = recipe.translationKey != null;
			buf.writeBoolean(bl);
			if(bl)
				buf.writeString(recipe.translationKey);
		}
	}
}