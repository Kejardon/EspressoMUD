package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class StdBody extends StdItem implements Body
{
	public String ID(){	return "StdBody";}

	protected CopyOnWriteArrayList<CharAffecter> charAffecters=new CopyOnWriteArrayList();
	//protected WVector<Race> myRaces=new WVector();	//make HybridBody for this instead
	protected Race myRace=null;
	protected Gender myGender=null;
	protected boolean dead=false;
	protected MOB myMob=null;
	protected CharStats baseCharStats;//=(CharStats)((Ownable)CMClass.COMMON.getNew("BodyCharStats")).setOwner(this);
	protected CharStats charStats;//=(CharStats)((Ownable)CMClass.COMMON.getNew("BodyCharStats")).setOwner(this);
	protected int[] birthday={-1, -1, -1};

	public StdBody()
	{
		name="a generic body";
		display="a nondescript person is here.";
		lFlags.add(ListenHolder.Flags.TICK);
//		desc="";
	}

//	public Environmental getEnvObject() {return myEnvironmental;}

	public MOB mob(){return myMob;}
	public Body setMob(MOB mob){myMob=mob; return this;}

	public int[] birthday(){return birthday;}
//	public int initializeBirthday(int ageHours, Race R) { }

	public CharStats baseCharStats(){
		if(baseCharStats==null)
			synchronized(this){if(baseCharStats==null) setBaseCharStats((CharStats)CMClass.COMMON.getNew("BodyCharStats"));}
		return baseCharStats;}
	public CharStats charStats(){
		if(charStats==null)
			synchronized(this){if(charStats==null) setBaseCharStats((CharStats)CMClass.COMMON.getNew("BodyCharStats"));}
		return charStats;}
	public void recoverCharStats()
	{
		baseCharStats.copyStatic(charStats);
		for(CharAffecter C : charAffecters)
			C.affectCharStats(this,charStats);
		//CMLib.database().saveObject(this);
	}

	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)((Ownable)newBaseCharStats.copyOf()).setOwner(this);
		charStats=(CharStats)((Ownable)CMClass.COMMON.getNew(newBaseCharStats.ID())).setOwner(this);
		recoverCharStats();
		charStats.resetState();
		CMLib.database().saveObject(this);
	}

	public String healthText(MOB viewer)
	{
//		String mxp="^<!ENTITY vicmaxhp \""+charStats().getMaxPoints(CharStats.Points.HIT)+"\"^>^<!ENTITY vichp \""+charStats().getPoints(CharStats.Points.HIT)+"\"^>^<Health^>^<HealthText \""+name()+"\"^>";
		//TODO: Race stuff for healthText
		return standardHealthText(viewer);//+"^</HealthText^>";	//mxp+<--
	}
	public String standardHealthText(MOB viewer)
	{
		String[] healthDescs=CMProps.getSListVar("HEALTH_CHART");
		int num=healthDescs.length;
		int pct=(int)(charStats().getPointsPercent(CharStats.Points.HIT)*(num-1));
		if(pct<0) pct=0;
		if(pct>=num) pct=num-1;
		return healthDescs[pct].replace("<MOB>",myMob.displayName(viewer));
	}

	public boolean amDead(){return dead;}
	public Body killMeDead()
	{
		dead=true;
		charStats().setPoints(CharStats.Points.HIT, 0);
		CMLib.database().saveObject(this);
		return this;
	}
	public void bringToLife(Room newLocation, boolean resetStats)
	{
		newLocation.bringHere(this, false);
		setContainer(newLocation);
		bringToLife();
		if(resetStats)
		{
			recoverCharStats();
			charStats().resetState();
		}
	}
	public void bringToLife()
	{
		dead=false;
		if(baseCharStats().getPoints(CharStats.Points.HIT)<=0)
			if(baseCharStats().setPoints(CharStats.Points.HIT, 1))
				baseCharStats().setMaxPoints(CharStats.Points.HIT, 1);
		if(charStats().getPoints(CharStats.Points.HIT)<=0)
			if(charStats().setPoints(CharStats.Points.HIT, 1))
				charStats().setMaxPoints(CharStats.Points.HIT, 1);
		CMLib.database().saveObject(this);
	}
	public boolean isRace(Race R){return myRace==R;}
	public void setRace(Race[] R)
	{
		if(R.length==1)
		{
			myRace=R[0];
			CMLib.database().saveObject(this);
		}
	}
	public void addRace(Race R){}	//Not supported by StdBody, need HybridBody
	public String raceName()
	{
		return (myRace==null?"":myRace.name());
	}
	public void setGender(Gender G){myGender=G; CMLib.database().saveObject(this);}
	public Gender gender(){return ((myGender==null)?(CMClass.GENDER.get("StdGender")):myGender);}

	//Affectable/Behavable shared
	public CopyOnWriteArrayList<CharAffecter> charAffecters(){return charAffecters;}
	public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
	}
	public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
	}

	public CMObject newInstance()
	{
		try
		{
			return (CMObject)this.getClass().newInstance();
		}
		catch(Exception e)
		{
			Log.errOut(ID(),e);
		}
		return new StdBody();
	}
	protected void cloneFix(StdBody E)
	{
		charAffecters=new CopyOnWriteArrayList();
		if(baseCharStats!=null) setBaseCharStats((CharStats)baseCharStats.copyOf());
		birthday=birthday.clone();
		if(myMob!=null)
		{
			myMob=(MOB)myMob.copyOf();
			myMob.setBody(this);
		}
		super.cloneFix(E);
		
	}
	public int value() { return baseGoldValue(); }
	public void setBaseValue(int newValue) { }
	public void setWornOut(int worn) { }
	public boolean damagable() {return false;}
	public void setDamagable(boolean bool){}

	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(myMob==null) return true;
		return myMob.okMessage(myHost, msg);
	}
	public boolean respondTo(CMMsg msg)
	{
		if(!super.respondTo(msg))
			return false;
		if(myMob==null) return true;
		return myMob.respondTo(msg);
	}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if(myMob!=null) myMob.executeMsg(myHost, msg);
	}

	public void destroy()
	{
		dead=true;
		super.destroy();
		if((myMob!=null)&&(myMob.playerStats()==null)) myMob.destroy();
	}

	//CMModifiable and CMSavable
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
/*	public void link()
	{
		super.link();
		if(mobToLink!=0)
		{
			myMob=SIDLib.CREATURE.get(mobToLink);
			mobToLink=0;
		}
	}
*/
	private enum SCode implements CMSavable.SaveEnum{
		BCS(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savSubFull(E.baseCharStats); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdBody)fromThis).baseCharStats;}
			public void load(StdBody E, ByteBuffer S){
				CharStats old=E.baseCharStats;
				E.baseCharStats=(CharStats)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.baseCharStats!=null) ((Ownable)E.baseCharStats).setOwner(E);
				if((old!=null)&&(old!=E.baseCharStats)) old.destroy(); } },
		CHS(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savSubFull(E.charStats); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdBody)fromThis).charStats;}
			public void load(StdBody E, ByteBuffer S){
				CharStats old=E.charStats;
				E.charStats=(CharStats)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.charStats!=null) ((Ownable)E.charStats).setOwner(E);
				if((old!=null)&&(old!=E.charStats)) old.destroy(); } },
		BDY(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savAInt(E.birthday); }
			public int size(){return 12;}
			public void load(StdBody E, ByteBuffer S){ E.birthday=CMLib.coffeeMaker().loadAInt(S); } },
		DED(){
			public ByteBuffer save(StdBody E){ return (ByteBuffer)ByteBuffer.wrap(new byte[1]).put(E.dead?(byte)1:(byte)0).rewind(); }
			public int size(){return 1;}
			public void load(StdBody E, ByteBuffer S){ E.dead=(S.get()!=0); } },
		RAC(){
			public ByteBuffer save(StdBody E){
				if(E.myRace==null) return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.myRace.ID()); }
			public int size(){return 0;}
			public void load(StdBody E, ByteBuffer S){ E.myRace=CMClass.RACE.get(CMLib.coffeeMaker().loadString(S)); } },
		GEN(){
			public ByteBuffer save(StdBody E){
				if(E.myGender==null) return GenericBuilder.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.myGender.ID()); }
			public int size(){return 0;}
			public void load(StdBody E, ByteBuffer S){ E.myGender=CMClass.GENDER.get(CMLib.coffeeMaker().loadString(S)); } },
		;
		public abstract ByteBuffer save(StdBody E);
		public abstract void load(StdBody E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((StdBody)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((StdBody)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		DEAD(){
			public String brief(StdBody E){return ""+E.dead;}
			public String prompt(StdBody E){return ""+E.dead;}
			public void mod(StdBody E, MOB M){E.dead=CMLib.genEd().booleanPrompt(M, ""+E.dead);} },
		MOB(){
			public String brief(StdBody E){return E.myMob==null?"null":E.myMob.ID();}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){CMLib.genEd().genMiscSet(M, E.myMob);} },
		CHARSTATS(){
			public String brief(StdBody E){return (E.charStats==null?"null":E.charStats.ID());}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){CMLib.genEd().genMiscSet(M, E.charStats());} },
		BASECHARSTATS(){
			public String brief(StdBody E){return (E.baseCharStats==null?"null":E.baseCharStats.ID());}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){CMLib.genEd().genMiscSet(M, E.baseCharStats());} },
		BIRTHDAY(){	//TODO: Better birthday modification code
			public String brief(StdBody E){return ""+E.birthday;}
			public String prompt(StdBody E){return ""+E.birthday;}
			public void mod(StdBody E, MOB M){CMLib.genEd().aintPrompt(M, E.birthday);} },
		RACE(){
			public String brief(StdBody E){return (E.myRace==null?"null":E.myRace.name());}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){E.myRace=CMLib.genEd().racePrompt(M);} },
		;
		public abstract String brief(StdBody fromThis);
		public abstract String prompt(StdBody fromThis);
		public abstract void mod(StdBody toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdBody)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdBody)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdBody)toThis, M);} }
}
