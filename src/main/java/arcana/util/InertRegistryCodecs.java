package arcana.util;

import com.mojang.serialization.Codec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class InertRegistryCodecs{
	
	/// Creates a Codec intended for client usage, where the referenced registry might not have been synced.
	/// In that case, the resulting list will either be empty (if a tag) or contain valueless entries (if directly specified).
	public static <U> Codec<RegistryEntryList<U>> create(RegistryKey<? extends Registry<U>> registryRef){
		return Codec.withAlternative(RegistryCodecs.entryList(registryRef), createInert(registryRef));
	}
	
	private static <U> Codec<RegistryEntryList<U>> createInert(RegistryKey<? extends Registry<U>> registryRef){
		InertOwner<U> owner = new InertOwner<>();
		return Codec.withAlternative(
				// tag case
				TagKey.codec(registryRef).xmap(
						tag -> RegistryEntryList.of(owner, tag),
						it -> it.getTagKey().get()
				),
				Codec.withAlternative(
						// single entry case
						Identifier.CODEC.xmap(
								id -> RegistryEntryList.of(new InertRegistryEntry<>(owner, RegistryKey.of(registryRef, id))),
								it -> it.get(0).getKey().get().getValue()
						),
						// direct list case
						Identifier.CODEC.listOf().<RegistryEntryList<U>>xmap(
								ids -> RegistryEntryList.of(ids.stream().map(x -> new InertRegistryEntry<>(owner, RegistryKey.of(registryRef, x))).toList()),
								it -> it.stream().map(x -> x.getKey().get().getValue()).toList()
						)
				)
		);
	}
	
	private static class InertOwner<T> implements RegistryEntryOwner<T>{}
	
	private static class InertRegistryEntry<T> extends RegistryEntry.Reference<T>{
		
		private InertRegistryEntry(RegistryEntryOwner<T> owner, RegistryKey<T> registryKey){
			super(Type.STAND_ALONE, owner, registryKey, null);
		}
	}
}