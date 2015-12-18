package csu.util;

/**
 * @author utisam
 */
public class SetPair<T1, T2> {

	private final int hash;
	private T1 first;
	private T2 second;
	
	public SetPair(T1 first, T2 second) {
		hash = first.hashCode() ^ second.hashCode(); ///xor
		this.first = first;
		this.second = second;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SetPair<?, ?>) {
			@SuppressWarnings("unchecked")
			SetPair<T1, T2> e = (SetPair<T1, T2>) o;
			return (this.first().equals(e.first()) && this.second().equals(e.second()))
				|| (this.first().equals(e.second()) && this.second().equals(e.first()));
		}
		return false;
	}
	
	public T1 first() {
		return first;
	}
	
	private T2 second() {
		return second;
	}
}
