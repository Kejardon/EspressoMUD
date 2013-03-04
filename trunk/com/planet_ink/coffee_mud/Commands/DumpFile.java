package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

@SuppressWarnings("unchecked")
public class DumpFile extends StdCommand
{
	public DumpFile(){access=new String[]{"DUMPFILE"};}

	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		commands.removeElementAt(0);
		if((commands.size()<2)||(commands.size()==2&&commands.get(0).equalsIgnoreCase("raw")))
		{
			mob.tell("dumpfile {raw} username|all {filename1 ...}");
			return false;
		}

		int numFiles = 0;
		int numSessions = 0;
		boolean rawMode=false;

		if(commands.elementAt(0).equalsIgnoreCase("raw"))
		{
			rawMode = true;
			commands.removeElementAt(0);
		}

		String targetName = commands.remove(0);
		boolean allFlag=(targetName.equalsIgnoreCase("all"));

		// so they can do dumpfile (username) RAW filename too
		if(!rawMode && (commands.elementAt(0).equalsIgnoreCase("raw")) )
		{
			rawMode = true;
			commands.removeElementAt(0);
		}

		StringBuffer fileText = new StringBuffer("");
		while (commands.size() > 0)
		{
			boolean wipeAfter = true;
			String fn = commands.remove(0);

			if (Resources.getResource(fn) != null)
				wipeAfter = false;

			StringBuffer ft = new CMFile(fn,mob,true).text();
			if (ft != null && ft.length() > 0)
			{
				fileText.append("\r\n");
				fileText.append(ft);
				++numFiles;
			}

			if (wipeAfter)
				Resources.removeResource(fn);
		}
		if (fileText.length() > 0)
		{
			for(Session thisSession : CMLib.sessions().toArray())
			{
				if (thisSession.killFlag() || (thisSession.mob()==null)) continue;
				if(!CMSecurity.isAllowed(mob,"DUMPFILE"))
					continue;
				if (allFlag || thisSession.mob().name().equalsIgnoreCase(targetName))
				{
					if (rawMode)
						thisSession.rawPrintln(fileText.toString());
					else
						thisSession.colorOnlyPrintln(fileText.toString());
					++numSessions;
				}
			}
		}
		mob.tell("dumped " + numFiles + " files to " + numSessions + " user(s)");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"DUMPFILE");}
}