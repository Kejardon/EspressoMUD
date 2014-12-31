package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/


public interface Skill extends CMObject
{
	public static final Skill[] dummySkillArray=new Skill[0];
	
	public String playerFriendlyName();
	
	public int maxLevel();
	public void updateBoost(MOB.MOBSkill forMe, int oldLevel, int newLevel);
	public void updateBoosts(MOB.Skilltable otherSkills, int oldLevel, int newLevel);
	public void initializeMyBoost(MOB.Skilltable source, MOB.MOBSkill forMe);
	public boolean satisfiesPrerequisites(MOB mob);
	public void registerHelper(Skill S);
}
