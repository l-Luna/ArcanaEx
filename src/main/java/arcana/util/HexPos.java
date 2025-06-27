package arcana.util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.lang.Math.*;

// axial coordinate system based on https://www.redblobgames.com/grids/hexagons

public record HexPos(int q, int r){
	
	public static final List<HexPos> NEIGHBORS = List.of(
			new HexPos(1, 0), new HexPos(1, -1), new HexPos(0, -1),
			new HexPos(-1, 0), new HexPos(-1, 1), new HexPos(0, 1)
	);
	
	public int s(){
		return -q - r;
	}
	
	public HexPos add(HexPos other){
		return new HexPos(q + other.q, r + other.r);
	}
	
	public HexPos sub(HexPos other){
		return new HexPos(q - other.q, r - other.r);
	}
	
	public int distanceTo(HexPos other){
		return sub(other).length();
	}
	
	public boolean adjacentWith(HexPos other){
		// check that one diff is +1, one diff is -1, and the last is 0
		int diffQ = q - other.q, diffR = r - other.r, diffS = s() - other.s();
		// because the sum is guaranteed to be 0, it suffices to check that the max value is +1
		// and the min value is -1 for this to be true
		return max(max(diffQ, diffR), diffS) == 1 && min(min(diffQ, diffR), diffS) == -1;
	}
	
	public boolean alignedWith(HexPos other){
		// same algorithm as before, except instead of requiring +1/-1/0, just require +N/-N/0
		int diffQ = q - other.q, diffR = r - other.r, diffS = s() - other.s();
		return min(min(diffQ, diffR), diffS) == -max(max(diffQ, diffR), diffS);
	}
	
	public HexPos negate(){
		return new HexPos(-q, -r);
	}
	
	public int length(){
		return max(max(abs(q), abs(r)), abs(s()));
	}
	
	// printing, parsing, serialization, deserialization
	
	public @NotNull String toString(){
		return q + "," + r;
	}
	
	public static HexPos fromString(String string){
		var split = string.split(",", 2);
		return new HexPos(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
	}
	
	public long toLong(){
		return ((long)q << 32) | r;
	}
	
	public static HexPos fromLong(long pos){
		return new HexPos((int)((pos & 0xFFFFFFFF00000000L) >>> 32), (int)pos /* cast automatically discards bits */);
	}
	
	// byte buffers?
}