package com.planet_ink.coffee_mud.core;
import java.util.*;
import java.lang.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public abstract class DVector<A> implements Cloneable
{
	//protected int dimensions;
	public abstract int dimensions();
	public DVector create(int dim)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		switch(dim)
		{
			case 2: return new D2Vector();
		}
		return new DNVector(dim);
	}
	protected final ReentrantReadWriteLock lock=new ReentrantReadWriteLock();
	protected final ArrayList<A> dim1;
	public DVector(){dim1=new ArrayList<>();}
	public DVector(int cap){dim1=new ArrayList<>(cap);}
	public static class DNVector<A> extends DVector<A>
	{
		protected final ArrayList[] dims;
		@Override public int dimensions(){return dims.length;}
		public DNVector(int dim){
			dims=new ArrayList[dim-1];
			for(int i=0;i<dim-1;i++) dims[i]=new ArrayList();
		}
		public DNVector(int dim, int cap){ super(cap);
			dims=new ArrayList[dim-1];
			for(int i=0;i<dim-1;i++) dims[i]=new ArrayList(cap);
		}
		
		@Override public void clear() {
			lock.writeLock().lock();
			try { dim1.clear(); for(ArrayList l : dims) l.clear(); }
			finally{ lock.writeLock().unlock(); }
		}
		@Override public DNVector<A> copyOf() {
			DNVector copy=new DNVector(dims.length);
			lock.readLock().lock();
			try {
				copy.dim1.addAll(dim1);
				for(int i=0; i<dims.length; i++) {
					copy.dims[i].addAll(dims[i]);
				}
				return copy;
			}
			finally {lock.readLock().unlock();}
		}
		@Override protected ArrayList getDimension(int i) {
			return (i==0?dim1:dims[i-1]);
		}
	}
	public static class D2Vector<A,B> extends DVector<A>
	{
		@Override public int dimensions(){return 2;}
		public D2Vector(){
			dim2=new ArrayList<>(); }
		public D2Vector(int cap){ super(cap);
			dim2=new ArrayList<>(cap); }
		private final ArrayList<B> dim2;
		
		@Override public void clear() {
			lock.writeLock().lock();
			try { dim1.clear(); dim2.clear(); }
			finally{ lock.writeLock().unlock(); }
		}
		@Override public D2Vector<A,B> copyOf() {
			D2Vector copy=new D2Vector(dim1.size());
			lock.readLock().lock();
			try {
				copy.dim1.addAll(dim1);
				copy.dim2.addAll(dim2);
				return copy;
			}
			finally {lock.readLock().unlock();}
		}
		@Override protected ArrayList getDimension(int i) {
			switch(i) {
				case 0: return dim1;
				case 1: return dim2;
				default: throw new IndexOutOfBoundsException();
			}
		}
		public void put(A o1, B o2) {
			lock.writeLock().lock();
			try{
				dim1.add(o1);
				dim2.add(o2);
			}finally{lock.writeLock().unlock();}
		}
	}
	//private ArrayList<Object[]> stuff;
	
	protected abstract ArrayList getDimension(int i);
	public abstract void clear();
	public void trimToSize()
	{
		lock.writeLock().lock();
		try{
			for(int i=0;i<dimensions();i++)
			{
				getDimension(i).trimToSize();
			}
		}
		finally{ lock.writeLock().unlock(); }
	}
	
	public int indexOf(Object O)
	{
		lock.readLock().lock();
		try{ return dim1.indexOf(O); }
		finally{ lock.readLock().unlock(); }
	}
	//public Object[] elementsAt(Object O)
	//{
	//	synchronized(stuff)
	//	{
	//		if(O==null)
	//		{
	//			for(int x=0;x<stuff.size();x++)
	//				if((stuff.get(x)[0])==null)
	//					return stuff.get(x);
	//		}
	//		else for(int x=0;x<stuff.size();x++)
	//			if(O.equals(stuff.get(x)[0]))
	//				return stuff.get(x);
	//	}
	//	return null;
	//}
	//public Object[] elementsAt(int x)
	//{
	//	synchronized(stuff)
	//	{
	//		return stuff.get(x);
	//	}
	//}
	
	//public Object[] removeElementsAt(int x)
	//{
	//	synchronized(stuff)
	//	{
	//		return stuff.remove(x);
	//	}
	//}
	
	public abstract DVector copyOf();

	//Note: All values MUST NOT be null, and be comparable to eachother, to sort them.
	//This is not checked anywhere within this code, and must be confirmed/caught externally instead.
	//public void sortBy(int dim)
	//{
	//	if((dim<0)||(dim>=dimensions)) throw new java.lang.IndexOutOfBoundsException();
	//	synchronized(stuff)
	//	{
	//		PriorityQueue<SortWrapper> sortList=new PriorityQueue(stuff.size());
	//		for(int i=0;i<stuff.size();i++)
	//		{
	//			Object[] objs=stuff.get(i);
	//			sortList.add(new SortWrapper((Comparable)objs[dim], objs));
	//		}
	//		for(int i=0;i<stuff.size();i++)
	//			stuff.set(i, sortList.poll().myRow);
	//	}
	//}
	private static class SortWrapper implements Comparable<SortWrapper>
	{
		public final Comparable myObj;
		public final Object[] myRow;
		public SortWrapper(Comparable obj, Object[] row){myObj=obj; myRow=row;}
		public boolean equals(Comparable O){return myObj.equals(((SortWrapper)O).myObj);}
		public int compareTo(SortWrapper O){return myObj.compareTo(((SortWrapper)O).myObj);}
	}

	//public static DVector toDVector(Hashtable h)
	//{
	//	DVector DV=new DVector(2, h.size());
	//	for(Enumeration e=h.keys();e.hasMoreElements();)
	//	{
	//		Object key=e.nextElement();
	//		DV.addRow(key,h.get(key));
	//	}
	//	return DV;
	//}
	
	public void put(Object... O)
	{
		if(dimensions()!=O.length) throw new java.lang.IndexOutOfBoundsException();
		lock.writeLock().lock();
		try{
			for(int i=0;i<O.length;i++)
			{
				getDimension(i).add(O[i]);
			}
		}finally{lock.writeLock().unlock();}
	}
	
	//public void addRow(Object... O)
	//{
	//	if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
	//	synchronized(stuff)
	//	{
	//		stuff.add(O);
	//	}
	//}
	
	//public void addCopyRow(Object[] O)
	//{
	//	if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
	//	synchronized(stuff)
	//	{
	//		stuff.add((Object[])O.clone());
	//	}
	//}
	
	public boolean contains(Object O){
		return indexOf(O)>=0;
	}
	public boolean containsIgnoreCase(String S)
	{
		
		lock.readLock().lock();
		try{
			if(S==null) return indexOf(null)>=0;
			for(A O : dim1)
				if(S.equalsIgnoreCase(O.toString()))
					return true;
			return false;
		}
		finally{ lock.readLock().unlock(); }
	}
	public int size()
	{
		lock.readLock().lock();
		try {return dim1.size();}
		finally{ lock.readLock().unlock(); }
	}
	//public void removeRow(int i)
	//{
	//	synchronized(stuff)
	//	{
	//		if(i>=0)
	//			stuff.remove(i);
	//	}
	//}
	//public void removeElement(Object O)
	//{
	//	synchronized(stuff)
	//	{
	//		removeRow(indexOf(O));
	//	}
	//}
	public Vector getDimensionVector(int dim)
	{
		if(dimensions()<=dim) throw new java.lang.IndexOutOfBoundsException();
		Vector V=new Vector(dim1.size());
		lock.readLock().lock();
		try { V.addAll(getDimension(dim)); }
		finally{ lock.readLock().unlock(); }
		return V;
	}
	public Object get(int i, int dim)
	{
		if(dimensions()<=dim) throw new java.lang.IndexOutOfBoundsException();
		lock.readLock().lock();
		try { return getDimension(dim).get(i); }
		finally{ lock.readLock().unlock(); }
	}
/* This is silly. Why would this ever be needed?
	public Vector getRowVector(int row)
	{
		Vector V=new Vector(dimensions);
		synchronized(stuff)
		{
			Object[] O=elementsAt(row);
			for(int v=0;v<O.length;v++)
				V.addElement(O[v]);
		}
		return V;
	}
	public Object elementAt(int i, int dim)
	{
		if(dimensions()<=dim) throw new java.lang.IndexOutOfBoundsException();
		lock.readLock().lock();
		try { return getDimension(dim).get(i); }
		finally{ lock.readLock().unlock(); }
	}
	public void setElementAt(int index, int dim, Object O)
	{
		if(dimensions<=dim) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff.get(index)[dim]=O;
		}
	}
	public void insertRowAt(int here, Object... O)
	{
		if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff.add(here, O);
		}
	}
	
	public static Enumeration empty_enum() {
		return new Enumeration() {
			public boolean hasMoreElements() { return false;}
			public Object nextElement() { return null;}
		};
	};
	
	public static Iterator empty_iter() {
		return new Iterator() {
			public boolean hasNext() { return false;}
			public Object next() { return null;}
			public void remove() {}
		};
	};
	
	public static Enumeration s_enum(List V) {
		return new Enumeration() {
			Iterator i=null;
			public boolean hasMoreElements() { return i.hasNext();}
			public Object nextElement() { return i.next();}
			public Enumeration setV(List V) {
				if((V==null)||(V.size()==0))
					return empty_enum();
				i=s_iter(V);
				return this;
			}
		}.setV(V);
	}
	
	public static Iterator s_iter(List V) 
	{
		return new Iterator() 
		{
			boolean more=false;
			Object prevO=null;
			Object O=null;
			List V=null;
			int c=0;
			
			public boolean hasNext() { return more; }
			
			public int confirmDex(Object O)
			{
				try {
					for(int i=0;i<3;i++)
						if(V.get(c-i)==O)
							return c+1-i;
				} catch(Exception e){}
				return c;
			}
			
			public Object next() 
			{
				if(!more) 
					throw new java.util.NoSuchElementException("");
				prevO=O;
				try {
					c=confirmDex(O);
					O=V.get(c);
					more=true;
				} catch(Exception e) {
					more=false;
					O=null;
				}
				return prevO;
			}
			
			public Iterator setV(List V) {
				if((V==null)||(V.size()==0)) 
					return empty_iter();
				this.V=V;
				more=false;
				try {
					if(V.size()>0)
					{
						O=V.get(0);
						more=true;
					}
				} catch(Throwable t) {}
				return this;
			}
			
			public void remove() {
				try { V.remove(prevO); c--; }
				catch(Exception e){}
			}
		}.setV(V);
	}
	public static Enumeration s_enum(Hashtable H, boolean keys) 
	{
		if((H==null)||(H.size()==0))
			return empty_enum();
		Vector V=new Vector(H.size());
		if(keys)
			V.addAll(H.keySet());
		else
		for(Enumeration e=H.elements();e.hasMoreElements();)
			V.addElement(e.nextElement());
		return s_enum(V);
	}
*/
}
