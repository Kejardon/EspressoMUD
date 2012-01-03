package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.DatabaseEngine;
import com.planet_ink.coffee_mud.Libraries.interfaces.XMLLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//TODO: Make this class store a real-time time, derive actual time as needed
@SuppressWarnings("unchecked")
public class DefaultTimeClock implements TimeClock, Ownable
{
	protected CMSavable parent;

	public String ID(){return "DefaultTimeClock";}
	public String name(){return "Time Object";}
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultTimeClock();}}
	public void initializeClass(){}
	public long lastTick=0;

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner){parent=owner; return this;}

	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	protected boolean loaded=false;
	protected int year=1;
	protected int baseYear=0;
	protected int month=1;
	protected int day=1;
	protected int time=0;
	protected long baseTime=System.currentTimeMillis();
	protected int hoursInDay=6;
	protected String[] monthsInYear={
			 "the 1st month","the 2nd month","the 3rd month","the 4th month",
			 "the 5th month","the 6th month","the 7th month","the 8th month"
	};
	protected int daysInMonth=20;
	protected int[] dawnToDusk={0,1,4,6};
	protected String[] weekNames={};
	protected String[] yearNames={"year #"};
	
	protected void recalcTime()	//Assume current time is accurate, get new baseTime
	{
		long revertThisFar=0;
		long multiplier=Tickable.TIME_MILIS_PER_MUDHOUR;
		revertThisFar+=time*multiplier;
		multiplier*=hoursInDay;
		revertThisFar+=day*multiplier;
		multiplier*=daysInMonth;
		revertThisFar+=month*multiplier;
		multiplier*=monthsInYear.length;
		baseYear=year;
		baseTime=lastTick-revertThisFar;
		CMLib.database().saveObject(this);
	}
	protected void setBaseTime(long newTime, int newBaseYear)	//Absolute base values
	{
		baseYear=newBaseYear;
		baseTime=newTime;
		lastTick=System.currentTimeMillis();
		long tempTime=(lastTick-newTime)/Tickable.TIME_MILIS_PER_MUDHOUR;
		long tempDay=tempTime/hoursInDay;
		time=(int)(tempTime%hoursInDay);
		long tempMonth=tempDay/daysInMonth;
		day=(int)(tempDay%daysInMonth);
		year=(int)(tempMonth/monthsInYear.length);
		month=(int)(tempMonth%monthsInYear.length);
	}
	
	public int getHoursInDay(){return hoursInDay;}
	public void setHoursInDay(int h){hoursInDay=h; recalcTime();}
	public int getDaysInMonth(){return daysInMonth;}
	public void setDaysInMonth(int d){daysInMonth=d; recalcTime();}
	public int getMonthsInYear(){return monthsInYear.length;}
	public String[] getMonthNames(){return monthsInYear;}
	public void setMonthsInYear(String[] months){monthsInYear=months; recalcTime();}
	public int[] getDawnToDusk(){return dawnToDusk;}
	public String[] getYearNames(){return yearNames;}
	public void setYearNames(String[] years){yearNames=years; CMLib.database().saveObject(this);}
	public void setDawnToDusk(int dawn, int day, int dusk, int night)
	{ 
		dawnToDusk[TIME_DAWN]=dawn;
		dawnToDusk[TIME_DAY]=day;
		dawnToDusk[TIME_DUSK]=dusk;
		dawnToDusk[TIME_NIGHT]=night;
		recalcTime();
	}
	public String[] getWeekNames(){return weekNames;}
	public int getDaysInWeek(){return weekNames.length;}
	public void setDaysInWeek(String[] days){weekNames=days; CMLib.database().saveObject(this);}
	
	public String getShortestTimeDescription()
	{
		StringBuffer timeDesc=new StringBuffer("");
		timeDesc.append(getYear());
		timeDesc.append("/"+getMonth());
		timeDesc.append("/"+getDayOfMonth());
		timeDesc.append(" HR:"+getTimeOfDay());
		return timeDesc.toString();
	}
	public String getShortTimeDescription()
	{
		StringBuffer timeDesc=new StringBuffer("");
		timeDesc.append("hour "+getTimeOfDay()+" on ");
		if(getDaysInWeek()>0)
		{
			long x=((long)getYear())*((long)getMonthsInYear())*getDaysInMonth();
			x=x+((long)(getMonth()-1))*((long)getDaysInMonth());
			x=x+getDayOfMonth();
			timeDesc.append(getWeekNames()[(int)(x%getDaysInWeek())]+", ");
		}
		timeDesc.append("the "+getDayOfMonth()+CMath.numAppendage(getDayOfMonth()));
		timeDesc.append(" day of "+getMonthNames()[getMonth()-1]);
		if(getYearNames().length>0)
			timeDesc.append(", "+CMStrings.replaceAll(getYearNames()[getYear()%getYearNames().length],"#",""+getYear()));
		return timeDesc.toString();
	}
	
	public void initializeINIClock(CMProps page)
	{
		if(CMath.s_int(page.getStr("HOURSINDAY"))>0)
			setHoursInDay(CMath.s_int(page.getStr("HOURSINDAY")));

		if(CMath.s_int(page.getStr("DAYSINMONTH"))>0)
			setDaysInMonth(CMath.s_int(page.getStr("DAYSINMONTH")));

		String monthsInYear=page.getStr("MONTHSINYEAR");
		if(monthsInYear.trim().length()>0)
			setMonthsInYear(CMParms.toStringArray(CMParms.parseCommas(monthsInYear,true)));

		setDaysInWeek(CMParms.toStringArray(CMParms.parseCommas(page.getStr("DAYSINWEEK"),true)));

		if(page.containsKey("YEARDESC"))
			setYearNames(CMParms.toStringArray(CMParms.parseCommas(page.getStr("YEARDESC"),true)));

		if(page.containsKey("DAWNHR")&&page.containsKey("DAYHR")
				&&page.containsKey("DUSKHR")&&page.containsKey("NIGHTHR"))
		setDawnToDusk(
						CMath.s_int(page.getStr("DAWNHR")),
						CMath.s_int(page.getStr("DAYHR")),
						CMath.s_int(page.getStr("DUSKHR")),
						CMath.s_int(page.getStr("NIGHTHR")));

//		CMProps.Ints.TICKSPERMUDDAY.setProperty(Tickable.TIME_MILIS_PER_MUDHOUR*CMLib.time().globalClock().getHoursInDay()/Tickable.TIME_TICK);
//		CMProps.Ints.TICKSPERMUDMONTH.setProperty(Tickable.TIME_MILIS_PER_MUDHOUR*CMLib.time().globalClock().getHoursInDay()*CMLib.time().globalClock().getDaysInMonth()/Tickable.TIME_TICK);
	}
	
	public String timeDescription(MOB mob, Room room)
	{
		StringBuffer timeDesc=new StringBuffer("");

		if(getTODCode()>=0)
			timeDesc.append(TOD_DESC[getTODCode()]);
		timeDesc.append("(Hour: "+getTimeOfDay()+"/"+(getHoursInDay()-1)+")");
		timeDesc.append("\n\rIt is ");
		if(getDaysInWeek()>0)
		{
			long x=((long)getYear())*((long)getMonthsInYear())*getDaysInMonth();
			x=x+((long)(getMonth()-1))*((long)getDaysInMonth());
			x=x+getDayOfMonth();
			timeDesc.append(getWeekNames()[(int)(x%getDaysInWeek())]+", ");
		}
		timeDesc.append("the "+getDayOfMonth()+CMath.numAppendage(getDayOfMonth()));
		timeDesc.append(" day of "+getMonthNames()[getMonth()-1]);
		if(getYearNames().length>0)
			timeDesc.append(", "+CMStrings.replaceAll(getYearNames()[getYear()%getYearNames().length],"#",""+getYear()));
		timeDesc.append(".\n\rIt is "+(TimeClock.SEASON_DESCS[getSeasonCode()]).toLowerCase()+".");
		return timeDesc.toString();
	}

	public int getYear(){return year;}
	public void setYear(int y){year=y; CMLib.database().saveObject(this);}

	public int getSeasonCode(){
		int div=getMonthsInYear()/4;
		if(month<div) return TimeClock.SEASON_WINTER;
		if(month<(div*2)) return TimeClock.SEASON_SPRING;
		if(month<(div*3)) return TimeClock.SEASON_SUMMER;
		return TimeClock.SEASON_FALL;
	}
	public int getMonth(){return month;}
	public void setMonth(int m){month=m; CMLib.database().saveObject(this);}

	public int getDayOfMonth(){return day;}
	public void setDayOfMonth(int d){day=d; CMLib.database().saveObject(this);}
	public int getTimeOfDay(){return time;}
	public int getTODCode()
	{
		if((time>=getDawnToDusk()[TimeClock.TIME_NIGHT])&&(getDawnToDusk()[TimeClock.TIME_NIGHT]>=0))
			return TimeClock.TIME_NIGHT;
		if((time>=getDawnToDusk()[TimeClock.TIME_DUSK])&&(getDawnToDusk()[TimeClock.TIME_DUSK]>=0))
			return TimeClock.TIME_DUSK;
		if((time>=getDawnToDusk()[TimeClock.TIME_DAY])&&(getDawnToDusk()[TimeClock.TIME_DAY]>=0))
			return TimeClock.TIME_DAY;
		if((time>=getDawnToDusk()[TimeClock.TIME_DAWN])&&(getDawnToDusk()[TimeClock.TIME_DAWN]>=0))
			return TimeClock.TIME_DAWN;
		// it's before night, dusk, day, and dawn... before dawn is still night.
		if(getDawnToDusk()[TimeClock.TIME_NIGHT]>=0)
			return TimeClock.TIME_NIGHT;
		return TimeClock.TIME_DAY;
	}
	public boolean setTimeOfDay(int t)
	{
		int oldCode=getTODCode();
		time=t;
		CMLib.database().saveObject(this);
		return getTODCode()!=oldCode;
	}
	
	public CMObject copyOf()
	{
		try
		{
			TimeClock C=(TimeClock)this.clone();
			return C;
		}
		catch(CloneNotSupportedException e)
		{
			return new DefaultTimeClock();
		}
	}

	public String deriveEllapsedTimeString(long millis)
	{
		int hours=(int)(millis/Tickable.TIME_MILIS_PER_MUDHOUR);
		int days=0;
		int months=0;
		int years=0;
		if(hours>getHoursInDay())
		{
			days=hours/getHoursInDay();
			hours=hours-(days*getHoursInDay());
		}
		if(days>getDaysInMonth())
		{
			months=days/getDaysInMonth();
			days=days-(months*getDaysInMonth());
		}
		if(months>getMonthsInYear())
		{
			years=months/getMonthsInYear();
			months=months-(years*getMonthsInYear());
		}
		StringBuffer buf=new StringBuffer("");
		if(years>0) buf.append(years+" years");
		if(months>0)
		{
			if(buf.length()>0) buf.append(", ");
			buf.append(months+" months");
		}
		if(days>0)
		{
			if(buf.length()>0) buf.append(", ");
			buf.append(days+" days");
		}
		if(hours>0)
		{
			if(buf.length()>0) buf.append(", ");
			buf.append(hours+" hours");
		}
		if(buf.length()==0) return "any second now";
		return buf.toString();
	}
	
	public void raiseLowerTheSunEverywhere()
	{
		try
		{
			for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
			{
				Area A=(Area)a.nextElement();
				if(A.getTimeObj()==this)
				for(Enumeration r=A.getProperMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					/*
					if((R!=null)&&((R.numInhabitants()>0)||(R.numItems()>0)))
					{
						R.recoverEnvStats();
						for(int m=0;m<R.numInhabitants();m++)
						{
							MOB mob=R.fetchInhabitant(m);
							if((mob!=null)
							&&(!mob.isMonster()))
							{
								switch(getTODCode())
								{
								case TimeClock.TIME_DAWN:
									mob.tell("It is now daytime."); break;
								case TimeClock.TIME_DAY: break;
									//mob.tell("The sun is now shining brightly."); break;
								case TimeClock.TIME_DUSK: break;
									//mob.tell("It is almost nighttime."); break;
								case TimeClock.TIME_NIGHT:
									mob.tell("It is nighttime."); break;
								}
							}
						}
					} */
					if(R!=null)
						R.recoverRoomStats();
				}
			}
		}catch(java.util.NoSuchElementException x){}
	}

	public void tickTock(int howManyHours)
	{
		int todCode=getTODCode();
		time+=howManyHours;
		lastTick=System.currentTimeMillis();
		if(howManyHours>0)
		{
			if(time>hoursInDay)
			{
				day+=time/hoursInDay;
				time=time%hoursInDay;
				if(day>daysInMonth)
				{
					month+=day/daysInMonth;
					day=day%daysInMonth;
					if(month>monthsInYear.length)
					{
						year+=month/monthsInYear.length;
						month=month%monthsInYear.length;
					}
				}
			}
		}
		else if(howManyHours<0)
		{
			if(time<0)
			{
				day+=time/hoursInDay-1;
				time=time%hoursInDay+hoursInDay;
				if(day<0)
				{
					month+=day/daysInMonth-1;
					day=day%daysInMonth+daysInMonth;
					if(month<0)
					{
						year+=month/monthsInYear.length-1;
						month=month%monthsInYear.length+monthsInYear.length;
					}
				}
			}
		}
		if(getTODCode()!=todCode) raiseLowerTheSunEverywhere();
	}
	public boolean tick(Tickable ticking, Tickable.TickID tickID)
	{
//		tickStatus=Tickable.TickStat.Not;
		if((System.currentTimeMillis()-lastTick)<=Tickable.TIME_MILIS_PER_MUDHOUR)
			return true;
		synchronized(this)
		{
			boolean timeToTick = ((System.currentTimeMillis()-lastTick)>Tickable.TIME_MILIS_PER_MUDHOUR);
			lastTick=System.currentTimeMillis();
			if(timeToTick)
				if(++time>=hoursInDay)
				{
					time=0;
					if(++day>daysInMonth)
					{
						day=1;
						if(++month>monthsInYear.length)
						{
							month=1;
							year++;
						}
					}
				}
		}
		return true;
	}
	public long lastTick(){return lastTick;}
	public long lastAct(){return 0;}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void destroy()
	{
		//TODO
		CMLib.database().deleteObject(this);
	}

	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){parent.saveThis();}

	private enum SCode implements CMSavable.SaveEnum{
		TIM(){
			public ByteBuffer save(DefaultTimeClock E){
				return (ByteBuffer)ByteBuffer.wrap(new byte[12]).putLong(E.baseTime).putInt(E.baseYear).rewind(); }
			public int size(){return 12;}
			public void load(DefaultTimeClock E, ByteBuffer S){ E.setBaseTime(S.getLong(), S.getInt()); } },
		DTD(){
			public ByteBuffer save(DefaultTimeClock E){ return CMLib.coffeeMaker().savAInt(E.dawnToDusk); }
			public int size(){return 16;}
			public void load(DefaultTimeClock E, ByteBuffer S){ E.dawnToDusk=CMLib.coffeeMaker().loadAInt(S); } },
		HRS(){
			public ByteBuffer save(DefaultTimeClock E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.hoursInDay).rewind(); }
			public int size(){return 4;}
			public void load(DefaultTimeClock E, ByteBuffer S){ E.hoursInDay=S.getInt(); } },
		DYS(){
			public ByteBuffer save(DefaultTimeClock E){ return (ByteBuffer)ByteBuffer.wrap(new byte[4]).putInt(E.daysInMonth).rewind(); }
			public int size(){return 4;}
			public void load(DefaultTimeClock E, ByteBuffer S){ E.daysInMonth=S.getInt(); } },
		WKS(){
			public ByteBuffer save(DefaultTimeClock E){ return CMLib.coffeeMaker().savAString(E.weekNames); }
			public int size(){return 0;}
			public void load(DefaultTimeClock E, ByteBuffer S){ E.weekNames=CMLib.coffeeMaker().loadAString(S); } },
		MNS(){
			public ByteBuffer save(DefaultTimeClock E){ return CMLib.coffeeMaker().savAString(E.monthsInYear); }
			public int size(){return 0;}
			public void load(DefaultTimeClock E, ByteBuffer S){ E.monthsInYear=CMLib.coffeeMaker().loadAString(S); } },
		YRS(){
			public ByteBuffer save(DefaultTimeClock E){ return CMLib.coffeeMaker().savAString(E.yearNames); }
			public int size(){return 0;}
			public void load(DefaultTimeClock E, ByteBuffer S){ E.yearNames=CMLib.coffeeMaker().loadAString(S); } },
		;
		public abstract ByteBuffer save(DefaultTimeClock E);
		public abstract void load(DefaultTimeClock E, ByteBuffer S);
		public ByteBuffer save(CMSavable E){return save((DefaultTimeClock)E);}
		public CMSavable subObject(CMSavable fromThis){return null;}
		public void load(CMSavable E, ByteBuffer S){load((DefaultTimeClock)E, S);} }
	private enum MCode implements CMModifiable.ModEnum{
		HOUR(){
			public String brief(DefaultTimeClock E){return ""+E.time;}
			public String prompt(DefaultTimeClock E){return ""+E.time;}
			public void mod(DefaultTimeClock E, MOB M){E.time=CMLib.genEd().intPrompt(M, ""+E.time); E.recalcTime();} },
		DAY(){
			public String brief(DefaultTimeClock E){return ""+E.day;}
			public String prompt(DefaultTimeClock E){return ""+E.day;}
			public void mod(DefaultTimeClock E, MOB M){E.day=CMLib.genEd().intPrompt(M, ""+E.day); E.recalcTime();} },
		MONTH(){
			public String brief(DefaultTimeClock E){return ""+E.month;}
			public String prompt(DefaultTimeClock E){return ""+E.month;}
			public void mod(DefaultTimeClock E, MOB M){E.month=CMLib.genEd().intPrompt(M, ""+E.month); E.recalcTime();} },
		YEAR(){
			public String brief(DefaultTimeClock E){return ""+E.year;}
			public String prompt(DefaultTimeClock E){return ""+E.year;}
			public void mod(DefaultTimeClock E, MOB M){E.year=CMLib.genEd().intPrompt(M, ""+E.year); E.recalcTime();} },
		WEEKNAMES(){
			public String brief(DefaultTimeClock E){return ""+E.weekNames.length;}
			public String prompt(DefaultTimeClock E){return ""+E.weekNames;}
			public void mod(DefaultTimeClock E, MOB M){CMLib.genEd().astringPrompt(M, E.weekNames, false); E.recalcTime();} },
		MONTHNAMES(){
			public String brief(DefaultTimeClock E){return ""+E.monthsInYear.length;}
			public String prompt(DefaultTimeClock E){return ""+E.monthsInYear;}
			public void mod(DefaultTimeClock E, MOB M){CMLib.genEd().astringPrompt(M, E.monthsInYear, false); E.recalcTime();} },
		YEARNAMES(){
			public String brief(DefaultTimeClock E){return ""+E.yearNames.length;}
			public String prompt(DefaultTimeClock E){return ""+E.yearNames;}
			public void mod(DefaultTimeClock E, MOB M){CMLib.genEd().astringPrompt(M, E.yearNames, false);} },
		HOURSOFDAY(){
			public String brief(DefaultTimeClock E){return ""+E.dawnToDusk;}
			public String prompt(DefaultTimeClock E){return ""+E.dawnToDusk;}
			public void mod(DefaultTimeClock E, MOB M){CMLib.genEd().aintPrompt(M, E.dawnToDusk); E.recalcTime();} },
		HOURSPERDAY(){
			public String brief(DefaultTimeClock E){return ""+E.hoursInDay;}
			public String prompt(DefaultTimeClock E){return ""+E.hoursInDay;}
			public void mod(DefaultTimeClock E, MOB M){E.hoursInDay=CMLib.genEd().intPrompt(M, ""+E.hoursInDay); E.recalcTime();} },
		DAYSPERMONTH(){
			public String brief(DefaultTimeClock E){return ""+E.daysInMonth;}
			public String prompt(DefaultTimeClock E){return ""+E.daysInMonth;}
			public void mod(DefaultTimeClock E, MOB M){E.daysInMonth=CMLib.genEd().intPrompt(M, ""+E.daysInMonth); E.recalcTime();} },
		;
		public abstract String brief(DefaultTimeClock fromThis);
		public abstract String prompt(DefaultTimeClock fromThis);
		public abstract void mod(DefaultTimeClock toThis, MOB M);
		public String brief(CMModifiable fromThis){return brief((DefaultTimeClock)fromThis);}
		public String prompt(CMModifiable fromThis){return prompt((DefaultTimeClock)fromThis);}
		public void mod(CMModifiable toThis, MOB M){mod((DefaultTimeClock)toThis, M);} }

}
