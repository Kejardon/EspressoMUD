package com.planet_ink.coffee_mud.core;
import java.util.*;

/* 
	Copyright 2011 Kejardon
	Hashmap with a list that can give an iterator of its contents, iterator always does its best to continue working.
*/

public class HashedList<E>
{
	private HashMap<E,ListNode<E>> set;
	
	public HashedList(){set=new HashMap<E,ListNode<E>>();}
	public HashedList(int size){set=new HashMap<E,ListNode<E>>(size);}
	
	private ListNode<E> first=null;
	
	public E first(){return (first==null)?null:first.O;}
	public void add(E object)
	{
		synchronized(set)
		{
			if(set.containsKey(object)) return;
			ListNode<E> newEntry=new ListNode<E>(object);
			set.put(object, newEntry);
			if(first!=null)
			{
				first.previous=newEntry;
				newEntry.next=first;
			}
			first=newEntry;
		}
	}
	public void remove(E object)
	{
		synchronized(set)
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
			}
		}
	}
	public boolean contains(E object)
	{
		return set.containsKey(object);
	}
	public Iterator<E> iterator()
	{
		return new ListIterate<E>(first);
	}
	public int size(){return set.size();}
	
	private class ListIterate<E> implements Iterator<E>
	{
		public ListNode<E> next=null;
		
		public ListIterate(ListNode first)
		{
			next=first;
		}
		public boolean hasNext()
		{
			return next!=null;
		}
		public E next()
		{
			if(next==null) throw new NoSuchElementException();
			E O=next.O;
			next=next.next;
			return O;
		}
		public void remove(){throw new UnsupportedOperationException();}
	}
	
	private class ListNode<E>
	{
		public ListNode next=null;
		public ListNode previous=null;
		public E O=null;
		
		public ListNode(E O)
		{
			this.O=O;
		}
	}
}
