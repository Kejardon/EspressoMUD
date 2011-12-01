package com.planet_ink.coffee_mud.Races.interfaces;
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
import java.util.*;

/*
Ok. Planning time. Bodies will have a weighted list of races and derive their actual characteristics from that list and saved scale modifiers.
Bodymap. There will be a set of limbs with some standardized naming scheme.
	Lowercase start (tail1) means not mirrored.
	Uppercase start (Arm1) means mirrored, there will always be two of them. Can prepend with L or R to specificy a specific one.
		Can table-check against itself, mirrored values will be the distance between each. Otherwise it's center-distance to target distance.
		If L or R is specified for both (LArm1 LLeg1), sqrt((difference of self-self)^2+(base-to-base)^2)
		If L or R is specified for one (LArm1, Leg1), sqrt((specified self)^2+(base-to-base)^2). Same with mirrored to not-mirrored.
*/
@SuppressWarnings("unchecked")
public interface Body extends Item	//, Rideable
{
	public MOB mob();
	public void setMob(MOB mob);

//	public int initializeBirthday(int ageHours, Race R);
	public int[] birthday();

	public CharStats baseCharStats();
	public CharStats charStats();
	public void recoverCharStats();
//	public void resetToMaxState();
	public void setBaseCharStats(CharStats newBaseCharStats);
	//public void randomizeSizeWeightFactor();
	//public void setSizeWeightFactors(int size, int weight);
	//public int sizeFactor();
	//public int weightFactor();

	public String healthText(MOB viewer);

	// Chance of impregnation(0-100) when having sex with the target. Can be negative- 0 means 'impossible naturally but possible with magic/science aid', -100 means 'completely impossible'
//	public int fertile(Body target);

	public boolean amDead();
	public Body killMeDead();
	public void bringToLife(Room newLocation, boolean resetStats);
	public void bringToLife();
//	public Enumeration<Race> myRace();	//actually inconvenient to get and no need atm...
	public boolean isRace(Race R);
	public void setRace(Race... R);
	public void addRace(Race R);
	public String raceName();
	public void setGender(Gender G);
	public Gender gender();

//	TODO: Make Gender object in this folder
//	public Gender gender();
//	public void setGender(Gender g);

	public enum Part	//Typically recognized parts. This will be part of the standardized naming scheme
	{
		Head,
		Neck,
		Torso,	//This should have more probably? :/
		Arm,
		ArmSegment,
		Hand,
		Palm,
		Finger,	//FingerSegments are kinda feasible but that would get too detailed IMO
		Tail,
		Leg,
		LegSegment,
		Foot,
		FootBridge,	//equivalent to palm
		Toe
	}
	
	public static class BodyPart
	{
		private Wearable.FitType type;
		private int[] nums;
		private Part part;
		private Vector<BodyPart> sub=new Vector<BodyPart>();
		private Vector<Wearable> equip=new Vector<Wearable>();
		public Wearable.FitType fitType(){return type;}
		public int[] nums(){return nums;}
		public Part part(){return part;}
		public Vector<BodyPart> subSegments() {return sub;}
		public Vector<Wearable> equipment() {return equip;}
		public void setFit(Wearable.FitType type){this.type=type;}
		public void setNums(int[] nums){this.nums=nums;}
		public void setPart (Part part){this.part=part;}
	}
}