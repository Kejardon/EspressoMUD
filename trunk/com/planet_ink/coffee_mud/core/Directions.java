package com.planet_ink.coffee_mud.core;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
import java.util.*;
@SuppressWarnings("unchecked")
public class Directions
{
	public enum Dirs
	{
		NORTH("N","the north","to the north"),
		SOUTH("S","the south","to the south"),
		EAST("E","the east","to the east"),
		WEST("W","the west","to the west"),
		NORTHEAST("NE","the northeast","to the northeast"),
		NORTHWEST("NW","the northwest","to the northwest"),
		SOUTHEAST("SE","the southeast","to the southeast"),
		SOUTHWEST("SW","the southwest","to the southwest"),
		UP("U","above","above you"),
		DOWN("D","below","below"),
		GATE("V","out of nowhere","there"),
		;
		private static boolean parsed=false;
		private String chars;
		private String from;
		private String to;
		private Dirs opposite;
		private Dirs(String S1, String S2, String S3){chars=S1;from=S2;to=S3;}
		public String chars(){return chars;}
		public String from(){return from;}
		public String to(){return to;}
		public Dirs opposite()
		{
			if(!parsed)
			{
				parsed=true;
				NORTH.opposite=SOUTH;
				SOUTH.opposite=NORTH;
				EAST.opposite=WEST;
				WEST.opposite=EAST;
				UP.opposite=DOWN;
				DOWN.opposite=UP;
				GATE.opposite=GATE;
				NORTHEAST.opposite=NORTHWEST;
				NORTHWEST.opposite=NORTHEAST;
				SOUTHWEST.opposite=SOUTHEAST;
				SOUTHEAST.opposite=SOUTHWEST;
			}
			return opposite;
		}
	}
	
	public static final EnumSet<Dirs> DIRECTIONS_BASE=EnumSet.range(Dirs.NORTH, Dirs.SOUTHWEST);
	public static final String DIRECTIONS_DESC="N, S, E, W, NE, NW, SE, SW, U, D, or V";
	
	public static final int NUM_DIRECTIONS=11;
	//public static int NUM_DIRECTIONS(){return 11;}
	//public static EnumSet DIRECTIONS_BASE(){ return BASE; };
	
	//public static String DIRECTIONS_DESC(){ return DIRECTIONS_DESC; };
	
	private static class StringNum
	{
		public StringNum(String S, int i){this.S=S; this.i=i;}
		private String S=null;
		private int i=0;
		public int value(){return i;}
		public String S(){return S;}
	}
	private static StringNum[] DIRECTIONS_FULL_CHART={
		new StringNum("UP",Dirs.UP.ordinal()),
		new StringNum("ABOVE",Dirs.UP.ordinal()),
		new StringNum("NORTH",Dirs.NORTH.ordinal()),
		new StringNum("EAST",Dirs.EAST.ordinal()),
		new StringNum("WEST",Dirs.WEST.ordinal()),
		new StringNum("SOUTH",Dirs.SOUTH.ordinal()),
		new StringNum("NORTHEAST",Dirs.NORTHEAST.ordinal()),
		new StringNum("NORTHWEST",Dirs.NORTHWEST.ordinal()),
		new StringNum("SOUTHWEST",Dirs.SOUTHWEST.ordinal()),
		new StringNum("SOUTHEAST",Dirs.SOUTHEAST.ordinal()),
		new StringNum("NW",Dirs.NORTHWEST.ordinal()),
		new StringNum("NE",Dirs.NORTHEAST.ordinal()),
		new StringNum("SW",Dirs.SOUTHWEST.ordinal()),
		new StringNum("SE",Dirs.SOUTHEAST.ordinal()),
		new StringNum("DOWN",Dirs.DOWN.ordinal()),
		new StringNum("BELOW",Dirs.DOWN.ordinal()),
		new StringNum("NOWHERE",Dirs.GATE.ordinal()),
		new StringNum("HERE",Dirs.GATE.ordinal()),
		new StringNum("THERE",Dirs.GATE.ordinal()),
		new StringNum("VORTEX",Dirs.GATE.ordinal())
	};

	public static Dirs getDirection(int code)
	{
		if(code>NUM_DIRECTIONS)
			return null;
		return Dirs.values()[code];
	}

	public static Dirs getDirectionCode(String theDir)
	{
		Dirs code=getGoodDirectionCode(theDir);
		if(code==null)
		{
			theDir=theDir.toUpperCase();
			Dirs[] options=Dirs.values();
			for(int i=0;i<NUM_DIRECTIONS;i++)
				if(theDir.startsWith(options[i].toString()))
					return options[i];
		}
		return code;
	}
	
	public static Dirs getGoodDirectionCode(String theDir)
	{
		if(theDir.length()==0) return null;
		theDir=theDir.toUpperCase();
		for(StringNum S : DIRECTIONS_FULL_CHART)
			if((S.S().startsWith(theDir))&&(S.value()<NUM_DIRECTIONS))
				return Dirs.values()[S.value()];
		return null;
	}

	public static int[] adjustXYByDirections(int x, int y, Dirs direction)
	{
		switch(direction)
		{
		case NORTH: y--; break;
		case SOUTH: y++; break;
		case EAST: x++; break;
		case WEST: x--; break;
		case NORTHEAST: x++; y--; break;
		case NORTHWEST: x--; y--; break;
		case SOUTHEAST: x++; y++; break;
		case SOUTHWEST: x--; y++; break;
		}
		return new int[]{x,y};
	}
}
