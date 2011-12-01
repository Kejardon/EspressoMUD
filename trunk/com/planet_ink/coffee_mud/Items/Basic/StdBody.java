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

/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
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
	protected CharStats baseCharStats=(CharStats)CMClass.Objects.COMMON.getNew("BodyCharStats");
	protected CharStats charStats=(CharStats)CMClass.Objects.COMMON.getNew("BodyCharStats");
	protected int[] birthday={-1, -1, -1};

	public StdBody()
	{
		name="a generic body";
		display="a nondescript person is here.";
		lFlags.add(ListenHolder.Flags.TICK);
//		desc="";
		((Ownable)myEnvironmental).setOwner(this);
	}

	public Environmental getEnvObject() {return myEnvironmental;}

	public MOB mob(){return myMob;}
	public void setMob(MOB mob){myMob=mob;}

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
	}

	public void setBaseCharStats(CharStats newBaseCharStats)
	{
		baseCharStats=(CharStats)newBaseCharStats.copyOf();
		charStats=(CharStats)CMClass.Objects.COMMON.getNew(newBaseCharStats.ID());
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
	}
	public boolean isRace(Race R){return myRaces.contains(R);}
	public void setRace(Race[] R)
	{
		WVector<Race> newRaces=new WVector<Race>();
		for(Race r : R)
		{
			int i=newRaces.index(r);
			if(i>=0) newRaces.setWeight(i, newRaces.weight(i));
			else newRaces.add(r);
		}
		myRaces=newRaces;
	}
	public void addRace(Race R)
	{
		int i=myRaces.index(R);
		if(i>=0) myRaces.setWeight(i, myRaces.weight(i));
		else myRaces.add(R);
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
	public void setGender(Gender G){myGender=G;}
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
		super.destroy();
		dead=true;
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

	private enum SCode implements CMSavable.SaveEnum{
		BCS(){
			public String save(StdBody E){ return CMLib.coffeeMaker().getSubStr(E.baseCharStats); }
			public void load(StdBody E, String S){ E.baseCharStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
		CHS(){
			public String save(StdBody E){ return CMLib.coffeeMaker().getSubStr(E.charStats); }
			public void load(StdBody E, String S){ E.charStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
		BDY(){
			public String save(StdBody E){ return CMLib.coffeeMaker().savAInt(E.birthday); }
			public void load(StdBody E, String S){ E.birthday=CMLib.coffeeMaker().loadAInt(S); } },
		DED(){
			public String save(StdBody E){ return ""+E.dead; }
			public void load(StdBody E, String S){ E.dead=Boolean.getBoolean(S); } },
		MOB(){
			public String save(StdBody E){ if(E.myMob==null||!E.myMob.isMonster()) return ""; return CMLib.coffeeMaker().getSubStr(E.myMob); }
			public void load(StdBody E, String S){ E.charStats=(CharStats)CMLib.coffeeMaker().loadSub(S); } },
		RAC(){
			public String save(StdBody E){ return CMLib.coffeeMaker().getWVectorStr(E.myRaces); }
			public void load(StdBody E, String S){ E.myRaces=CMLib.coffeeMaker().setWVectorStr(S); } },
		;
		public abstract String save(StdBody E);
		public abstract void load(StdBody E, String S);
		public String save(CMSavable E){return save((StdBody)E);}
		public void load(CMSavable E, String S){load((StdBody)E, S);} }
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
