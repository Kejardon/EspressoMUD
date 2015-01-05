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
public class CustomRideable implements Rideable, Ownable
{

	protected CopyOnWriteArrayList<Item> riders=new CopyOnWriteArrayList();
	protected CMSavable parent=null;
	protected int saveNum=0;
	protected boolean mobile=false;
	protected String putString="on";
	protected String stateString="sitting on";
	protected String mountString="sit(s) on";
	protected String dismountString="stand(s) from";
	protected String stateStringSubject="sat on by";
	protected boolean amDestroyed=false;


	//CMObject
	@Override public String ID(){return "CustomRideable";}
	@Override public CustomRideable newInstance(){try{return getClass().newInstance();}catch(Exception e){Log.errOut("CustomRideable", e);}return new CustomRideable();}
	@Override public CustomRideable copyOf(){return null;}
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
/*		if(parent!=null)
			amDestroyed=parent.amDestroyed();	*/
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
	public String putString(Item R) { return putString; }
	public String stateString(Item R) { return stateString; }
	public String mountString(Item R) { return mountString; }
	public String dismountString(Item R) { return dismountString; }
	public String stateStringSubject(Item R) { return stateStringSubject; }
	public void setPutString(String S) {putString=S; CMLib.database().saveObject(this);}
	public void setStateString(String S) {stateString=S; CMLib.database().saveObject(this);}
	public void setMountString(String S) {mountString=S; CMLib.database().saveObject(this);}
	public void setDismountString(String S) {dismountString=S; CMLib.database().saveObject(this);}
	public void setStateStringSubject(String S) {stateStringSubject=S; CMLib.database().saveObject(this);}
	private enum SCode implements SaveEnum<CustomRideable>{
		MBL(){
			public ByteBuffer save(CustomRideable E){return ByteBuffer.wrap(new byte[] {(E.mobile?(byte)1:(byte)0)}); }
			public int size(){return 1;}
			public void load(CustomRideable E, ByteBuffer S){E.mobile=(S.get()!=0);} },
		PUT(){
			public ByteBuffer save(CustomRideable E){
				if(E.putString.equals("on")) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.putString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.putString=CMLib.coffeeMaker().loadString(S); } },
		STA(){
			public ByteBuffer save(CustomRideable E){
				if(E.stateString.equals("sitting on")) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.stateString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.stateString=CMLib.coffeeMaker().loadString(S); } },
		MNT(){
			public ByteBuffer save(CustomRideable E){
				if(E.mountString.equals("sit(s) on")) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.mountString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.mountString=CMLib.coffeeMaker().loadString(S); } },
		DIS(){
			public ByteBuffer save(CustomRideable E){
				if(E.dismountString.equals("stand(s) from")) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.dismountString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.dismountString=CMLib.coffeeMaker().loadString(S); } },
		SUB(){
			public ByteBuffer save(CustomRideable E){
				if(E.putString.equals("sat on by")) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.stateStringSubject); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.stateStringSubject=CMLib.coffeeMaker().loadString(S); } },
		;
		public CMSavable subObject(CustomRideable fromThis){return null;} }
	private enum MCode implements ModEnum<CustomRideable>{
		RIDERS() {
			public String brief(CustomRideable E){return ""+E.numRiders();}
			public String prompt(CustomRideable E){return "";}
			public void mod(CustomRideable E, MOB M){
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
			public String brief(CustomRideable E){return ""+E.isMobileRide();}
			public String prompt(CustomRideable E){return ""+E.isMobileRide();}
			public void mod(CustomRideable E, MOB M){E.setMobileRide(CMLib.genEd().booleanPrompt(M, ""+E.isMobileRide()));} },
		PUT() {
			public String brief(CustomRideable E){return E.putString(null);}
			public String prompt(CustomRideable E){return E.putString(null);}
			public void mod(CustomRideable E, MOB M){E.setPutString(CMLib.genEd().stringPrompt(M, E.putString(null), false));} },
		STATE() {
			public String brief(CustomRideable E){return E.stateString(null);}
			public String prompt(CustomRideable E){return E.stateString(null);}
			public void mod(CustomRideable E, MOB M){E.setStateString(CMLib.genEd().stringPrompt(M, E.stateString(null), false));} },
		MOUNT() {
			public String brief(CustomRideable E){return E.mountString(null);}
			public String prompt(CustomRideable E){return E.mountString(null);}
			public void mod(CustomRideable E, MOB M){E.setMountString(CMLib.genEd().stringPrompt(M, E.mountString(null), false));} },
		DISMOUNT() {
			public String brief(CustomRideable E){return E.dismountString(null);}
			public String prompt(CustomRideable E){return E.dismountString(null);}
			public void mod(CustomRideable E, MOB M){E.setDismountString(CMLib.genEd().stringPrompt(M, E.dismountString(null), false));} },
		SUBJECT() {
			public String brief(CustomRideable E){return E.stateStringSubject(null);}
			public String prompt(CustomRideable E){return E.stateStringSubject(null);}
			public void mod(CustomRideable E, MOB M){E.setStateStringSubject(CMLib.genEd().stringPrompt(M, E.stateStringSubject(null), false));} },
		; }
}
