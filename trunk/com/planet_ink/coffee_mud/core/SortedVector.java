package com.planet_ink.coffee_mud.core;
import java.util.*;
import java.lang.Comparable;

/* 
	Copyright 2011 Kejardon
	Simple small thing to add objects to a vector, assuming the vector is presorted and comparable.
	Optimized for adding large objects in a vector of smaller objects.
	Not properly set up but screw it, it'll work how I want it to work.
*/
//This is really just about as good as the class can get without putting try/catch blocks everywhere in case of typecasting problems.
public class SortedVector<E extends Comparable<? super E>> extends Vector<E>
{
	public boolean add(E O)
	{
		int i=0;
		for(i=size()-1;i>=0;i--)
			if(O.compareTo(get(i))>0)
				break;
		add(i+1, O);
		return true;
	}
	//More efficent option for objects that may go anywhere in the vector.
	public void addRandom(E O)
	{
		int i=Collections.binarySearch(this, O);
		if(i<0) i=-i-1;
		add(i, O);
	}
	public E lastElement()
	{
		try{return super.lastElement();}
		catch(NoSuchElementException e){}
		return null;
	}
	public boolean contains(E O)
	{
		return (Collections.binarySearch(this, O)>=0);
	}
	public boolean contains(Object O){return false;}
	public boolean remove(E O)
	{
		int i=Collections.binarySearch(this, O);
		if(i>=0)
		{
			remove(i);
			return true;
		}
		return false;
	}
	public boolean remove(Object O){return false;}
	public int indexOf(E O)
	{
		return Collections.binarySearch(this, O);
	}
	public int indexOf(Object O){return -1;}
	
	//Container class for making sortable objects
	//Call SortedList's instead, it's identical to the below
/*
	public static class SortableObject<E> implements Comparable<SortableObject>
	{
		public final E myObj=null;
		public final myInt=0;
		public SortableObject(E O, int i){myInt=i; myObj=O;}
		
		public int compareTo(SortableObject O)
		{ return myInt-O.myInt; }
		public boolean equals(Object O)
		{
			if(O instanceof SortableObject)
				return myInt==((SortableObject)O).myInt;
			return false;
		}
//		public int myInt(){return myInt;}
	}
*/
}
