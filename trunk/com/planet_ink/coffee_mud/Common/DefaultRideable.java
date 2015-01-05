package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class DefaultRideable implements Rideable, Ownable
{
	protected CopyOnWriteArrayList<Item> riders=new CopyOnWriteArrayList();
	protected CMSavable parent=null;
	protected int saveNum=0;
	protected boolean amDestroyed=false;
	protected boolean mobile=false;
/*
	protected String putString="on";
	protected String stateString="sitting on";
	protected String mountString="sit(s) on";
	protected String dismountString="stand(s) from";
	protected String stateStringSubject="sat on by";
*/

	//CMObject
	@Override public String ID(){return "DefaultRideable";}
	@Override public DefaultRideable newInstance(){try {return getClass().newInstance();}catch(Exception e){Log.errOut("DefaultRideable", e);}return new DefaultRideable();}
	@Override public DefaultRideable copyOf(){return null;}
	@Override public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public void destroy()
	{
		//TODO
		amDestroyed=true;
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
				saveNum=SIDLib.RIDEABLE.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.RIDEABLE.removeNumber(saveNum);
			saveNum=num;
			SIDLib.RIDEABLE.assignNumber(num, this);
		}
	}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(this);}
	public void prepDefault(){}

	//Rideable
	public boolean isMobileRide(){return mobile;}
	public void setMobileRide(boolean mob){mobile=mob; CMLib.database().saveObject(this);}
	public boolean canBeRidden(Item E) { return true; }
	public boolean hasRider(Item E) { return riders.contains(E); }
	public void addRider(Item E)
	{
		E.setRide(parent);
		riders.add(E);
//		E.getEnvObject().recoverEnvStats();
	}
	public void removeRider(Item E)
	{
		riders.remove(E);
		E.setRide(null);
//		E.getEnvObject().recoverEnvStats();
	}
	public Item removeRider(int i)
	{
		Item E=riders.remove(i);
		E.setRide(null);
//		E.getEnvObject().recoverEnvStats();
		return E;
	}
	public void dropRiders()
	{
		if(riders.size()>0)
		{
			Item[] items=riders.toArray(Item.dummyItemArray);
			IterCollection itemCol=IterCollection.ICFactory(items);
			riders.removeAll(itemCol);
			for(Item I : items)
				I.setRide(null);
		}
	}
	public Item getRider(int index)
	{
		try { return riders.get(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Iterator<Item> allRiders(){return riders.iterator();}
	public int numRiders() { return riders.size(); }
	public String putString(Item R) { return "on"; }
	public String stateString(Item R) { return "sitting on"; }
	public String mountString(Item R) { return "sit(s) on"; }
	public String dismountString(Item R) { return "stand(s) from"; }
	public String stateStringSubject(Item R) { return "sat on by"; }
	public void setPutString(String S) {}
	public void setStateString(String S) {}
	public void setMountString(String S) {}
	public void setDismountString(String S) {}
	public void setStateStringSubject(String S) {}
	private enum SCode implements SaveEnum<DefaultRideable>{
		MBL(){
			public ByteBuffer save(DefaultRideable E){return ByteBuffer.wrap(new byte[] {(E.mobile?(byte)1:(byte)0)}); }
			public int size(){return 1;}
			public void load(DefaultRideable E, ByteBuffer S){E.mobile=(S.get()!=0);} },
		;
		public CMSavable subObject(DefaultRideable fromThis){return null;} }
	private static enum MCode implements ModEnum<DefaultRideable>{
		RIDERS() {
			public String brief(DefaultRideable E){return ""+E.numRiders();}
			public String prompt(DefaultRideable E){return "";}
			public void mod(DefaultRideable E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done))
				{
					Vector<Item> V=new Vector(E.riders);
					int i=CMLib.genEd().promptVector(M, V, false);
					if(--i<0) done=true;
					else if(i<V.size())
					{
						char action=M.session().prompt("(E)ject or (M)odify "+V.get(i).name()+" (default E)? ","E").trim().toUpperCase().charAt(0);
						if(action=='E') E.removeRider(V.get(i));
						else if(action=='M') CMLib.genEd().genMiscSet(M, V.get(i));
					}
				} } },
		MOBILE() {
			public String brief(DefaultRideable E){return ""+E.isMobileRide();}
			public String prompt(DefaultRideable E){return ""+E.isMobileRide();}
			public void mod(DefaultRideable E, MOB M){E.setMobileRide(CMLib.genEd().booleanPrompt(M, ""+E.isMobileRide()));} },
		; }
}
