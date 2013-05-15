package com.planet_ink.coffee_mud.Common;
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
//TODO: Make this class store a real-time time, derive actual time as needed
@SuppressWarnings("unchecked")
public class DefaultTimeClock implements TimeClock, Ownable
{
	protected CMSavable parent;

	public String ID(){return "DefaultTimeClock";}
	public String name(){return "Time Object";}
	public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultTimeClock();}}
	public void initializeClass(){}
	//public long lastTick=0;

	//Ownable
	public CMSavable owner(){return parent;}
	public Ownable setOwner(CMSavable owner)
	{
		if(parent==null)
		{
			if(owner!=null)
				CMLib.threads().addClock(this);
		}
		else if(owner==null)
			CMLib.threads().delClock(this);
		parent=owner;
		return this;
	}

	protected Tickable.TickStat tickStatus=Tickable.TickStat.Not;
	protected int tickCount=0;
	protected int year=1;
	protected int baseYear=0;
	protected int month=1;
	protected int day=1;
	protected int hour=0;
	protected int subHour=0;
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
	
	public Tickable.TickStat getTickStatus(){return tickStatus;}
	protected void recalcTime()	//Assume current time is accurate, get new baseTime
	{
		long revertThisFar=0;
		long multiplier=Tickable.TIME_TICK; //Tickable.TIME_MILIS_PER_MUDHOUR;
		revertThisFar+=subHour*multiplier;
		multiplier*=Tickable.TIME_TICKS_PER_MUDHOUR;
		revertThisFar+=hour*multiplier;
		multiplier*=hoursInDay;
		revertThisFar+=day*multiplier;
		multiplier*=daysInMonth;
		revertThisFar+=month*multiplier;
		multiplier*=monthsInYear.length;
		baseYear=year;
		baseTime=System.currentTimeMillis()-revertThisFar;
		if(parent!=null)parent.saveThis();
	}
	protected void setBaseTime(long newTime, int newBaseYear)	//Absolute base values
	{
		baseYear=newBaseYear;
		baseTime=newTime;
		//lastTick=System.currentTimeMillis();
		long tempTime=(System.currentTimeMillis()-newTime)/Tickable.TIME_TICK;
		subHour=(int)(tempTime%Tickable.TIME_TICKS_PER_MUDHOUR);
		tempTime=tempTime/Tickable.TIME_TICKS_PER_MUDHOUR;
		hour=(int)(tempTime%hoursInDay);
		tempTime=tempTime/hoursInDay;
		day=(int)(tempTime%daysInMonth);
		tempTime=tempTime/daysInMonth;
		month=(int)(tempTime%monthsInYear.length);
		year=(int)(tempTime/monthsInYear.length);
		if(parent!=null)parent.saveThis();
/*		long tempHour=(tempTime)/Tickable.TIME_TICKS_PER_MUDHOUR;
		long tempDay=tempHour/hoursInDay;
		hour=(int)(tempHour%hoursInDay);
		long tempMonth=tempDay/daysInMonth;
		day=(int)(tempDay%daysInMonth);
		year=(int)(tempMonth/monthsInYear.length);
		month=(int)(tempMonth%monthsInYear.length); */
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
	public void setYearNames(String[] years){yearNames=years; if(parent!=null)parent.saveThis();}
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
	public void setDaysInWeek(String[] days){weekNames=days; recalcTime();}
	
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
			timeDesc.append(", "+getYearNames()[getYear()%getYearNames().length].replace("#",""+getYear()));
		return timeDesc.toString();
	}
	
	public String timeDescription(MOB mob, Room room)
	{
		StringBuffer timeDesc=new StringBuffer("");

		if(getTODCode()>=0)
			timeDesc.append(TOD_DESC[getTODCode()]);
		timeDesc.append("(Hour: "+getTimeOfDay()+"/"+(getHoursInDay()-1)+")");
		timeDesc.append("\r\nIt is ");
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
			timeDesc.append(", "+getYearNames()[getYear()%getYearNames().length].replace("#",""+getYear()));
		timeDesc.append(".\r\nIt is "+(TimeClock.SEASON_DESCS[getSeasonCode()]).toLowerCase()+".");
		return timeDesc.toString();
	}

	public int getYear(){return year;}
	public void setYear(int y){year=y; recalcTime();}

	public int getSeasonCode(){
		int div=getMonthsInYear()/4;
		if(month<div) return TimeClock.SEASON_WINTER;
		if(month<(div*2)) return TimeClock.SEASON_SPRING;
		if(month<(div*3)) return TimeClock.SEASON_SUMMER;
		return TimeClock.SEASON_FALL;
	}
	public int getMonth(){return month;}
	public void setMonth(int m){month=m; recalcTime();}

	public int getDayOfMonth(){return day;}
	public void setDayOfMonth(int d){day=d; recalcTime();}
	public int getTimeOfDay(){return hour;}
	public int getTODCode()
	{
		if((hour>=getDawnToDusk()[TimeClock.TIME_NIGHT])&&(getDawnToDusk()[TimeClock.TIME_NIGHT]>=0))
			return TimeClock.TIME_NIGHT;
		if((hour>=getDawnToDusk()[TimeClock.TIME_DUSK])&&(getDawnToDusk()[TimeClock.TIME_DUSK]>=0))
			return TimeClock.TIME_DUSK;
		if((hour>=getDawnToDusk()[TimeClock.TIME_DAY])&&(getDawnToDusk()[TimeClock.TIME_DAY]>=0))
			return TimeClock.TIME_DAY;
		if((hour>=getDawnToDusk()[TimeClock.TIME_DAWN])&&(getDawnToDusk()[TimeClock.TIME_DAWN]>=0))
			return TimeClock.TIME_DAWN;
		// it's before night, dusk, day, and dawn... before dawn is still night.
		if(getDawnToDusk()[TimeClock.TIME_NIGHT]>=0)
			return TimeClock.TIME_NIGHT;
		return TimeClock.TIME_DAY;
	}
	public boolean setTimeOfDay(int t)
	{
		int oldCode=getTODCode();
		hour=t;
		recalcTime();
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

/*	public String deriveEllapsedTimeString(long millis)
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
	} */
	
	public void raiseLowerTheSunEverywhere()
	{
		try
		{
			for(Iterator<Area> a=CMLib.map().areas();a.hasNext();)
			{
				Area A=a.next();
				if(A.getTimeObj()==this)
				for(Room R : A.getProperMap())
				{
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
		hour+=howManyHours;
		//lastTick=System.currentTimeMillis();
		if(howManyHours>0)
		{
			if(hour>hoursInDay)
			{
				day+=hour/hoursInDay;
				hour=hour%hoursInDay;
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
			if(hour<0)
			{
				day+=hour/hoursInDay-1;
				hour=hour%hoursInDay+hoursInDay;
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
	public int tickCounter(){return tickCount;}
	public void tickAct(){}
	public boolean tick(int tickTo)
	{
		//It shouldn't be needed... but 'ideally' if tickCount==0 the clock should recalc current subhour/hour/etc.
		//Eh. The calculation is simple enough, I'll include it.
		if(tickCount==0)
		{
			tickCount=tickTo-1;
			setBaseTime(baseTime, baseYear);
		}
		while(tickCount<tickTo)
		{
			tickCount++;
			if(!doTick()) {tickCount=0; return false;}
		}
		return true;
	}
	protected boolean doTick()
	{
		if(++subHour>=Tickable.TIME_TICKS_PER_MUDHOUR)
		{
			subHour=0;
			if(++hour>=hoursInDay)
			{
				hour=0;
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
	//public long lastTick(){return lastTick;}
	//public long lastAct(){return 0;}
	public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public void destroy()
	{
		//TODO?
		//CMLib.database().deleteObject(this);
	}
	public boolean amDestroyed()
	{
		if(parent!=null)
			return parent.amDestroyed();
		return true;
	}

	public SaveEnum[] totalEnumS(){return SCode.values();}
	public Enum[] headerEnumS(){return new Enum[] {SCode.values()[0]} ;}
	public ModEnum[] totalEnumM(){return MCode.values();}
	public Enum[] headerEnumM(){return new Enum[] {MCode.values()[0]};}
	public int saveNum(){return 0;}
	public void setSaveNum(int num){}
	public boolean needLink(){return false;}
	public void link(){}
	public void saveThis(){if(parent!=null)parent.saveThis();}
	public void prepDefault(){}

	private enum SCode implements SaveEnum<DefaultTimeClock>{
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
		public CMSavable subObject(DefaultTimeClock fromThis){return null;} }
	private enum MCode implements ModEnum<DefaultTimeClock>{
		SUBHOUR(){
			public String brief(DefaultTimeClock E){return ""+E.subHour;}
			public String prompt(DefaultTimeClock E){return ""+E.subHour;}
			public void mod(DefaultTimeClock E, MOB M){E.subHour=CMLib.genEd().intPrompt(M, ""+E.subHour); E.recalcTime();} },
		HOUR(){
			public String brief(DefaultTimeClock E){return ""+E.hour;}
			public String prompt(DefaultTimeClock E){return ""+E.hour;}
			public void mod(DefaultTimeClock E, MOB M){E.hour=CMLib.genEd().intPrompt(M, ""+E.hour); E.recalcTime();} },
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
		; }

}
