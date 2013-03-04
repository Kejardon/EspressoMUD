package com.planet_ink.coffee_mud.Skills;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Melee extends StdSkill
{
	public String ID() {return "Melee";}
	
	public int maxLevel(){return 100;}
	public String playerFriendlyName(){return "Melee Combat";}
	public void initializeClass()
	{
		initializeSkillRelations(
				new String[]{"Combat"},
				new Float[]{(float)0.05}
			);
		initPrereqs(new PrereqSkillHolder(CMClass.SKILL.get("Combat"), 1));
	}

	public CMObject newInstance() { return this; }
	public CMObject copyOf() { return this; }
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	
}
