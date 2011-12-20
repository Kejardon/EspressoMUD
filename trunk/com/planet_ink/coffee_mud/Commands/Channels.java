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
public class Channels extends StdCommand
{
	public Channels(){}

	private String[] access={"CHANNELS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		StringBuffer buf=new StringBuffer("Available channels: \n\r");
		int col=0;
		String[] names=CMLib.channels().getChannelNames();
		for(int x=0;x<names.length;x++)
			if(CMLib.masking().maskCheck(CMLib.channels().getChannelMask(x),mob,true))
			{
				if((++col)>3)
				{
					buf.append("\n\r");
					col=1;
				}
				String channelName=names[x];
				boolean onoff=(pstats.getChannelMask()&(1<<x))>0;
				buf.append(CMStrings.padRight("^<CHANNELS '"+(onoff?"":"NO")+"'^>"+channelName+"^</CHANNELS^>"+(onoff?" (OFF)":""),24));
			}
		if(names.length==0)
			buf.append("None!");
		else
			buf.append("\n\rUse NOCHANNELNAME (ex: NOGOSSIP) to turn a channel off.");
		mob.tell(buf.toString());
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
}