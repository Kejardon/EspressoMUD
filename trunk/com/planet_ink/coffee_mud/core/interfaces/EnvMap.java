/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.planet_ink.coffee_mud.core.interfaces;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public interface EnvMap extends Ownable, CMSavable, CMModifiable, CMCommon
{
	public static class EnvLocation
	{
		public static final EnvLocation[] dummyELArray=new EnvLocation[0];
		protected static final ConcurrentLinkedQueue<EnvLocation> ItemLocCache=new ConcurrentLinkedQueue();
		//Special method of getting a new ItemLocation. Contains uninitialized data.
		public static EnvLocation ELFactory()
		{
			EnvLocation EL=ItemLocCache.poll();
			if(EL==null)
				EL=new EnvLocation();
			return EL;
		}
		//Preferred method of getting a new ItemLocation.
		public static EnvLocation getEL(Environmental.EnvHolder I, int x, int y, int z)
		{
			EnvLocation EL=ELFactory();
			EL.item=I;
			EL.x=x;
			EL.y=y;
			EL.z=z;
			return EL;
		}
		public Environmental.EnvHolder item;
		public int x;
		public int y;
		public int z;
		public boolean returnEL()
		{
			item=null;
			return ItemLocCache.offer(this);
		}
		public boolean equals(Object O)
		{
			if(O instanceof EnvLocation)
			{
				return ((EnvLocation)O).item==item;
			}
			return false;
		}
	}

	@Override public EnvMap copyOf();
	@Override public EnvMap newInstance();
	public int size();
	public void clear(boolean returnELs);
	public Environmental.EnvHolder[] toArray();
	public Iterator<Environmental.EnvHolder> allThings();
	public void copyFrom(EnvMap other);
	public void placeThing(Environmental.EnvHolder item, int x, int y, int z);
	public void removeThing(Environmental.EnvHolder item);
	public EnvLocation position(Environmental.EnvHolder I);
	public int distance(Environmental.EnvHolder A, Environmental.EnvHolder B);
	public int distance(Environmental.EnvHolder A, int x, int y, int z);
}
