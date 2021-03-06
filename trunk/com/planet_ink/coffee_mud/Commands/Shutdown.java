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

public class Shutdown extends StdCommand
{
	public Shutdown(){access=new String[]{"SHUTDOWN"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(mob.isMonster()) return false;
		boolean keepItDown=true;
		boolean noPrompt=false;
		//String externalCommand=null;
		for(int i=commands.size()-1;i>=1;i--)
		{
			String s=commands.elementAt(i);
			if(s.equalsIgnoreCase("RESTART"))
			{ keepItDown=false; commands.removeElementAt(i);}
			else
			if(s.equalsIgnoreCase("NOPROMPT"))
			{ noPrompt=true; commands.removeElementAt(i); }
		}

		if((!noPrompt)
		&&(!mob.session().confirm("Are you fully aware of the consequences of this act (y/N)?","N")))
			return false;
		
		for(Session S : CMLib.sessions().toArray())
			S.colorOnlyPrintln("\r\n\r\n^x"+CMProps.Strings.MUDNAME.property()+" is now shutting down!^.^?\r\n");

		if(keepItDown)
			Log.errOut("CommandProcessor",mob.name()+" starts system shutdown...");
		else
			Log.errOut("CommandProcessor",mob.name()+" starts system restart...");
		mob.tell("Starting shutdown...");

		com.planet_ink.coffee_mud.application.MUD.globalShutdown(mob.session(),keepItDown);
		return false;
	}

	@Override public int prompter(){return 2;}
	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return false;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"SHUTDOWN");}
}
