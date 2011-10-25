package com.planet_ink.coffee_mud.Items.interfaces;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;


public interface Wearable extends Item
{
	public enum WearType
	{
		Piercing,	//Basically, earrings
		Wrapping,	//Stuff that tightens to fit the form
		Fit,	//Just hangs on because it's not too big to fall off.
		Loose	//stuff like on the neck where excess can hang anywhere
	}
	//Limbs will have a type(below), length, size(thickness, usually), and extremity(variable depending on type, typically up to 100)
	public enum FitType	//For the Fit type, to specify shape
	{
		CylinderTaper,	//Common tail shape, perfect cylinder at 0 extremity, goes to a point at 100 (100 will be common for tails)
		CylinderBulge,	//i.e. arm. A perfect cylinder will be this with 0 extremity. Probably neck too.
		CylinderEgg,	//i.e. leg. Also perfect cylinder with 0 extremity.
		FlatCurve,	//i.e Palm or foot. Extremity would be degree of curve, 0 would be flat.
		Head,	//Extremity will be a raw type of shape rather than a degree.
		Round	//Basically, breasts :V Extremity would be how much it extends. 0 will be flat, 50 will be perfect spheres, above would be long and going into CylinderEgg territory.
		//Torso is kinda a FlatCurve of some type. Hmm. Leave it alone I think..
	}
/*
	public WearType wearType();

	public int limbs();
	public void setLimbs();
	public HashMap limbMap();
	public int limbDistance(int limb1, int limb2);
*/
	public static class LimbFit // implements CMModifiable.CMMHolder, CMSavable.CMSHolder
	{
		private FitType type;
		private int[] nums;	//Length, Size, Extremity
		public FitType fitType(){return type;}
		public int[] nums(){return nums;}
		public void setFit(FitType newType) {type=newType;}
		public void setNums(int[] newNums) {nums=newNums;}
	}
//	public static class DefaultWearable implements Wearable
//	nah, make this a StdWearable
}
