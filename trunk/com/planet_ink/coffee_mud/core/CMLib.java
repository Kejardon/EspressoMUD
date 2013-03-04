package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

import java.util.*;
import java.lang.reflect.Modifier;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
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

	public static DatabaseEngine database(){return (DatabaseEngine)libraries[Library.DATABASE.ordinal()];}
	public static ThreadEngine threads(){return (ThreadEngine)libraries[Library.THREADS.ordinal()];}
	public static ExternalHTTPRequests httpUtils(){return (ExternalHTTPRequests)libraries[Library.HTTP.ordinal()];}
	public static ListingLibrary lister(){return (ListingLibrary)libraries[Library.LISTER.ordinal()];}
	public static MoneyLibrary beanCounter(){return (MoneyLibrary)libraries[Library.MONEY.ordinal()];}
	public static MaterialLibrary materials(){return (MaterialLibrary)libraries[Library.MATERIALS.ordinal()];}
	public static CombatLibrary combat(){return (CombatLibrary)libraries[Library.COMBAT.ordinal()];}
	public static HelpLibrary help(){return (HelpLibrary)libraries[Library.HELP.ordinal()];}
	public static TrackingLibrary tracking(){return (TrackingLibrary)libraries[Library.TRACKING.ordinal()];}
	public static MaskingLibrary masking(){return (MaskingLibrary)libraries[Library.MASKING.ordinal()];}
	public static ChannelsLibrary channels(){return (ChannelsLibrary)libraries[Library.CHANNELS.ordinal()];}
	public static CommonCommands commands(){return (CommonCommands)libraries[Library.COMMANDS.ordinal()];}
	public static EnglishParsing english(){return (EnglishParsing)libraries[Library.ENGLISH.ordinal()];}
	public static TelnetFilter coffeeFilter(){return (TelnetFilter)libraries[Library.TELNET.ordinal()];}
	public static GenericBuilder coffeeMaker(){return (GenericBuilder)libraries[Library.OBJBUILDERS.ordinal()];}
	public static SessionsList sessions(){return (SessionsList)libraries[Library.SESSIONS.ordinal()];}
	public static CMFlagLibrary flags(){return (CMFlagLibrary)libraries[Library.FLAGS.ordinal()];}
	public static XMLLibrary xml(){return (XMLLibrary)libraries[Library.XML.ordinal()];}
	public static CMMiscUtils utensils(){return (CMMiscUtils)libraries[Library.UTENSILS.ordinal()];}
	public static WorldMap map(){return (WorldMap)libraries[Library.MAP.ordinal()];}
	public static LanguageLibrary lang(){return (LanguageLibrary)libraries[Library.LANGUAGE.ordinal()];}
	public static DiceLibrary dice(){return (DiceLibrary)libraries[Library.DICE.ordinal()];}
	public static TimeManager time(){return (TimeManager)libraries[Library.TIME.ordinal()];}
	public static ColorLibrary color(){return (ColorLibrary)libraries[Library.COLOR.ordinal()];}
	public static CharCreationLibrary login(){return (CharCreationLibrary)libraries[Library.LOGIN.ordinal()];}
	public static PlayerLibrary players(){return (PlayerLibrary)libraries[Library.PLAYERS.ordinal()];}
	public static GenericEditor genEd(){return (GenericEditor)libraries[Library.GENEDITOR.ordinal()];}
	public static MiscLibrary misc(){return (MiscLibrary)libraries[Library.MISC.ordinal()];}

	public static Library convertToLibraryCode(Object O)
	{
		if(O instanceof DatabaseEngine) return Library.DATABASE;
		if(O instanceof ThreadEngine) return Library.THREADS;
		if(O instanceof ExternalHTTPRequests) return Library.HTTP;
		if(O instanceof ListingLibrary) return Library.LISTER;
		if(O instanceof MoneyLibrary) return Library.MONEY;
		if(O instanceof CombatLibrary) return Library.COMBAT;
		if(O instanceof HelpLibrary) return Library.HELP;
		if(O instanceof TrackingLibrary) return Library.TRACKING;
		if(O instanceof LanguageLibrary) return Library.LANGUAGE;
		if(O instanceof MaskingLibrary) return Library.MASKING;
		if(O instanceof ChannelsLibrary) return Library.CHANNELS;
		if(O instanceof CommonCommands) return Library.COMMANDS;
		if(O instanceof EnglishParsing) return Library.ENGLISH;
		if(O instanceof TelnetFilter) return Library.TELNET;
		if(O instanceof GenericBuilder) return Library.OBJBUILDERS;
		if(O instanceof SessionsList) return Library.SESSIONS;
		if(O instanceof CMFlagLibrary) return Library.FLAGS;
		if(O instanceof XMLLibrary) return Library.XML;
		if(O instanceof CMMiscUtils) return Library.UTENSILS;
		if(O instanceof WorldMap) return Library.MAP;
		if(O instanceof DiceLibrary) return Library.DICE;
		if(O instanceof TimeManager) return Library.TIME;
		if(O instanceof ColorLibrary) return Library.COLOR;
		if(O instanceof CharCreationLibrary) return Library.LOGIN;
		if(O instanceof MaterialLibrary) return Library.MATERIALS;
		if(O instanceof PlayerLibrary) return Library.PLAYERS;
		if(O instanceof GenericEditor) return Library.GENEDITOR;
		if(O instanceof MiscLibrary) return Library.MISC;
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