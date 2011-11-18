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
@SuppressWarnings("unchecked")
public class Areas extends StdCommand
{
	public Areas(){}

	private String[] access={"AREAS"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		StringBuffer msg=new StringBuffer("^HComplete areas list:^?^N\n\r");
		Vector areasVec=new Vector();
		for(Enumeration<Area> a=CMLib.map().areas();a.hasMoreElements();)
		{
			Area A=a.nextElement();
			String name=A.name();
			areasVec.addElement(name);
		}
		int col=0;
		for(int i=0;i<areasVec.size();i++)
		{
			if((++col)>3)
			{
				msg.append("\n\r");
				col=1;
			}
			msg.append(CMStrings.padRight((String)areasVec.elementAt(i),22)+"^N");
		}
		msg.append("\n\r\n\r^HEnter 'HELP (AREA NAME) for more information.^?");
		if((mob!=null)&&(mob.session()!=null))
			mob.session().colorOnlyPrintln(msg.toString());
		return false;
	}

	public boolean canBeOrdered(){return true;}
}
