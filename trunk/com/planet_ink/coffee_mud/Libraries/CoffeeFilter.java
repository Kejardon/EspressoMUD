package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class CoffeeFilter extends StdLibrary implements TelnetFilter
{
	public String ID(){return "CoffeeFilter";}
	public Hashtable<String, Integer> tagTable=null;
	
	public Hashtable<String, Integer> getTagTable()
	{
		if(tagTable==null)
			synchronized(this){if(tagTable==null) tagTable=CMStrings.makeNumericHash(TelnetFilter.FILTER_DESCS);}
		return tagTable;
	}
	
	public String toRawString(String msg)
	{
		char[] output=msg.toCharArray();
		int out=0;
		for(int i=0;i<output.length;i++)
		{
			if(output[i]=='^')
			{
				if(++i==output.length) break;
				switch(output[i])
				{
					case '\\':
						output[out++]='\r';
						output[out++]='\n';
						break;
					case '^':
						output[out++]='^';
						break;
					case '[':
						while((++i<output.length)&&(output[i]!=']')){}
					default:
						break;
				}
			}
			else
				output[out++]=output[i];
		}
		return String.valueOf(output, 0, out);
	}
	
	public String[] wrapOnlyFilter(String msg, int wrap)
	{
		int loop=-1;
		char[] charBuff=msg.toCharArray();
		int len=(wrap>0)?wrap:(Integer.MAX_VALUE/2);
		int firstAlpha=-1;
		int writeTo=0;
		int numLines=1;
		
		while(charBuff.length>loop+1)
		{
			int lastSp=-1;
			while((loop<len)&&(charBuff.length>loop+1))
			{
				char nextChar=charBuff[++loop];
				toLoop:
				{
					toSwitch:
					switch(nextChar)
					{
					case ' ':
						lastSp=writeTo;
						break;
					case (char)13:
						break toLoop;
					case (char)10:
						numLines++;
						if(wrap>0) len=loop+wrap;
						break;
					case '\033': // skip escapes
						if((loop+1 < charBuff.length) && (charBuff[loop+1]=='['))
						{
							while((loop < charBuff.length) && ((nextChar=charBuff[loop])!='m'))
							{
								charBuff[writeTo++]=nextChar;
								loop++;
								len++;
							}
							len++; // and one more for the 'm'.
						}
						break;
					case '^':
						if(loop+1<charBuff.length) switch((nextChar=charBuff[++loop]))
						{
							case '\\':
								nextChar='\n';
								numLines++;
								if(wrap>0) len=loop+wrap;
								break toSwitch;
							case '[':
								charBuff[writeTo++]='^';
								charBuff[writeTo++]=nextChar;
								len+=3;	//^, [, ]
								while((++loop<charBuff.length)&&((nextChar=charBuff[loop])!=']'))
								{
									charBuff[writeTo++]=nextChar;
									len++;
								}
								//charBuff[writeTo++]=nextChar;
								break;
							default:
								len++;
							case '^':
								charBuff[writeTo++]='^';
								//charBuff[writeTo++]=nextChar;
								len++;
								loop++;
								break;
						}
						break;
					default:
						if((firstAlpha < 0)&&(Character.isLetter(charBuff[loop])))
							firstAlpha = loop;
					/*case '`': case '>': case '"': case '(': case '<':*/
						break;
					}
					charBuff[writeTo++]=nextChar;
				}
			}

			if((len<charBuff.length)&&(lastSp>=0))
			{
				charBuff[lastSp]='\n';
				numLines++;
				len=loop+wrap;
				lastSp=-1;
			}
			else
				len++;
		}

		if(firstAlpha>=0)
			charBuff[firstAlpha]=Character.toUpperCase(charBuff[firstAlpha]);
		String[] output=new String[numLines];
		int i=0;
		for(loop=0;((loop<numLines)&&(i<writeTo));loop++)
		{
			int lastStart=i;
			while((charBuff[i++]!='\n')&&(i<writeTo));
			output[loop]=String.valueOf(charBuff, lastStart, i++);
		}
		return output;
	}
	
	// no word-wrapping, text filtering or ('\','n') -> '\n' translations
	//TODO AFTER FULLOUTFILTER
	public String colorOnlyFilter(String msg, Session S)
	{
		if((msg==null)||(msg.length()==0)||(S==null)) return msg;
		StringBuilder buf=new StringBuilder(msg.length()*5/4);

		int loop=0;
		String currentColor=ColorLibrary.COLOR_NONE;	//"\033[0m"
		ArrayList<String> oldColors=null;//=new ArrayList();

		while(msg.length()>loop)
		{
			char nextChar=msg.charAt(loop);
			switch(nextChar)
			{
			case (char)0:
				buf.append(nextChar);
				break;
			case '\033': // skip escapes
				if((loop+1 < msg.length()) && (msg.charAt(loop+1)=='['))
				{
					int i=msg.indexOf('m', loop+2);
					if(i>0)
					{
						if(oldColors==null) oldColors=new ArrayList();
						oldColors.add(currentColor);
						currentColor=msg.substring(loop, i+1);
						buf.append(msg, loop, i);	//Does not append the m, that will be done at end of loop.
						loop=i;
						nextChar='m';
					}
				}
				else nextChar=(char)0;
				break;
			case '^':
				if(loop+1==msg.length()) break;
				char escapeChar=msg.charAt(++loop);
				switch(escapeChar)
				{
				//Reset color command
				case '.':
				//ANSI Colors
				case '0': case '1': case '2': case '3':
				case '4': case '5': case '6': case '7':
				//Telnet Formatting. Ignored by oldColors
				case 'I': case 'i': case 'U': case 'u': //case 'B': case 'b': 
					nextChar=(char)0;
					String newColor=S.getColor(escapeChar);
					if(newColor.length()>0)
					{
						buf.append(newColor);
						if((escapeChar>='0')&&(escapeChar<='7'))
						{
							if(oldColors==null) oldColors=new ArrayList();
							oldColors.add(currentColor);
							currentColor=newColor;
						}
						else if(escapeChar=='.')
						{
							currentColor=ColorLibrary.COLOR_NONE;
							oldColors=null;
						}
					}
					break;
				//Previous color
				case '?':
					if((oldColors!=null)&&(oldColors.size()>0))
					{
						currentColor=oldColors.remove(oldColors.size()-1);
						buf.append(currentColor);
					}
					break;
				case '[':
					int i=msg.indexOf(']', loop+2);
					if(i>0)
					{
						switch(msg.charAt(loop+1))
						{
						case 'F':
						case 'f':
						case 'B':
						case 'b':
							String substring=S.getColor(msg.substring(loop+1, i), currentColor);
							if(substring.length()>0)
							{
								if(oldColors==null) oldColors=new ArrayList();
								oldColors.add(currentColor);
								currentColor=substring;
								buf.append(substring);
							}
							break;
						default:
							buf.append(msg, loop-1, i+1);
							break;
						}
						loop=i;
					}
					nextChar=(char)0;
					break;
				default:
					buf.append(nextChar);
					nextChar=escapeChar;
					break;
				}
				break;
			default:
				break;
			}
			if(nextChar!=(char)0)
				buf.append(nextChar);
			loop++;
		}

		if((oldColors!=null)&&(oldColors.size()>0)) buf.append(ColorLibrary.COLOR_NONE);

		if(CMSecurity.isDebugging("OUTPUT"))
			Log.debugOut("CoffeeFilter","OUTPUT: "+(((S!=null)&&(S.mob()!=null))?S.mob().name():"")+": "+buf.toString());
		return buf.toString();
	}
	
	public String getLastWord(StringBuilder buf, int lastSp, int lastSpace)
	{
		String lastWord="";
		if(lastSp>lastSpace)
		{
			lastWord=CMStrings.removeColors(buf.substring(lastSpace,lastSp)).trim().toUpperCase();
			while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(0))))
				  lastWord=lastWord.substring(1);
				while((lastWord.length()>0)&&(!Character.isLetterOrDigit(lastWord.charAt(lastWord.length()-1))))
				  lastWord=lastWord.substring(0,lastWord.length()-1);
			for(int i=lastWord.length()-1;i>=0;i--)
				if(!Character.isLetterOrDigit(lastWord.charAt(i)))
				{ lastWord=lastWord.substring(i+1); break;}
		}
		/*else
		{
			for(int i=(lastSpace-1);((i>=0)&&(!Character.isLetterOrDigit(buf.charAt(i))));i--)
				lastWord=buf.charAt(i)+lastWord;
			lastWord=
			lastWord=CMStrings.removeColors(lastWord).trim().toUpperCase();
		}*/
		return lastWord;
	}

	//Check to see if pluralization/conjugation should be used depending on last word. 'A', '1', and 'YOU' are checked.
	public boolean shouldUseExtension(StringBuilder buf, int lastSp, int lastSpace)
	{
		if(lastSp<lastSpace) return false;
		char[] lastWord=new char[3];
		int lastWordIndex=0;
		for(int i=lastSpace+1;i<lastSp;i++)
		{
			char nextChar=buf.charAt(i);
			if (nextChar=='\033') // skip escapes
			{
				i=buf.indexOf("m", i);
				if(i==-1) return false;
			}
			else
			{
				if(lastWordIndex==3) return false;
				lastWord[lastWordIndex++]=Character.toUpperCase(nextChar);
			}
		}
		//Could/should recode this to be able to check against a list of possible words better. But for now..
		return ( ((lastWordIndex==1)&&((lastWord[0]=='A')||(lastWord[0]=='1')))
			||((lastWordIndex==3)&&(lastWord[0]=='Y')&&(lastWord[0]=='O')&&(lastWord[0]=='U')) );
	}

	// supported here <?-HIS-HER>, <?-HIM-HER>, <?-NAME>,
	// <?-NAMESELF>, <?-HE-SHE>, <?-IS-ARE>, <?-HAS-HAVE>
	public String handleSTO(String cmd, MOB forMob, Interactable source, Interactable target, CMObject tool)
	{
		String replacement="";
		Interactable regarding=null;
		switch(cmd.charAt(0))
		{
			case 'S': regarding=source; break;
			case 'T': regarding=target; break;
			case 'O':
				if(tool instanceof Interactable) regarding=(Interactable)tool;
				break;
		}
		//String replacement=null;
		Integer I=(Integer)getTagTable().get(cmd.substring(1));
		if(I==null)
			return cmd.substring(1);
		else switch(I.intValue())
		{
		case NAME:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="you";
			else if(regarding instanceof MOB)
				replacement=((MOB)regarding).displayName(forMob);
			else
				replacement=regarding.name();
			break;
		case NAMENOART:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="you";
			else if(regarding instanceof MOB)
				replacement=CMLib.english().cleanArticles(((MOB)regarding).displayName(forMob));
			else
				replacement=CMLib.english().cleanArticles(regarding.name());
			break;
		case NAMESELF:
			if(regarding==null){}
				//replacement="";
			else if(((source==target)||(target==null))&&(forMob==regarding))
				replacement="yourself";
			else if(forMob==regarding)
				replacement="you";
			else if(source==target)
				replacement=((regarding instanceof Body)?(((Body)regarding).gender().object()+"self"):"itself");
			else if(regarding instanceof MOB)
				replacement=((MOB)regarding).displayName(forMob);
			else
				replacement=regarding.name();
			break;
		case YOUPOSS:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="your";
			else if(regarding instanceof MOB)
				replacement=((MOB)regarding).displayName(forMob)+"'s";
			else
				replacement=regarding.name()+"'s";
			break;
		case HISHER:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="your";
			else if(regarding instanceof Body)
				replacement=((Body)regarding).gender().possessive();
			else
				replacement="its";
			break;
		case HIMHER:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="you";
			else if(regarding instanceof Body)
				replacement=((Body)regarding).gender().object();
			else
				replacement="it";
			break;
		case HIMHERSELF:
			if(regarding==null)
				replacement="themself";
			else if(forMob==regarding)
				replacement="yourself";
			else if(regarding instanceof Body)
				replacement=((Body)regarding).gender().object()+"self";
			else
				replacement="itself";
			break;
		case HISHERSELF:
			if(regarding==null)
				replacement="themself";
			else if(forMob==regarding)
				replacement="yourself";
			else if(regarding instanceof Body)
				replacement=((Body)regarding).gender().possessive()+"self";
			else
				replacement="itself";
			break;
		case HESHE:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="you";
			else if(regarding instanceof Body)
				replacement=((Body)regarding).gender().subject();
			else
				replacement="its";
			break;
		case SIRMADAM:
			if(regarding==null){}
				//replacement="";
			else if(regarding instanceof Body)
				replacement=((Body)regarding).gender().sirmadam();
			else
				replacement="sir";
			break;
		case ISARE:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="are";
			else if((regarding instanceof MOB)||(regarding instanceof Body))
				replacement="is";
			break;
		case HASHAVE:
			if(regarding==null){}
				//replacement="";
			else if(forMob==regarding)
				replacement="have";
			else if((regarding instanceof MOB)||(regarding instanceof Body))
				replacement="has";
			break;
		}
		return replacement;
	}
	public String fullOutFilter(Session S,
								MOB mob,
								Interactable source,
								Interactable target,
								CMObject tool,
								String msg,
								boolean wrapOnly)
	{
		if((msg==null)||(msg.length()==0)||(S==null)) return msg;

		boolean doSagain=false;
		boolean firstSdone=false;
		StringBuilder buf=new StringBuilder(msg.length()*5/4);

		int wrap=S.getWrap();
		int len=(wrap>0)?wrap:(Integer.MAX_VALUE/2);
		int loop=0;
		int lastSpace=0;
		int firstAlpha=-1;
		String currentColor=ColorLibrary.COLOR_NONE;	//"\033[0m"
		ArrayList<String> oldColors=null;//=new ArrayList();

		while(msg.length()>loop)
		{
			int lastSp=-1;
			while((loop<len)&&(msg.length()>loop))
			{
				char nextChar=msg.charAt(loop);
				switch(nextChar)
				{
				case (char)0:
					buf.append(nextChar);
					break;
				case ' ':
					if(lastSp>lastSpace)
						lastSpace=lastSp;
					lastSp=buf.length();
					break;
				case '\r':
					buf.append(nextChar);
					nextChar=(char)10;
					if((loop+1<msg.length())&&((msg.charAt(loop+1))==(char)10))
						loop++;
					if(wrap>0) len=loop+wrap;
					lastSpace=msg.length();
					lastSp=-1;
					break;
				case '\n':
					//No need for a check, this SHOULD always be skipped, if it happens there was no 13(\r)
					buf.append((char)13);
					if(wrap>0) len=loop+wrap;
					lastSpace=msg.length();
					lastSp=-1;
					break;
				case '\033': // skip escapes
					len++;	//Always for the escape
					if((loop+1 < msg.length()) && (msg.charAt(loop+1)=='['))
					{
						int i=msg.indexOf('m', loop+2);
						if(i>0)
						{
							if(oldColors==null) oldColors=new ArrayList();
							oldColors.add(currentColor);
							currentColor=msg.substring(loop, i+1);
							buf.append(msg, loop, i);	//Does not append the m, that will be done at end of loop.
							len+=(i-loop);	//everything after the escape
							loop=i;
							nextChar='m';
						}
					}
					else nextChar=(char)0;
					break;
				case '^':
					if(loop+1==msg.length()) break;
					char escapeChar=msg.charAt(++loop);
					switch(escapeChar)
					{
					//Reset color command
					case '.':
					//ANSI Colors
					case '0': case '1': case '2': case '3':
					case '4': case '5': case '6': case '7':
					//Telnet Formatting. Ignored by oldColors
					case 'I': case 'i': case 'U': case 'u': //case 'B': case 'b':
						nextChar=(char)0;
						len+=2;
						String newColor=S.getColor(escapeChar);
						if(newColor.length()>0)
						{
							buf.append(newColor);
							if((escapeChar>='0')&&(escapeChar<='7'))
							{
								if(oldColors==null) oldColors=new ArrayList();
								oldColors.add(currentColor);
								currentColor=newColor;
							}
							else if(escapeChar=='.')
							{
								currentColor=ColorLibrary.COLOR_NONE;
								oldColors=null;
							}
						}
						break;
					//Previous color
					case '?':
						if((oldColors!=null)&&(oldColors.size()>0))
						{
							currentColor=oldColors.remove(oldColors.size()-1);
							buf.append(currentColor);
						}
						len+=2;
						break;
					case '^':
						len++;
						break;
					case '\\':
						buf.append((char)13);
						nextChar=(char)10;
						if(wrap>0) len=loop+wrap;
						lastSpace=msg.length();
						lastSp=-1;
						break;
					case 's':	//Pluralization AND conjugation check
						if(doSagain||shouldUseExtension(buf,lastSp,lastSpace))
						{
							len+=2;
							nextChar=(char)0;
							doSagain=true;
						}
						else
						{
							nextChar=Character.isUpperCase(buf.charAt(buf.length()-1))?'S':'s';
							len++;
						}
						firstSdone=true;
						break;
					case 'y':	//Conjugation check (not intended to check for numbers)
						if(doSagain||shouldUseExtension(buf,lastSp,lastSpace))
						{
							nextChar=Character.isUpperCase(buf.charAt(buf.length()-1))?'Y':'y';
							len++;
							doSagain=true;
						}
						else
						{
							if(Character.isUpperCase(buf.charAt(buf.length()-1)))
							{
								buf.append("IE");
								nextChar='S';
							}
							else
							{
								buf.append("ie");
								nextChar='s';
							}
							len--;
						}
						firstSdone=true;
						break;
					case 'e':	//Conjugation check (not intended to check for numbers)
						if(doSagain||shouldUseExtension(buf,lastSp,lastSpace))
						{
							nextChar=(char)0;
							len+=2;
							doSagain=true;
						}
						else
						{
							if(Character.isUpperCase(buf.charAt(buf.length()-1)))
							{
								buf.append("E");
								nextChar='S';
							}
							else
							{
								buf.append("e");
								nextChar='s';
							}
						}
						firstSdone=true;
						break;
					//case 'a':	//Is/are conjugation check
					case '[':
						int i=msg.indexOf(']', loop+2);
						if(i>0)
						{
							switch(msg.charAt(loop+1))
							{
							case 'F': case 'f': case 'B': case 'b':
							{
								String substring=S.getColor(msg.substring(loop+1, i), currentColor);
								if(substring.length()>0)
								{
									if(oldColors==null) oldColors=new ArrayList();
									oldColors.add(currentColor);
									currentColor=substring;
									buf.append(substring);
								}
								break;
							}
							case 'S': case 'T': case 'O':
							{
								if(i==loop+2) break;
								String substring=handleSTO(msg.substring(loop+1, i), mob, source, target, tool);
								if((!firstSdone)&&(substring=="you")) doSagain=true;	//Safe because java compiler
								if(substring.length()>0)
								{
									if(firstAlpha < 0)
										for(int j=0;j<substring.length();j++)
											if(Character.isLetter(substring.charAt(j)))
												{ firstAlpha = buf.length()+j; break; }
									buf.append(substring);
									len-=substring.length();
								}
							}
							default:
								break;
							}
							len+=2+(i-loop);	//+2 for the ^ (skipped by loop++) and ] (i is not inclusive)
							loop=i;
						}
						nextChar=(char)0;
						break;
					default:
						len++;
						nextChar=(char)0;
						break;
					}
					break;
				default:
					if((firstAlpha < 0)&&(Character.isLetter(nextChar)))
						firstAlpha = buf.length();
					break;
				}
				if(nextChar!=(char)0)
					buf.append(nextChar);
				loop++;
			}

			if((len<msg.length())&&(lastSp>=0))
			{
				buf.setCharAt(lastSp,(char)13);
				lastSpace=lastSp+1;
				buf.insert(lastSpace,(char)10);
				len=loop+wrap;
			}
			else
				len++;
		}

		if(firstAlpha>=0)
			buf.setCharAt(firstAlpha,Character.toUpperCase(buf.charAt(firstAlpha)));
		if((oldColors!=null)&&(oldColors.size()>0)) buf.append(ColorLibrary.COLOR_NONE);

		if(CMSecurity.isDebugging("OUTPUT"))
			Log.debugOut("CoffeeFilter","OUTPUT: "+(((S!=null)&&(S.mob()!=null))?S.mob().name():"")+": "+buf.toString());
		return buf.toString();
	}

	/*public String safetyFilter(String s)
	{
		StringBuffer s1=new StringBuffer(s);
		
		int x=-1;
		while((++x)<s1.length())
		{
			if(s1.charAt(x)=='\r')
			{
				s1.deleteCharAt(x);
				x--;
			}
			else
			if(s1.charAt(x)=='\n')
			{
				s1.setCharAt(x,'\\');
				s1.insert(x+1,'n');
				x++;
			}
			else
			if(s1.charAt(x)=='\'')
				s1.setCharAt(x,'`');
		}
		return s1.toString();
	}
	public String simpleOutFilter(String msg)	//Now toRawString
	{
		if(msg==null) return null;
		StringBuffer buf=new StringBuffer(msg);
		for(int i=0;i<buf.length();i++)
		{
			switch(buf.charAt(i))
			{
			case '`':
				buf.setCharAt(i,'\'');
				break;
			case '\\':
				if(i<buf.length()-1)
				{
					switch(buf.charAt(i+1))
					{
					case 'n':
					case 'r':
						{
						buf.setCharAt(i,(char)13);
						if((i>=buf.length()-2)||((i<buf.length()-2)&&((buf.charAt(i+2))!=10)))
							buf.setCharAt(i+1,(char)10);
						else
						if(i<buf.length()-2)
							buf.deleteCharAt(i+1);
						}
						break;
					case '\'':
					case '`':
						{
						buf.setCharAt(i,'\'');
						buf.deleteCharAt(i+1);
						}
						break;
					}
				}
				break;
			}
		}
		if(CMSecurity.isDebugging("OUTPUT"))
			Log.debugOut("CoffeeFilter","OUTPUT: ?: "+buf.toString());
		return buf.toString();
	}
	public StringBuffer simpleInFilter(StringBuffer input, boolean allowMXP)
	{
		if(input==null) return null;
		int x=0;
		while(x<input.length())
		{
			char c=input.charAt(x);
			if(c=='\'')
				input.setCharAt(x,'`');
			else
			if((c=='^')&&(x<(input.length()-1))&&(!allowMXP))
			{
				switch(input.charAt(x+1))
				{
				case '<':
				case '>':
				case '&':
					input.deleteCharAt(x);
					break;
				}
			}
			else
			if(c==8)
			{
				String newStr=input.toString();
				if(x==0)
					input=new StringBuffer(newStr.substring(x+1));
				else
				{
					input=new StringBuffer(newStr.substring(0,x-1)+newStr.substring(x+1));
					x--;
				}
				x--;
			}
			x++;
		}
		return new StringBuffer(input.toString());
	}
	public String fullInFilter(String input, boolean allowMXP)
	{
		if(input==null) return null;
		StringBuffer buf=new StringBuffer(input);
		for(int i=0;i<buf.length();i++)
		{
			switch(buf.charAt(i))
			{
			case (char)10:
				buf.setCharAt(i,'r');
				buf.insert(i,'\\');
				break;
			case (char)13:
				buf.setCharAt(i,'n');
				buf.insert(i,'\\');
				break;
			}
		}
		return simpleInFilter(buf,allowMXP).toString();
	}*/
}