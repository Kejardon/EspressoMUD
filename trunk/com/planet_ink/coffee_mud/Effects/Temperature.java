package com.planet_ink.coffee_mud.Effects;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.nio.ByteBuffer;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class Temperature extends StdEffect
{
	public String ID() { return "Temperature"; }
	public CMObject newInstance()
	{
		try { return (CMObject)this.getClass().newInstance(); }
		catch(Exception e) { Log.errOut(ID(),e); }
		return new Temperature();
	}
//	public int temperature=0;
	public boolean invoke(Affectable target, int asLevel)
	{
//		temperature=asLevel;
		Environmental env=Environmental.O.getFrom(target);
		affected = env;
		if(env==null) return false;
		if(asLevel!=-1)
			env.setTemperature(asLevel);
		startTickDown(env, Integer.MAX_VALUE);
		return true;
	}
	public boolean doTick()
	{
		//TODO: heat transference. For now no heat will transfer. Child temperature check too?
		/*
		ItemCollection collection=ItemCollection.O.getFrom(obj);
		if(collection!=null)
		*/
		CMObject obj=Ownable.O.getOwnerFrom(affected);
		if(obj instanceof Item)
		{
			Environmental myEnv=(Environmental)affected;
			Environmental parentEnv = Environmental.O.getFrom(((Item)obj).container());
			if(parentEnv!=null && parentEnv.temperature()!=myEnv.temperature())
				return true;
		}
		unInvoke();
		return false;
	}

	//CMModifiable and CMSavable
	/*
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null)
		{
			ModEnum[] arrA=MCode.values();
			ModEnum[] arrB=super.totalEnumM();
			totalEnumM=CMParms.appendToArray(arrA, arrB, ModEnum[].class);
		}
		return totalEnumM;
	}
	public Enum[] headerEnumM()
	{
		if(headerEnumM==null)
		{
			Enum[] arrA=new Enum[] {MCode.values()[0]};
			Enum[] arrB=super.headerEnumM();
			headerEnumM=CMParms.appendToArray(arrA, arrB, Enum[].class);
		}
		return headerEnumM;
	}
	private static SaveEnum[] totalEnumS=null;
	private static Enum[] headerEnumS=null;
	public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null)
		{
			SaveEnum[] arrA=SCode.values();
			SaveEnum[] arrB=super.totalEnumS();
			totalEnumS=CMParms.appendToArray(arrA, arrB, SaveEnum[].class);
		}
		return totalEnumS;
	}
	public Enum[] headerEnumS()
	{
		if(headerEnumS==null)
		{
			Enum[] arrA=new Enum[] {SCode.values()[0]};
			Enum[] arrB=super.headerEnumS();
			headerEnumS=CMParms.appendToArray(arrA, arrB, Enum[].class);
		}
		return headerEnumS;
	}
	private enum SCode implements SaveEnum{
		DED(){
			public ByteBuffer save(Temperature E){ return (ByteBuffer)ByteBuffer.allocate(4).putInt(E.temperature).rewind(); }
			public int size(){return 4;}
			public void load(Temperature E, ByteBuffer S){ E.temperature=S.getInt(); } },
		;
		public abstract ByteBuffer save(Temperature E);
		public abstract void load(Temperature E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((Temperature)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((Temperature)E, S);} }
	private enum MCode implements ModEnum{
		DEAD(){
			public String brief(Temperature E){return ""+E.temperature;}
			public String prompt(Temperature E){return ""+E.temperature;}
			public void mod(Temperature E, MOB M){E.temperature=CMLib.genEd().intPrompt(M, ""+E.temperature);} },
		;
		public abstract String brief(Temperature fromThis);
		public abstract String prompt(Temperature fromThis);
		public abstract void mod(Temperature toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((Temperature)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((Temperature)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((Temperature)toThis, M);} }
	*/
}
