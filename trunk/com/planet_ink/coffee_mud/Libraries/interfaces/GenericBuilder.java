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
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public interface GenericBuilder extends CMLibrary
{
	//Use this instead of making useless buffers
	public static final ByteBuffer emptyBuffer=ByteBuffer.wrap(new byte[0]);

	//Main functions
//	public ByteBuffer getPropertiesStr(CMSavable E);
//	public void setPropertiesStr(CMSavable E, String buf);

	//Public functions for common save/load code
//	public CMSavable loadSub(String A);
//	public ByteBuffer getSubStr(CMSavable Obj);
	public byte[] loadAByte(ByteBuffer A);
	public ByteBuffer savAShort(short[] val);
	public short[] loadAShort(ByteBuffer A);
	public ByteBuffer savAInt(int[] val);
	public int[] loadAInt(ByteBuffer A);
	public ByteBuffer savALong(long[] val);
	public long[] loadALong(ByteBuffer A);
	public ByteBuffer savADouble(double[] val);
	public double[] loadADouble(ByteBuffer A);
	public ByteBuffer savABoolean(boolean[] val);
	public boolean[] loadABoolean(ByteBuffer A);
	public ByteBuffer savAChar(char[] val);
	public char[] loadAChar(ByteBuffer A);
	public ByteBuffer savAString(String[] val);
	public ByteBuffer savAAString(String[]... val);
	public String[] loadAString(ByteBuffer A);
	public ByteBuffer savString(String val);
	public String loadString(ByteBuffer A);
//	public ByteBuffer savSubFixed(CMSavable.CMSubSavable sub);
//	public ByteBuffer savSubVar(CMSavable.CMSubSavable sub);
	public ByteBuffer savSubFull(CMSavable sub);
	public CMSavable loadSub(ByteBuffer buf, CMSavable source, CMSavable.SaveEnum subCall);
	public ByteBuffer savSaveNums(CMSavable[] e);
	public ByteBuffer getRaceWVector(WVector<Race> V);
	public WVector<Race> setRaceWVector(ByteBuffer S);
	public ByteBuffer getEnumWVector(WVector<? extends Enum> V);
	public <T extends Enum> WVector<T> setEnumWVector(Class<T> enumClass, ByteBuffer S);
	public ByteBuffer savExits(Room.REMap[] exits);
	public int[][] loadExits(ByteBuffer buf);
//	public String savStringsInterlaced(String[] ... val);
//	public String[][] loadStringsInterlaced(String A, int dim);
//	public String getSaveNumStr(Enumeration<CMSavable> e);
//	public String getVectorStr(Vector V);
//	public Vector setVectorStr(String S);
//	public String getWVectorStr(WVector V);
//	public WVector setWVectorStr(String S);
}
