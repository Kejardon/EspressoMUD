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
//TODO later. Need to revamp this

public class CMColor extends StdLibrary
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

	@Override public String ID(){return "CMColor";}
	
	//public String[] clookup=null;
	public String[] htlookup=null;

	public String mixHTMLCodes(String code1, String code2)
	{
		String html=null;
		if((code1==null)||(code1.length()==0))
			html=code2;
		else
		if((code2==null)||(code2.length()==0)) 
			html=code1;
		else
		if(code1.startsWith(" ")&&(code2.startsWith("<FONT")))
			html=code2+code1;
		else
		if(code2.startsWith(" ")&&(code1.startsWith("<FONT")))
			html=code1+code2;
		else
		if(code1.startsWith("<")&&(code2.startsWith("<")))
			html=code1+">"+code2;
		else
		if(!code1.startsWith("<"))
			html=code2;
		else
			html=code1;
		if(html.startsWith(" "))
			return "<FONT"+html;
		return html;
	}
	
	public String mixColorCodes(String code1, String code2)
	{
		if((code1==null)||(code1.length()==0)) return code2;
		if((code2==null)||(code2.length()==0)) return code1;
		if(code1.charAt(code1.length()-1)!=code2.charAt(code2.length()-1))
			return code1+code2;
		if(code2.startsWith("\033["))code2=code2.substring("\033[".length());
		return code1.substring(0,code1.length()-1)+";"+code2;
	}
	
	public CMMsg fixSourceFightColor(CMMsg msg)
	{
		if(msg.sourceMessage()!=null)
			msg.setSourceMessage(msg.sourceMessage().replace("^F","^f"));
		if(msg.targetMessage()!=null)
			msg.setTargetMessage(msg.targetMessage().replace("^F","^e"));
		return msg;
	}

	//All available options that currently default:
	// !"#$%&'()*+,-/89:;<=>
	//@ABCGKPRVY]_
	//'abgjklnprz{|}~
	public static final char COLORCODE_CHANNEL='Q';
	public static final char COLORCODE_CHANNELFORE='q';
	public static final char COLORCODE_IMPORTANT1='x';
	public static final char COLORCODE_IMPORTANT2='X';
	public static final char COLORCODE_IMPORTANT3='Z';
	public static final char COLORCODE_ROOMTITLE='O';
	public static final char COLORCODE_ROOMDESC='L';
	public static final char COLORCODE_DIRECTION='D';
	public static final char COLORCODE_DOORDESC='d';
	public static final char COLORCODE_ITEM='o'; //I
	public static final char COLORCODE_MOB='M';
	public static final char COLORCODE_HITPOINTS='h';
	public static final char COLORCODE_MANA='m';
	public static final char COLORCODE_MOVES='v';
	public static final char COLORCODE_NORMAL='N';
	public static final char COLORCODE_HIGHLIGHT='H';
	public static final char COLORCODE_UNEXPDIRECTION='W'; //U
	public static final char COLORCODE_UNEXPDOORDESC='w'; //u
	/*
	public static final class ColorCode
	{
		final char ColorCode;
		final int ColorIndex;
		final String DefaultColorString;
	}*/
	protected static final ColorCode[] byIndex=new ColorCode[25];
	protected static final ColorCode[] byChar=new ColorCode[128-32];
	public static enum ColorCode
	{
		NORMAL('N',0,"0;37"), //Grey
		YOU_FIGHT('f',1,"1;35"), //Light purple
		FIGHT_YOU('F',2,"1;31"), //Light red. Was 'e'
		FIGHT('c',3,"22;31"), //Red. Was 'F'
		SPELL('S',4,"1;33"), //Yellow
		EMOTE('E',5,"1;35"), //Light purple
		WEATHER('J',6,"1;37"), //White
		TALK('T',7,"1;34"), //Light blue
		TELL('t',8,"22;36"), //Cyan
		CHANNEL('Q',9,"1;36;44"), //Light cyan on blue
		CHANNELFORE('q',10,"1;36"), //Light cyan
		IMPORTANT1('x',11,"1;36;44"), //Light cyan on blue
		IMPORTANT2('X',12,"1;33;44"), //Yellow on blue
		IMPORTANT3('Z',13,"1;33;41"), //Yellow on red
		ROOMTITLE('O',14,"1;36"), //Light cyan
		ROOMDESC('L',15,"1;37"), //White
		DIRECTION('D',16,"1;36;44"), //Light cyan on blue
		DOORDESC('d',17,"1;34"), //Light blue
		ITEM('o',18,"1;32"), //Light green. Was 'I'
		MOB('M',19,"1;35"), //Light purple
		HITPOINTS('h',20,"1;36"), //Light cyan
		MANA('m',21,"1;36"), //Light cyan
		MOVES('v',22,"1;36"), //Light cyan
		UNEXPDIRECTION('W',23,"22;36;44"), //Cyan on blue. Was 'U'
		UNEXPDOORDESC('w',24,"1;34"), //Light blue. Was 'u'
		;
		public final char Code;
		public final int Index;
		public final String DefaultString;
		public final String DefaultWholeString;
		
		ColorCode(char c, int i, String s)
		{
			Code=c;Index=i;DefaultString=s;
			DefaultWholeString="\033["+s+"m";
			if(Index>=byIndex.length) Log.errOut("CMColor","byIndex array too short for "+Index);
			else byIndex[Index]=this;
			int charIndex=c-32;
			if(charIndex>=0 && charIndex<96) byChar[charIndex]=this;
		}
		
		public static ColorCode get(int i){return byIndex[i];}
		public static ColorCode get(char c)
		{
			int charIndex=c-32;
			if(charIndex>=0 && charIndex<96) return byChar[charIndex];
			return null;
		}
	};
	public static ColorCode getCC(char c)
	{
		return ColorCode.get(c);
	}
	
	/*public String[] standardHTMLlookups()
	{
		if(htlookup==null)
		{
			htlookup=new String[256];
			
			htlookup['!']=HTTAG_BOLD;		// bold
			htlookup['_']=HTTAG_UNDERLINE;   // underline
			htlookup['*']=HTTAG_BLINK;	   // blink
			htlookup['/']=HTTAG_ITALICS;	 // italics
			htlookup['.']=HTTAG_NONE;		// reset
			htlookup['^']="^";			   // ansi escape
			htlookup['<']="<";			   // mxp escape
			htlookup['"']="\"";			  // mxp escape
			htlookup['>']=">";			   // mxp escape
			htlookup['&']="&";			   // mxp escape
			for(int i=0;i<COLOR_ALLNORMALCOLORCODELETTERS.length;i++)
				htlookup[COLOR_ALLNORMALCOLORCODELETTERS[i].charAt(0)]=COLOR_ALLHTTAGS[i];
			
			// default color settings:
			htlookup[COLORCODE_HIGHLIGHT]=HTTAG_LIGHTCYAN;
			htlookup[COLORCODE_YOU_FIGHT]=HTTAG_LIGHTPURPLE;
			htlookup[COLORCODE_FIGHT_YOU]=HTTAG_LIGHTRED;
			htlookup[COLORCODE_FIGHT]=HTTAG_RED;
			htlookup[COLORCODE_SPELL]=HTTAG_YELLOW;
			htlookup[COLORCODE_EMOTE]=HTTAG_LIGHTPURPLE;
			htlookup[COLORCODE_WEATHER]=HTTAG_WHITE;
			htlookup[COLORCODE_TALK]=HTTAG_LIGHTBLUE;
			htlookup[COLORCODE_TELL]=HTTAG_CYAN;
			htlookup[COLORCODE_CHANNEL]=mixHTMLCodes(HTTAG_LIGHTCYAN,HTTAG_BGBLUE);
			htlookup[COLORCODE_CHANNELFORE]=HTTAG_LIGHTCYAN;
			htlookup[COLORCODE_IMPORTANT1]=mixHTMLCodes(HTTAG_LIGHTCYAN,HTTAG_BGBLUE);
			htlookup[COLORCODE_IMPORTANT2]=mixHTMLCodes(HTTAG_YELLOW,HTTAG_BGBLUE);
			htlookup[COLORCODE_IMPORTANT3]=mixHTMLCodes(HTTAG_YELLOW,HTTAG_BGRED);
			htlookup[COLORCODE_ROOMTITLE]=HTTAG_LIGHTCYAN;
			htlookup[COLORCODE_ROOMDESC]=HTTAG_WHITE;
			htlookup[COLORCODE_DIRECTION]=mixHTMLCodes(HTTAG_LIGHTCYAN,HTTAG_BGBLUE);
			htlookup[COLORCODE_DOORDESC]=HTTAG_LIGHTBLUE;
			htlookup[COLORCODE_ITEM]=HTTAG_LIGHTGREEN;
			htlookup[COLORCODE_MOB]=HTTAG_LIGHTPURPLE;
			htlookup[COLORCODE_HITPOINTS]=HTTAG_LIGHTCYAN;
			htlookup[COLORCODE_MANA]=HTTAG_LIGHTCYAN;
			htlookup[COLORCODE_MOVES]=HTTAG_LIGHTCYAN;
			htlookup[COLORCODE_UNEXPDIRECTION]=mixHTMLCodes(HTTAG_CYAN,HTTAG_BGBLUE);
			htlookup[COLORCODE_UNEXPDOORDESC]=HTTAG_LIGHTBLUE;
			Vector schemeSettings=CMParms.parseCommas(CMProps.Strings.COLORSCHEME.property(),true);
			for(int i=0;i<schemeSettings.size();i++)
			{
				String s=(String)schemeSettings.get(i);
				int x=s.indexOf("=");
				if(x>0)
				{
					String key=s.substring(0,x).trim();
					String value=s.substring(x+1).trim();
					char codeChar=' ';
					for(int ii=0;ii<COLORCODE_ALLCODENAMES.length;ii++)
						if(key.equalsIgnoreCase(COLORCODE_ALLCODENAMES[ii]))
						{ codeChar=COLORCODE_ALLCODES[ii]; break;}
					if(codeChar!=' ')
					{
						String newVal=null;
						String addColor=null;
						String addCode=null;
						while(value.length()>0)
						{
							x=value.indexOf("+");
							if(x<0)
							{
								addColor=value;
								value="";
							}
							else
							{
								addColor=value.substring(0,x).trim();
								value=value.substring(x+1).trim();
							}
							addCode=null;
							for(int ii=0;ii<COLOR_ALLCOLORNAMES.length;ii++)
								if(addColor.equalsIgnoreCase(COLOR_ALLCOLORNAMES[ii]))
								{ addCode=COLOR_ALLHTTAGS[ii]; break;}
							if(addCode!=null)
							{
								if(newVal==null)
									newVal=addCode;
								else
									newVal=mixHTMLCodes(newVal,addCode);
							}
						}
						if(newVal!=null)
							htlookup[codeChar]=newVal;
					}
				}
			}

			for(int i=0;i<htlookup.length;i++)
			{
				String s=htlookup[i];
				if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
					htlookup[i]=htlookup[s.charAt(1)];
			}
			htlookup[COLORCODE_NORMAL]=HTTAG_NONE;
		}
		return htlookup;
	}
	public void clearLookups(){clookup=null;}
	public String[] standardColorLookups()
	{
		if(clookup==null)
		{
			clookup=new String[256];
			clookup['!']=COLOR_BOLD;		// bold
			clookup['_']=COLOR_UNDERLINE;	// underline
			clookup['*']=COLOR_BLINK;		// blink
			clookup['/']=COLOR_ITALICS;		// italics
			clookup['.']=COLOR_NONE;		// reset
			clookup['^']="^";				// ansi escape
			clookup['<']="<";				// mxp escape
			clookup['"']="\"";				// mxp escape
			clookup['>']=">";				// mxp escape
			clookup['&']="&";				// mxp escape
			clookup['@']=null;				// ** special 256 color code
			for(int i=0;i<COLOR_ALLNORMALCOLORCODELETTERS.length;i++)
				clookup[COLOR_ALLNORMALCOLORCODELETTERS[i].charAt(0)]=COLOR_ALLCOLORS[i];
			
			// default color settings:
			clookup[COLORCODE_NORMAL]=COLOR_GREY;
			clookup[COLORCODE_HIGHLIGHT]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_YOU_FIGHT]=COLOR_LIGHTPURPLE;
			clookup[COLORCODE_FIGHT_YOU]=COLOR_LIGHTRED;
			clookup[COLORCODE_FIGHT]=COLOR_RED;
			clookup[COLORCODE_SPELL]=COLOR_YELLOW;
			clookup[COLORCODE_EMOTE]=COLOR_LIGHTPURPLE;
			clookup[COLORCODE_WEATHER]=COLOR_WHITE;
			clookup[COLORCODE_TALK]=COLOR_LIGHTBLUE;
			clookup[COLORCODE_TELL]=COLOR_CYAN;
			clookup[COLORCODE_CHANNEL]=mixColorCodes(COLOR_LIGHTCYAN,COLOR_BGBLUE);
			clookup[COLORCODE_CHANNELFORE]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_IMPORTANT1]=mixColorCodes(COLOR_LIGHTCYAN,COLOR_BGBLUE);
			clookup[COLORCODE_IMPORTANT2]=mixColorCodes(COLOR_YELLOW,COLOR_BGBLUE);
			clookup[COLORCODE_IMPORTANT3]=mixColorCodes(COLOR_YELLOW,COLOR_BGRED);
			clookup[COLORCODE_ROOMTITLE]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_ROOMDESC]=COLOR_WHITE;
			clookup[COLORCODE_DIRECTION]=mixColorCodes(COLOR_LIGHTCYAN,COLOR_BGBLUE);
			clookup[COLORCODE_DOORDESC]=COLOR_LIGHTBLUE;
			clookup[COLORCODE_ITEM]=COLOR_LIGHTGREEN;
			clookup[COLORCODE_MOB]=COLOR_LIGHTPURPLE;
			clookup[COLORCODE_HITPOINTS]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_MANA]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_MOVES]=COLOR_LIGHTCYAN;
			clookup[COLORCODE_UNEXPDIRECTION]=mixColorCodes(COLOR_CYAN,COLOR_BGBLUE);
			clookup[COLORCODE_UNEXPDOORDESC]=COLOR_LIGHTBLUE;
			Vector schemeSettings=CMParms.parseCommas(CMProps.Strings.COLORSCHEME.property(),true);
			for(int i=0;i<schemeSettings.size();i++)
			{
				String s=(String)schemeSettings.get(i);
				int x=s.indexOf("=");
				if(x>0)
				{
					String key=s.substring(0,x).trim();
					String value=s.substring(x+1).trim();
					char codeChar=' ';
					for(int ii=0;ii<COLORCODE_ALLCODENAMES.length;ii++)
						if(key.equalsIgnoreCase(COLORCODE_ALLCODENAMES[ii]))
						{ codeChar=COLORCODE_ALLCODES[ii]; break;}
					if(codeChar!=' ')
					{
						String newVal=null;
						String addColor=null;
						String addCode=null;
						while(value.length()>0)
						{
							x=value.indexOf("+");
							if(x<0)
							{
								addColor=value;
								value="";
							}
							else
							{
								addColor=value.substring(0,x).trim();
								value=value.substring(x+1).trim();
							}
							addCode=null;
							for(int ii=0;ii<COLOR_ALLCOLORNAMES.length;ii++)
								if(addColor.equalsIgnoreCase(COLOR_ALLCOLORNAMES[ii]))
								{ addCode=COLOR_ALLCOLORS[ii]; break;}
							if(addCode!=null)
							{
								if(newVal==null)
									newVal=addCode;
								else
									newVal=mixColorCodes(newVal,addCode);
							}
						}
						if(newVal!=null)
							clookup[codeChar]=newVal;
					}
				}
			}
			
				

			for(int i=0;i<clookup.length;i++)
			{
				String s=clookup[i];
				if((s!=null)&&(s.startsWith("^"))&&(s.length()>1))
					clookup[i]=clookup[s.charAt(1)];
			}
		}
		return clookup;
	}
	public int translateSingleCMCodeToANSIOffSet(char code)
	{
		return "krgybpcw".indexOf(code);
	}
	public String translateCMCodeToANSI(String code)
	{
		if(code.length()==0) return code;
		if(!code.startsWith("^")) return code;
		int background=code.indexOf("|");
		int bold=0;
		for(int i=0;i<code.length();i++)
			if(Character.isLowerCase(code.charAt(i)))
			{
				bold=1;
				break;
			}
		if(background>0)
			return "\033["+(40+translateSingleCMCodeToANSIOffSet(code.charAt(1)))+";"+bold+";"+(30+translateSingleCMCodeToANSIOffSet(code.substring(background+1)))+"m";
		return "\033["+bold+";"+(30+translateSingleCMCodeToANSIOffSet(code.charAt(1)))+"m";
	}
	public String translateANSItoCMCode(String code)
	{
		if(code.length()==0) return code;
		if(code.indexOf("^")==0) return code;
		if(code.indexOf("|")>0) return code;
		String code1=null;
		String code2=null;
		boolean bold=(code.indexOf(";1;")>0)||(code.indexOf("[1;")>0);
		for(int i=0;i<COLOR_CODELETTERSINCARDINALORDER.length;i++)
		{
			if((code1==null)&&(code.indexOf(""+(40+i))>0))
				code1="^"+Character.toUpperCase(COLOR_CODELETTERSINCARDINALORDER[i].charAt(0));
			if((code2==null)&&(code.indexOf(""+(30+i))>0))
				code2="^"+(bold?COLOR_CODELETTERSINCARDINALORDER[i]:(""+Character.toUpperCase(COLOR_CODELETTERSINCARDINALORDER[i].charAt(0))));
		}
		if((code1!=null)&&(code2!=null))
			return code1+"|"+code2;
		else
		if((code1==null)&&(code2!=null))
			return code2;
		else
		if((code1!=null)&&(code2==null))
			return code1;
		else
			return "^W";
	}*/
}