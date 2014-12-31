package com.planet_ink.coffee_mud.core;
import java.util.*;
import java.lang.Comparable;

/* 
	Copyright 2011 Kejardon
	Simple small thing to add objects to a list, assuming the list is presorted and comparable.
	Optimized for adding large objects in a list of smaller objects, also add will return true
	if it became the new first element.
	Not properly set up but screw it, it'll work how I want it to work.
*/

public class SortedList<E extends Comparable> extends LinkedList<E>
{
	public boolean add(E O)
	{
		ListIterator l=listIterator(size());
		boolean notPassed=true;
		while(l.hasPrevious())
		{
			if(O.compareTo(l.previous())>0)
			{
				notPassed=false;
				l.next();
				break;
			}
		}
		l.add(O);
		return notPassed;
	}
	
	//Container class for making sortable objects
	public static class SortableObject<E> implements Comparable<SortableObject>
	{
		public final E myObj;
		public final int myInt;
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
}
