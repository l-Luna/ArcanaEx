package arcana.mixin.lectern;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.LecternBlockEntityRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LecternBlockEntityRenderer.class)
public class LecternBlockEntityRendererMixin{

	@ModifyExpressionValue(method = "render(Lnet/minecraft/block/entity/LecternBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V",
	                       at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/block/entity/EnchantingTableBlockEntityRenderer;BOOK_TEXTURE:Lnet/minecraft/client/util/SpriteIdentifier;", opcode = Opcodes.GETSTATIC))
	private static SpriteIdentifier getBookSprite(SpriteIdentifier original, LecternBlockEntity lectern, float f, MatrixStack ms, VertexConsumerProvider vcp, int i, int j){
		/*ItemStack book = lectern.getBook();
		if(book.isIn(ArcanaTags.LECTERN_WHITELIST))
			return new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, arcId("entity/lectern/" + Registry.ITEM.getId(book.getItem()).getPath()));*/
		return original;
	}
}