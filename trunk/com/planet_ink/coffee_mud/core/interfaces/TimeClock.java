package com.planet_ink.coffee_mud.core.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/*
 * This interface represents a complete calendar, a complete lunar cycle, and 
 * schedule for days and nights.  It also manages the current date and time.
 */
public interface TimeClock extends Tickable, CMCommon, CMSavable, CMModifiable
{
	public String timeDescription(MOB mob, Room room);
	public String getShortTimeDescription();
	public String getShortestTimeDescription();
	public int getYear();
	public void setYear(int y);
	public int getMonth();
	public void setMonth(int m);
	public int getDayOfMonth();
	public void setDayOfMonth(int d);
	public int getTimeOfDay();
	public boolean setTimeOfDay(int t);
	public int getTODCode();
	public int getSeasonCode();
	public void tickTock(int howManyHours);
	public int getHoursInDay();
	public void setHoursInDay(int h);
	public int getDaysInMonth();
	public void setDaysInMonth(int d);
	public int getMonthsInYear();
	public String[] getMonthNames();
	public void setMonthsInYear(String[] months);
	public int[] getDawnToDusk();
	public void setDawnToDusk(int dawn, int day, int dusk, int night);
	public String[] getWeekNames();
	public int getDaysInWeek();
	public void setDaysInWeek(String[] days);
	public String[] getYearNames();
	public void setYearNames(String[] years);
	@Override public TimeClock newInstance();
	@Override public TimeClock copyOf();
	//public String deriveEllapsedTimeString(long millis);

	public final static String[] TOD_DESC={
		"It is dawn ","It is daytime ","It is dusk ","It is nighttime "
	};
	public final static int TIME_DAWN=0;
	public final static int TIME_DAY=1;
	public final static int TIME_DUSK=2;
	public final static int TIME_NIGHT=3;

	public final static int SEASON_SPRING=0;
	public final static int SEASON_SUMMER=1;
	public final static int SEASON_FALL=2;
	public final static int SEASON_WINTER=3;
	
	public final static String[] SEASON_DESCS={"SPRING","SUMMER","FALL","WINTER"};
	public final static int[][] defaultTemperatures =
	{
		{28000, 28500, 28000, 27500},
		{28500, 29000, 28500, 28000},
		{28000, 28500, 28000, 27500},
		{27000, 27500, 27000, 26500}
	};
	
}
