package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
public class Dice extends StdLibrary
{
	@Override public String ID(){return "Dice";}
	private Random randomizer = null;

	public synchronized Random getRandomizer() {
		if(randomizer == null)
			randomizer = new Random(System.currentTimeMillis());
		return randomizer;
	}
	public Dice()
	{
		super();
		randomizer = new Random(System.currentTimeMillis());
	}
	
	public boolean normalizeAndRollLess(int score)
	{
		return (rollPercentage()<normalizeBy5(score));
	}
	public int normalizeBy5(int score)
	{
		if(score>95)
			return 95;
		else
		if(score<5)
			return 5;
		return score;
	}

	public int roll(int number, int die, int modifier)
	{
		if(die<=0) 
			return modifier;
		int total=0;
		for(int i=0;i<number;i++)
			total+=randomizer.nextInt(die)+1;
		return total + modifier;
	}

	public int rollPercentage()
	{
		return (Math.abs(randomizer.nextInt() % 100)) + 1;
	}
}