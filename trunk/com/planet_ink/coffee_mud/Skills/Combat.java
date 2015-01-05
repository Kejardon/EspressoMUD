package com.planet_ink.coffee_mud.Skills;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Combat extends StdSkill
{
	@Override public String ID() {return "Combat";}
	//public boolean satisfiesPrerequisites(MOB mob){return true;}
	public int maxLevel(){return 20;}
	public String playerFriendlyName(){return "Combat";}
	@Override public void initializeClass()
	{
		initializeSkillRelations(
				new String[]{"Melee"},
				new Float[]{(float)0.05}
			);
	}

	public CMObject newInstance() { return this; }
	public CMObject copyOf() { return this; }
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
	
}
