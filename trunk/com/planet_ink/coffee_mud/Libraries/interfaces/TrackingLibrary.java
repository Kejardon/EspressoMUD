package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

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
public interface TrackingLibrary extends CMLibrary
{
/*
	public Vector findBastardTheBestWay(Room location, Room destRoom, TrackingFlags flags, int maxRadius);
	public Vector findBastardTheBestWay(Room location, Vector<Room> destRooms, TrackingFlags flags, int maxRadius);
	public int trackNextDirectionFromHere(Vector<Room> theTrail, Room location, boolean openOnly);
	public void stopTracking(MOB mob);
	public int radiatesFromDir(Room room, Vector<Room> rooms);
	public void getRadiantRooms(Room room, Vector<Room> rooms, TrackingFlags flags, Room radiateTo, int maxDepth, HashSet<Room> ignoreRooms);
	public Vector getRadiantRooms(Room room, TrackingFlags flags, int maxDepth);
	public boolean beMobile(MOB mob,
							boolean dooropen,
							boolean wander,
							boolean roomprefer, 
							boolean roomobject,
							long[] status,
							Vector<Room> rooms);
	public void wanderAway(MOB M, boolean mindPCs, boolean andGoHome);
	public void wanderFromTo(MOB M, Room toHere, boolean mindPCs);
	public void wanderIn(MOB M, Room toHere);
	public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook, boolean noriders);
	public boolean move(MOB mob, int directionCode, boolean flee, boolean nolook);
	public int findExitDir(MOB mob, Room R, String desc);
	public int findRoomDir(MOB mob, Room R);
	public Vector findAllTrails(Room from, Room to, Vector<Room> radiantTrail);
	public Vector findAllTrails(Room from, Vector<Room> tos, Vector<Room> radiantTrail);
	public String getTrailToDescription(Room R1, Vector<Room> set, String where, boolean areaNames, boolean confirm, int radius, HashSet<Room> ignoreRooms, int maxMins);
	
	public static enum TrackingFlag {NOHOMES,OPENONLY,AREAONLY,NOEMPTYGRIDS,NOAIR,NOWATER};
	
	public static class TrackingFlags extends HashSet {
		private static final long serialVersionUID = 1L;
		public TrackingFlags add(TrackingFlag flag) { super.add(flag); return this;}
	}
*/
}
