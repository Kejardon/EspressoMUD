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
public class ATopics extends StdCommand
{
	public ATopics(){access=new String[]{"ARCTOPICS","ATOPICS"};}

	public static void doTopics(MOB mob, Properties rHelpFile, String helpName, String resName)
	{
		StringBuffer topicBuffer=(StringBuffer)Resources.getResource(resName);
		if(topicBuffer==null)
		{
			topicBuffer=new StringBuffer();

			Vector<String> reverseList=new Vector();
			for(Enumeration<String> e=(Enumeration)rHelpFile.keys();e.hasMoreElements();)
			{
				String ptop = e.nextElement();
				String thisTag=rHelpFile.getProperty(ptop);
				if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
					|| (rHelpFile.getProperty(thisTag)== null) )
						reverseList.addElement(ptop);
			}

			Collections.sort(reverseList);
			topicBuffer=new StringBuffer("Help topics: \r\n\r\n");
			topicBuffer.append(CMLib.lister().fourColumns(reverseList,"HELP"));
			topicBuffer=new StringBuffer(topicBuffer.toString().replace('_',' '));
			Resources.submitResource(resName,topicBuffer);
		}
		if((mob!=null)&&(!mob.isMonster()))
			mob.session().colorOnlyPrintln(topicBuffer.toString()+"\r\n\r\nEnter "+helpName+" (TOPIC NAME) for more information.",false);
	}


	public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		Properties arcHelpFile=CMLib.help().getArcHelpFile();
		if(arcHelpFile.size()==0)
		{
			if(mob!=null)
				mob.tell("No archon help is available.");
			return false;
		}

		doTopics(mob,arcHelpFile,"AHELP", "ARCHON TOPICS");
		return false;
	}

	public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"AHELP");}
}