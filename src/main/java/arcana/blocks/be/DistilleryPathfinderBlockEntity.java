package arcana.blocks.be;

import arcana.ArcanaRegistry;
import arcana.aspects.Aspect;
import arcana.aspects.AspectMap;
import arcana.aspects.Aspects;
import arcana.aspects.ItemAspectRegistry;
import arcana.aura.AuraWorld;
import arcana.aura.FluxOrigin;
import arcana.components.Researcher;
import arcana.research.BuiltinResearch;
import arcana.research.Research;
import arcana.screens.DistilleryPathfinderScreenHandler;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

public class DistilleryPathfinderBlockEntity extends BlockEntity implements NamedScreenHandlerFactory{
	
	private static final Text title = Text.translatable("block.arcana.distillery_pathfinder");
	
	public SimpleInventory material = new SimpleInventory(1), fuel = new SimpleInventory(1);
	public int burnTime, maxBurnTime, progress;
	public Optional<UUID> ownerUuid = Optional.empty();
	
	public final PropertyDelegate propertyDelegate = new PropertyDelegate(){
		// [burn time, max burn time, progress]
		public int get(int index){
			return switch(index){
				case 0 -> burnTime;
				case 1 -> maxBurnTime;
				case 2 -> progress;
				default -> -1;
			};
		}
		
		public void set(int index, int value){
			switch(index){
				case 0 -> burnTime = index;
				case 1 -> maxBurnTime = value;
				case 2 -> progress = value;
			}
		}
		
		public int size(){
			return 3;
		}
	};
	
	public DistilleryPathfinderBlockEntity(BlockPos pos, BlockState state){
		super(ArcanaRegistry.DISTILLERY_PATHFINDER_BE, pos, state);
		material.addListener(__ -> markDirty());
		fuel.addListener(__ -> markDirty());
	}
	
	protected void writeNbt(NbtCompound nbt){
		super.writeNbt(nbt);
		nbt.put("material", material.getStack(0).writeNbt(new NbtCompound()));
		nbt.put("fuel", fuel.getStack(0).writeNbt(new NbtCompound()));
		
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("maxBurnTime", maxBurnTime);
		nbt.putInt("progress", progress);
		
		ownerUuid.ifPresent(uuid -> nbt.putUuid("owner", uuid));
	}
	
	public void readNbt(NbtCompound nbt){
		super.readNbt(nbt);
		material.setStack(0, ItemStack.fromNbt(nbt.getCompound("material")));
		fuel.setStack(0, ItemStack.fromNbt(nbt.getCompound("fuel")));
		
		burnTime = nbt.getInt("burnTime");
		maxBurnTime = nbt.getInt("maxBurnTime");
		progress = nbt.getInt("progress");
		
		if(nbt.containsUuid("owner"))
			ownerUuid = Optional.of(nbt.getUuid("owner"));
	}
	
	public void tick(World world, BlockPos pos, BlockState state){
		// no material? no problem
		ItemStack material = this.material.getStack(0), fuel = this.fuel.getStack(0);
		boolean canActivate = !material.isEmpty()
				&& (burnTime > 0 || (!fuel.isEmpty() && FuelRegistry.INSTANCE.get(fuel.getItem()) > 0))
				&& !ItemAspectRegistry.get(material).isEmpty();
		
		if(canActivate){
			progress++;
			if(burnTime == 0){
				maxBurnTime = burnTime = FuelRegistry.INSTANCE.get(fuel.getItem());
				fuel.decrement(1);
			}
			markDirty();
		}else if(progress > 0){
			progress--;
			markDirty();
		}
		if(burnTime > 0){
			burnTime--;
			markDirty();
		}
		
		// now explode!
		if(progress > 13 * 20){
			AspectMap materialAspects = ItemAspectRegistry.get(material);
			// pick 3 random aspects the item has to generate crystals for
			for(int i = 0; i < 3; i++){
				Aspect a = materialAspects.aspectByIndex(world.random.nextInt(materialAspects.size()));
				ItemStack crystalStack = new ItemStack(Aspects.crystals.get(a));
				BlockPos p = getPos();
				ItemEntity crystalEntity = new ItemEntity(world, p.getX() + 0.5f, p.getY() + 1, p.getZ() + 0.5f, crystalStack);
				crystalEntity.addVelocity((world.random.nextFloat() - 0.5f) / 4f, 0.3f + world.random.nextFloat() / 3f, (world.random.nextFloat() - 0.5f) / 4f);
				world.spawnEntity(crystalEntity);
			}
			
			material.decrement(1);
			markDirty(); // lol
			world.setBlockState(pos, ArcanaRegistry.TAINT_GOO.getDefaultState());
			AuraWorld.from(world).incrementFlux(12, FluxOrigin.DISTILLERY_FAILURE, pos);
			// TODO: close any screens of this block
			
			if(ownerUuid.isPresent() && world instanceof ServerWorld sw && sw.getEntity(ownerUuid.get()) instanceof PlayerEntity pe){
				Researcher researcher = Researcher.from(pe);
				researcher.completePuzzle(Research.getPuzzle(BuiltinResearch.distilleryPathfinderPuzzle));
				researcher.doSync();
			}
		}
	}
	
	public Text getDisplayName(){
		return title;
	}
	
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player){
		return new DistilleryPathfinderScreenHandler(syncId, inv, material, fuel, propertyDelegate);
	}
	
	public void markDirty(){
		super.markDirty();
		if(world instanceof ServerWorld sw)
			sw.getChunkManager().markForUpdate(pos);
	}
	
	public Packet<ClientPlayPacketListener> toUpdatePacket(){
		return BlockEntityUpdateS2CPacket.create(this);
	}
	
	public NbtCompound toInitialChunkDataNbt(){
		return createNbt();
	}
}