package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class ChanWho extends StdCommand
{
	public ChanWho(){access=new String[]{"CHANWHO"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String channel=CMParms.combine(commands,1);
		if((channel==null)||(channel.length()==0))
		{
			mob.tell("You must specify a channel name. Try CHANNELS for a list.");
			return false;
		}
		int x=channel.indexOf("@");
		String mud=null;
		if(x>0)
		{
			mud=channel.substring(x+1);
			int channelInt=CMLib.channels().getChannelIndex(channel.substring(0,x).toUpperCase());
			channel=CMLib.channels().getChannelName(channelInt).toUpperCase();
			if((channel.length()==0)||(channelInt<0))
			{
				mob.tell("You must specify a valid channel name. Try CHANNELS for a list.");
				return false;
			}
			return false;
		}
		int channelInt=CMLib.channels().getChannelIndex(channel.toUpperCase());
		channel=CMLib.channels().getChannelName(channelInt);
		if(channelInt<0)
		{
			mob.tell("You must specify a valid channel name. Try CHANNELS for a list.");
			return false;
		}
		String head="^x\r\nListening on "+channel+":^?^.^N\r\n";
		StringBuffer buf=new StringBuffer("");
		for(Session ses : CMLib.sessions().toArray())
		{
			MOB mob2=ses.mob();
			if((CMLib.channels().mayReadThisChannel(mob2,channelInt))
			&&((!ses.killFlag())&&(!mob2.playerStats().hasIgnored(mob)))
			&&(CMLib.flags().isInTheGame(mob2,true)))
				buf.append("^x[^?^.^N"+CMStrings.padRight(mob2.name(),20)+"^x]^?^.^N\r\n");
		}
		if(buf.length()==0)
			mob.tell(head+"Nobody!");
		else
			mob.tell(head+buf.toString());
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}