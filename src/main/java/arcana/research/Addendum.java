package arcana.research;

import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import java.util.List;

import static arcana.util.StreamUtil.streamAndApply;

public record Addendum(
		Entry owner,
		Identifier id,
		String name,
		List<EntrySection> sections,
		List<Requirement> autoUnlockReqs){
	
	public NbtCompound toNbt(){
		NbtCompound nbt = new NbtCompound();
		
		nbt.putString("id", id.toString());
		nbt.putString("name", name);
		nbt.put("sections", sections.stream().map(EntrySection::getPassData).collect(NbtUtil.toNbtList()));
		nbt.put("autoUnlockReqs", autoUnlockReqs.stream().map(Requirement::getPassData).collect(NbtUtil.toNbtList()));
		
		return nbt;
	}
	
	public static Addendum fromNbt(NbtCompound compound, Entry owner){
		Identifier id = new Identifier(compound.getString("id"));
		String name = compound.getString("name");
		
		List<EntrySection> sections = streamAndApply(
				compound.getList("sections", NbtElement.COMPOUND_TYPE), NbtCompound.class,
				EntrySection::deserialize).toList();
		
		List<Requirement> autoUnlockReqs = streamAndApply(
				compound.getList("autoUnlockReqs", NbtElement.COMPOUND_TYPE), NbtCompound.class,
				Requirement::deserialize).toList();
		
		return new Addendum(owner, id, name, sections, autoUnlockReqs);
	}
}