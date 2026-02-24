package arcana.research;

import arcana.util.NbtUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
		List<EntrySection> sections = NbtUtil.readList(compound, "sections", EntrySection::deserialize);
		List<Requirement> autoUnlockReqs = NbtUtil.readList(compound, "autoUnlockReqs", Requirement::deserialize);
		return new Addendum(owner, id, name, sections, autoUnlockReqs);
	}
	
	public boolean equals(Object obj){
		return obj instanceof Addendum other && other.id().equals(id());
	}
	
	public int hashCode(){
		return id().hashCode();
	}
	
	public @NotNull String toString(){
		return id().toString();
	}
}