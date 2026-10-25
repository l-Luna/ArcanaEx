package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.screens.MnemonicChestScreen;
import arcana.util.MnemonicInventoryData;
import arcana.util.SidedArrayInventory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class MnemonicChestBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<MnemonicInventoryData>{
	
	public final MnemonicInventoryData mnemonics = new MnemonicInventoryData();
	public final SidedArrayInventory inventory = new SidedArrayInventory(27)
			.withDefaultSidedRange(0, 27)
			.withListener(mnemonics)
			.withInsertionFilter(mnemonics::filter);
	
	public MnemonicChestBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.MNEMONIC_CHEST_BE, pos, state);
	}
	
	public MnemonicInventoryData getScreenOpeningData(ServerPlayerEntity player){
		return mnemonics;
	}
	
	public Text getDisplayName(){
		return Text.translatable("container.arcana.mnemonic_chest");
	}
	
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player){
		return new MnemonicChestScreen.Handler(syncId, playerInventory, inventory, mnemonics);
	}
	
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		nbt.put("inventory", inventory.toNbtList(lookup));
		nbt.put("mnemonics", mnemonics.toNbt(lookup));
	}
	
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup){
		inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE), lookup);
		mnemonics.readNbt(nbt.get("mnemonics"), lookup);
	}
}