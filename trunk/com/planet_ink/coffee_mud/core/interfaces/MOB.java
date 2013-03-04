package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")

//Let's see.
//A MOB will have a body, that is almost the main feature of a mob.
//A MOB will have its own inventory for things it's holding, a body won't (although the body's stats and stuff will be used to check if it can be held). A body WILL have equipment however as a sort of inventory.
//A MOB will be affectable independant of its body... but generally not targetable independant of its body (things like scry being the exception). So most interactions will go through the body.
//Body will have to be fairly transparent between the MOB and others.
//A body will be rideable but not really moveable without a MOB.
public interface MOB extends ItemCollection.ItemHolder, Interactable, CMSavable, CMModifiable, TickActer
{
	public static final MOB[] dummyMOBArray=new MOB[0];
	public class QueuedCommand	//Nothing more than a storage object instead of having an Object[] and typecasting stuff
	{
		public long nextAct;
		public Command command;
		public String cmdString;
		public int commandType;
		public Object data;
		public int metaFlags;
	}
	public class Skilltable extends Hashtable<Skill, MOBSkill>
	{
		//max level around 2369038960
		public long totalEXP=0;
		public int level=0;
		public HashSet<Skill> forgettingSkills=new HashSet<Skill>();
		public boolean changeState(Skill key, int state)
		{
			MOBSkill skill=get(key);
			if(skill==null)
			{
				if(state!=MOBSkill.LEARNING) return false;
				put(key, new MOBSkill(key));
				return true;
				/*
				MOBSkill oldSkill=put(key, new MOBSkill(key));
				if(oldSkill!=null) totalEXP-=MOBSkill.levelTiers[oldSkill.baseLevel];
				*/
			}
			if(skill.learningState == state) return false;
			skill.learningState=state;
			return true;
		}
		public void put(Skill key, int set)
		{
			put(key, set, -1);
		}
		public void put(Skill key, int set, int state)
		{
			MOBSkill skill=get(key);
			if(skill==null)
			{
				skill=new MOBSkill(key, set);
				if(state!=-1)
				{
					skill.learningState=state;
					if(state==MOBSkill.FORGETTING)
						forgettingSkills.add(key);
					else
						forgettingSkills.remove(key);
				}
				MOBSkill oldSkill=put(key, skill);
				if(oldSkill!=null) totalEXP-=MOBSkill.levelTiers[oldSkill.baseLevel];
			}
			else
			{
				totalEXP-=MOBSkill.levelTiers[skill.baseLevel];
				skill.EXP=set;
				if(state!=-1)
				{
					skill.learningState=state;
					if(state==MOBSkill.FORGETTING)
						forgettingSkills.add(key);
					else
						forgettingSkills.remove(key);
				}
			}
			skill.calculateLevel(this);
			totalEXP+=MOBSkill.levelTiers[skill.baseLevel];
			calculateLevel();
		}
		//Primarily used as quick easy way to add/train skills
		public int add(Skill key, int add)
		{
			return add(key, add, -1, true);
		}
		//Primarily used to load skills
		public int add(Skill key, int add, int state)
		{
			return add(key, add, state, false);
		}
		//Full form of function
		public int add(Skill key, int add, int state, boolean forgetOthers)
		{
			MOBSkill skill=get(key);
			if(skill==null)
			{
				skill=new MOBSkill(key, add);
				if(state!=-1)
				{
					skill.learningState=state;
					if(state==MOBSkill.FORGETTING)
						forgettingSkills.add(key);
					else
						forgettingSkills.remove(key);
				}
				MOBSkill oldSkill=put(key, skill);
				if(oldSkill!=null) totalEXP-=MOBSkill.levelTiers[oldSkill.baseLevel];
			}
			else
			{
				totalEXP-=MOBSkill.levelTiers[skill.baseLevel];
				skill.EXP+=add;
				if(state!=-1)
				{
					skill.learningState=state;
					if(state==MOBSkill.FORGETTING)
						forgettingSkills.add(key);
					else
						forgettingSkills.remove(key);
				}
			}
			skill.calculateLevel(this);
			totalEXP+=MOBSkill.levelTiers[skill.baseLevel];
			int oldLevel = level;
			calculateLevel();
			if(oldLevel < level && forgetOthers && !forgettingSkills.isEmpty())
			{
				if(forgetEXP(totalEXP - 5 * MOBSkill.levelTiers[oldLevel] + 1))
				{
					level--;
					calculateLevel();
				}
			}
			return skill.EXP;
		}
		public boolean forgetEXP(long targetEXP)
		{
			ArrayList<Skill> skillsToLose=null;
			for(Iterator<Skill> iter=forgettingSkills.iterator();iter.hasNext();)
			//(Skill S : (Vector<Skill>)CMParms.denumerate(forgettingSkills.iterator()))
			{
				Skill S = iter.next();
				MOBSkill skillToLose=get(S);
				if(skillToLose==null || skillToLose.learningState!=MOBSkill.FORGETTING)
				{
					//TODO: Log error out
					iter.remove();
					continue;
				}
				if(skillToLose.baseLevel==0)
				{
					skillToLose.learningState=MOBSkill.MAINTAINING;
					iter.remove();
					continue;
				}
				if(MOBSkill.levelTiers[skillToLose.baseLevel-1] < targetEXP)
				{
					if(skillsToLose==null) skillsToLose=new ArrayList();
					skillsToLose.add(S);
					targetEXP -= MOBSkill.levelTiers[skillToLose.baseLevel-1];
				}
				else
				{
					int extraEXP = (int) (MOBSkill.levelTiers[skillToLose.baseLevel] - targetEXP);
					int finalLevel = MOBSkill.levelForEXP(extraEXP);
					skillToLose.EXP = MOBSkill.levelTiers[finalLevel]-1;
					skillToLose.calculateLevel(this);
					if(skillsToLose!=null) for(Skill Sk : skillsToLose)
					{
						skillToLose=get(S);
						skillToLose.EXP=MOBSkill.levelTiers[0]-1;
						skillToLose.calculateLevel(this);
					}
					return true;
				}
			}
			return false;
		}
		public void calculateLevel()
		{
			if(level > 0 && 5*MOBSkill.levelTiers[level-1] > totalEXP)
				level = 0;
			while(5*MOBSkill.levelTiers[level] <= totalEXP && level < MOBSkill.MAXLEVEL)
				level++;
		}
	}
	//TODO: Rehaul this class to be more friendly with the structure.
	public static class MOBSkill
	{
		public static final int FORGETTING=0;
		public static final int MAINTAINING=1;
		public static final int LEARNING=2;
		
		public static final int MAXLEVEL=150; //Current formula breaks at about 175
		public static final int[] levelTiers;
		static
		{
			levelTiers=new int[MAXLEVEL];
			double base = 1.07152;
			double denom = 1 - base;
			for(int i=0;i<MAXLEVEL;i++)
			{
				double numer = 1 - Math.pow(base, i+1);
				levelTiers[i]=1000*(int)(numer/denom);
			}
		}
		public static int levelForEXP(int totalEXP)
		{
			int level=0;
			while(levelTiers[level] < totalEXP && level < MAXLEVEL)
				level++;
			return level;
		}
				
		public final Skill mySkill;
		public int learningState=LEARNING;
		public int EXP=0;
		public int EXPBoost=0;
		public int uncappedEXPBoost=0;
		public int levelBoost=0;
		public int level=0;
		public int baseLevel=0;
		//Level cost: Previous level + 1000 * (1.07152)^level.
		//Level 1 = 1,000 - Level 100 = Level 99 + 1,000,000
		public MOBSkill(Skill skill)
		{
			mySkill=skill;
		}
		public MOBSkill(Skill skill, int exp)
		{
			mySkill=skill;
			EXP=exp;
		}
		//I actually like the efficiency of this EXP crawler, I might not need to rehaul this class.
		public void calculateLevel(Skilltable source)
		{
			int oldLevel = level;
			int max=levelTiers[mySkill.maxLevel()];
			if(EXP > max) EXP = max;
			int totalEXP = EXP+uncappedEXPBoost;
			if(EXPBoost > EXP)
				totalEXP+=EXP;
			else
				totalEXP+=EXPBoost;
			if(baseLevel > 0 && levelTiers[baseLevel-1] > EXP)
				baseLevel = 0;
			while(levelTiers[level] < EXP && baseLevel < mySkill.maxLevel())
				baseLevel++;
			if(level > 0 && levelTiers[level-1] > totalEXP)
				level = 0;
			while(levelTiers[level] <= totalEXP && level < MAXLEVEL)
				level++;
			if(level!=oldLevel)
			{
				mySkill.updateBoosts(source, oldLevel, level);
			}
		}
		//TODO: calculateBoosts for new initialized skills
		public int level(){return level+levelBoost;}
	}

	public Body body();
	public void setBody(Body newBody);

	//public EatCode getEat();

	public String[] getTitles();
	public String getActiveTitle();
	public void setActiveTitle(String S);
	public void addTitle(String title);
	public void removeTitle(String title);

	public String titledName();
	public String displayName(MOB mob);
	public String genericName();
//	public String displayText(MOB viewer);

	public Item fetchInventory(String itemName);
//	public Item fetchCarried(Item goodLocation, String itemName);
	public Vector fetchInventories(String itemName);
	public boolean isMine(Interactable env);
	public void giveItem(Item thisContainer);

	/** Some general statistics about MOBs.  See the
	 * CharStats class (in interfaces) for more info. */
	public PlayerStats playerStats();
	public void setPlayerStats(PlayerStats newStats);
	public CharStats baseCharStats();
	public CharStats charStats();
	public void recoverCharStats();
	public void setBaseCharStats(CharStats newBaseCharStats);
	
	public MOBSkill getSkill(Skill S);
	public Set<Skill> knownSkills();
	public boolean changeSkillState(Skill key, int state);

	/** Combat and death */
//	public void removeFromGame(boolean killSession);
	public Interactable getVictim();
	public void setVictim(Interactable mob);

	/** Primary mob communication */
	public void tell(Interactable source, Interactable target, Vector<CMObject> tool, String msg);
	public void tell(Interactable source, Interactable target, CMObject tool, String msg);
	public void tell(Interactable source, Interactable target, String msg);
	public void tell(String msg);
	public void enqueCommand(QueuedCommand qCom, QueuedCommand afterCommand);
	public void enqueCommand(String commands, int metaFlags);
	public int commandQueSize();
	public boolean doCommand(QueuedCommand command);	//currently return is sorta meaningless
	public double actions();
	public void setActions(double remain);

	/** Whether a session object is attached to this MOB */
	public Session session();
	public void setSession(Session newSession);
	public void setTempSession(Session newSession);
	public boolean isMonster();
	//public boolean canReach(Interactable I);

	// Alternate body stuff. I'll figure this out later.
//	public boolean isPossessing();
//	public MOB soulMate();
//	public void setSoulMate(MOB mob);
//	public void dispossess(boolean giveMsg);

	// gained attributes
//	public long getAgeHours();
//	public void setAgeHours(long newVal);

	// misc characteristics
	public boolean willFollowOrdersOf(MOB mob);

	// location!
	public Room location();
	public void setLocation(Room newPlace);
/*
	public Item fetchWornItem(String itemName);
	public Vector fetchWornItems(long wornCode, short aboveOrAroundLayer, short layerAttributes);
	public Item fetchFirstWornItem(long wornCode);
	public Item fetchWieldedItem();
*/
}
