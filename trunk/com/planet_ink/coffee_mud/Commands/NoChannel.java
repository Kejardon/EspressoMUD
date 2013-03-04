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
@SuppressWarnings("unchecked")
public class NoChannel extends StdCommand
{
	public NoChannel(){access=new String[0];}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		PlayerStats pstats=mob.playerStats();
		if(pstats==null) return false;
		String channelName=commands.elementAt(0).toUpperCase().trim().substring(2);
		commands.removeElementAt(0);
		int channelNum=-1;
		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).equalsIgnoreCase(channelName))
			{
				channelNum=c;
				channelName=CMLib.channels().getChannelName(c);
			}
		}
		if(channelNum<0)
		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).toUpperCase().startsWith(channelName))
			{
				channelNum=c;
				channelName=CMLib.channels().getChannelName(c);
			}
		}
		if((channelNum<0)
		||(!CMLib.masking().maskCheck(CMLib.channels().getChannelMask(channelNum),mob,true)))
		{
			mob.tell("This channel is not available to you.");
			return false;
		}
		if((pstats.getChannelMask()&channelNum)!=0)
		{
			pstats.setChannelMask(pstats.getChannelMask()|(1<<channelNum));
			mob.tell("The "+channelName+" channel has been turned off.  Use `"+channelName.toUpperCase()+"` to turn it back on.");
		}
		else
			mob.tell("The "+channelName+" channel is already off.");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}
