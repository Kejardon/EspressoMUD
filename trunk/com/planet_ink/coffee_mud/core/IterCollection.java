/*
 * Highly efficient Collection-cum-Iterator that is unsafe for common use but
 * works for the specialized purpose I want it for.
 */
package com.planet_ink.coffee_mud.core;

import com.planet_ink.coffee_mud.core.interfaces.ItemCollection;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
/**
 *
 * @author Kevin
 */
public class IterCollection<E> implements Collection, Iterator
{
	protected static final ConcurrentLinkedQueue<IterCollection> cache=new ConcurrentLinkedQueue();
	//Special method of getting a new ItemLocation. Contains uninitialized data.
	public static IterCollection ICFactory(Object[] data)
	{
		IterCollection IC=cache.poll();
		if(IC==null)
			IC=new IterCollection(data);
		else
		{
			IC.myArray=data;
			IC.position=0;
		}
		return IC;
	}
	protected E[] myArray;
	protected int position=0;
	public IterCollection(E[] array)
	{
		myArray=array;
	}

	public int size() { return myArray.length; }

	public boolean isEmpty() { return myArray.length==0; }

	public boolean contains(Object o)
	{
		for(E obj : myArray)
			if(obj.equals(o)) return true;
		return false;
	}

	public Iterator iterator() { return this; }

	public Object[] toArray() { return myArray; }

	public Object[] toArray(Object[] a) { return myArray; }

	public boolean add(Object e) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean containsAll(Collection c)
	{
		out:
		for(Object objA : c)
		{
			for(E objB : myArray)
				if(objA.equals(objB)) continue out;
			return false;
		}
		return true;
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void clear() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean hasNext()
	{
		return position<myArray.length;
	}

	public Object next() {
		try{ return myArray[position++]; }
		catch(ArrayIndexOutOfBoundsException e){throw new NoSuchElementException();}
	}

	public void remove() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	public void reset() {position=0;}
	public void returnIC()
	{
		myArray=null;
		cache.offer(this);
	}
}
