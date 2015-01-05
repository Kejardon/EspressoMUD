package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;
/* 
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class DebugCommand extends StdCommand
{
	public DebugCommand(){access=new String[]{"DEBUGCOMMAND"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		String commandType="";
		if(commands.size()>1)
		{
			commandType=((String)commands.elementAt(1)).toUpperCase();
		}
		switch(commandType)
		{
			case "PANICDUMP":
				mob.tell("Dumping ALL threads:");
				panicDump(mob, commands);
				mob.tell("Dump complete");
				break;
		}
		
		return false;
	}
	public static void panicDump(MOB mob, Vector commands)
	{
		mob.tell(getFullThreadDump());
	}
	public static String getFullThreadDump()
	{
		StringBuffer lines=new StringBuffer("^xStatus|Name                 ^.^?\n\r");
		try
		{
			ThreadGroup topTG = Thread.currentThread().getThreadGroup();
			while (topTG != null && topTG.getParent() != null)
				topTG = topTG.getParent();
			if (topTG != null)
				dumpThreadGroup(lines,topTG);

		}
		catch (Exception e)
		{
			lines.append("\n\rException while listing threads: ").append(e.getMessage()).append ("\n\r");
		}
		return lines.toString();
	}
	public static void dumpThreadGroup(StringBuffer lines,ThreadGroup tGroup)
	{
		int ac = tGroup.activeCount();
		int agc = tGroup.activeGroupCount();
		Thread tArray[] = new Thread [ac+1];
		ThreadGroup tgArray[] = new ThreadGroup [agc+1];

		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);

		lines.append(" ^HTGRP^?  ^H").append(tGroup.getName()).append("^?\n\r");

		for (int i = 0; i<ac; ++i)
		{
			if (tArray[i] != null)
			{
                lines.append(tArray[i].isAlive()? "  ok   " : " BAD!  ");
                lines.append(CMStrings.padRight(tArray[i].getName(),20)).append(": ");
				dumpStack(lines, tArray[i]);
			}
		}

		if (agc > 0)
		{
			lines.append("{\n\r");
			for (int i = 0; i<agc; ++i)
			{
				if (tgArray[i] != null)
					dumpThreadGroup(lines,tgArray[i]);
			}
			lines.append("}\n\r");
		}
	}
	public static void dumpStack(StringBuffer lines,Thread theThread)
	{
		java.lang.StackTraceElement[] s=(java.lang.StackTraceElement[])theThread.getStackTrace();
		//Would be nice: Exceptions here to skip
		lines.append("\n\r");
		for(int i=0;i<s.length;i++)
			lines.append("   ").append(s[i].getClassName()).append(": ").append(s[i].getMethodName()).append("(").append(s[i].getFileName()).append(": ").append(s[i].getLineNumber()).append(")\n\r");
	}	
	
	@Override public int commandType(MOB mob, String cmds){return CT_NON_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isASysOp(mob);}
}
