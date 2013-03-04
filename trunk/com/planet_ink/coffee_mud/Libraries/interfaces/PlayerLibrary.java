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
public interface PlayerLibrary extends CMLibrary, Runnable
{
	public int numPlayers();
	public void addPlayer(MOB newOne);
	public void delPlayer(MOB oneToDel);
	public MOB getPlayer(String calledThis);
//	public MOB getLoadPlayer(String last);
//	public PlayerAccount getLoadAccount(String calledThis);
	public PlayerAccount getAccount(String calledThis);
	public boolean accountExists(String name);
	public Enumeration<MOB> players();
	public void obliteratePlayer(MOB deadMOB, boolean quiet);
	public void obliterateAccountOnly(PlayerAccount deadAccount);
	public boolean playerExists(String name);
	public void forceTick();
	public void unqueuePlayers();
	public void queueAccount(PlayerAccount newOne);
	public void queuePlayer(MOB newOne);
	public boolean swapPlayer(MOB mob, String oldName);
//	public int savePlayers();
//	public Enumeration thinPlayers(String sort, Hashtable cache);
//	public int getCharThinSortCode(String codeName, boolean loose);
//	public String getThinSortValue(ThinPlayer player, int code); 

/*
	public static final String[] CHAR_THIN_SORT_CODES={ "NAME","CLASS","RACE","LEVEL","AGE","LAST","IP"};
	public static final String[] CHAR_THIN_SORT_CODES2={ "CHARACTER","CHARCLASS","RACE","LVL","HOURS","DATE","LASTIP"};
	
	public static final String[] ACCOUNT_THIN_SORT_CODES={ "NAME","LAST","EMAIL","IP","NUMPLAYERS"};
	
	public static class ThinPlayer
	{
		public String name="";
		public String charClass="";
		public String race="";
		public int level=0;
		public int age=0;
		public long last=0;
		public String ip="";
	}
	
	public static class ThinnerPlayer
	{
		public String name="";
		public String password="";
		public String accountName="";
		public MOB loadedMOB=null;
	}
*/
}
