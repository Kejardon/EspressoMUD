package com.planet_ink.coffee_mud.core;
import java.util.*;
//NOTE: This object is not naturally synchronized, but SHOULD be synchronized or carefully handled.

/*
	Copyright 2011 Kejardon
	A Vector that keeps a 'weight' for each object it holds, as a portion of this WVector's total weight.
*/
@SuppressWarnings("unchecked")
public class WVector<E> implements Cloneable
{
	private Vector<WeightedObject<E>> objects;
	private int totalWeight=0;
	private int largest=-1;

	private class WeightedObject<E>
	{
		public E obj;
		public int weight=1;
		public WeightedObject(E O, int weight)
		{ this.obj=O; this.weight=weight; return; }
		public WeightedObject(E O) {this.obj=O; return;}
//		public int weight(){return weight;}
//		public void setWeight(int weight){this.weight=weight;}
	}

	public WVector(){objects=new Vector<WeightedObject<E>>();}
	public WVector(int size){objects=new Vector<WeightedObject<E>>(size);}

	public int weight() {return totalWeight;}
	public int size() {return objects.size();}

	public int largestWeight() { return largest; }
	public int index(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj))
				return i;

		return -1;
	}
	public E get(int i) {return objects.get(i).obj;}

	public int weight(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj))
				return objects.get(i).weight;

		return -1;
	}
	public int weight(int i) { return objects.get(i).weight; }
	public double pct(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj))
				return ((double)objects.get(i).weight)/((double)totalWeight);

		return -1;
	}
	public double pct(int i) { return ((double)objects.get(i).weight)/((double)totalWeight); }
	public void add(E O, int weight)
	{
		objects.add(new WeightedObject(O, weight));
		totalWeight+=weight;
		if((largest==-1)||(weight>objects.get(largest).weight))
			largest=objects.size()-1;
	}
	public void add(E O)
	{
		objects.add(new WeightedObject(O));
		totalWeight++;
		if((largest==-1)||(1>objects.get(largest).weight))
			largest=objects.size()-1;
	}
	public void insert(int i, E O, int weight)
	{
		objects.add(i, new WeightedObject(O, weight));
		totalWeight+=weight;
		if((largest==-1)||(weight>objects.get(largest).weight))
			largest=i;
		else if(largest>=i) largest++;
	}
	public void insert(int i, E O)
	{
		objects.add(i, new WeightedObject(O));
		totalWeight++;
		if((largest==-1)||(1>objects.get(largest).weight))
			largest=i;
		else if(largest>=i) largest++;
	}
	public boolean contains(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj))
				return true;

		return false;
	}
	public boolean remove(E O)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj))
			{
				totalWeight-=objects.get(i).weight;
				objects.remove(i);
				if(largest==i)
				{
					largest=-1;
					int largestVal=0;
					for(i=0;i<objects.size();i++)
						if((largest==-1)||(objects.get(i).weight>largestVal))
						{
							largestVal=objects.get(i).weight;
							largest=i;
						}
				}
				else if(largest>i) largest--;
				return true;
			}
		return false;
	}
	public boolean replace(E O1, E O2) { return replace(O1, O2, 1); }
	public boolean replace(E O1, E O2, int w)
	{
		for(int i=0;i<objects.size();i++)
			if(O1.equals(objects.get(i).obj))
			{
				WeightedObject wobj=objects.get(i);
				int largestVal=wobj.weight;
				totalWeight=totalWeight+w-largestVal;
				wobj.obj=O2;
				wobj.weight=w;
				if((largest==i)&&(w<largestVal))
				{
					largest=-1;
					for(i=0;i<objects.size();i++)
						if((largest==-1)||(objects.get(i).weight>largestVal))
						{
							largestVal=objects.get(i).weight;
							largest=i;
						}
				}
				else if(w>objects.get(largest).weight)
					largest=i;
				return true;
			}
		return false;
	}
	public boolean replace(int i, E O2) { return replace(i, O2, 1); }
	public boolean replace(int i, E O2, int w)
	{
		if(i<objects.size())
		{
			WeightedObject wobj=objects.get(i);
			int largestVal=wobj.weight;
			totalWeight=totalWeight+w-largestVal;
			wobj.obj=O2;
			wobj.weight=w;
			if((largest==i)&&(w<largestVal))
			{
				largest=-1;
				for(i=0;i<objects.size();i++)
					if((largest==-1)||(objects.get(i).weight>largestVal))
					{
						largestVal=objects.get(i).weight;
						largest=i;
					}
			}
			else if(w>objects.get(largest).weight)
				largest=i;
			return true;
		}
		return false;
	}
	public E remove(int i)
	{
		totalWeight-=objects.get(i).weight;
		E obj=objects.remove(i).obj;
		if(largest==i)
		{
			largest=-1;
			int largestVal=0;
			for(i=0;i<objects.size();i++)
				if((largest==-1)||(objects.get(i).weight>largestVal))
				{
					largestVal=objects.get(i).weight;
					largest=i;
				}
		}
		else if(largest>i) largest--;
		return obj;
	}
	public int setWeight(E O, int weight)
	{
		for(int i=0; i<objects.size(); i++)
			if(O.equals(objects.get(i).obj))
			{
				int old=objects.get(i).weight;
				totalWeight+=(weight-old);
				objects.get(i).weight=weight;
				if((largest==i)&&(weight<old))
				{
					int largestVal=0;
					largest=-1;
					for(i=0;i<objects.size();i++)
						if((largest==-1)||(objects.get(i).weight>largestVal))
						{
							largestVal=objects.get(i).weight;
							largest=i;
						}
				}
				else if(weight>objects.get(largest).weight)
					largest=i;
				return old;
			}
		return -1;
	}
	public int setWeight(int i, int weight)
	{
		int old=objects.get(i).weight;
		totalWeight+=(weight-old);
		objects.get(i).weight=weight;
		if((largest==i)&&(weight<old))
		{
			int largestVal=0;
			largest=-1;
			for(i=0;i<objects.size();i++)
				if((largest==-1)||(objects.get(i).weight>largestVal))
				{
					largestVal=objects.get(i).weight;
					largest=i;
				}
		}
		else if(weight>objects.get(largest).weight)
			largest=i;
		return old;
	}
	public E getFromPct(double pct)
	{
		int weight=(int)Math.round(pct*totalWeight);
		int i=0;
		for(;i<objects.size();i++)
		{
			if((weight-=objects.get(i).weight)<=0)
				return objects.get(i).obj;
		}
		return objects.get(i-1).obj;
	}
	public WVector<E> clone()
	{
		WVector<E> newThis=new WVector<E>();
		for(int i=0;i<objects.size();i++)
		{
			WeightedObject<E> O=objects.get(i);
			newThis.add(O.obj, O.weight);
		}
		return newThis;
	}
	public void simplify()
	{
		if(objects.size()==0) return;
		int gcd=objects.get(0).weight;
		for(int i=1;i<objects.size();i++)
		{
			gcd=CMath.gcd(objects.get(i).weight,gcd);
			if(gcd==1) return;
		}
		for(WeightedObject<E> O : (WeightedObject<E>[])objects.toArray(new WeightedObject[0]))
			O.weight=O.weight/gcd;
		totalWeight/=gcd;
	}
	public void toArrays(E[] objs, int[] weights)
	{
		WeightedObject<E>[] src=(WeightedObject<E>[])objects.toArray(new WeightedObject[objects.size()]);
		for(int i=0;i<objs.length;i++)
		{
			objs[i]=src[i].obj;
			weights[i]=src[i].weight;
		}
		return;
	}
}