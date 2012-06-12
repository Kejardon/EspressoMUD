package com.planet_ink.coffee_mud.core;
import java.util.*;
import java.lang.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class DVector implements Cloneable, java.io.Serializable
{
	public static final long serialVersionUID=0;
	/*public static final Enumeration emptyEnumeration=new Vector().elements();
	public static final Iterator emptyIterator=new Vector().iterator();*/
	protected int dimensions;
	private ArrayList<Object[]> stuff;
	public DVector(int dim)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new ArrayList();
	}
	public DVector(int dim, int startingSize)
	{
		if(dim<1) throw new java.lang.IndexOutOfBoundsException();
		dimensions=dim;
		stuff=new ArrayList(startingSize);
	}
	
	public void clear()
	{
		synchronized(stuff) { stuff.clear(); }
	}

	public void trimToSize()
	{
		synchronized(stuff) { stuff.trimToSize(); }
	}
	
	public int indexOf(Object O)
	{
		synchronized(stuff)
		{
			int x=0;
			if(O==null)
			{
				for(x=0;x<stuff.size();x++)
					if((stuff.get(x)[0])==null)
						return x;
			}
			else
			for(x=0;x<stuff.size();x++)
				if(O.equals(stuff.get(x)[0]))
					return x;
		}
		return -1;
	}
	public Object[] elementsAt(Object O)
	{
		synchronized(stuff)
		{
			if(O==null)
			{
				for(int x=0;x<stuff.size();x++)
					if((stuff.get(x)[0])==null)
						return stuff.get(x);
			}
			else for(int x=0;x<stuff.size();x++)
				if(O.equals(stuff.get(x)[0]))
					return stuff.get(x);
		}
		return null;
	}
	public Object[] elementsAt(int x)
	{
		synchronized(stuff)
		{
			return stuff.get(x);
		}
	}
	
	public Object[] removeElementsAt(int x)
	{
		synchronized(stuff)
		{
			return stuff.remove(x);
		}
	}
	
	public DVector copyOf()
	{
		DVector V;
		synchronized(stuff)
		{
			V=new DVector(dimensions, stuff.size());
			for(int i=0;i<stuff.size();i++)
				V.stuff.add(stuff.get(i).clone());
		}
		return V;
	}

	//Note: All values MUST NOT be null, and be comparable to eachother, to sort them.
	//This is not checked anywhere within this code, and must be confirmed/caught externally instead.
	public void sortBy(int dim)
	{
		if((dim<0)||(dim>=dimensions)) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			PriorityQueue<SortWrapper> sortList=new PriorityQueue(stuff.size());
			for(int i=0;i<stuff.size();i++)
			{
				Object[] objs=stuff.get(i);
				sortList.add(new SortWrapper((Comparable)objs[dim], objs));
			}
			for(int i=0;i<stuff.size();i++)
				stuff.set(i, sortList.poll().myRow);
		}
	}
	private static class SortWrapper implements Comparable<SortWrapper>
	{
		public final Comparable myObj;
		public final Object[] myRow;
		public SortWrapper(Comparable obj, Object[] row){myObj=obj; myRow=row;}
		public boolean equals(Comparable O){return myObj.equals(((SortWrapper)O).myObj);}
		public int compareTo(SortWrapper O){return myObj.compareTo(((SortWrapper)O).myObj);}
	}

	public static DVector toDVector(Hashtable h)
	{
		DVector DV=new DVector(2, h.size());
		for(Enumeration e=h.keys();e.hasMoreElements();)
		{
			Object key=e.nextElement();
			DV.addRow(key,h.get(key));
		}
		return DV;
	}
	
	public void addRow(Object... O)
	{
		if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff.add(O);
		}
	}
	
	public void addCopyRow(Object[] O)
	{
		if(dimensions!=O.length) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			stuff.add((Object[])O.clone());
		}
	}
	
	public boolean contains(Object O){
		return indexOf(O)>=0;
	}
	public boolean containsIgnoreCase(String S)
	{
		synchronized(stuff)
		{
			if(S==null) return indexOf(null)>=0;
			for(Object[] O : stuff)
				if(S.equalsIgnoreCase(O[0].toString()))
					return true;
		}
		return false;
	}
	public int size()
	{
		return stuff.size();
	}
	public void removeRow(int i)
	{
		synchronized(stuff)
		{
			if(i>=0)
				stuff.remove(i);
		}
	}
	public void removeElement(Object O)
	{
		synchronized(stuff)
		{
			removeRow(indexOf(O));
		}
	}
	public Vector getDimensionVector(int dim)
	{
		if(dimensions<=dim) throw new java.lang.IndexOutOfBoundsException();
		Vector V;
		synchronized(stuff)
		{
			V=new Vector(stuff.size());
			for(int i=0;i<stuff.size();i++)
				V.add(stuff.get(i)[dim]);
		}
		return V;
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
*/
	public Object elementAt(int i, int dim)
	{
		if(dimensions<=dim) throw new java.lang.IndexOutOfBoundsException();
		synchronized(stuff)
		{
			return stuff.get(i)[dim];
		}
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
		/* this is slower -- more than twice as slow, believe it or not! */
		//return keys?((Hashtable)H.clone()).keys():((Hashtable)H.clone()).elements(); 
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
}
