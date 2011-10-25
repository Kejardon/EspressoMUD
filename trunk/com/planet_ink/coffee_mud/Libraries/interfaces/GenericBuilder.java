package com.planet_ink.coffee_mud.Libraries.interfaces;
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
public interface GenericBuilder extends CMLibrary
{
	//Main functions
	public String getPropertiesStr(CMSavable E);
	public void setPropertiesStr(CMSavable E, String buf);

	//Public functions for common save/load code
	public CMSavable loadSub(String A);
	public String getSubStr(CMSavable Obj);
	public String savAShort(short[] val);
	public short[] loadAShort(String A);
	public String savAInt(int[] val);
	public int[] loadAInt(String A);
	public String savALong(long[] val);
	public long[] loadALong(String A);
	public String savADouble(double[] val);
	public double[] loadADouble(String A);
	public String savABoolean(boolean[] val);
	public boolean[] loadABoolean(String A);
	public String savAChar(char[] val);
	public char[] loadAChar(String A);
	public String savAString(String[] val);
	public String[] loadAString(String A);
	public String savStringsInterlaced(String[] ... val);
	public String[][] loadStringsInterlaced(String A, int dim);
	public String getVectorStr(Vector V);
	public Vector setVectorStr(String S);

}
