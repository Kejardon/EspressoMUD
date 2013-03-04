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
public interface CharCreationLibrary extends CMLibrary
{
	// mob is optional
	public ArrayList<Race> raceQualifies();
	//public boolean isOkName(String login);
	public void reloadTerminal(MOB mob);
	public void notifyFriends(MOB mob, String message);
	public LoginResult createCharacter(PlayerAccount acct, String login, Session session) throws java.io.IOException;
	public LoginResult login(Session session) throws java.io.IOException;
	public LoginResult selectAccountCharacter(PlayerAccount acct, Session session) throws java.io.IOException;
	public void pageRooms(CMProps page, HashMap<String, Integer> table, String start);
	public void initStartRooms(CMProps page);
	public void initDeathRooms(CMProps page);
	public void initBodyRooms(CMProps page);
	public Room getDefaultStartRoom(MOB mob);
	public Room getDefaultDeathRoom(MOB mob);
	public Room getDefaultBodyRoom(MOB mob);

//	public final static String DEFAULT_BADNAMES = " LIST DELETE QUIT NEW HERE YOU SHIT FUCK CUNT ALL FAGGOT ASSHOLE ARSEHOLE PUSSY COCK SLUT BITCH DAMN CRAP GOD JESUS CHRIST NOBODY SOMEBODY MESSIAH ADMIN SYSOP ";

	public enum LoginResult
	{
		NO_LOGIN, NORMAL_LOGIN, ACCOUNT_LOGIN, SESSION_SWAP, CCREATION_EXIT
	}
}
