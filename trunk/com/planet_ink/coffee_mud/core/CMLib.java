package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.lang.reflect.Modifier;

//import org.mozilla.javascript.Context;
//import org.mozilla.javascript.ScriptableObject;


/*
   Copyright 2000-2010 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
@SuppressWarnings("unchecked")
public class CMLib
{
	static final long serialVersionUID=42;
	public String getClassName(){return "CMLib";}
	private static final Vector mudThreads=new Vector();
	private static CMLib libs=null;
	public CMLib(){
		super();
		if(libs==null) libs=this;
	}
	public static CMLib instance(){return libs;}
	public enum Library
	{
		DATABASE, THREADS, HTTP, LISTER, MONEY, SHOPS,
		COMBAT, HELP, TRACKING, MASKING, CHANNELS,
		COMMANDS, ENGLISH, SLAVERY, FLAGS, OBJBUILDERS,
		SESSIONS, TELNET, XML, SOCIALS, UTENSILS, MAP,
		ENCODER, DICE, TIME, COLOR, LOGIN, MATERIALS,
		LANGUAGE, PLAYERS, TITLES, ABLEPARMS, GENEDITOR;
	}
	private static final EnumSet<Library> LibrarySet=EnumSet.allOf(Library.class);
	private final CMLibrary[] libraries=new CMLibrary[LibrarySet.size()];
	private boolean[] registered=new boolean[LibrarySet.size()];

	public static CMath math(){return CMath.instance();}
	public static CMParms parms(){return CMParms.instance();}
	public static CMStrings strings(){return CMStrings.instance();}
	public static CMClass classes(){return CMClass.instance();}
	public static CMSecurity security(){return CMSecurity.instance();}
	public static Directions directions(){return Directions.instance();}
	public static Log log(){return Log.instance();}
	public static Vector hosts(){return mudThreads;}
	public static MudHost mud(int port){
		if(mudThreads.size()==0)
			return null;
		else
		if(port<=0)
			return (MudHost)mudThreads.firstElement();
		else
		for(int i=0;i<mudThreads.size();i++)
			if(((MudHost)mudThreads.elementAt(i)).getPort()==port)
				return (MudHost)mudThreads.elementAt(i);
		return null;
	}
	public static Resources resources(){return Resources.instance();}
	public static CMProps props(){return CMProps.instance();}
	public static Enumeration libraries(){
		Vector V=new Vector();
		for(int l=0;l<LibrarySet.size();l++)
			if(libs.libraries[l]!=null)
				V.addElement(libs.libraries[l]);
		return V.elements();
	}
	public static CMFile newFile(String currentPath, String filename, boolean pleaseLogErrors)
	{ return new CMFile(currentPath,filename,null,pleaseLogErrors,false); }

	public static DatabaseEngine database(){return (DatabaseEngine)libs.libraries[Library.DATABASE.ordinal()];}
	public static ThreadEngine threads(){return (ThreadEngine)libs.libraries[Library.THREADS.ordinal()];}
	public static ExternalHTTPRequests httpUtils(){return (ExternalHTTPRequests)libs.libraries[Library.HTTP.ordinal()];}
	public static ListingLibrary lister(){return (ListingLibrary)libs.libraries[Library.LISTER.ordinal()];}
	public static MoneyLibrary beanCounter(){return (MoneyLibrary)libs.libraries[Library.MONEY.ordinal()];}
//	public static ShoppingLibrary coffeeShops(){return (ShoppingLibrary)libs.libraries[Library.SHOPS.ordinal()];}
	public static MaterialLibrary materials(){return (MaterialLibrary)libs.libraries[Library.MATERIALS.ordinal()];}
	public static CombatLibrary combat(){return (CombatLibrary)libs.libraries[Library.COMBAT.ordinal()];}
	public static HelpLibrary help(){return (HelpLibrary)libs.libraries[Library.HELP.ordinal()];}
	public static TrackingLibrary tracking(){return (TrackingLibrary)libs.libraries[Library.TRACKING.ordinal()];}
	public static MaskingLibrary masking(){return (MaskingLibrary)libs.libraries[Library.MASKING.ordinal()];}
	public static ChannelsLibrary channels(){return (ChannelsLibrary)libs.libraries[Library.CHANNELS.ordinal()];}
	public static CommonCommands commands(){return (CommonCommands)libs.libraries[Library.COMMANDS.ordinal()];}
	public static EnglishParsing english(){return (EnglishParsing)libs.libraries[Library.ENGLISH.ordinal()];}
//	public static SlaveryLibrary slavery(){return (SlaveryLibrary)libs.libraries[Library.SLAVERY.ordinal()];}
	public static TelnetFilter coffeeFilter(){return (TelnetFilter)libs.libraries[Library.TELNET.ordinal()];}
	public static GenericBuilder coffeeMaker(){return (GenericBuilder)libs.libraries[Library.OBJBUILDERS.ordinal()];}
	public static SessionsList sessions(){return (SessionsList)libs.libraries[Library.SESSIONS.ordinal()];}
	public static CMFlagLibrary flags(){return (CMFlagLibrary)libs.libraries[Library.FLAGS.ordinal()];}
	public static XMLLibrary xml(){return (XMLLibrary)libs.libraries[Library.XML.ordinal()];}
//	public static SocialsList socials(){return (SocialsList)libs.libraries[Library.SOCIALS.ordinal()];}
	public static CMMiscUtils utensils(){return (CMMiscUtils)libs.libraries[Library.UTENSILS.ordinal()];}
	public static WorldMap map(){return (WorldMap)libs.libraries[Library.MAP.ordinal()];}
	public static TextEncoders encoder(){return (TextEncoders)libs.libraries[Library.ENCODER.ordinal()];}
	public static LanguageLibrary lang(){return (LanguageLibrary)libs.libraries[Library.LANGUAGE.ordinal()];}
	public static DiceLibrary dice(){return (DiceLibrary)libs.libraries[Library.DICE.ordinal()];}
	public static TimeManager time(){return (TimeManager)libs.libraries[Library.TIME.ordinal()];}
	public static ColorLibrary color(){return (ColorLibrary)libs.libraries[Library.COLOR.ordinal()];}
	public static CharCreationLibrary login(){return (CharCreationLibrary)libs.libraries[Library.LOGIN.ordinal()];}
	public static PlayerLibrary players(){return (PlayerLibrary)libs.libraries[Library.PLAYERS.ordinal()];}
	public static AutoTitlesLibrary titles(){return (AutoTitlesLibrary)libs.libraries[Library.TITLES.ordinal()];}
//	public static AbilityParameters ableParms(){return (AbilityParameters)libs.libraries[Library.ABLEPARMS.ordinal()];}
	public static GenericEditor genEd(){return (GenericEditor)libs.libraries[Library.GENEDITOR.ordinal()];}

	public static Library convertToLibraryCode(Object O)
	{
		if(O instanceof DatabaseEngine) return Library.DATABASE;
		if(O instanceof ThreadEngine) return Library.THREADS;
		if(O instanceof ExternalHTTPRequests) return Library.HTTP;
		if(O instanceof ListingLibrary) return Library.LISTER;
		if(O instanceof MoneyLibrary) return Library.MONEY;
//		if(O instanceof ShoppingLibrary) return Library.SHOPS;
		if(O instanceof CombatLibrary) return Library.COMBAT;
		if(O instanceof HelpLibrary) return Library.HELP;
		if(O instanceof TrackingLibrary) return Library.TRACKING;
		if(O instanceof LanguageLibrary) return Library.LANGUAGE;
		if(O instanceof MaskingLibrary) return Library.MASKING;
		if(O instanceof ChannelsLibrary) return Library.CHANNELS;
		if(O instanceof CommonCommands) return Library.COMMANDS;
		if(O instanceof EnglishParsing) return Library.ENGLISH;
//		if(O instanceof SlaveryLibrary) return Library.SLAVERY;
		if(O instanceof TelnetFilter) return Library.TELNET;
		if(O instanceof GenericBuilder) return Library.OBJBUILDERS;
		if(O instanceof SessionsList) return Library.SESSIONS;
		if(O instanceof CMFlagLibrary) return Library.FLAGS;
		if(O instanceof XMLLibrary) return Library.XML;
//		if(O instanceof SocialsList) return Library.SOCIALS;
		if(O instanceof CMMiscUtils) return Library.UTENSILS;
		if(O instanceof WorldMap) return Library.MAP;
		if(O instanceof TextEncoders) return Library.ENCODER;
		if(O instanceof DiceLibrary) return Library.DICE;
		if(O instanceof TimeManager) return Library.TIME;
		if(O instanceof ColorLibrary) return Library.COLOR;
		if(O instanceof CharCreationLibrary) return Library.LOGIN;
		if(O instanceof MaterialLibrary) return Library.MATERIALS;
		if(O instanceof PlayerLibrary) return Library.PLAYERS;
		if(O instanceof AutoTitlesLibrary) return Library.TITLES;
//		if(O instanceof AbilityParameters) return Library.ABLEPARMS;
		if(O instanceof GenericEditor) return Library.GENEDITOR;
		return null;
	}

	public static void registerLibrary(CMLibrary O)
	{
		Library code=convertToLibraryCode(O);
		if(code!=null)
		{
			if(libs==null) new CMLib();
			libs.libraries[code.ordinal()]=O;
			libs.registered[code.ordinal()]=true;
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
		CMLib lib=libs;
		for(int l=0;l<lib.libraries.length;l++)
			if(lib.libraries[l]!=null)
				lib.libraries[l].propertiesLoaded();
		//Below two will proooobably be nuked, but not yet
//		RawMaterial.CODES.reset();
	}
	
	public static void activateLibraries() {
		for(Library L : Library.values())
			if(libs.libraries[L.ordinal()]==null)
				Log.errOut("CMLib","Unable to find library "+L.toString());
			else
				libs.libraries[L.ordinal()].activate();
	}
	
	public static CMLibrary library(int lcode) {
		return libs.libraries[lcode];
	}
	
	public static void registerLibraries(Enumeration e)
	{
		for(;e.hasMoreElements();)
			registerLibrary((CMLibrary)e.nextElement());
	}
	
	public static int countRegistered()
	{
		int x=0;
		for(int i=0;i<libs.registered.length;i++)
			if(libs.registered[i]) x++;
		return x;
	}
	public static String unregistered()
	{
		StringBuffer str=new StringBuffer("");
		for(int i=0;i<libs.registered.length;i++)
			if(!libs.registered[i]) str.append(""+i+", ");
		return str.toString();
	}
}
