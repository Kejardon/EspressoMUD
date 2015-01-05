package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class StdBody extends StdItem implements Body
{
	@Override public String ID(){	return "StdBody";}

	protected CopyOnWriteArrayList<CharAffecter> charAffecters=new CopyOnWriteArrayList();
	//protected WVector<Race> myRaces=new WVector();	//make HybridBody for this instead
	protected Race myRace=CMClass.RACE.get("StdRace");
	protected Gender myGender=null;
	protected boolean dead=true;
	protected MOB myMob=null;
	protected CharStats baseCharStats;//=(CharStats)((Ownable)CMClass.COMMON.getNew("BodyCharStats")).setOwner(this);
	protected CharStats charStats;//=(CharStats)((Ownable)CMClass.COMMON.getNew("BodyCharStats")).setOwner(this);
	protected int[] birthday={-1, -1, -1};
	@Override public void initializeClass(){super.initializeClass(); myRace=CMClass.RACE.get("StdRace");} //Make sure CMClass's instance has non-null race
	protected EatCode myEatAction=null;//defaultEatCode;

	public StdBody()
	{
		name="a generic body";
		display="a nondescript person is here.";
		lFlags.add(ListenHolder.Flags.TICK);
//		desc="";
	}

	@Override public EatCode getEat(){return (myEatAction==null?myRace:myEatAction);}
//	public Environmental getEnvObject() {return myEnvironmental;}

	@Override public MOB mob(){return myMob;}
	@Override public Body setMob(MOB mob){myMob=mob; return this;}

	@Override public int[] birthday(){return birthday;}
//	public int initializeBirthday(int ageHours, Race R) { }

	@Override public CharStats baseCharStats(){
		if(baseCharStats==null)
			synchronized(this){if(baseCharStats==null) setBaseCharStats((CharStats)CMClass.COMMON.getNew("BodyCharStats"));}
		return baseCharStats;}
	@Override public CharStats charStats(){
		if(charStats==null)
			synchronized(this){if(charStats==null) setBaseCharStats((CharStats)CMClass.COMMON.getNew("BodyCharStats"));}
		return charStats;}
	@Override public void recoverCharStats()
	{
		baseCharStats.copyStatic(charStats);
		for(CharAffecter C : charAffecters)
			C.affectCharStats(this,charStats);
		//CMLib.database().saveObject(this);
	}

	@Override public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)((Ownable)newBaseCharStats.copyOf()).setOwner(this);
		charStats=(CharStats)((Ownable)CMClass.COMMON.getNew(newBaseCharStats.ID())).setOwner(this);
		recoverCharStats();
		charStats.resetState();
		CMLib.database().saveObject(this);
	}

	@Override public String healthText(MOB viewer)
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

	@Override public boolean amDead(){return dead;}
	@Override public Body killMeDead()
	{
		dead=true;
		charStats().setPoints(CharStats.Points.HIT, 0);
		CMLib.database().saveObject(this);
		return this;
	}
	@Override public void bringToLife(Room newLocation, boolean resetStats)
	{
		newLocation.bringHere(this, false);
		//setContainer(newLocation);
		bringToLife();
		if(resetStats)
		{
			recoverCharStats();
			charStats().resetState();
		}
	}
	@Override public void bringToLife()
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
	@Override public boolean isComposite(){return false;}
	@Override public Race race(){return myRace;}
	@Override public WVector<Race> raceSet(){return null;}
	@Override public boolean isRace(Race R){return myRace==R;}
	@Override public void setRace(Race R)
	{
		myRace=R;
		CMLib.database().saveObject(this);
	}
	@Override public void setRaces(WVector<Race> R)
	{
		if(R.size()==1)
		{
			myRace=R.get(0);
			CMLib.database().saveObject(this);
		}
	}
	@Override public void addRace(Race R){}	//Not supported by StdBody, need HybridBody
	@Override public String raceName()
	{
		return myRace.name();
	}
	@Override public void setGender(Gender G){myGender=G; CMLib.database().saveObject(this);}
	@Override public Gender gender(){return ((myGender==null)?(CMClass.GENDER.get("StdGender")):myGender);}

	//Affectable/Behavable shared
	@Override public CopyOnWriteArrayList<CharAffecter> charAffecters(){return charAffecters;}
	@Override public void removeListener(Listener oldAffect, EnumSet flags)
	{
		ListenHolder.O.removeListener(this, oldAffect, flags);
	}
	@Override public void addListener(Listener newAffect, EnumSet flags)
	{
		ListenHolder.O.addListener(this, newAffect, flags);
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
	@Override public int value() { return baseGoldValue(); }
	@Override public void setBaseValue(int newValue) { }
	@Override public void setWornOut(int worn) { }
	@Override public boolean damagable() {return false;}
	@Override public void setDamagable(boolean bool){}

	@Override protected boolean doTick()
	{
		tickStatus=Tickable.TickStat.Start;
		if(!dead)
		{
			myRace.recoverTick(this, charStats);
			//TODO: Limb stuff also!
		}
		return ((!dead)||(super.doTick()));
	}

	protected static ListenHolder.DummyListener RefuseEatResponseLive = new ListenHolder.DummyListener()
	{
		@Override public boolean respondTo(CMMsg msg, Object data)
		{
			StdBody body=(StdBody)data;
			MOB mob=body.myMob;
			Interactable target=msg.target();
			Room room=mob.location();
			if((target!=null) && (room.hasLock()))
			{
				room.doAndReturnMsg(CMClass.getMsg(mob, target, (Vector)null, EnumSet.of(CMMsg.MsgCode.VISUAL), "^[S-NAME] prevent^s ^[T-NAME] from trying to eat ^[S-HIM-HER]"));
			}
			return false;
		}
	};
	protected static ListenHolder.DummyListener CheckBiteResponse = new ListenHolder.DummyListener()
	{
		@Override public boolean respondTo(CMMsg msg, Object data)
		{
			StdBody body=(StdBody)data;
			if(!body.getEat().satisfiesEatReqs(msg))
			{
				Room room=CMLib.map().roomLocation(body);
				CMObject thing=msg.firstTool();
				Interactable target=(thing instanceof Interactable)?(Interactable)thing:null;
				if((target!=null)&&(room.hasLock()))
				{
					room.doAndReturnMsg(CMClass.getMsg(body, target, (Vector)null, EnumSet.of(CMMsg.MsgCode.VISUAL), "^[S-NAME] tr^y to chew on ^[T-NAME] but can't eat ^[T-HIM-HER]."));
				}
				return false;
			}
			return true;
		}
	};



	@Override public boolean okMessage(ListenHolder.OkChecker myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;

		Interactable target=msg.target();
		boolean always=msg.hasOthersCode(CMMsg.MsgCode.ALWAYS);
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
			case EAT:
				if(target==this)
				{
					if(!always)
					{
						if(!getEat().satisfiesEatPrereqs(msg))
							return false;
						msg.addResponse(ListenHolder.InbetweenListener.newListener(CheckBiteResponse,this), 10);
					}
				}
				if(msg.isTool(this) && (myMob!=null) && (!dead))
				{
					msg.addResponse(ListenHolder.InbetweenListener.newListener(RefuseEatResponseLive, this), 9);
				}
				break;
			/*
			case DRINK:
				if(target==this)
				{
					if((!always)&&(!getDrink().satisfiesDrinkPrereqs(msg)))
						return false;
					//msg.addResponse(new ListenHolder.InbetweenListener(DrinkResponse, this), 9);
				}
				break;
			*/
		}

		if(myMob==null) return true;
		return myMob.okMessage(myHost, msg);
	}
	@Override public boolean respondTo(CMMsg msg){ return true; }
	@Override public void executeMsg(ListenHolder.ExcChecker myHost, CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		Interactable target=msg.target();
		boolean always=msg.hasOthersCode(CMMsg.MsgCode.ALWAYS);
		for(CMMsg.MsgCode code : msg.othersCode())
		switch(code)
		{
			case EAT:
				if(target==this)
				{
					getEat().handleEat(msg);
				}
				break;
			/*
			case DRINK:
				if(target==this)
				{
					DrinkCode dCode=getDrink();
					if((!always)&&(!dCode.satisfiesDrinkPrereqs(msg)))
						break;
					dCode.handleDrink(msg);
				}
				break;
			*/
		}
		if(myMob!=null) myMob.executeMsg(myHost, msg);
	}

	@Override public void destroy()
	{
		dead=true;
		super.destroy();
		if((myMob!=null)&&(myMob.playerStats()==null)) myMob.destroy();
	}

	//CMModifiable and CMSavable
	private static ModEnum[] totalEnumM=null;
	private static Enum[] headerEnumM=null;
	@Override public ModEnum[] totalEnumM()
	{
		if(totalEnumM==null)
		{
			ModEnum[] arrA=MCode.values();
			ModEnum[] arrB=super.totalEnumM();
			totalEnumM=CMParms.appendToArray(arrA, arrB, ModEnum[].class);
		}
		return totalEnumM;
	}
	@Override public Enum[] headerEnumM()
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
	@Override public SaveEnum[] totalEnumS()
	{
		if(totalEnumS==null)
		{
			SaveEnum[] arrA=SCode.values();
			SaveEnum[] arrB=super.totalEnumS();
			totalEnumS=CMParms.appendToArray(arrA, arrB, SaveEnum[].class);
		}
		return totalEnumS;
	}
	@Override public Enum[] headerEnumS()
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
	private enum SCode implements SaveEnum<StdBody>{
		BCS(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savSubFull(E.baseCharStats); }
			public int size(){return -1;}
			public CMSavable subObject(StdBody fromThis){return fromThis.baseCharStats;}
			public void load(StdBody E, ByteBuffer S){
				CharStats old=E.baseCharStats;
				E.baseCharStats=(CharStats)CMLib.coffeeMaker().loadSub(S, E, this);
				if(E.baseCharStats!=null) ((Ownable)E.baseCharStats).setOwner(E);
				if((old!=null)&&(old!=E.baseCharStats)) old.destroy(); } },
		CHS(){
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savSubFull(E.charStats); }
			public int size(){return -1;}
			public CMSavable subObject(StdBody fromThis){return fromThis.charStats;}
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
			public ByteBuffer save(StdBody E){ return CMLib.coffeeMaker().savString(E.myRace.ID()); }
			public int size(){return 0;}
			public void load(StdBody E, ByteBuffer S){ E.myRace=CMClass.RACE.get(CMLib.coffeeMaker().loadString(S)); } },
		GEN(){
			public ByteBuffer save(StdBody E){
				if(E.myGender==null) return CoffeeMaker.emptyBuffer;
				return CMLib.coffeeMaker().savString(E.myGender.ID()); }
			public int size(){return 0;}
			public void load(StdBody E, ByteBuffer S){ E.myGender=CMClass.GENDER.get(CMLib.coffeeMaker().loadString(S)); } },
		;
		public CMSavable subObject(StdBody fromThis){return null;} }
	private enum MCode implements ModEnum<StdBody>{
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
			public String brief(StdBody E){return E.myRace.name();}
			public String prompt(StdBody E){return "";}
			public void mod(StdBody E, MOB M){E.myRace=CMLib.genEd().racePrompt(M);} },
		; }
}
