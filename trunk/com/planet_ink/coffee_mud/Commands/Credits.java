package com.planet_ink.coffee_mud.Commands;
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
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Credits extends StdCommand
{
	public Credits(){}

	private String[] access={"CREDITS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer credits=new CMFile(Resources.buildResourcePath("text")+"credits.txt",null,true).text();
    	try { credits = CMLib.httpUtils().doVirtualPage(credits);}catch(Exception ex){}
		if((credits!=null)&&(mob.session()!=null)&&(credits.length()>0))
			mob.session().colorOnlyPrintln(credits.toString());
		else
		{
			mob.tell("CoffeeMud 5.6.2 is (C)2000-2010 by Bo Zimmerman");
			mob.tell("EspressoMUD is (C)2011 by Kejardon");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
}
