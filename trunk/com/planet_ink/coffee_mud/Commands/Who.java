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
public class Who extends StdCommand
{
	public Who(){}

	private String[] access={"WHO","WH"};
	public String[] getAccessWords(){return access;}
	
	protected static final String shortHead=
		 "^x["
		+CMStrings.padRight("Race",12)+" "
//		+CMStrings.padRight("Level",7)
		+"] Character name^.^N\n\r";
		 
	
	public StringBuffer showWhoShort(MOB who)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("[");
		msg.append(CMStrings.padRight(who.body().raceName(),12)+" ");
//		msg.append(CMStrings.padRight(" ",7));
		String name=null;
		name=who.titledName();
		if((who.session()!=null)&&(who.session().afkFlag()))
		{
			long t=(who.session().getIdleMillis()/1000);
			String s=null;
			if(t>600)
			{
				t=t/60;
				if(t>120)
				{
					t=t/60;
					if(t>48)
					{
						t=t/24;
						s=t+"d";
					}
					else s=t+"h";
				}
				else s=t+"m";
			}
			else s=t+"s";
			name=name+(" (idle: "+s+")");
		}
		msg.append("] "+CMStrings.padRight(name,40));
		msg.append("\n\r");
		return msg;
	}
	
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String mobName=CMParms.combine(commands,1);
		HashSet<MOB> friends=null;
		if((mobName!=null)
		&&(mob!=null)
		&&(mobName.equalsIgnoreCase("friends"))
		&&(mob.playerStats()!=null))
		{
			friends=mob.playerStats().getFriends();
			mobName=null;
		}
		
		StringBuffer msg=new StringBuffer("");
		for(int s=0;s<CMLib.sessions().size();s++)
		{
			Session thisSession=CMLib.sessions().elementAt(s);
			MOB mob2=thisSession.mob();

			if((mob2!=null)
			&&(!thisSession.killFlag())
			&&((friends==null)||(friends.contains(mob2)))
			&&(CMLib.flags().isInTheGame(mob2,true)))
				msg.append(showWhoShort(mob2));
		}
		if((mobName!=null)&&(msg.length()==0))
			msg.append("That person doesn't appear to be online.\n\r");
		else
			mob.tell(shortHead+msg.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
}
