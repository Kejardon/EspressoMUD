package com.planet_ink.coffee_mud.Effects.Languages;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Common extends StdLanguage
{
	@Override public String ID() { return "Common"; }
	public String name(){ return "Common";}
	public Common()
	{
		super();
		proficiency=100;
	}
	public int proficiency(){return 100;}
/*
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		boolean anythingDone=false;
		for(int a=0;a<mob.numEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Language))
				if(((Language)A).beingSpoken(ID()))
				{
					anythingDone=true;
					((Language)A).setBeingSpoken(ID(),false);
				}
		}
		isAnAutoEffect=false;
		if(!auto)
		{
			String msg=null;
			if(!anythingDone)
				msg="already speaking "+name()+".";
			else
				msg="now speaking "+name()+".";
			mob.tell("You are "+msg);
		}
		return true;
	}
*/
}