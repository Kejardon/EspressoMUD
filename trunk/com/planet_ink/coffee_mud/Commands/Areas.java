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

public class Areas extends StdCommand
{
	public Areas(){access=new String[]{"AREAS"};}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		StringBuffer msg=new StringBuffer("^HComplete areas list:^?^N\r\n");
		ArrayList<String> areasVec=new ArrayList();
		for(Iterator<Area> a=CMLib.map().areas();a.hasNext();)
		{
			Area A=a.next();
			String name=A.name();
			areasVec.add(name);
		}
		int col=0;
		for(int i=0;i<areasVec.size();i++)
		{
			if((++col)>3)
			{
				msg.append("\r\n");
				col=1;
			}
			msg.append(CMStrings.padRight(areasVec.get(i),22)+"^N");
		}
		msg.append("\r\n\r\n^HEnter 'HELP (AREA NAME) for more information.^?");
		if((mob!=null)&&(mob.session()!=null))
			mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
}