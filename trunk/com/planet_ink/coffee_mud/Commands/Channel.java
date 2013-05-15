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
public class Channel extends StdCommand
{
	public Channel(){access=null;}
	public String[] getAccessWords()
	{
		if(access!=null) return access;
		access=CMLib.channels().getChannelNames();
		return access;
	}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
/*
		if((commands.size()>2)&&(commands.firstElement() instanceof Boolean))
		{
			boolean systemMsg=((Boolean)commands.firstElement()).booleanValue();
			String channelName=(String)commands.elementAt(1);
			String message=(String)commands.elementAt(2);
			CMLib.channels().reallyChannel(mob,channelName,message,systemMsg);
			return true;
		}
*/
		return channel(mob, commands, false);
	}

	public boolean channel(MOB mob, Vector<String> commands, boolean systemMsg)
	{
		PlayerStats pstats=mob.playerStats();
		String channelName=commands.remove(0).toUpperCase().trim();
		int channelInt=CMLib.channels().getChannelIndex(channelName);
		int channelNum=CMLib.channels().getChannelCodeNumber(channelName);

		if((pstats!=null)&&((pstats.getChannelMask()&(1<<channelInt))>0))
		{
			pstats.setChannelMask(pstats.getChannelMask()&(pstats.getChannelMask()-channelNum));
			mob.tell(channelName+" has been turned on.  Use `NO"+channelName.toUpperCase()+"` to turn it off again.");
			return false;
		}
		
		if(commands.size()==0)
		{
			mob.tell(channelName+" what?");
			return false;
		}
/*
		for(int i=0;i<commands.size();i++)
		{
			String s=commands.elementAt(i);
			if(s.indexOf(" ")>=0)
				commands.setElementAt("\""+s+"\"",i);
		}
*/
		if(!CMLib.masking().maskCheck(CMLib.channels().getChannelMask(channelInt),mob,true))
		{
			mob.tell("This channel is not available to you.");
			return false;
		}
		
		HashSet<CMChannels.ChannelFlag> flags=CMLib.channels().getChannelFlags(channelInt);
		
		if((commands.size()==2)
		&&(mob.session()!=null)
		&&(commands.firstElement().equalsIgnoreCase("last"))
		&&(CMath.isNumber(commands.lastElement())))
		{
			int num=CMath.s_int(commands.lastElement());
			Vector<String> que=CMLib.channels().getChannelQue(channelInt);
			boolean showedAny=false;
			if(que.size()>0)
			{
				if(num>que.size()) num=que.size();
				for(int i=que.size()-num;i<que.size();i++)
				{
					String msg=que.get(i);
					showedAny=CMLib.channels().channelTo(mob.session(),channelInt,msg)||showedAny;
				}
			}
			if(!showedAny)
			{
				mob.tell("There are no previous entries on this channel.");
				return false;
			}
		}
		else
		if(flags.contains("READONLY"))
		{
			mob.tell("This channel is read-only.");
			return false;
		}
		else
		if(flags.contains("PLAYERREADONLY")&&(!mob.isMonster()))
		{
			mob.tell("This channel is read-only.");
			return false;
		}
		else
			CMLib.channels().reallyChannel(mob,channelName,CMParms.combine(commands,0),systemMsg);
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
}