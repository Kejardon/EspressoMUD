package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Alias extends StdCommand
{
	public Alias(){ access=new String[]{"ALIAS"}; }
	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if((mob.playerStats()==null)||(mob.session()==null))
			return false;
		PlayerStats ps=mob.playerStats();
		while((mob.session()!=null)&&(!mob.session().killFlag()))
		{
			StringBuffer menu=new StringBuffer("^xAlias definitions:^.^?\r\n");
			String[] aliasNames=ps.getAliasNames();
			for(int i=0;i<aliasNames.length;i++)
				menu.append(CMStrings.padRight((i+1)+". "+aliasNames[i],15)+": "+ps.getAlias(aliasNames[i])+"\r\n");
			menu.append((aliasNames.length+1)+". Add a new alias\r\n");
			mob.tell(menu.toString());
			String which=mob.session().prompt("Enter a selection: ","");
			if(which.length()==0)
				break;
			int num=CMath.s_int(which);
			String selection=null;
			if((num>0)&&(num<=(aliasNames.length)))
			{
				selection=aliasNames[num-1];
				try{
					if(mob.session().choose("\r\nAlias selected '"+selection+"'.\r\nWould you like to D)elete or M)odify this alias (d/M)? ","MD","M").equals("D"))
					{
						ps.delAliasName(selection);
						mob.tell("Alias deleted.");
						selection=null;
					}
				}
				catch(java.io.IOException e){return false;}
			}
			else
			if(num<=0)
				break;
			else
			{
				selection=mob.session().prompt("Enter a new alias string consisting of letters and numbers only.\r\n: ","").trim().toUpperCase();
				if(selection.length()==0)
					selection=null;
				else
				if(ps.getAlias(selection).length()>0)
				{
					selection=null;
					mob.tell("That alias already exists.  Select it from the menu to delete or modify.");
				}
				else
				{
					for(int i=0;i<selection.length();i++)
						if(!Character.isLetterOrDigit(selection.charAt(i)))
						{
							selection=null;
							break;
						}
					if(selection==null)
						mob.tell("Your alias name may only contain letters and numbers without spaces. ");
				}
			}
			if(selection!=null)
			{
				mob.session().rawPrintln("Enter a value for alias '"+selection+"'.  Use ~ to separate commands.");
				String value=mob.session().prompt(": ","").trim();
//				value=value.replace("<","");
//				value=value.replace("&","");
				if((value.length()==0)&&(ps.getAlias(selection).length()>0))
					mob.tell("(No change)");
				else
				if(value.length()==0)
				{
					mob.tell("Aborted.");
					ps.delAliasName(selection);
				}
				else
				{
					ps.setAlias(selection,value);
					mob.tell("The alias was successfully changed.");
				}
			}
		}
		return true;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public int prompter(){return 1;}
	@Override public boolean canBeOrdered(){return true;}
}