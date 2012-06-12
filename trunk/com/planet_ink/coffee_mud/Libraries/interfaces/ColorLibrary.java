package com.planet_ink.coffee_mud.Libraries.interfaces;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Effects.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//TODO in general
public interface ColorLibrary extends CMLibrary
{
	public static final String[] ASCI_COLORS={
		"\033[22;49;30m","\033[22;49;31m","\033[22;49;32m","\033[22;49;33m",
		"\033[22;49;34m","\033[22;49;35m","\033[22;49;36m","\033[22;49;37m",
		"\033[1;49;30m","\033[1;49;31m","\033[1;49;32m","\033[1;49;33m",
		"\033[1;49;34m","\033[1;49;35m","\033[1;49;36m","\033[1;49;37m",
	};
	public static final String[] xTermToANSI=new String[]{
		"0","1","2","3","4","5","6","7","0;1","1;1","2;1","3;1","4;1","5;1","6;1","7;1",
		"0",  "4",  "4",  "4",  "4",  "4",
		"2",  "6",  "4;1","4;1","4;1","4;1",
		"2",  "6",  "6",  "4;1","4;1","4;1",
		"2",  "6",  "6",  "6",  "6",  "6",
		"2",  "2;1","6",  "6",  "6",  "6",
		"2;1","2;1","6;1","6;1","6;1","6;1",
		"1",  "5",  "5",  "5",  "5",  "5",
		"3",  "0;1","7",  "4;1","4;1","4;1",
		"3",  "0;1","6",  "4;1","4;1","4;1",
		"3",  "0;1","6",  "4;1","4;1","4;1",
		"3",  "2;1","6;1","6;1","6;1","6;1",
		"2;1","2;1","2;1","6;1","6;1","6;1",
		"1",  "5",  "5",  "5",  "5",  "5",
		"3",  "5",  "5",  "5",  "5",  "5",
		"3",  "7",  "0;1","4;1","4;1","4;1",
		"3",  "2;1","7",  "6;1","6;1","6;1",
		"3",  "2;1","2;1","6;1","6;1","6;1",
		"3",  "2;1","2;1","2;1","6;1","6;1",
		"1",  "5",  "5",  "5",  "5",  "5",
		"3",  "5",  "5",  "5",  "5",  "5",
		"3",  "3",  "5",  "5",  "5",  "5",
		"3",  "2;1","7",  "7",  "4;1","4;1",
		"3",  "2;1","2;1","6;1","6;1","6;1",
		"3;1","2;1","2;1","2;1","6;1","6;1",
		"1",  "5",  "5",  "5",  "5",  "5",
		"1",  "1;1","1;1","5",  "5",  "5",
		"3",  "1;1","5",  "5",  "5",  "5",
		"3",  "3",  "7",  "5;1","5;1","5;1",
		"3",  "3",  "7",  "7",  "7",  "7",
		"3;1","3;1","2;1","2;1","6;1","6;1",
		"1",  "5;1","5;1","5;1","5;1","5;1",
		"1",  "1;1","1;1","5;1","5;1","5;1",
		"3",  "1;1","1;1","5;1","5;1","5;1",
		"3",  "1;1","1;1","5;1","5;1","5;1",
		"3;1","3;1","3;1","1;1","7;1","7;1",
		"3;1","3;1","3;1","3;1","7;1","7;1",
		"0","0","0","0","0","0","0;1","0;1","0;1","0;1","0;1","0;1",
		"7","7","7","7","7","7","7;1","7;1","7;1","7;1","7;1","7;1"};
	public static final char[] xTermToANSINoBold=new char[]{
		'0','1','2','3','4','5','6','7','7','1','2','3','4','5','6','7',
		'0','4','4','4','4','4',
		'2','6','6','4','4','4',
		'2','6','6','6','4','4',
		'2','6','6','6','6','6',
		'2','2','6','6','6','6',
		'2','2','6','6','6','6',
		'1','5','5','5','5','5',
		'3','7','7','4','4','4',
		'3','7','6','4','4','4',
		'3','2','6','6','6','6',
		'3','2','2','6','6','6',
		'2','2','2','6','6','6',
		'1','5','5','5','5','5',
		'3','5','5','5','5','5',
		'3','7','7','5','5','5',
		'3','2','7','6','6','6',
		'3','2','2','6','6','6',
		'3','2','2','2','6','6',
		'1','5','5','5','5','5',
		'3','5','5','5','5','5',
		'3','3','5','5','5','5',
		'3','2','7','7','6','6',
		'3','3','2','6','6','6',
		'3','2','2','6','6','6',
		'1','5','5','5','5','5',
		'1','1','5','5','5','5',
		'3','1','5','5','5','5',
		'3','3','7','5','5','5',
		'3','3','7','7','7','7',
		'3','3','3','6','6','6',
		'1','5','5','5','5','5',
		'1','1','1','5','5','5',
		'3','1','1','5','5','5',
		'3','1','1','5','5','5',
		'3','3','3','1','7','7',
		'3','3','3','3','7','7',
		'0','0','0','0','0','0','0','0','7','7','7','7',
		'7','7','7','7','7','7','7','7','7','7','7','7'};

	public static final String COLOR_NONE="\033[0m";
	public static final String COLOR_ITALIC="\033[3m";
	public static final String COLOR_UNDERLINE="\033[4m";
	public static final String COLOR_ENDITALIC="\033[23m";
	public static final String COLOR_ENDUNDERLINE="\033[24m";
	/*
	public static final String COLOR_WHITE="\033[1;37m";
	public static final String COLOR_LIGHTGREEN="\033[1;32m";
	public static final String COLOR_LIGHTBLUE="\033[1;34m";
	public static final String COLOR_LIGHTRED="\033[1;31m";
	public static final String COLOR_YELLOW="\033[1;33m";
	public static final String COLOR_LIGHTCYAN="\033[1;36m";
	public static final String COLOR_LIGHTPURPLE="\033[1;35m";
	public static final String COLOR_GREY="\033[0;37m";
	public static final String COLOR_GREEN="\033[0;32m";
	public static final String COLOR_BLUE="\033[0;34m";
	public static final String COLOR_RED="\033[0;31m";
	public static final String COLOR_BROWN="\033[0;33m";
	public static final String COLOR_CYAN="\033[0;36m";
	public static final String COLOR_PURPLE="\033[0;35m";
	public static final String COLOR_DARKGREY="\033[1;30m";
	public static final String COLOR_BLACK="\033[0;30m";
	public static final String COLOR_BOLD="\033[1m";
	public static final String COLOR_UNDERLINE="\033[4m";
	public static final String COLOR_BLINK="\033[5m";
	public static final String COLOR_ITALICS="\033[6m";
	public static final String COLOR_BGWHITE="\033[47m";
	public static final String COLOR_BGGREEN="\033[42m";
	public static final String COLOR_BGBLUE="\033[44m";
	public static final String COLOR_BGRED="\033[41m";
	public static final String COLOR_BGYELLOW="\033[43m";
	public static final String COLOR_BGCYAN="\033[46m";
	public static final String COLOR_BGPURPLE="\033[45m";
	public static final String COLOR_BGBLACK="\033[40m";
	public static final String COLOR_BGDEFAULT="\033[49m";
	
	public static final String HTTAG_WHITE="<FONT COLOR=WHITE";
	public static final String HTTAG_LIGHTGREEN="<FONT COLOR=LIGHTGREEN";
	public static final String HTTAG_LIGHTBLUE="<FONT COLOR=BLUE";
	public static final String HTTAG_LIGHTRED="<FONT COLOR=RED";
	public static final String HTTAG_YELLOW="<FONT COLOR=YELLOW";
	public static final String HTTAG_LIGHTCYAN="<FONT COLOR=CYAN";
	public static final String HTTAG_LIGHTPURPLE="<FONT COLOR=VIOLET";
	public static final String HTTAG_GREY="<FONT COLOR=LIGHTGREY";
	public static final String HTTAG_GREEN="<FONT COLOR=GREEN";
	public static final String HTTAG_BLUE="<FONT COLOR=#000099";
	public static final String HTTAG_RED="<FONT COLOR=#993300";
	public static final String HTTAG_BROWN="<FONT COLOR=#999966";
	public static final String HTTAG_CYAN="<FONT COLOR=DARKCYAN";
	public static final String HTTAG_PURPLE="<FONT COLOR=PURPLE";
	public static final String HTTAG_DARKGREY="<FONT COLOR=GRAY";
	public static final String HTTAG_BLACK="<FONT COLOR=BLACK";
	public static final String HTTAG_NONE="</I></U></BLINK></B></FONT";
	public static final String HTTAG_BOLD="<B";
	public static final String HTTAG_UNDERLINE="<U";
	public static final String HTTAG_BLINK="<BLINK";
	public static final String HTTAG_ITALICS="<I";
	public static final String HTTAG_BGWHITE=" style=\"background-color: white\"";
	public static final String HTTAG_BGGREEN=" style=\"background-color: green\"";
	public static final String HTTAG_BGBLUE=" style=\"background-color: #000099\"";
	public static final String HTTAG_BGRED=" style=\"background-color: #993300\"";
	public static final String HTTAG_BGYELLOW=" style=\"background-color: #999966\"";
	public static final String HTTAG_BGCYAN=" style=\"background-color: darkcyan\"";
	public static final String HTTAG_BGPURPLE=" style=\"background-color: purple\"";
	public static final String HTTAG_BGBLACK=" style=\"background-color: black\"";
	public static final String HTTAG_BGDEFAULT=" style=\"background-color: white\"";
	
	public static final String[] COLOR_CODELETTERSINCARDINALORDER={
		"k","r","g","y","b","p","c","w",null,null
	};
	public static final String[] COLOR_ALLCOLORS={
		COLOR_WHITE,COLOR_LIGHTGREEN,COLOR_LIGHTBLUE,COLOR_LIGHTRED,
		COLOR_YELLOW,COLOR_LIGHTCYAN,COLOR_LIGHTPURPLE,COLOR_GREY,
		COLOR_GREEN,COLOR_BLUE,COLOR_RED,COLOR_BROWN,
		COLOR_CYAN,COLOR_PURPLE,COLOR_DARKGREY,COLOR_BLACK,COLOR_NONE,
		COLOR_BOLD,COLOR_UNDERLINE,COLOR_BLINK,COLOR_ITALICS,
		COLOR_BGWHITE,COLOR_BGGREEN,COLOR_BGBLUE,COLOR_BGRED,
		COLOR_BGYELLOW,COLOR_BGCYAN,COLOR_BGPURPLE,COLOR_BGBLACK,
		COLOR_BGDEFAULT,
		
	};
	public static final String[] COLOR_ALLHTTAGS={
		HTTAG_WHITE,HTTAG_LIGHTGREEN,HTTAG_LIGHTBLUE,HTTAG_LIGHTRED,
		HTTAG_YELLOW,HTTAG_LIGHTCYAN,HTTAG_LIGHTPURPLE,HTTAG_GREY,
		HTTAG_GREEN,HTTAG_BLUE,HTTAG_RED,HTTAG_BROWN,
		HTTAG_CYAN,HTTAG_PURPLE,HTTAG_DARKGREY,HTTAG_BLACK,HTTAG_NONE,
		HTTAG_BOLD,HTTAG_UNDERLINE,HTTAG_BLINK,HTTAG_ITALICS,
		HTTAG_BGWHITE,HTTAG_BGGREEN,HTTAG_BGBLUE,HTTAG_BGRED,
		HTTAG_BGYELLOW,HTTAG_BGCYAN,HTTAG_BGPURPLE,HTTAG_BGBLACK,
		HTTAG_BGDEFAULT
	};
	public static final String[] COLOR_ALLCOLORNAMES={
		"WHITE","LIGHTGREEN","LIGHTBLUE","LIGHTRED",
		"YELLOW","LIGHTCYAN","LIGHTPURPLE","GREY",
		"GREEN","BLUE","RED","BROWN",
		"CYAN","PURPLE","DARKGREY","BLACK","NONE",
		"BOLD","UNDERLINE","BLINK","ITALICS",
		"BGWHITE","BGGREEN","BGBLUE","BGRED",
		"BGYELLOW","BGCYAN","BGPURPLE","BGBLACK",
		"BGDEFAULT"
	};
	
	public static final String[] COLOR_ALLNORMALCOLORCODELETTERS={
		"w","g","b","r",
		"y","c","p","W",
		"G","B","R","Y",
		"C","P","k"
	};
	public static final String[] COLOR_ALLEXTENDEDCOLORCODELETTERS={
		"w","g","b","r",
		"y","c","p","W",
		"G","B","R","Y",
		"C","P","k","K"
	};
	//remaining=aijlnoszAJV
	public static final char COLORCODE_YOU_FIGHT='f';
	public static final char COLORCODE_FIGHT_YOU='e';
	public static final char COLORCODE_FIGHT='F';
	public static final char COLORCODE_SPELL='S';
	public static final char COLORCODE_EMOTE='E';
	public static final char COLORCODE_WEATHER='J';
	public static final char COLORCODE_TALK='T';
	public static final char COLORCODE_TELL='t';
	public static final char COLORCODE_CHANNEL='Q';
	public static final char COLORCODE_CHANNELFORE='q';
	public static final char COLORCODE_IMPORTANT1='x';
	public static final char COLORCODE_IMPORTANT2='X';
	public static final char COLORCODE_IMPORTANT3='Z';
	public static final char COLORCODE_ROOMTITLE='O';
	public static final char COLORCODE_ROOMDESC='L';
	public static final char COLORCODE_DIRECTION='D';
	public static final char COLORCODE_DOORDESC='d';
	public static final char COLORCODE_ITEM='I';
	public static final char COLORCODE_MOB='M';
	public static final char COLORCODE_HITPOINTS='h';
	public static final char COLORCODE_MANA='m';
	public static final char COLORCODE_MOVES='v';
	public static final char COLORCODE_NORMAL='N';
	public static final char COLORCODE_HIGHLIGHT='H';
	public static final char COLORCODE_UNEXPDIRECTION='U';
	public static final char COLORCODE_UNEXPDOORDESC='u';
	public static final char[] COLORCODE_ALLCODES={
		COLORCODE_YOU_FIGHT,COLORCODE_FIGHT_YOU,COLORCODE_FIGHT,COLORCODE_SPELL,
		COLORCODE_EMOTE,COLORCODE_TALK,COLORCODE_TELL,COLORCODE_CHANNEL,
		COLORCODE_CHANNELFORE,COLORCODE_IMPORTANT1,COLORCODE_IMPORTANT2,
		COLORCODE_IMPORTANT3,COLORCODE_ROOMTITLE,COLORCODE_ROOMDESC,
		COLORCODE_DIRECTION,COLORCODE_DOORDESC,COLORCODE_ITEM,COLORCODE_MOB,
		COLORCODE_HITPOINTS,COLORCODE_MANA,COLORCODE_MOVES,COLORCODE_NORMAL,
		COLORCODE_HIGHLIGHT,COLORCODE_UNEXPDIRECTION,COLORCODE_UNEXPDOORDESC,
		COLORCODE_WEATHER
	};
	public static final String[] COLORCODE_ALLCODENAMES={
		"YOU-FIGHT","FIGHT-YOU","FIGHT","SPELL","EMOTE","TALK",
		"TELL","CHANNEL","CHANNELFORE","IMPORTANT1",
		"IMPORTANT2","IMPORTANT3","ROOMTITLE","ROOMDESC",
		"DIRECTION","DOORDESC","ITEM","MOB",
		"HITPOINTS","MANA","MOVES","NORMAL",
		"HIGHLIGHT","UNEXPDIRECTION","UNEXPDOORDESC","WEATHER"
	};

	public static final String COLOR_FR0G3B5="\033[38;5;"+(16+(0*36)+(3*6)+5)+"m";
	public static final String COLOR_BR0G3B5="\033[48;5;"+(16+(0*36)+(3*6)+5)+"m";
	*/
	//public void clearLookups();
	//public int translateSingleCMCodeToANSIOffSet(String code);
	//public String translateCMCodeToANSI(String code);
	//public String translateANSItoCMCode(String code);
	public String mixHTMLCodes(String code1, String code2);
	public String mixColorCodes(String code1, String code2);
	public CMMsg fixSourceFightColor(CMMsg msg);
	//public String[] standardHTMLlookups();
	//public String[] standardColorLookups();
	
}
