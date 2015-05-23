package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import com.planet_ink.coffee_mud.core.interfaces.Environmental.EnvHolder;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class DefaultEnvMap implements EnvMap
{
	protected ConcurrentHashMap<EnvHolder, EnvLocation> inventory=new ConcurrentHashMap();
	protected CMObject parent=null;
	protected int[] itemsToLoad=null;
	protected int saveNum=0;
	protected boolean amDestroyed=false;
	
	//CMObject
	@Override public String ID(){return "DefaultEnvMap";}
	@Override public DefaultEnvMap newInstance(){try {return getClass().newInstance();}catch(Exception e){Log.errOut("DefaultEnvMap", e);}return new DefaultEnvMap();}
	@Override public DefaultEnvMap copyOf(){return null;}	//TODO
	@Override public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Ownable
	public CMObject owner(){return parent;}
	public Ownable setOwner(CMObject owner){parent=owner; return this;}

	public void destroy()
	{
		amDestroyed=true;

		for(EnvLocation I : inventory.values())
			I.returnEL();

		if(saveNum!=0)
			CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed()
	{
		return amDestroyed;
	}

	//CMModifiable and CMSavable
	@Override public SaveEnum[] totalEnumS(){return SCode.values();}
	@Override public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	@Override public ModEnum[] totalEnumM(){return MCode.values();}
	@Override public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if((saveNum==0)&&(!amDestroyed))
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.ENVMAP.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.ENVMAP.removeNumber(saveNum);
			saveNum=num;
			SIDLib.ENVMAP.assignNumber(num, this);
		}
	}
	public boolean needLink(){return true;}
	public static EnvHolder getHolder(int type, int SID)
	{
		switch(type)
		{
			case 0: return SIDLib.ITEM.get(SID);
			case 1: return SIDLib.EXITINSTANCE.get(SID);
		}
		throw new IllegalArgumentException(type+" is not a defined number ID");
	}
	public static byte getType(CMSavable O)
	{
		if(O instanceof Item) return 0;
		if(O instanceof ExitInstance) return 1;
		// More 'thorough' but excessive method.
		SIDLib.Objects type=SIDLib.getType(O);
		if(type==null) throw new IllegalArgumentException(O.class.toString()+" has no associated save index Objects.");
		//These returns should never be reached because of above checks, but if I ever do something crazy to break them...
		if(type==SIDLib.ITEM) return 0;
		if(type==SIDLib.EXITINSTANCE) return 1;

		Log.errOut("DefaultEnvMap","SIDLib.Objects "+type.name()+" has no number ID!");
		return -1;
	}
	public void link()
	{
		if(itemsToLoad!=null)
		{
			for(int i=0;i<itemsToLoad.length;i+=5)
			{
				EnvHolder holder=getHolder(itemsToLoad[i], itemsToLoad[i+1]);
				if(holder==null) continue;
				//if(parent instanceof ListenHolder)
					//item.registerListeners((ListenHolder)parent);
				//holder.setContainer(parent);
				inventory.put(holder, EnvLocation.getEL(holder, itemsToLoad[i+2], itemsToLoad[i+3], itemsToLoad[i+4]));
			}
			itemsToLoad=null;
		}
	}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	public int size(){return inventory.size();}
	public EnvHolder[] toArray()
	{ return inventory.keySet().toArray(EnvHolder.dummyEHArray); }
	public void copyFrom(EnvMap other)
	{
		if(other.size()>0)
		{
			EnvHolder[] items=other.toArray();
			for(EnvHolder I : items)
			{
				EnvLocation loc=other.position(I);
				if(loc==null) continue;
				EnvLocation found=inventory.putIfAbsent(I, loc);
				if(found!=null)
				{
					found.x=loc.x;
					found.y=loc.y;
					found.z=loc.z;
					loc.returnEL();
				}
			}
			CMLib.database().saveObject(this);
		}
		other.clear(false);
		return;
	}
	public void clear(boolean returnELs)
	{
		if(returnELs) for(EnvLocation loc : inventory.values())
			loc.returnEL();
		inventory.clear();
		CMLib.database().saveObject(this);
	}
	public void placeThing(EnvHolder item, int x, int y, int z)
	{
		EnvLocation loc=inventory.get(item);
		if(loc==null)
		{
			loc=EnvMap.EnvLocation.getEL(item, x, y, z);
			inventory.put(item, loc);
		}
		else
		{
			loc.x=x;
			loc.y=y;
			loc.z=z;
		}
		CMLib.database().saveObject(this);
	}
	public void removeThing(EnvHolder item)
	{
		inventory.remove(item);
		CMLib.database().saveObject(this);
	}
	public Iterator<EnvHolder> allThings()
	{
		return inventory.keySet().iterator();
	}
	public EnvLocation position(EnvHolder I)
	{
		return inventory.get(I);
	}
	public int distance(EnvHolder A, EnvHolder B)
	{
		EnvLocation posA=inventory.get(A);
		EnvLocation posB=inventory.get(B);
		if(A==null || B==null) return -1;
		int x=posA.x-posB.x;
		int y=posA.y-posB.y;
		int z=posA.z-posB.z;
		return (int)Math.sqrt( ((long)x)*x + ((long)y)*y + ((long)z)*z );
	}
	public int distance(EnvHolder A, int x, int y, int z)
	{
		EnvLocation pos=inventory.get(A);
		if(pos==null) return -1;
		x-=pos.x;
		y-=pos.y;
		z-=pos.z;
		return (int)Math.sqrt( ((long)x)*x + ((long)y)*y + ((long)z)*z );
	}

	private enum SCode implements SaveEnum<DefaultEnvMap>{
		INV(){
			public ByteBuffer save(DefaultEnvMap E){
				if(E.inventory.size()>0) {
					EnvLocation[] locs=E.inventory.values().toArray(EnvLocation.dummyELArray);
					ByteBuffer buf=ByteBuffer.wrap(new byte[locs.length*17]);
					for(EnvLocation loc : locs) {
						if(!(loc.item instanceof CMSavable)) continue;
						CMSavable obj=(CMSavable)loc.item;
						buf.put(getType(obj)).putInt(obj.saveNum()).putInt(loc.x).putInt(loc.y).putInt(loc.z); }
					buf.limit(buf.position());
					buf.rewind();
					return buf; }
				return CoffeeMaker.emptyBuffer; }
			public int size(){return 0;}
			public void load(DefaultEnvMap E, ByteBuffer S) {
				E.itemsToLoad=new int[S.remaining()/17 * 5];
				for(int i=0;i<E.itemsToLoad.length;i+=5) {
					E.itemsToLoad[i]=S.get();
					E.itemsToLoad[i+1]=S.getInt();
					E.itemsToLoad[i+2]=S.getInt();
					E.itemsToLoad[i+3]=S.getInt();
					E.itemsToLoad[i+4]=S.getInt(); } } },
		;
		public CMSavable subObject(DefaultEnvMap fromThis){return null;} }
	private enum MCode implements ModEnum<DefaultEnvMap>{
		INVENTORY() {
			public String brief(DefaultEnvMap E){return ""+E.inventory.size();}
			public String prompt(DefaultEnvMap E){return "";}
			public void mod(DefaultEnvMap E, MOB M){
				while((M.session()!=null)&&(!M.session().killFlag())) {
					Vector<EnvHolder> V=new Vector(E.inventory.keySet());
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) break;
					else if(i==V.size()) M.tell("TODO");
					else if(i<V.size()) {
						char action=M.session().prompt("(D)estroy, (M)odify, or (P)lace it (default P)? ","P").trim().toUpperCase().charAt(0);
						if(action=='D' && (V.get(i) instanceof CMSavable)) ((CMSavable)V.get(i)).destroy();
						else if(action=='M' && (V.get(i) instanceof CMModifiable)) CMLib.genEd().genMiscSet(M, (CMModifiable)V.get(i));
						else if(action=='P') {
							int x=0; int y=0; int z=0;
							EnvLocation loc=E.inventory.get(V.get(i));
							if(loc!=null) { x=loc.x; y=loc.y; z=loc.z; }
							x=CMLib.genEd().intPrompt(M, x+"");
							y=CMLib.genEd().intPrompt(M, y+"");
							z=CMLib.genEd().intPrompt(M, z+"");
							E.placeThing(V.get(i), x, y, z); } } } } },
		; }
}
