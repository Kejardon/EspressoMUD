package com.planet_ink.coffee_mud.Races;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class StdRace implements Race
{
	public String ID(){	return "StdRace"; }
	public String name(){ return "StdRace"; }
	public int shortestMale(){return 24;}
	public int shortestFemale(){return 24;}
	public int heightVariance(){return 5;}
	public int lightestWeight(){return 60;}
	public int weightVariance(){return 10;}
	public long forbiddenWornBits(){return 0;}
	public String racialCategory(){return "Unknown";}
	public boolean isGeneric(){return false;}
	public boolean classless(){return false;}
	public boolean leveless(){return false;}
	public boolean expless(){return false;}

	//								an ey ea he ne ar ha to le fo no gi mo wa ta wi
	private static final int[] parts={-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public int[] bodyMask(){return parts;}

	public CMObject newInstance(){return this;}
	public void initializeClass(){}
	private int[] agingChart={0,1,3,15,35,53,70,74,78};
	public int[] getAgingChart(){return agingChart;}

	protected static final Vector empty=new Vector();
	protected Weapon naturalWeapon=null;
	protected Vector naturalWeaponChoices=null;
	protected Vector outfitChoices=null;
	protected Hashtable racialEffectMap=null;
	protected String[] racialEffectNames(){return null;}
	protected int[] racialEffectLevels(){return null;}
	protected String[] racialEffectParms(){return null;}
	protected boolean uncharmable(){return false;}
	protected boolean destroyBodyAfterUse(){return false;}
	protected String baseStatChgDesc = null;
	protected String sensesChgDesc = null;
	protected String dispChgDesc = null;
	protected String abilitiesDesc = null;
	protected String languagesDesc = null;

	public int availabilityCode(){return Area.THEME_FANTASY|Area.THEME_SKILLONLYMASK;}

	public boolean fertile(){return true;}

	public CMObject copyOf()
	{
		try
		{
			StdRace E=(StdRace)this.clone();
			return E;

		}
		catch(CloneNotSupportedException e)
		{
			return this;
		}
	}

	public Race healthBuddy(){return this;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{

	}
	public void affectCharStats(MOB affectedMob, CharStats charStats)
	{
	}
	public void agingAffects(MOB mob, CharStats baseStats, CharStats charStats)
	{
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(uncharmable()
		&&(msg.target()==myHost)
		&&(msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(myHost instanceof MOB)
		&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_CHARMING)))
		{
			msg.source().location().show(msg.source(),myHost,CMMsg.MSG_OK_VISUAL,"<T-NAME> seem(s) unaffected by the charm magic from <S-NAMESELF>.");
			return false;
		}
		return true;
	}

	public boolean genitalsExposed(Vector V)
	{
		if((V==null)||(V.size()==0)) return true;
		for(int i=0;i<V.size();i++)
			if(((Item)V.elementAt(i)).fetchEffect("Prop_Mateholes")==null)
				return false;
		
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
	}
	public String arriveStr()
	{
		return "arrives";
	}
	public String leaveStr()
	{
		return "leaves";
	}
	public void level(MOB mob, Vector gainedAbilityIDs){}

	public long getTickStatus(){return Tickable.STATUS_NOT;}
	public boolean tick(Tickable myChar, int tickID){return true;}
	public void startRacing(MOB mob, boolean verifyOnly)
	{
		if(!verifyOnly)
		{
			setHeightWeight(mob.baseEnvStats(),(char)mob.baseCharStats().gender());
		}
	}
	public Weapon myNaturalWeapon()
	{
		if(naturalWeapon==null)
			naturalWeapon=CMClass.getWeapon("StdWeapon");
		return naturalWeapon;
	}

	public Vector outfit(MOB myChar){return outfitChoices;}

	public String healthText(MOB viewer, MOB mob)
	{
		return CMLib.combat().standardMobCondition(viewer,mob);
	}

	protected Weapon funHumanoidWeapon()
	{
		if(naturalWeaponChoices==null)
		{
			naturalWeaponChoices=new Vector();
			for(int i=1;i<11;i++)
			{
				naturalWeapon=CMClass.getWeapon("StdWeapon");
				if(naturalWeapon==null) continue;
				switch(i)
				{
					case 1:
					case 2:
					case 3:
					naturalWeapon.setName("a quick punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 4:
					naturalWeapon.setName("fingernails and teeth");
					naturalWeapon.setWeaponType(Weapon.TYPE_PIERCING);
					break;
					case 5:
					naturalWeapon.setName("an elbow");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 6:
					naturalWeapon.setName("a backhand");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 7:
					naturalWeapon.setName("a strong jab");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 8:
					naturalWeapon.setName("a stinging punch");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 9:
					naturalWeapon.setName("a knee");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
					case 10:
					naturalWeapon.setName("a head butt");
					naturalWeapon.setWeaponType(Weapon.TYPE_BASHING);
					break;
				}
				naturalWeaponChoices.addElement(naturalWeapon);
			}
		}
		if(naturalWeaponChoices.size()>0)
			return (Weapon)naturalWeaponChoices.elementAt(CMLib.dice().roll(1,naturalWeaponChoices.size(),-1));
		return CMClass.getWeapon("StdWeapon");
	}

	public Vector myResources(){return new Vector();}
	public void setHeightWeight(EnvStats stats, char gender)
	{
		int weightModifier=0;
		if(weightVariance()>0)
			weightModifier=CMLib.dice().roll(1,weightVariance(),0);
		stats.setWeight(lightestWeight()+weightModifier);
		int heightModifier=0;
		if(heightVariance()>0)
		{
			if(weightModifier>0)
			{
				double variance=CMath.div(weightModifier,weightVariance());
				heightModifier=(int)Math.round(CMath.mul(heightVariance(),variance));
			}
			else
				heightModifier=CMLib.dice().roll(1,heightVariance(),0);
		}
		if (gender == 'M')
			stats.setHeight(shortestMale()+heightModifier);
 		else
			stats.setHeight(shortestFemale()+heightModifier);
	}

	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	protected Item makeResource(String name, int type)
	{
		return (Item)CMLib.materials().makeResource(type,ID(),true,name);
	}

	public DeadBody getCorpseContainer(MOB mob, Room room)
	{
		if(room==null) room=mob.location();

		DeadBody Body=(DeadBody)CMClass.getItem("Corpse");
		Body.setCharStats((CharStats)mob.baseCharStats().copyOf());
		Body.baseEnvStats().setLevel(mob.baseEnvStats().level());
		Body.baseEnvStats().setWeight(mob.baseEnvStats().weight());
		Body.setPlayerCorpse(!mob.isMonster());
		Body.setTimeOfDeath(System.currentTimeMillis());
		Body.setName("the body of "+mob.Name().replace('\'','`'));
		Body.setMobName(mob.Name().replace('\'','`'));
		Body.setMobDescription(mob.description().replace('\'','`'));
		Body.setDisplayText("the body of "+mob.Name().replace('\'','`')+" lies here.");
		Ability AGE=mob.fetchEffect("Age");
		if(AGE!=null) Body.addNonUninvokableEffect(AGE);
		if(room!=null)
			room.addItemRefuse(Body,mob.isMonster()?CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_BODY):CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_BODY));
		Body.setDestroyAfterLooting(destroyBodyAfterUse());
		Body.recoverEnvStats();

		Vector items=new Vector();
		CMLib.beanCounter().getTotalAbsoluteNativeValue(mob); // converts mob.get-Money();
		Hashtable containerMap=new Hashtable();
		Hashtable itemMap=new Hashtable();
		DVector lootPolicies=CMLib.utensils().parseLootPolicyFor(mob);
		for(int i=0;i<mob.numItems();)
		{
			Item thisItem=mob.getItem(i);
			if((thisItem!=null)&&(thisItem.savable()))
			{
				if(mob.isMonster())
				{
					Item newItem=(Item)thisItem.copyOf();
					if(newItem instanceof Container)
						itemMap.put(thisItem,newItem);
					if(thisItem.container()!=null)
						containerMap.put(thisItem,thisItem.container());
					newItem.setContainer(null);
					newItem.recoverEnvStats();
					thisItem=newItem;
					i++;
				}
				else
					mob.removeItem(thisItem);
				thisItem.unWear();
				if(thisItem.container()==null)
					thisItem.setContainer(Body);
				if(room!=null)
					room.addItem(thisItem);
				items.addElement(thisItem);
			}
			else
			if(thisItem!=null)
				mob.removeItem(thisItem);
			else
				i++;
		}

		Item dropItem=CMLib.catalog().getDropItem(mob,false);
		if(dropItem!=null)
		{
			dropItem.unWear();
			if(dropItem.container()==null)
				dropItem.setContainer(Body);
			if(room!=null)
				room.addItem(dropItem);
			items.addElement(dropItem);
		}

		for(Enumeration e=itemMap.keys();e.hasMoreElements();)
		{
			Item oldItem=(Item)e.nextElement();
			Item newItem=(Item)itemMap.get(oldItem);
			Item oldContainer=(Item)containerMap.get(oldItem);
			if((oldContainer!=null)&&(newItem!=null))
				newItem.setContainer((Item)itemMap.get(oldContainer));
		}
		if(destroyBodyAfterUse())
		{
			for(int r=0;r<myResources().size();r++)
			{
				Item I=(Item)myResources().elementAt(r);
				if(I!=null)
				{
					I=(Item)I.copyOf();
					I.setContainer(Body);
					if(room!=null)
						room.addItemRefuse(I,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_MONSTER_EQ));
				}
			}
		}
		return Body;
	}

	public Vector racialEffects(MOB mob)
	{
		if(racialEffectNames()==null)
			return empty;

		if((racialEffectMap==null)
		&&(racialEffectNames()!=null)
		&&(racialEffectLevels()!=null)
		&&(racialEffectParms()!=null))
			racialEffectMap=new Hashtable();

		if(racialEffectMap==null) return empty;

		Integer level=null;
		if(mob!=null)
			level=Integer.valueOf(mob.envStats().level());
		else
			level=Integer.valueOf(Integer.MAX_VALUE);

		if(racialEffectMap.containsKey(level))
			return (Vector)racialEffectMap.get(level);
		Vector finalV=new Vector();
		for(int v=0;v<racialEffectLevels().length;v++)
		{
			if((racialEffectLevels()[v]<=level.intValue())
			&&(racialEffectNames().length>v)
			&&(racialEffectParms().length>v))
			{
				Ability A=CMClass.getAbility(racialEffectNames()[v]);
				if(A!=null)
				{
					A.setProficiency(100);
					A.setSavable(false);
					A.setMiscText(racialEffectParms()[v]);
					A.makeNonUninvokable();
					finalV.addElement(A);
				}
			}
		}
		racialEffectMap.put(level,finalV);
		return finalV;
	}

	public String getStatAdjDesc()
	{
		makeStatChgDesc();
		return baseStatChgDesc;
	}
	public String getSensesChgDesc()
	{
		makeStatChgDesc();
		return sensesChgDesc;
	}
	public String getDispositionChgDesc()
	{
		makeStatChgDesc();
		return dispChgDesc;
	}
	public String getAbilitiesDesc()
	{
		makeStatChgDesc();
		return abilitiesDesc;
	}
	public String getLanguagesDesc()
	{
		makeStatChgDesc();
		return languagesDesc;
	}
	public String racialParms(){ return "";}
	public void setRacialParms(String parms){}
	
	protected void clrStatChgDesc()
	{ 
		baseStatChgDesc=null;
		dispChgDesc=null;
		sensesChgDesc=null;
		abilitiesDesc = null;
		languagesDesc = null;
	}
	protected void makeStatChgDesc()
	{
		if((baseStatChgDesc == null)
		||(dispChgDesc==null)
		||(sensesChgDesc==null))
		{
			StringBuilder str=new StringBuilder("");
			MOB mob=CMClass.getMOB("StdMOB");
			mob.setSession(null);
			mob.baseCharStats().setMyRace(this);
			startRacing(mob,false);
			mob.recoverCharStats();
			mob.recoverCharStats();
			mob.recoverEnvStats();
			MOB mob2=CMClass.getMOB("StdMOB");
			mob2.setSession(null);
			mob2.baseCharStats().setMyRace(new StdRace());
			mob2.recoverCharStats();
			mob2.recoverEnvStats();
			dispChgDesc=CMLib.flags().describeDisposition(mob);
			sensesChgDesc=CMLib.flags().describeSenses(mob);
			mob.destroy();
			mob2.destroy();
			baseStatChgDesc=str.toString();
			if(baseStatChgDesc.endsWith(", "))
				baseStatChgDesc=baseStatChgDesc.substring(0,baseStatChgDesc.length()-2);
			StringBuilder astr=new StringBuilder("");
			StringBuilder lstr=new StringBuilder("");
			abilitiesDesc=astr.toString();
			if(abilitiesDesc.endsWith(", "))
				abilitiesDesc=abilitiesDesc.substring(0,abilitiesDesc.length()-2);
			languagesDesc=lstr.toString();
			if(languagesDesc.endsWith(", "))
				languagesDesc=languagesDesc.substring(0,languagesDesc.length()-2);
		}
	}
	
	
	public boolean sameAs(Race E)
	{
		if(!(E instanceof StdRace)) return false;
		return true;
	}
}
