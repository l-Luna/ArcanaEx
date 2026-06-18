package arcana;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.NoSuchElementException;

import static arcana.Arcana.arcId;

public class Registerable<T>{
	
	private final T value;
	private RegistryEntry<T> entry;
	
	public Registerable(T value){
		this.value = value;
	}
	
	public T value(){
		return value;
	}
	
	public RegistryEntry<T> entry(){
		if(entry == null)
			throw new NoSuchElementException("RegistryEntry not set for \"" + value + "\"");
		return entry;
	}
	
	public Registerable<T> register(Registry<T> registry, String id){
		entry = Registry.registerReference(registry, arcId(id), value);
		return this;
	}
}