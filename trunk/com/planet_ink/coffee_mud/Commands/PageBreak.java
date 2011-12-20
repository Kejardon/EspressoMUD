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
public class PageBreak extends StdCommand
{
	public PageBreak(){}

	private String[] access={"PAGEBREAK"};
	public String[] getAccessWords(){return access;}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((mob==null)||(mob.playerStats()==null))
			return false;
		
		if(commands.size()<2)
		{
			String pageBreak=(mob.playerStats().getPageBreak()!=0)?(""+mob.playerStats().getPageBreak()):"Disabled";
			mob.tell("Change your page break to what? Your current page break setting is: "+pageBreak+". Enter a number larger than 0 or 'disable'.");
			return false;
		}
		String newBreak=CMParms.combine(commands,1);
		int newVal=mob.playerStats().getWrap();
		if((CMath.isInteger(newBreak))&&(CMath.s_int(newBreak)>0))
			newVal=CMath.s_int(newBreak);
		else
		if("DISABLED".startsWith(newBreak.toUpperCase()))
			newVal=0;
		else
		{
			mob.tell("'"+newBreak+"' is not a valid setting. Enter a number larger than 0 or 'disable'.");
			return false;
		}
		mob.playerStats().setPageBreak(newVal);
		String pageBreak=(mob.playerStats().getPageBreak()!=0)?(""+mob.playerStats().getPageBreak()):"Disabled";
		mob.tell("Your new page break setting is: "+pageBreak+".");
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
}