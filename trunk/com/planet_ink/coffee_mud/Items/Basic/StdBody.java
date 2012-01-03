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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary;


import java.util.*;
import java.nio.ByteBuffer;

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

	protected Vector<CharAffecter> charAffecters=null;
	protected WVector<Race> myRaces=new WVector();
	protected Gender myGender=null;
	protected boolean dead=false;
	protected MOB myMob=null;
	protected CharStats baseCharStats=(CharStats)((Ownable)CMClass.Objects.COMMON.getNew("BodyCharStats")).setOwner(this);
	protected CharStats charStats=(CharStats)((Ownable)CMClass.Objects.COMMON.getNew("BodyCharStats")).setOwner(this);
	protected int[] birthday={-1, -1, -1};

//	protected int mobToLink=0;

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

	public CharStats baseCharStats(){return baseCharStats;}
	public CharStats charStats(){return charStats;}
	public void recoverCharStats()
	{
		baseCharStats.copyStatic(charStats);
		if(charAffecters!=null)
		for(int a=charAffecters.size();a>0;a--)
			charAffecters.get(a-1).affectCharStats(this,charStats);
		CMLib.database().saveObject(this);
	}

	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)((Ownable)newBaseCharStats.copyOf()).setOwner(this);
		charStats=(CharStats)((Ownable)CMClass.Objects.COMMON.getNew(newBaseCharStats.ID())).setOwner(this);
		recoverCharStats();
		charStats.resetState();
	}

	public String healthText(MOB viewer)
	{
		String mxp="^<!ENTITY vicmaxhp \""+charStats().getMaxPoints(CharStats.Points.HIT)+"\"^>^<!ENTITY vichp \""+charStats().getPoints(CharStats.Points.HIT)+"\"^>^<Health^>^<HealthText \""+name()+"\"^>";
		//TODO: Race stuff for healthText
		return mxp+standardHealthText(viewer)+"^</HealthText^>";
	}
	public String standardHealthText(MOB viewer)
	{
		String[] healthDescs=CMProps.getSListVar("HEALTH_CHART");
		int num=healthDescs.length;
		int pct=(int)(charStats().getPointsPercent(CharStats.Points.HIT)*(num-1));
		if(pct<0) pct=0;
		if(pct>=num) pct=num-1;
		return CMStrings.replaceAll(healthDescs[pct],"<MOB>",myMob.displayName(viewer));
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
	public boolean isRace(Race R){return myRaces.contains(R);}
	public void setRace(Race[] R)
	{
		WVector<Race> newRaces=new WVector<Race>();
		for(Race r : R)
		{
			int i=newRaces.index(r);
			if(i>=0) newRaces.setWeight(i, newRaces.weight(i));
			else newRaces.add(r, 1);
		}
		myRaces=newRaces;
		CMLib.database().saveObject(this);
	}
	public void addRace(Race R)
	{
		int i=myRaces.index(R);
		if(i>=0) myRaces.setWeight(i, myRaces.weight(i)+1);
		else myRaces.add(R, 1);
		CMLib.database().saveObject(this);
	}
	public String raceName()
	{
		StringBuilder name=new StringBuilder("");
		for(int i=0;i<myRaces.size();i++)
		{
			name.append(myRaces.get(i).name());
			if(i<myRaces.size()-1) name.append("-");
		}
		return name.toString();
	}
	public void setGender(Gender G){myGender=G; CMLib.database().saveObject(this);}
	public Gender gender(){return myGender;}

	//Affectable/Behavable shared
	public Vector<CharAffecter> charAffecters(){if(charAffecters==null) charAffecters=new Vector(); return charAffecters;}
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
		super.cloneFix(E);
	}
	public CMObject copyOf()
	{
		try
		{
			StdBody E=(StdBody)this.clone();
			E.cloneFix(this);
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this.newInstance();
		}
	}

	public int value() { return baseGoldValue(); }
	public void setBaseValue(int newValue) { }
	public void setWornOut(int worn) { }
	public boolean damagable() {return false;}
	public void setDamagable(boolean bool){}

	public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		super.okMessage(myHost, msg);
		return true;
	}
	public boolean respondTo(CMMsg msg){super.respondTo(msg); return true;}
	public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
	}

	public void destroy()
	{
		dead=true;
		super.destroy();
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
			ModEnum[] total=new ModEnum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			totalEnumM=total;
		}
		return totalEnumM;
	}
	public Enum[] headerEnumM()
	{
		if(headerEnumM==null)
		{
			Enum[] arrA=new Enum[] {MCode.values()[0]};
			Enum[] arrB=super.headerEnumM();
			Enum[] total=new Enum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			headerEnumM=total;
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
			SaveEnum[] total=new SaveEnum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			totalEnumS=total;
		}
		return totalEnumS;
	}
	public Enum[] headerEnumS()
	{
		if(headerEnumS==null)
		{
			Enum[] arrA=new Enum[] {SCode.values()[0]};
			Enum[] arrB=super.headerEnumS();
			Enum[] total=new Enum[arrA.length+arrB.length];
			System.arraycopy(arrA, 0, total, 0, arrA.length);
			System.arraycopy(arrB, 0, total, arrA.length, arrB.length);
			headerEnumS=total;
		}
		return headerEnumS;
	}
/*	public void link()
	{
		super.link();
		if(mobToLink!=0)
		{
			myMob=(MOB)SIDLib.Objects.CREATURE.get(mobToLink);
			mobToLink=0;
		}
	}
*/
	private enum SCode implements CMSavable.SaveEnum{
		BCS(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savSubFull(E.baseCharStats); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdBody)fromThis).baseCharStats;}
			public void load(StdBody E, ByteBuffer S){ E.baseCharStats=(CharStats)((Ownable)CMLib.coffeeMaker().loadSub(S, E.baseCharStats)).setOwner(E); } },
		CHS(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savSubFull(E.charStats); }
			public int size(){return -1;}
			public CMSavable subObject(CMSavable fromThis){return ((StdBody)fromThis).charStats;}
			public void load(StdBody E, ByteBuffer S){ E.charStats=(CharStats)((Ownable)CMLib.coffeeMaker().loadSub(S, E.charStats)).setOwner(E); } },
		BDY(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savAInt(E.birthday); }
			public int size(){return 12;}
			public void load(StdBody E, ByteBuffer S){ E.birthday=CMLib.coffeeMaker().loadAInt(S); } },
		DED(){
			public ByteBuffer save(StdBody E){ return (ByteBuffer)ByteBuffer.wrap(new byte[1]).put(E.dead?(byte)1:(byte)0).rewind(); }
			public int size(){return 1;}
			public void load(StdBody E, ByteBuffer S){ E.dead=(S.get()!=0); } },
/*	Handled MOB side, not body side.
		MOB(){
			public ByteBuffer save(StdBody E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.myMob.saveNum()).rewind(); }
			public int size(){return 4;}
			public void load(StdBody E, ByteBuffer S){ E.mobToLink=S.getInt(); } },
*/
		RAC(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().getRaceWVector(E.myRaces); }
			public int size(){return 0;}
			public void load(StdBody E, ByteBuffer S){ E.myRaces=CMLib.coffeeMaker().setRaceWVector(S); } },
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
			public String brief(StdBody E){return E.charStats.ID();}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){CMLib.genEd().genMiscSet(M, E.charStats);} },
		BASECHARSTATS(){
			public String brief(StdBody E){return E.baseCharStats.ID();}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){CMLib.genEd().genMiscSet(M, E.baseCharStats);} },
		BIRTHDAY(){	//TODO: Better birthday modification code
			public String brief(StdBody E){return ""+E.birthday;}
			public String prompt(StdBody E){return ""+E.birthday;}
			public void mod(StdBody E, MOB M){CMLib.genEd().aintPrompt(M, E.birthday);} },
		RACE(){
			public String brief(StdBody E){return ((E.myRaces.size()==1)?(E.myRaces.get(0).name()):(""+E.myRaces.size()));}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					WVector<Race> V=E.myRaces.clone();
					int i=CMLib.genEd().promptWVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						Race R=CMLib.genEd().racePrompt(M);
						if(R!=null) E.myRaces.add(R, CMLib.genEd().intPrompt(M, "1")); }
					else if(i<V.size()) {
						int newWeight=CMLib.genEd().intPrompt(M, ""+V.weight(i));
						if(newWeight==0) E.myRaces.remove(V.get(i));
						else E.myRaces.setWeight(V.get(i), newWeight); } } } },
		;
		public abstract String brief(StdBody fromThis);
		public abstract String prompt(StdBody fromThis);
		public abstract void mod(StdBody toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((StdBody)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((StdBody)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((StdBody)toThis, M);} }
}
