package com.planet_ink.coffee_mud.Common.interfaces;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
@SuppressWarnings("unchecked")
public interface Bind extends CMObject, Ownable, CMSavable, CMModifiable, CMCommon
{
	public static final int MAGNETIC=1;
	public static final int PRESSURE=MAGNETIC*2;
	public static final int GRAFT=PRESSURE*2;
	
	public Item itemA();
	public Item itemB();
	public int bindType();
	public void setType(int type);
	public int strength();
	public void setStrength(int str);
}