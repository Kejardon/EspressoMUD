package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import java.nio.ByteBuffer;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class CustomRideable implements Rideable, Ownable
{

	protected Vector<Item> riders=new Vector<Item>(1);
	protected CMSavable parent=null;
	protected int saveNum=0;
	protected boolean mobile=false;
	protected String putString="on";
	protected String stateString="sitting on";
	protected String mountString="sit(s) on";
	protected String dismountString="stand(s) from";
	protected String stateStringSubject="sat on by";


	//CMObject
	public String ID(){return "CustomRideable";}
	public CMObject newInstance(){return new CustomRideable();}
	public CMObject copyOf(){return null;}
	public void initializeClass(){}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	public void destroy()
	{
		//TODO
		CMLib.database().deleteObject(this);
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum()
	{
		if(saveNum==0)
		synchronized(this)
		{
			if(saveNum==0)
				saveNum=SIDLib.Objects.RIDEABLE.getNumber(this);
		}
		return saveNum;
	}
	public void setSaveNum(int num)
	{
		synchronized(this)
		{
			if(saveNum!=0)
				SIDLib.Objects.RIDEABLE.removeNumber(saveNum);
			saveNum=num;
			SIDLib.Objects.RIDEABLE.assignNumber(num, this);
		}
	}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){CMLib.database().saveObject(this);}

	//Rideable
	public boolean isMobileRide(){return mobile;}
	public void setMobileRide(boolean mob){mobile=mob; CMLib.database().saveObject(this);}
	public boolean canBeRidden(Item E) { return true; }
	public boolean hasRider(Item E) { return riders.contains(E); }
	public void addRider(Item E)
	{
		E.setRide(this);
		riders.addElement(E);
//		E.getEnvObject().recoverEnvStats();
	}
	public void removeRider(Item E)
	{
		riders.removeElement(E);
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
	public Item getRider(int index)
	{
		try { return riders.elementAt(index); }
		catch(java.lang.ArrayIndexOutOfBoundsException x){}
		return null;
	}
	public Vector<Item> allRiders(){return (Vector<Item>)riders.clone();}
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
	private enum SCode implements CMSavable.SaveEnum{
		MBL(){
			public ByteBuffer save(CustomRideable E){return ByteBuffer.wrap(new byte[] {(E.mobile?(byte)1:(byte)0)}); }
			public int size(){return 1;}
			public void load(CustomRideable E, ByteBuffer S){E.mobile=(S.get()!=0);} },
		PUT(){
			public ByteBuffer save(CustomRideable E){
				if(E.putString=="on") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.putString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.putString=CMLib.coffeeMaker().loadString(S); } },
		STA(){
			public ByteBuffer save(CustomRideable E){
				if(E.stateString=="sitting on") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.stateString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.stateString=CMLib.coffeeMaker().loadString(S); } },
		MNT(){
			public ByteBuffer save(CustomRideable E){
				if(E.mountString=="sit(s) on") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.mountString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.mountString=CMLib.coffeeMaker().loadString(S); } },
		DIS(){
			public ByteBuffer save(CustomRideable E){
				if(E.dismountString=="stand(s) from") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.dismountString); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.dismountString=CMLib.coffeeMaker().loadString(S); } },
		SUB(){
			public ByteBuffer save(CustomRideable E){
				if(E.putString=="sat on by") return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.stateStringSubject); }
			public int size(){return 0;}
			public void load(CustomRideable E, ByteBuffer S){ E.stateStringSubject=CMLib.coffeeMaker().loadString(S); } },
		;
		public abstract ByteBuffer save(CustomRideable E);
		public abstract void load(CustomRideable E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((CustomRideable)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((CustomRideable)E, S);} }
	private static enum MCode implements ModEnum{
		RIDERS() {
			public String brief(CustomRideable E){return ""+E.numRiders();}
			public String prompt(CustomRideable E){return "";}
			public void mod(CustomRideable E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done))
				{
					Vector<Item> V=E.allRiders();
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
		;
		public abstract String brief(CustomRideable fromThis);
		public abstract String prompt(CustomRideable fromThis);
		public abstract void mod(CustomRideable toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((CustomRideable)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((CustomRideable)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((CustomRideable)toThis, M);} }
}