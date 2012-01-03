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
public class Stat  extends StdCommand
{
	public Stat(){}

	private String[] access={"STAT"};
	public String[] getAccessWords(){return access;}

	public static final int ABLETYPE_EQUIPMENT=0;
	public static final int ABLETYPE_INVENTORY=1;
	public static final int ABLETYPE_TITLES=2;
	
	public static final String[][] ABLETYPE_DESCS={
		{"EQUIPMENT","EQ","EQUIP"},
		{"INVENTORY","INVEN","INV"},
		{"TITLES","TITLE"},
	};

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		if(mob.isMonster()) return false;
		commands.removeElementAt(0);
		if((commands.size()>0)
		&&((String)commands.firstElement()).equals("?"))
		{
			StringBuilder msg = new StringBuilder("STAT allows the following options: \n\r");
			msg.append("[MOB/PLAYER NAME]");
			for(int i=0;i<ABLETYPE_DESCS.length;i++)
				msg.append(", "+ABLETYPE_DESCS[i][0]);
			mob.tell(msg.toString());
			return false;
		}
		String s1=(commands.size()>0)?((String)commands.elementAt(0)).toUpperCase():"";
		String s2=(commands.size()>1)?((String)commands.elementAt(1)).toUpperCase():"";
		
		int ableTypes=-1;
		if(commands.size()>1)
		{
			String s=((String)commands.elementAt(0)).toUpperCase();
			for(int i=0;i<ABLETYPE_DESCS.length;i++)
				for(int is=0;is<ABLETYPE_DESCS[i].length;is++)
					if(s.equals(ABLETYPE_DESCS[i][is]))
					{
						ableTypes=i;
						commands.removeElementAt(0);
						break;
					}
		}
		String MOBname=CMParms.combine(commands,0);
		MOB target=mob.location().fetchInhabitant(MOBname);
		if(target==null)
			target=CMLib.players().getPlayer(MOBname);
		if(target==null)
		{
			mob.tell("You can't stat '"+MOBname+"'  -- he doesn't exist.");
			return false;
		}

		StringBuilder str=new StringBuilder("");
		if(ableTypes==ABLETYPE_EQUIPMENT)
			str=CMLib.commands().getEquipment(mob,target);
		else
		if(ableTypes==ABLETYPE_INVENTORY)
			str=CMLib.commands().getInventory(mob,target);
		else
		if(ableTypes==ABLETYPE_TITLES)
		{
			str.append("Titles:");
			StringBuffer ttl=new StringBuffer("");
			if(target.playerStats()!=null)
				for(int t=0;t<target.getTitles().size();t++)
				{
					String title = (String)target.getTitles().elementAt(t);
					ttl.append(" "+title+",");
				}
			if(ttl.length()==0)
				ttl.append(" None!");
			ttl.deleteCharAt(ttl.length()-1);
			str.append(ttl);
			str.append("\n\r");
		}
		else
			str=CMLib.commands().getScore(target);
		mob.session().wraplessPrintln(str.toString());
		return false;
	}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"STAT");}
}
