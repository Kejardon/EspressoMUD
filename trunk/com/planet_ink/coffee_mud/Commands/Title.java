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
	private String[] access={"TITLE"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if((mob.playerStats()==null)||(mob.getTitles().size()==0))
		{
			mob.tell("You don't have any titles to select from.");
			return false;
		}
		String currTitle=(String)mob.getTitles().elementAt(0);
		if(currTitle.startsWith("{")&&currTitle.endsWith("}"))
		{
			mob.tell("You can not change your current title.");
			return false;
		}
		PlayerStats ps=mob.playerStats();
		StringBuffer menu=new StringBuffer("^xTitles:^.^?\n\r");
		if(!mob.getTitles().contains("*")) mob.getTitles().addElement("*");
		for(int i=0;i<mob.getTitles().size();i++)
		{
			String title=(String)mob.getTitles().elementAt(i);
			if(title.startsWith("{")&&title.endsWith("}")) title=title.substring(1,title.length()-1);
			if(title.equalsIgnoreCase("*"))
				menu.append(CMStrings.padRight(""+(i+1),2)+": Do not use a title.\n\r");
			else
				menu.append(CMStrings.padRight(""+(i+1),2)+": "+CMStrings.replaceAll(title,"*",mob.name())+"\n\r");
		}
		int selection=1;
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			mob.tell(menu.toString());
			String which=mob.session().prompt("Enter a selection: ",""+selection);
			if(which.length()==0)
				break;
			int num=CMath.s_int(which);
			if((num>0)&&(num<=mob.getTitles().size()))
			{
				selection=num;
				break;
			}
		}
		if(selection==1)
			mob.tell("No change");
		else
		{
			String which=(String)mob.getTitles().elementAt(selection-1);
			mob.getTitles().removeElementAt(selection-1);
			mob.getTitles().insertElementAt(which,0);
			mob.tell("Title changed accepted.");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
}

