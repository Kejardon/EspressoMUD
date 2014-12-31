package com.planet_ink.coffee_mud.core;
import java.util.*;

/* 
	Copyright 2011 Kejardon
	Hashmap with a list that can give an ordered iterator of its contents, iterator always does its best to continue working.
	Optimized for adding larger values first and smaller values later, removal time always the same (fast).
*/

public class SortedHashedList<E>
{
	private HashMap<E,ListNode<E>> set=new HashMap<E,ListNode<E>>();
	
	private ListNode<E> first=null;
	private ListNode<E> last=null;
	
	public E first(){return first.O;}
	public E last(){return last.O;}
	public void add(E object, int value)
	{
		ListNode<E> newEntry=new ListNode<E>(object, value);
		ListNode<E> oldEntry=set.put(object, newEntry);
		if(oldEntry!=null)
		{
			if(oldEntry.previous!=null)
				oldEntry.previous.next=oldEntry.next;
			else
				first=oldEntry.next;
			if(oldEntry.next!=null)
				oldEntry.next.previous=oldEntry.previous;
			else
				last=oldEntry.previous;
		}
		if(first==null)
		{
			first=newEntry;
			last=newEntry;
		}
		else
		{
			ListNode<E> previous=last;
			while( (previous!=null) && (previous.value<value) )
				previous=previous.previous;
			if(previous==null)
			{
				first.previous=newEntry;
				newEntry.next=first;
				first=newEntry;
			}
			else
			{
				newEntry.next=previous.next;
				if(newEntry.next==null) last=newEntry;
				else previous.next.previous=newEntry;
				previous.next=newEntry;
				newEntry.previous=previous;
			}
		}
	}
	public void remove(E object)
	{
		ListNode<E> oldEntry=set.remove(object);
		if(oldEntry!=null)
		{
			if(oldEntry.previous!=null)
				oldEntry.previous.next=oldEntry.next;
			else
				first=oldEntry.next;
			if(oldEntry.next!=null)
				oldEntry.next.previous=oldEntry.previous;
			else
				last=oldEntry.previous;
		}
	}
	public int get(E object)
	{
		ListNode<E> node=set.get(object);
		if(node==null) return -1;
		return node.value;
	}
	public boolean contains(E object)
	{
		return set.get(object)!=null;
	}
	public Iterator<E> iterator()
	{
		return new ListIterate(first);
	}
	
	private class ListIterate implements Iterator
	{
		public ListNode next=null;
		
		public ListIterate(ListNode first)
		{
			next=first;
		}
		public boolean hasNext()
		{
			return next!=null;
		}
		public Object next()
		{
			if(next==null) throw new NoSuchElementException();
			Object O=next.O;
			next=next.next;
			return O;
		}
		public void remove(){throw new UnsupportedOperationException();}
	}
	
	private class ListNode<E>
	{
		public ListNode next=null;
		public ListNode previous=null;
		public int value=0;
		public E O=null;
		
		public ListNode(E O, int value)
		{
			this.O=O;
			this.value=value;
		}
	}
}
