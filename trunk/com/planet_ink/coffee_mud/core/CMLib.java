package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.core.database.DBManager;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;

import java.util.*;
import java.lang.reflect.Modifier;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class CMLib
{
	//TODO: Shutdown Restart stuff
	public String getClassName(){return "CMLib";}
	public enum Library
	{
		//SHOPS, SLAVERY, SOCIALS, ENCODER, TITLES, ABLEPARMS
		DATABASE, THREADS, HTTP, LISTER, MONEY,
		COMBAT, HELP, TRACKING, MASKING, CHANNELS,
		COMMANDS, ENGLISH, FLAGS, OBJBUILDERS,
		SESSIONS, TELNET, XML, UTENSILS, MAP,
		DICE, TIME, COLOR, LOGIN, MATERIALS,
		LANGUAGE, PLAYERS, GENEDITOR, MISC;
	}
	/* Current known init order:
	Database, HTTP, Threads,
	*/
	private static final EnumSet<Library> LibrarySet=EnumSet.allOf(Library.class);
	private static final CMLibrary[] libraries=new CMLibrary[LibrarySet.size()];
	private static boolean[] registered=new boolean[LibrarySet.size()];

	public static CMath math(){return CMath.instance();}
	public static CMParms parms(){return CMParms.instance();}
	public static CMStrings strings(){return CMStrings.instance();}
	public static CMClass classes(){return CMClass.instance();}
	public static CMSecurity security(){return CMSecurity.instance();}
	public static Log log(){return Log.instance();}
	public static Resources resources(){return Resources.instance();}
	public static CMProps props(){return CMProps.instance();}
	public static Iterator<CMLibrary> libraries(){
		ArrayList V=new ArrayList();
		for(int l=0;l<LibrarySet.size();l++)
			if(libraries[l]!=null)
				V.add(libraries[l]);
		return V.iterator();
	}

	public static DBManager database(){return (DBManager)libraries[Library.DATABASE.ordinal()];}
	public static ServiceEngine threads(){return (ServiceEngine)libraries[Library.THREADS.ordinal()];}
	public static ExternalHTTPRequests httpUtils(){return (ExternalHTTPRequests)libraries[Library.HTTP.ordinal()];}
	public static CMLister lister(){return (CMLister)libraries[Library.LISTER.ordinal()];}
	public static BeanCounter beanCounter(){return (BeanCounter)libraries[Library.MONEY.ordinal()];}
	public static RawCMaterial materials(){return (RawCMaterial)libraries[Library.MATERIALS.ordinal()];}
	public static MUDFight combat(){return (MUDFight)libraries[Library.COMBAT.ordinal()];}
	public static MUDHelp help(){return (MUDHelp)libraries[Library.HELP.ordinal()];}
	public static MUDTracker tracking(){return (MUDTracker)libraries[Library.TRACKING.ordinal()];}
	public static MUDZapper masking(){return (MUDZapper)libraries[Library.MASKING.ordinal()];}
	public static CMChannels channels(){return (CMChannels)libraries[Library.CHANNELS.ordinal()];}
	public static CommonMsgs commands(){return (CommonMsgs)libraries[Library.COMMANDS.ordinal()];}
	public static EnglishParser english(){return (EnglishParser)libraries[Library.ENGLISH.ordinal()];}
	public static CoffeeFilter coffeeFilter(){return (CoffeeFilter)libraries[Library.TELNET.ordinal()];}
	public static CoffeeMaker coffeeMaker(){return (CoffeeMaker)libraries[Library.OBJBUILDERS.ordinal()];}
	public static Sessions sessions(){return (Sessions)libraries[Library.SESSIONS.ordinal()];}
	public static Sense flags(){return (Sense)libraries[Library.FLAGS.ordinal()];}
	public static XMLManager xml(){return (XMLManager)libraries[Library.XML.ordinal()];}
	public static CoffeeUtensils utensils(){return (CoffeeUtensils)libraries[Library.UTENSILS.ordinal()];}
	public static CMMap map(){return (CMMap)libraries[Library.MAP.ordinal()];}
	public static DirtyLanguage lang(){return (DirtyLanguage)libraries[Library.LANGUAGE.ordinal()];}
	public static Dice dice(){return (Dice)libraries[Library.DICE.ordinal()];}
	public static CoffeeTime time(){return (CoffeeTime)libraries[Library.TIME.ordinal()];}
	public static CMColor color(){return (CMColor)libraries[Library.COLOR.ordinal()];}
	public static CharCreation login(){return (CharCreation)libraries[Library.LOGIN.ordinal()];}
	public static CMPlayers players(){return (CMPlayers)libraries[Library.PLAYERS.ordinal()];}
	public static CMGenEditor genEd(){return (CMGenEditor)libraries[Library.GENEDITOR.ordinal()];}
	public static MiscLib misc(){return (MiscLib)libraries[Library.MISC.ordinal()];}

	public static Library convertToLibraryCode(Object O)
	{
		if(O instanceof DBManager) return Library.DATABASE;
		if(O instanceof ServiceEngine) return Library.THREADS;
		if(O instanceof ExternalHTTPRequests) return Library.HTTP;
		if(O instanceof CMLister) return Library.LISTER;
		if(O instanceof BeanCounter) return Library.MONEY;
		if(O instanceof MUDFight) return Library.COMBAT;
		if(O instanceof MUDHelp) return Library.HELP;
		if(O instanceof MUDTracker) return Library.TRACKING;
		if(O instanceof DirtyLanguage) return Library.LANGUAGE;
		if(O instanceof MUDZapper) return Library.MASKING;
		if(O instanceof CMChannels) return Library.CHANNELS;
		if(O instanceof CommonMsgs) return Library.COMMANDS;
		if(O instanceof EnglishParser) return Library.ENGLISH;
		if(O instanceof CoffeeFilter) return Library.TELNET;
		if(O instanceof CoffeeMaker) return Library.OBJBUILDERS;
		if(O instanceof Sessions) return Library.SESSIONS;
		if(O instanceof Sense) return Library.FLAGS;
		if(O instanceof XMLManager) return Library.XML;
		if(O instanceof CoffeeUtensils) return Library.UTENSILS;
		if(O instanceof CMMap) return Library.MAP;
		if(O instanceof Dice) return Library.DICE;
		if(O instanceof CoffeeTime) return Library.TIME;
		if(O instanceof CMColor) return Library.COLOR;
		if(O instanceof CharCreation) return Library.LOGIN;
		if(O instanceof RawCMaterial) return Library.MATERIALS;
		if(O instanceof CMPlayers) return Library.PLAYERS;
		if(O instanceof CMGenEditor) return Library.GENEDITOR;
		if(O instanceof MiscLib) return Library.MISC;
		return null;
	}

	public static void registerLibrary(CMLibrary O)
	{
		Library code=convertToLibraryCode(O);
		if(code!=null)
		{
			libraries[code.ordinal()]=O;
			registered[code.ordinal()]=true;
		}
	}
	@SuppressWarnings("deprecation")
	public static void killThread(Thread t, long sleepTime, int attempts)
	{
		try{
			if(t==null) return;
			t.interrupt();
			try{Thread.sleep(sleepTime);}catch(Exception e){}
			int att=0;
			while((att<attempts)&&t.isAlive())try{att++;Thread.sleep(sleepTime);}catch(Exception e){}
			try {if(t.isAlive()) { try { t.stop();}catch(Throwable tx){} } }catch(java.lang.ThreadDeath td) {}
		}
		catch(Throwable th){}
	}
	
	public static boolean s_sleep(long millis) {
		try{ Thread.sleep(millis); } catch(java.lang.InterruptedException ex) { return false;}
		return true;
	}

	public static void propertiesLoaded() {
		for(int l=0;l<libraries.length;l++)
			if(libraries[l]!=null)
				libraries[l].propertiesLoaded();
	}
	
	public static void activateLibraries() {
		for(Library L : Library.values())
			if(libraries[L.ordinal()]==null)
				Log.errOut("CMLib","Unable to find library "+L.toString());
			else
				libraries[L.ordinal()].activate();
	}
	
	public static void finalInitialize() {
		for(Library L : Library.values())
			if(libraries[L.ordinal()]==null)
				Log.errOut("CMLib","Unable to find library "+L.toString());
			else
				libraries[L.ordinal()].finalInitialize();
	}
	
	public static CMLibrary library(int lcode) {
		return libraries[lcode];
	}
	
	public static void registerLibraries(Enumeration e)
	{
		for(;e.hasMoreElements();)
			registerLibrary((CMLibrary)e.nextElement());
	}
	
	public static int countRegistered()
	{
		int x=0;
		for(int i=0;i<registered.length;i++)
			if(registered[i]) x++;
		return x;
	}
	public static String unregistered()
	{
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<registered.length;i++)
			if(!registered[i]) str.append(""+i+", ");
		return str.toString();
	}
}