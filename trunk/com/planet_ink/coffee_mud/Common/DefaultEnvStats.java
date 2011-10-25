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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.Vector;

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
public class DefaultEnvStats implements EnvStats
{
	public String ID(){return "DefaultEnvStats";}
//	protected int[] stats={0,0,100,0,0,0,0,0,0,0};
	protected double Speed=1.0;			// should be positive
//	protected String replacementName=null;
	protected Vector<String> ambiances=new Vector();
//	private final static String[] empty=new String[0];
	protected int width;
	protected int length;
	protected int height;
	protected int weight;
	protected int magic;
//	protected int disposition;
	//TODO: Material should probably be stored here? Still need to decide how

	public DefaultEnvStats(){}

//	public int sensesMask(){return stats[STAT_SENSES];}
//	public int disposition(){return disposition;}
//	public int level(){return stats[STAT_LEVEL];}
	public int ability(){return magic;}
//	public int rejuv(){return stats[STAT_REJUV];}
	public int weight(){return weight;}
	public int height(){return height;}
	public int length(){return length;}
	public int width(){return width;}
//	public int armor(){return stats[STAT_ARMOR];}
//	public int damage(){return stats[STAT_DAMAGE];}
	public double speed(){return Speed;}
//	public int attackAdjustment(){return stats[STAT_ATTACK];}
//	public String newName(){ return replacementName;}
	public String[] ambiances(){ return (String[])ambiances.toArray();}

//	public void setRejuv(int newRejuv){stats[STAT_REJUV]=newRejuv;}
//	public void setLevel(int newLevel){stats[STAT_LEVEL]=newLevel;}
//	public void setArmor(int newArmor){stats[STAT_ARMOR]=newArmor;}
//	public void setDamage(int newDamage){stats[STAT_DAMAGE]=newDamage;}
	public void setWeight(int newWeight){weight=newWeight;}
	public void setSpeed(double newSpeed){Speed=newSpeed;}
//	public void setAttackAdjustment(int newAdjustment){stats[STAT_ATTACK]=newAdjustment;}
	public void setAbility(int newAdjustment){magic=newAdjustment;}
//	public void setDisposition(int newDisposition){disposition=newDisposition;}
//	public void setSensesMask(int newMask){stats[STAT_SENSES]=newMask;}
	public void setHeight(int newHeight){height=newHeight;}
	public void setLength(int newLength){length=newLength;}
	public void setWidth(int newWidth){weight=newWidth;}
//	public void setName(String newName){ replacementName=newName;}
//	public String getCombatStats(){return "L"+stats[STAT_LEVEL]+":A"+stats[STAT_ARMOR]+":K"+stats[STAT_ATTACK]+":D"+stats[STAT_DAMAGE];}
	public void addAmbiance(String ambiance)
	{
		for(int i=0;i<ambiances.size();i++)
			if(ambiances.get(i).equalsIgnoreCase(ambiance))
				return;
		ambiances.add(ambiance);
	}
	public void delAmbiance(String ambiance)
	{
		for(i=0;i<ambiances.length;i++)
			if(ambiances.get(i).equalsIgnoreCase(ambiance))
				{ ambiances.remove(i); return; }
	}

	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultEnvStats();}}
	public void initializeClass(){}
	public CMObject copyOf()
	{
		try
		{
			DefaultEnvStats E=(DefaultEnvStats)this.clone();
			E.stats=(int[])E.stats.clone();
			return E;
		}
		catch(java.lang.CloneNotSupportedException e)
		{
			return new DefaultEnvStats();
		}
	}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	public void copyInto(EnvStats intoStats)
	{
		if(intoStats instanceof DefaultEnvStats)
		{
			DefaultEnvStats copy=(DefaultEnvStats)intoStats;
			copy.width=width;
			copy.length=length;
			copy.height=height;
			copy.weight=weight;
			copy.magic=magic;
			copy.Speed=Speed;
			copy.ambiances=ambiances.clone();
		}
	}

	//CMModifiable and CMSavable
	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]};}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}

	private enum SCode implements CMSavable.SaveEnum{
		SPD(){
			public String save(DefaultEnvStats E){ return ""+Double.doubleToLongBits(E.Speed); }
			public void load(DefaultEnvStats E, String S){ E.Speed=Double.longBitsToDouble(Long.parseLong(S)); } },
		INT(){
			public String save(DefaultEnvStats E){ return CMLib.coffeeMaker().savAInt(new int[] {E.width, E.length, E.height, E.weight, E.magic}); }
			public void load(DefaultEnvStats E, String S){
				int[] ints=CMLib.coffeeMaker().loadAInt(S);
				E.width=ints[0]; E.length=ints[1]; E.height=ints[2]; E.weight=ints[3]; E.magic=ints[4]; } },
		AMB(){
			public String save(DefaultEnvStats E){ return CMLib.coffeeMaker().saveAString((String[])E.ambiances.toArray()); }
			public void load(DefaultEnvStats E, String S){ for(String newF : CMLib.coffeeMaker().loadAString(S)) E.ambiances.add(newF); } },
		;
		public abstract String save(DefaultEnvStats E);
		public abstract void load(DefaultEnvStats E, String S);
		public String save(CMSavable E){return save((DefaultEnvStats)E);}
		public void load(CMSavable E, String S){load((DefaultEnvStats)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		AMBIENCES(){
			public String brief(DefaultEnvStats E){ return ""+E.ambiances.size();}
			public String prompt(DefaultEnvStats E){ return "";}
			public void mod(DefaultEnvStats E, MOB M){
				boolean done=false;
				while((M.session()!=null)&&(!M.session().killFlag())&&(!done)) {
					Vector V=E.ambiances.clone();
					int i=CMLib.genEd().promptVector(M, V, true);
					if(--i<0) done=true;
					else if(i==V.size()) {
						String S=CMLib.genEd().promptString(M, "", false);
						if(S.length>0) {
							E.delAmbiance(S);
							E.addAmbiance(S); } }
					else if(i<V.size()) E.delAmbiance(V.get(i)); } } },
		SPEED(){
			public String brief(DefaultEnvStats E){return ""+E.Speed;}
			public String prompt(DefaultEnvStats E){return ""+E.Speed;}
			public void mod(DefaultEnvStats E, MOB M){E.Speed=CMLib.genEd().doublePrompt(M, ""+E.Speed);} },
		WIDTH(){
			public String brief(DefaultEnvStats E){return ""+E.width;}
			public String prompt(DefaultEnvStats E){return ""+E.width;}
			public void mod(DefaultEnvStats E, MOB M){E.width=CMLib.genEd().intPrompt(M, ""+E.width);} },
		LENGTH(){
			public String brief(DefaultEnvStats E){return ""+E.length;}
			public String prompt(DefaultEnvStats E){return ""+E.length;}
			public void mod(DefaultEnvStats E, MOB M){E.length=CMLib.genEd().intPrompt(M, ""+E.length);} },
		HEIGHT(){
			public String brief(DefaultEnvStats E){return ""+E.height;}
			public String prompt(DefaultEnvStats E){return ""+E.height;}
			public void mod(DefaultEnvStats E, MOB M){E.height=CMLib.genEd().intPrompt(M, ""+E.height);} },
		WEIGHT(){
			public String brief(DefaultEnvStats E){return ""+E.weight;}
			public String prompt(DefaultEnvStats E){return ""+E.weight;}
			public void mod(DefaultEnvStats E, MOB M){E.weight=CMLib.genEd().intPrompt(M, ""+E.weight);} },
		MAGIC(){
			public String brief(DefaultEnvStats E){return ""+E.magic;}
			public String prompt(DefaultEnvStats E){return ""+E.magic;}
			public void mod(DefaultEnvStats E, MOB M){E.magic=CMLib.genEd().intPrompt(M, ""+E.magic);} },
/*		DISPOSITION(){
			public String brief(DefaultEnvStats E){return ""+E.disposition;}
			public String prompt(DefaultEnvStats E){return ""+E.disposition;}
			public void mod(DefaultEnvStats E, MOB M){E.disposition=CMLib.genEd().intPrompt(M, ""+E.disposition);} }, */
		;
		public abstract String brief(DefaultEnvStats fromThis);
		public abstract String prompt(DefaultEnvStats fromThis);
		public abstract void mod(DefaultEnvStats toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultEnvStats)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultEnvStats)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultEnvStats)toThis, M);} }
/*
	public boolean sameAs(EnvStats E){ return true; }
*/}
