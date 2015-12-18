package csu.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * @author utisam
 *
 * @param <T>
 */
public class UnionFindTree<T> implements Collection<T> {
	
	public final Map<T, T> parent;
	public final Map<T, Integer> rank;
	
	public UnionFindTree() {
		parent = new HashMap<T, T>();
		rank = new HashMap<T, Integer>();
	}
	
	public UnionFindTree(final int size) {
		parent = new HashMap<T, T>(size);
		rank = new HashMap<T, Integer>(size);
	}
	
	public UnionFindTree(final Collection<T> c) {
		parent = new HashMap<T, T>(c.size());
		rank = new HashMap<T, Integer>(c.size());
		addAll(c);
	}
	
	protected UnionFindTree(final Map<T, T> parentMap, final Map<T, Integer> rankMap) {
		parent = parentMap;
		rank = rankMap;
	}
	
	@Override
	public boolean add(final T o) {
		if (o == null) throw new NullPointerException();
		return parent.put(o, o) == null && rank.put(o, 0) == null;
	}
	
	@Override
	public boolean addAll(final Collection<? extends T> c) {
		boolean result = false;
		for (T x : c) {
			result = this.add(x) || result;
		}
		return result;
	}
	
	private T find(final T x) {
		if (x == null) return null;
		final T px = parent.get(x);
		if (px == null) return null;
		if (px.equals(x)) {   // x is the root
			return x;
		}
		final T y = find(px);
		parent.put(x, y);
		return y;
	}
	
	public void unite(final T x, final T y) {
		final T xRoot = find(x);
		final T yRoot = find(y);
		if (xRoot == null || xRoot.equals(yRoot)) return;
		final int xRank = rank.get(xRoot);
		final int yRank = rank.get(yRoot); 
		if (xRank < yRank) {
			parent.put(xRoot, yRoot);
		} else {
			parent.put(yRoot, xRoot);
			if (xRank == yRank) rank.put(xRoot, xRank + 1);
		}
	}
	
	public boolean same(final T x, final T y) {
		final T xRoot = find(x);
		final T yRoot = find(y);
		return xRoot != null && yRoot != null && xRoot.equals(yRoot);
	}

	@Override
	public void clear() {
		parent.clear();
		rank.clear();
	}

	@Override
	public boolean contains(final Object o) {
		return parent.containsKey(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return parent.keySet().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return parent.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return parent.keySet().iterator();
	}

	@Override
	public boolean remove(final Object o) {
		try {
			separate(o);
			return parent.remove(o) != null && rank.remove(o) != null;
		} catch (ClassCastException e) {
		}
		return false;
	}

	public void separate(final Object o) {
		final T p = parent.get(o);
		if (o.equals(p)) {
			final ArrayList<T> separated = new ArrayList<T>();
			for (Entry<T, T> entry : parent.entrySet()) {
				if (entry.getValue().equals(o)) {
					T key = entry.getKey();
					entry.setValue(key);
					separated.add(key);
				}
			}
			for (int i = 1; i < separated.size(); ++i) {
				unite(separated.get(0), separated.get(i));
			}
		} else {
			for (Entry<T, T> entry : parent.entrySet()) {
				if (entry.getValue().equals(o)) {
					entry.setValue(p);
				}
			}
		}
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean result = false;
		for (Object o : c) {
			result = remove(o) || result;
		}
		return result;
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		boolean result = false;
		for (Iterator<T> it = iterator(); it.hasNext();) {
			T next = it.next();
			if (!c.contains(next)) {
				it.remove();
				result = true;
			}
		}
		return result;
	}
	
	@Override
	public Spliterator<T> spliterator() {
		return Spliterators.spliterator(this, 0);
	}
	
	public void resetAll() {
		for (Entry<T, T> entry : parent.entrySet()) {
			entry.setValue(entry.getKey());
		}
		for (Entry<T, Integer> entry : rank.entrySet()) {
			entry.setValue(0);
		}
	}

	@Override
	public int size() {
		return parent.size();
	}

	@Override
	public Object[] toArray() {
		return parent.keySet().toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return parent.keySet().toArray(a);
	}
}
