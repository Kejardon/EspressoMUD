package com.planet_ink.coffee_mud.core;
import java.util.*;
@SuppressWarnings("unchecked")
public class WVector<E> implements Cloneable
{
	private class WeightedObject<E>
	{
		private E O;
		private int weight=0;
		public WeightedObject(E O, int weight)
		{ this.O=O; this.weight=weight; return; }
		public WeightedObject(E O) {this.O=O; return;}
		public E obj(){return O;}
		public int weight(){return weight;}
		public void setWeight(int weight){this.weight=weight;}
	}
	private Vector<WeightedObject<E>> objects=new Vector<WeightedObject<E>>();
	private int totalWeight=0;

	public int weight() {return totalWeight;}
	public int size() {return objects.size();}

	public int index(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj()))
				return i;

		return -1;
	}
	public E get(int i) {return objects.get(i).obj();}

	public int weight(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj()))
				return objects.get(i).weight();

		return -1;
	}
	public int weight(int i) { return objects.get(i).weight(); }
	public double pct(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj()))
				return ((double)objects.get(i).weight())/((double)totalWeight);

		return -1;
	}
	public double pct(int i) { return ((double)objects.get(i).weight())/((double)totalWeight); }
	public void add(E O, int weight)
	{
		objects.add(new WeightedObject(O, weight));
		totalWeight+=weight;
	}
	public void add(E O)
	{
		objects.add(new WeightedObject(O));
	}
	public boolean remove(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj()))
			{
				totalWeight-=objects.get(i).weight();
				objects.remove(i);
				return true;
			}
		return false;
	}
	public E remove(int i)
	{
		totalWeight-=objects.get(i).weight;
		return objects.remove(i).obj();
	}
	public int setWeight(E O, int weight)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj()))
			{
				int old=objects.get(i).weight();
				totalWeight+=(weight-old);
				objects.get(i).setWeight(weight);
				return old;
			}
		return -1;
	}
	public int setWeight(int i, int weight)
	{
		int old=objects.get(i).weight();
		totalWeight+=(weight-old);
		objects.get(i).setWeight(weight);
		return old;
	}
	public E getFromPct(double pct)
	{
		int weight=(int)Math.round(pct*totalWeight);
		int i=0;
		for(;i<objects.size();i++)
		{
			if((weight-=objects.get(i).weight())<=0)
				return objects.get(i).obj();
		}
		return objects.get(i-1).obj();
	}
	public WVector<E> clone()
	{
		WVector<E> newThis=new WVector<E>();
		for(int i=0;i<objects.size();i++)
		{
			WeightedObject<E> O=objects.get(i);
			newThis.add(O.obj(), O.weight());
		}
		return newThis;
		
	}
}