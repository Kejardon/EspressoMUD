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
public class Title extends StdCommand
{
	public Title(){access=new String[]{"TITLE"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String[] titleOptions=mob.getTitles();
		if((mob.playerStats()==null)||(titleOptions.length==0))
		{
			mob.tell("You don't have any titles to select from.");
			return false;
		}
		String currTitle=titleOptions[0];
		if(currTitle.startsWith("{")&&currTitle.endsWith("}"))
		{
			mob.tell("You can not change your current title.");
			return false;
		}
		PlayerStats ps=mob.playerStats();
		StringBuilder menu=new StringBuilder("^xTitles:^.^?\r\n");
		{
			boolean needAst=false;
			for(String str : titleOptions)
				if(str.equals("*"))
				{
					needAst=false;
					break;
				}
			if(needAst) mob.addTitle("*");
		}
		int selection=1;
		for(String title : titleOptions)
		{
			if(title.startsWith("{")&&title.endsWith("}")) title=title.substring(1,title.length()-1);
			if(title.equalsIgnoreCase("*"))
				menu.append(CMStrings.padRight(""+(selection++),2)).append(": Do not use a title.\r\n");
			else
				menu.append(CMStrings.padRight(""+(selection++),2)).append(": ").append(title.replace("*",mob.name())).append("\r\n");
		}
		selection=1;
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			mob.tell(menu.toString());
			String which=mob.session().prompt("Enter a selection: ",""+selection);
			if(which.length()==0)
				break;
			int num=CMath.s_int(which);
			if((num>0)&&(num<=titleOptions.length))
			{
				selection=num;
				break;
			}
		}
		if(selection==1)
			mob.tell("No change");
		else
		{
			String which=titleOptions[selection-1];
			mob.setActiveTitle(which);
			mob.tell("Title changed accepted.");
		}
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}