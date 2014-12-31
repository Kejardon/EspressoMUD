package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;

public interface Bind extends CMObject, Ownable, CMSavable, CMModifiable, CMCommon
{
	public static final int MAGNETIC=1;
	public static final int PRESSURE=MAGNETIC*2;
	public static final int GRAFT=PRESSURE*2;
	
	@Override public Bind copyOf();
	@Override public Bind newInstance();
	public Item itemA();
	public Item itemB();
	public int bindType();
	public void setType(int type);
	public int strength();
	public void setStrength(int str);
}