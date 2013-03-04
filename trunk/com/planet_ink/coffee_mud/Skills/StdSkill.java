package com.planet_ink.coffee_mud.Skills;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;


/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public abstract class StdSkill implements Skill
{
	protected HashMap<Skill, Float> skillsToHelp=new HashMap();
	protected ArrayList<Skill> skillsHelpingThis=new ArrayList<Skill>();

	protected class PrereqSkillHolder
	{
		public Skill skill;
		public int rank;
		public PrereqSkillHolder(Skill skill, int rank)
		{
			this.skill = skill;
			this.rank = rank;
		}
	}
	protected PrereqSkillHolder[] prereqSkills;
	//public int maxLevel(){return 100;}
	public boolean satisfiesPrerequisites(MOB mob)
	{
		if(prereqSkills!=null)
		{
			for(PrereqSkillHolder prereq : prereqSkills)
			{
				MOB.MOBSkill exist=mob.getSkill(prereq.skill);
				if(exist==null || exist.level()<prereq.rank)
					return false;
			}
		}
		return true;
	}
	//public void initializeClass(){super.initializeSkillRelations(new String[]{}, new Float[]{});}
	protected void initPrereqs(PrereqSkillHolder... prereqs)
	{
		int finalSize=0;
		for(int i = 0; i < prereqs.length; i++)
		{
			PrereqSkillHolder prereq = prereqs[i];
			if(prereq.skill != null)
				finalSize++;
			else
				Log.errOut("StdSkill", "Missing/incorrect skill prereq " + i + " for " + ID());
		}
		if(finalSize != prereqs.length)
		{
			if(finalSize == 0)
				prereqs = null;
			else
			{
				PrereqSkillHolder[] fix = new PrereqSkillHolder[finalSize];
				finalSize = 0;
				for(PrereqSkillHolder prereq : prereqs)
				{
					if(prereq.skill != null)
					{
						fix[finalSize] = prereq;
						finalSize++;
					}
				}
				prereqs = fix;
			}
		}
		prereqSkills = prereqs;
	}
	public void initializeSkillRelations(String[] skills, Float[] values)
	{
		for(int i=0;i<skills.length;i++)
		{
			Skill otherSkill=CMClass.SKILL.get(skills[i]);
			if(otherSkill!=null)
			{
				skillsToHelp.put(otherSkill, values[i]);
				otherSkill.registerHelper(this);
			}
		}
	}
	public void registerHelper(Skill S)
	{
		skillsHelpingThis.add(S);
	}
	public void updateBoosts(MOB.Skilltable otherSkills, int oldLevel, int newLevel)
	{
		int oldEXP=(oldLevel > 0 ? MOB.MOBSkill.levelTiers[oldLevel-1] : 0);
		int newEXP=MOB.MOBSkill.levelTiers[newLevel-1];
		for(Skill skillToHelp : skillsToHelp.keySet())
		{
			MOB.MOBSkill giveBoostHere=otherSkills.get(skillToHelp);
			if(giveBoostHere!=null)
			{
				Float multiplier=skillsToHelp.get(skillToHelp);
				giveBoostHere.EXPBoost+=(int)(newEXP * multiplier.floatValue()) - (int)(oldEXP * multiplier.floatValue());
				giveBoostHere.calculateLevel(otherSkills);
			}
		}
	}
	public void updateBoost(MOB.MOBSkill forMe, int oldLevel, int newLevel)
	{
		Float multiplier=skillsToHelp.get(forMe.mySkill);
		if(multiplier!=null)
		{
			int oldEXP=(oldLevel > 0 ? MOB.MOBSkill.levelTiers[oldLevel-1] : 0);
			int newEXP=MOB.MOBSkill.levelTiers[newLevel-1];
			forMe.EXPBoost+=(int)(newEXP * multiplier.floatValue()) - (int)(oldEXP * multiplier.floatValue());
		}
	}
	public void initializeMyBoost(MOB.Skilltable source, MOB.MOBSkill forMe)
	{
		for(Skill skillHelpingThis : skillsHelpingThis)
		{
			MOB.MOBSkill getBoostFrom = source.get(skillHelpingThis);
			if(getBoostFrom!=null)
				getBoostFrom.mySkill.updateBoost(forMe, 0, getBoostFrom.level());
		}
		forMe.calculateLevel(source);
	}
	
}
