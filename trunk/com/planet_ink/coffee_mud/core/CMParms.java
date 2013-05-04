package com.planet_ink.coffee_mud.core;
import java.util.*;
import com.planet_ink.coffee_mud.core.interfaces.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMParms
{
	private CMParms(){super();}
	private static CMParms inst=new CMParms();
	public static CMParms instance(){return inst;}

	public static String firstWord(String str)
	{
		int spaceIndex=str.indexOf(" ");
		if(spaceIndex>=0)
			return str.substring(0, spaceIndex);
		return str;
	}
	public static String firstWord(String str, boolean includeQuotes)
	{
		int spaceIndex=str.indexOf(" ");
		if(includeQuotes)
		{
			int strIndex=str.indexOf("\"");
			if((strIndex>=0)&&((strIndex<spaceIndex)||(spaceIndex<0)))
			{
				int endStrIndex=str.indexOf("\"",strIndex+1);
				if(endStrIndex>strIndex)
					return str.substring(0, endStrIndex);
			}
		}
		if(spaceIndex>=0)
			return str.substring(0, spaceIndex);
		return str;
	}
	//Combine methods: Take a Collection (usually Vector<String>), and mesh them into a single String
	public static String combine(Vector commands, int startAt, int endAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
			Combined.append(commands.elementAt(commandIndex).toString()+" ");
		return Combined.toString().trim();
	}
	public static String combineWithQuotes(Vector commands, int startAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
		{
			String s=commands.elementAt(commandIndex).toString();
			if(s.indexOf(" ")>=0)
				Combined.append("\"").append(s).append("\" ");
			else
				Combined.append(s+" ");
		}
		return Combined.toString().trim();
	}
	public static String combineWithQuotes(Vector commands, int startAt, int endAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<endAt;commandIndex++)
		{
			String s=commands.elementAt(commandIndex).toString();
			if(s.indexOf(" ")>=0)
				Combined.append("\"").append(s).append("\" ");
			else
				Combined.append(s+" ");
		}
		return Combined.toString().trim();
	}
	public static String combineAfterIndexWithQuotes(Vector<String> commands, String match)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=0;commandIndex<0;commandIndex++)
		{
			String s=commands.elementAt(commandIndex);
			if(s.indexOf(" ")>=0)
				Combined.append("\"").append(s).append("\" ");
			else
				Combined.append(s+" ");
		}
		return Combined.toString().trim();
	}
	public static String combineWithX(Vector commands, String X, int startAt)
	{
		StringBuffer Combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
		{
			String s=commands.elementAt(commandIndex).toString();
			Combined.append(s+X);
		}
		return Combined.toString().trim();
	}
	public static String combine(Vector commands, int startAt)
	{
		StringBuffer combined=new StringBuffer("");
		if(commands!=null)
		for(int commandIndex=startAt;commandIndex<commands.size();commandIndex++)
			combined.append(commands.elementAt(commandIndex).toString()+" ");
		return combined.toString().trim();
	}
	public static String combine(HashSet flags, int startAt)
	{
		StringBuffer combined=new StringBuffer("");
		if(flags!=null)
		for(Iterator i=flags.iterator();i.hasNext();)
			combined.append(i.next().toString()+" ");
		return combined.toString().trim();
	}

	//Parse: Take a String and split it up into a Vector<String> according to some divisor. Default is ' '.
	public static Vector<String> parse(String str)
	{   return parse(str,-1);   }
	//Similar but makes sure ='s have text before and after them
	public static Vector<String> paramParse(String str)
	{
		Vector<String> commands=parse(str,-1);
		for(int i=0;i<commands.size();i++)
		{
			String s=commands.elementAt(i);
			if(s.startsWith("=")&&(s.length()>1)&&(i>0))
			{
				String prev=commands.elementAt(i-1);
				commands.setElementAt(prev+s,i-1);
				commands.removeElementAt(i);
				i--;
			}
			else
			if(s.endsWith("=")&&(s.length()>1)&&(i<(commands.size()-1)))
			{
				String next=commands.elementAt(i+1);
				commands.setElementAt(s+next,i);
				commands.removeElementAt(i+1);
			}
			else
			if(s.equals("=")&&((i>0)&&(i<(commands.size()-1))))
			{
				String prev=commands.elementAt(i-1);
				String next=commands.elementAt(i+1);
				commands.setElementAt(prev+"="+next,i-1);
				commands.removeElementAt(i);
				commands.removeElementAt(i+1);
				i--;
			}
		}
		return commands;
	}
	public static String removeFirst(String str)
	{
		int spaceIndex=str.indexOf(" ");
		int strIndex=str.indexOf("\"");
		if((strIndex>=0)&&((strIndex<spaceIndex)||(spaceIndex<0)))
		{
			int endStrIndex=str.indexOf("\"",strIndex+1);
			if(endStrIndex>strIndex)
			{
				return str.substring(endStrIndex+1).trim();
			}
			else
			{
				return "";
			}
		}
		else if(spaceIndex>=0)
		{
			return str.substring(spaceIndex+1).trim();
		}
		return "";
	}
	public static Vector<String> parse(String str, int upTo)	//How many elements the Vector may have.
	{
		Vector<String> commands=new Vector();
		if(str==null) return commands;
		str=str.trim();
		int spaceIndex=str.indexOf(" ");
		int strIndex=str.indexOf("\"");
		done:
		while(str.length()>0)
		{
			String CMD;
			while((strIndex>=0)&&((strIndex<spaceIndex)||(spaceIndex<0)))
			{
				int endStrIndex=str.indexOf("\"",strIndex+1);
				if(endStrIndex<0)
				{
					commands.add(str);
					break done;
				}
				spaceIndex=str.indexOf(" ", endStrIndex+1);
				strIndex=str.indexOf("\"", endStrIndex+1);
			}
			if(spaceIndex>=0)
			{
				CMD=str.substring(0,spaceIndex).trim();
				str=str.substring(spaceIndex+1).trim();
				spaceIndex=str.indexOf(" ");
				if(strIndex>=0)
					strIndex=str.indexOf("\"");
			}
			else
			{
				CMD=str.trim();
				str="";
			}
			if(CMD.length()>0)
			{	//TODO: Ponder if these should be intern.
				commands.add(CMD);
				if((upTo>=0)&&(commands.size()>=upTo))
				{
					if(str.length()>0)
						commands.add(str);
					break;
				}
			}
		}
		return commands;
	}
	public static Vector<String> parse(String str, int startFrom, int upTo)
	{
		if(startFrom <= 0) return parse(str, upTo);
		str=str.trim();
		int spaceIndex=str.indexOf(" ");
		int strIndex=str.indexOf("\"");
		done:
		while(str.length()>0)
		{
			while((strIndex>=0)&&((strIndex<spaceIndex)||(spaceIndex<0)))
			{
				int endStrIndex=str.indexOf("\"",strIndex+1);
				if(endStrIndex<0)
					break done;
				spaceIndex=str.indexOf(" ", endStrIndex+1);
				strIndex=str.indexOf("\"", endStrIndex+1);
			}
			if(spaceIndex<0)
				return new Vector();
			String CMD=str.substring(0,spaceIndex).trim();
			str=str.substring(spaceIndex+1).trim();
			if(CMD.length()>0)
			{
				startFrom--;
				if(startFrom==0)
					return parse(str, upTo);
			}
			spaceIndex=str.indexOf(" ");
			if(strIndex>=0)
				strIndex=str.indexOf("\"");
		}
		return new Vector();
	}
	
	public static Vector<String> parseCommas(String s, boolean ignoreNulls)	//If true, does not add empty ("") strings
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(",");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(",");
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	//Adds strings only if they are one of the valid flag options
	public static Vector<String> parseCommadFlags(String s, String[] flags)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(",");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			int index=CMParms.indexOfIgnoreCase(flags, s2);
			if((s2.length()>0)&&(index>=0))
				V.addElement(flags[index]);
			x=s.indexOf(",");
		}
		int index=CMParms.indexOfIgnoreCase(flags, s);
		if((s.length()>0)&&(index>=0))
			V.addElement(flags[index]);
		return V;
	}
	public static Vector<String> parseTabs(String s, boolean ignoreNulls)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf("\t");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf("\t");
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	public static Vector<String> parseAny(String s, String delimeter, boolean ignoreNulls)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(delimeter);
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+delimeter.length()).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(delimeter);
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	//Just case-insensitive version of parseAny
	public static Vector<String> parseAnyWords(String s, String delimeter, boolean ignoreNulls)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		delimeter=delimeter.toUpperCase();
		int x=s.toUpperCase().indexOf(delimeter);
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+delimeter.length()).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(delimeter);
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	public static Vector<String> parseSquiggles(String s)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf("~");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			V.addElement(s2);
			x=s.indexOf("~");
		}
		return V;
	}
	public static Vector<String> parseSentences(String s)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(".");
		while(x>=0)
		{
			String s2=s.substring(0,x+1);
			s=s.substring(x+1);
			V.addElement(s2);
			x=s.indexOf(".");
		}
		return V;
	}
	public static Vector<String> parseSquiggleDelimited(String s, boolean ignoreNulls)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf("~");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((s2.length()>0)||(!ignoreNulls))
				V.addElement(s2);
			x=s.indexOf("~");
		}
		if((s.length()>0)||(!ignoreNulls))
			V.addElement(s);
		return V;
	}
	public static Vector<String> parseSemicolons(String s, boolean ignoreNulls)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(";");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(";");
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}
	public static Vector<String> parseSpaces(String s, boolean ignoreNulls)
	{
		Vector<String> V=new Vector();
		if((s==null)||(s.length()==0)) return V;
		int x=s.indexOf(" ");
		while(x>=0)
		{
			String s2=s.substring(0,x).trim();
			s=s.substring(x+1).trim();
			if((!ignoreNulls)||(s2.length()>0))
				V.addElement(s2);
			x=s.indexOf(" ");
		}
		if((!ignoreNulls)||(s.trim().length()>0))
			V.addElement(s.trim());
		return V;
	}

	//getParm family: Find key, find comparison after it(= for normal, +/- for plus, </>/!/= for compare), return result.
	//Will search far/past text to find comparison
	public static String getParmStr(String text, String key, String defaultVal)	//Stop at =
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
				{
					if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
						return defaultVal;
					x++;
				}
				if(x<text.length())
				{
					boolean endWithQuote=false;
					while((x<text.length())&&(!Character.isLetterOrDigit(text.charAt(x))))
					{
						if(text.charAt(x)=='\"')
						{
							endWithQuote=true;
							x++;
							break;
						}
						x++;
					}
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
							&&((!endWithQuote)&&(!Character.isWhitespace(text.charAt(x)))&&(text.charAt(x)!=';')&&(text.charAt(x)!=','))
							||((endWithQuote)&&(text.charAt(x)!='\"')))
								x++;
						return text.substring(0,x).trim();
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultVal;
	}

	private static int[] makeIntArray(int x, int y){ int[] xy=new int[2]; xy[0]=x;xy[1]=y;return xy;}

	//Return [0] is char for compare type(fails to report 'or equal'), [1] is compare result(-1, 0, 1: fail, n/a, pass)
	public static int[] getParmCompare(String text, String key, int value)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())
					&&(text.charAt(x)!='>')
					&&(text.charAt(x)!='<')
					&&(text.charAt(x)!='!')
					&&(text.charAt(x)!='='))
					x++;
				if(x<text.length()-1)
				{
					char comp=text.charAt(x);
					boolean andEqual=(text.charAt(x)=='=');
					if(text.charAt(x+1)=='='){ x++; andEqual=true;}
					if(x<text.length()-1)
					{
						while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
							x++;
						if(x<text.length())
						{
							text=text.substring(x);
							x=0;
							while((x<text.length())&&(Character.isDigit(text.charAt(x))))
								x++;
							int found=CMath.s_int(text.substring(0,x));
							if(andEqual&&(found==value))
								return makeIntArray(comp,(comp=='!')?-1:1);
							switch(comp)
							{
								case '>': return makeIntArray(comp,(value>found)?1:-1);
								case '<': return makeIntArray(comp,(value<found)?1:-1);
								//case '!': makeIntArray(comp,1);
							}
							return makeIntArray(comp,-1);
						}
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return makeIntArray('\0',0);
	}
	public static int getParmPlus(String text, String key)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
				{
					if(text.charAt(x)=='=')
						return 0;
					x++;
				}
				if(x<text.length())
				{
					char pm=text.charAt(x);
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						if(pm=='+')
							return CMath.s_int(text.substring(0,x));
						return -CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0;
	}
	public static double getParmDoublePlus(String text, String key)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='+')&&(text.charAt(x)!='-'))
				{
					if(text.charAt(x)=='=')
						return 0.0;
					x++;
				}
				if(x<text.length())
				{
					char pm=text.charAt(x);
					while((x<text.length())
					&&(!Character.isDigit(text.charAt(x)))
					&&(text.charAt(x)!='.'))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
						&&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
							x++;
						if(text.substring(0,x).indexOf(".")<0)
						{
							if(pm=='+')
								return (double)CMath.s_int(text.substring(0,x));
							return (double)(-CMath.s_int(text.substring(0,x)));
						}
						if(pm=='+')
							return CMath.s_double(text.substring(0,x));
						return -CMath.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return 0.0;
	}
	public static double getParmDouble(String text, String key, double defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if(x<text.length())
				{
					while((x<text.length())
					&&(!Character.isDigit(text.charAt(x)))
					&&(text.charAt(x)!='.'))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())
						&&((Character.isDigit(text.charAt(x)))||(text.charAt(x)=='.')))
							x++;
						if(text.substring(0,x).indexOf(".")<0)
							return (double)CMath.s_long(text.substring(0,x));
						return CMath.s_double(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}
	public static int getParmInt(String text, String key, int defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='=')&&(!Character.isDigit(text.charAt(x))))
				{
					if((text.charAt(x)=='+')||(text.charAt(x)=='-'))
						return defaultValue;
					x++;
				}
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					while((x<text.length())&&(!Character.isDigit(text.charAt(x))))
						x++;
					if(x<text.length())
					{
						text=text.substring(x);
						x=0;
						while((x<text.length())&&(Character.isDigit(text.charAt(x))))
							x++;
						return CMath.s_int(text.substring(0,x));
					}
				}
				x=-1;
			}
			else
				x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}
	public static boolean getParmBool(String text, String key, boolean defaultValue)
	{
		int x=text.toUpperCase().indexOf(key.toUpperCase());
		while(x>=0)
		{
			if((x==0)||(!Character.isLetter(text.charAt(x-1))))
			{
				while((x<text.length())&&(text.charAt(x)!='='))
					x++;
				if((x<text.length())&&(text.charAt(x)=='='))
				{
					String s=text.substring(x+1).trim();
					if(Character.toUpperCase(s.charAt(0))=='T') return true;
					if(Character.toUpperCase(s.charAt(0))=='F') return false;
				}
			}
			x=text.toUpperCase().indexOf(key.toUpperCase(),x+1);
		}
		return defaultValue;
	}

	public static String[] toStringArray(Vector V)
	{
		if((V==null)||(V.size()==0))
			return CMClass.dummyStringArray;
		String[] s=new String[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=V.elementAt(v).toString();
		return s;
	}
	public static String[] toStringArray(Object[] O)
	{
		if(O==null) return CMClass.dummyStringArray;
		String[] s=new String[O.length];
		for(int o=0;o<O.length;o++)
			s[o]=(O[o]!=null)?O[o].toString():"";
		return s;
	}
	public static String[] toStringArray(HashSet V)
	{
		if((V==null)||(V.size()==0))
			return CMClass.dummyStringArray;
		String[] s=new String[V.size()];
		int v=0;
		for(Iterator i=V.iterator();i.hasNext();)
			s[v++]=(i.next()).toString();
		return s;
	}
	public static String[] toStringArray(Hashtable V)
	{
		if((V==null)||(V.size()==0))
			return CMClass.dummyStringArray;
		String[] s=new String[V.size()];
		int v=0;
		for(Enumeration e=V.keys();e.hasMoreElements();)
		{
			String KEY=(String)e.nextElement();
			s[v]=(String)V.get(KEY);
			v++;
		}
		return s;
	}
	public static long[] toLongArray(Vector V)
	{
		if((V==null)||(V.size()==0))
			return CMClass.dummylongArray;
		long[] s=new long[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_long(V.elementAt(v).toString());
		return s;
	}
	public static int[] toIntArray(Vector V)
	{
		if((V==null)||(V.size()==0))
			return CMClass.dummyintArray;
		int[] s=new int[V.size()];
		for(int v=0;v<V.size();v++)
			s[v]=CMath.s_int(V.elementAt(v).toString());
		return s;
	}

	public static String toSemicolonList(byte[] bytes)
	{
		StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.length;b++)
			str.append(Byte.toString(bytes[b])+(b<(bytes.length-1)?";":""));
		return str.toString();
	}
	public static String toSemicolonList(String[] bytes)
	{
		StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.length;b++)
			str.append(bytes[b]+(b<(bytes.length-1)?";":""));
		return str.toString();
	}
	public static String toSemicolonList(Object[] bytes)
	{
		StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.length;b++)
			str.append(bytes[b]+(b<(bytes.length-1)?";":""));
		return str.toString();
	}
	public static String toSemicolonList(Enumeration bytes)
	{
		StringBuffer str=new StringBuffer("");
		Object o;
		for(;bytes.hasMoreElements();)
		{
			o=(Object)bytes.nextElement();
			str.append(o.toString()+(bytes.hasMoreElements()?";":""));
		}
		return str.toString();
	}
	public static String toSemicolonList(Vector bytes)
	{
		StringBuffer str=new StringBuffer("");
		for(int b=0;b<bytes.size();b++)
			str.append(bytes.elementAt(b)+(b<(bytes.size()-1)?";":""));
		return str.toString();
	}

	//Creates escapes for semicolons in content so they can be distinguished from delimiters.
	public static String toSafeSemicolonList(Vector list)
	{
		return toSafeSemicolonList(list.toArray());
	}
	public static String toSafeSemicolonList(Object[] list)
	{
		StringBuffer buf1=new StringBuffer("");
		StringBuffer s=null;
		for(int l=0;l<list.length;l++)
		{
			String toCheck=list[l].toString();
			if((toCheck.indexOf('\\')>=0)||(toCheck.indexOf(';')>=0))
			{
				s=new StringBuffer(toCheck);
				for(int i=0;i<s.length();i++)
					switch(s.charAt(i))
					{
						case '\\':
						case ';':
							s.insert(i,'\\');
							i++;
							break;
					}
				toCheck=s.toString();
			}
			buf1.append(toCheck);
			if(l<list.length-1)
				buf1.append(';');
		}
		return buf1.toString();
	}
	//Handles the above output
	public static Vector<String> parseSafeSemicolonList(String list, boolean ignoreNulls)
	{
		if(list==null) return new Vector();
		StringBuffer buf1=new StringBuffer(list);
		int lastDex=0;
		Vector<String> V=new Vector();
		for(int l=0;l<buf1.length();l++)
			switch(buf1.charAt(l))
			{
			case '\\':
				buf1.delete(l,l+1);
				break;
			case ';':
				if((!ignoreNulls)||(lastDex<l))
					V.addElement(buf1.substring(lastDex,l));
				lastDex=l+1;
				break;
			}
		if((!ignoreNulls)||(lastDex<buf1.length()));
			V.addElement(buf1.substring(lastDex,buf1.length()));
		return V;
	}

	public static byte[] fromByteList(String str)
	{
		Vector V=CMParms.parseSemicolons(str,true);
		if(V.size()>0)
		{
			byte[] bytes=new byte[V.size()];
			for(int b=0;b<V.size();b++)
				bytes[b]=Byte.parseByte((String)V.elementAt(b));
			return bytes;
		}
		return CMClass.dummybyteArray;
	}

	public static String toStringList(String[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(Object[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(Hashtable V)
	{
		if((V==null)||(V.size()==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(Enumeration e=V.keys();e.hasMoreElements();)
		{
			String KEY=(String)e.nextElement();
			s.append(KEY+"="+(V.get(KEY).toString())+"/");
		}
		return s.toString();
	}
	public static String toStringList(Enumeration e)
	{
		if(!e.hasMoreElements()) return "";
		StringBuffer s=new StringBuffer("");
		Object o=null;
		for(;e.hasMoreElements();)
		{
			o=e.nextElement();
			s.append(", "+o);
		}
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(long[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(boolean[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(byte[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+((int)V[v]));
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(char[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+((long)V[v]));
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(int[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(double[] V)
	{
		if((V==null)||(V.length==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.length;v++)
			s.append(", "+V[v]);
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}

	public static String toStringList(Vector V)
	{
		if((V==null)||(V.size()==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(int v=0;v<V.size();v++)
			s.append(", "+V.elementAt(v).toString());
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	public static String toStringList(HashSet V)
	{
		if((V==null)||(V.size()==0)){
			return "";
		}
		StringBuffer s=new StringBuffer("");
		for(Iterator i=V.iterator();i.hasNext();)
			s.append(", "+i.next().toString());
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}
	//These last two just use other class methods instead of toString()
	public static String toInteractableStringList(Enumeration<? extends Interactable> e)
	{
		if(!e.hasMoreElements()) return "";
		StringBuffer s=new StringBuffer(e.nextElement().name());
		for(;e.hasMoreElements();)
			s.append(", "+e.nextElement().name());
		if(s.length()==0) return "";
		return s.toString();
	}
	public static String toInteractableStringList(Iterator<? extends Interactable> e)
	{
		if(!e.hasNext()) return "";
		StringBuffer s=new StringBuffer(e.next().name());
		for(;e.hasNext();)
			s.append(", "+e.next().name());
		//if(s.length()==0) return "";
		return s.toString();
	}
	public static String toCMObjectStringList(Enumeration<? extends CMObject> e)
	{
		if(!e.hasMoreElements()) return "";
		StringBuffer s=new StringBuffer("");
		CMObject o=null;
		for(;e.hasMoreElements();)
		{
			o=e.nextElement();
			s.append(", "+o.ID());
		}
		if(s.length()==0) return "";
		return s.toString().substring(2);
	}

	public static boolean equalVectors(Vector V1, Vector V2)
	{
		if((V1==null)&&(V2==null)) return true;
		if((V1==null)||(V2==null)) return false;
		if(V1.size()!=V2.size()) return false;
		for(int v=0;v<V1.size();v++)
			if(!V1.elementAt(v).equals(V2.elementAt(v)))
				return false;
		return true;
	}

	public static Hashtable makeHashtable(String[][] O)
	{
		Hashtable H =new Hashtable(O!=null?O.length:0);
		if(O!=null)
			for(int o=0;o<O.length;o++)
				H.put(O[o][0].toUpperCase().trim(),O[o][1]);
		return H;
	}
	public static Hashtable makeHashtable(Object[][] O)
	{
		Hashtable H =new Hashtable(O!=null?O.length:0);
		if(O!=null)
			for(int o=0;o<O.length;o++)
				H.put(O[o][0],O[o][1]);
		return H;
	}

	public static Vector makeVector(Enumeration e)
	{
		Vector V=new Vector();
		if(e!=null)
		for(;e.hasMoreElements();)
			V.addElement(e.nextElement());
		return V;
	}
/*	public static HashSet makeHashSet(String[] O)
	{
		HashSet V=new HashSet();
		if(O!=null)
		for(int s=0;s<O.length;s++)
			V.add(O[s]);
		return V;
	} */
	public static HashSet makeHashSet(Vector O)
	{
		HashSet V=new HashSet();
		if(O!=null)
		for(int s=0;s<O.size();s++)
			V.add(O.elementAt(s));
		return V;
	}
	public static HashSet makeHashSet(Enumeration E)
	{
		HashSet V=new HashSet();
		if(E!=null)
		for(;E.hasMoreElements();)
			V.add(E.nextElement());
		return V;
	}

	public static Vector makeVector(Object... O)
	{
		Vector V=new Vector();
		for(Object obj : O)
			V.add(obj);
		return V;
	}
	public static HashSet makeHashSet(Object... O)
	{
		HashSet V=new HashSet();
		for(Object obj : O)
			V.add(obj);
		return V;
	}

	//NOTE: This is useful and well-coded! Should make use of this method. Cannot use primitives for it though.
	public static <T> T[] appendToArray(T[] front, T[] back)
	{
		if((back==null)||(back.length==0)) return (front==null?(T[])CMClass.dummyObjectArray:front);
		if((front==null)||(front.length==0)) return back;
		T[] newa = Arrays.copyOf(front, front.length + back.length);
		for(int i=1;i<=back.length;i++)
			newa[newa.length-i]=back[back.length-i];
		return newa;
	}
	public static <U, T extends U> U[] appendToArray(T[] front, T[] back, Class<? extends U[]> newType)
	{
		if((back==null)||(back.length==0)) return (front==null?(T[])CMClass.dummyObjectArray:front);
		if((front==null)||(front.length==0)) return back;
		U[] newa = Arrays.copyOf(front, front.length + back.length, newType);
		for(int i=1;i<=back.length;i++)
			newa[newa.length-i]=back[back.length-i];
		return newa;
	}
	public static void addToVector(Vector from, Vector to)
	{
		if(from!=null)
		for(int i=0;i<from.size();i++)
			to.addElement(from.elementAt(i));
	}

	public static void delFromVector(Vector del, Vector from)
	{
		if(del!=null)
		for(int i=0;i<del.size();i++)
			from.removeElement(del.elementAt(i));
	}

	public static boolean containsIgnoreCase(Vector<String> V, String s)
	{
		for(int v=0;v<V.size();v++)
			if(s.equalsIgnoreCase(V.elementAt(v)))
				return true;
		return false;
	}
	public static boolean containsIgnoreCase(String[] supported, String expertise)
	{ return indexOfIgnoreCase(supported,expertise)>=0;}

	//Same as clone except deep copy of Vectors
	public static Vector copyVector(Vector V)
	{
		Vector V2=new Vector();
		for(int v=0;v<V.size();v++)
		{
			Object h=V.elementAt(v);
			if(h instanceof Vector)
				V2.addElement(copyVector((Vector)h));
			else
				V2.addElement(h);
		}
		return V2;
	}

	public static int indexOf(String[] supported, String expertise)
	{
		if((supported==null)||(expertise==null)) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].equals(expertise))
				return i;
		return -1;
	}
	public static int indexOfIgnoreCase(Enumeration supported, String key)
	{
		if(supported==null) return -1;
		for(int index=0;supported.hasMoreElements();index++)
			if(supported.nextElement().toString().equalsIgnoreCase(key))
				return index;
		return -1;
	}
	public static int indexOf(int[] supported, int x)
	{
		if(supported==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i]==x)
				return i;
		return -1;
	}
	public static int indexOf(long[] supported, long x)
	{
		if(supported==null) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i]==x)
				return i;
		return -1;
	}
	public static int indexOf(Enumeration supported, Object key)
	{
		if(supported==null) return -1;
		for(int index=0;supported.hasMoreElements();index++)
			if(supported.nextElement().equals(key))
				return index;
		return -1;
	}
	public static int indexOfIgnoreCase(Iterator supported, String key)
	{
		if(supported==null) return -1;
		for(int index=0;supported.hasNext();index++)
			if(supported.next().toString().equalsIgnoreCase(key))
				return index;
		return -1;
	}
	public static int indexOf(Iterator supported, Object key)
	{
		if(supported==null) return -1;
		for(int index=0;supported.hasNext();index++)
			if(supported.next().equals(key))
				return index;
		return -1;
	}
	public static int indexOfIgnoreCase(String[] supported, String expertise)
	{
		if((supported==null)||(expertise==null)) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].equalsIgnoreCase(expertise))
				return i;
		return -1;
	}

	public static boolean contains(String[] supported, String expertise)
	{ return indexOf(supported,expertise)>=0;}

	public static int indexOf(Object[] supported, Object expertise)
	{
		if((supported==null)||(expertise==null)) return -1;
		for(int i=0;i<supported.length;i++)
			if(supported[i].equals(expertise))
				return i;
		return -1;
	}
	public static boolean contains(Object[] supported, Object expertise)
	{ return indexOf(supported,expertise)>=0;}
	public static boolean contains(int[] supported, int x)
	{ return indexOf(supported,x)>=0;}

	public static int startsWith(String[] supported, String expertise)
	{
		if(expertise==null) return -1;
		if(supported==null) return 0;
		for(int i=0;i<supported.length;i++)
			if(supported[i].startsWith(expertise))
				return i;
		return -1;
	}

	public static int startsWithIgnoreCase(String[] supported, String expertise)
	{
		if(expertise==null) return -1;
		if(supported==null) return 0;
		for(int i=0;i<supported.length;i++)
			if(supported[i].toUpperCase().startsWith(expertise.toUpperCase()))
				return i;
		return -1;
	}

	public static boolean startsAnyWith(String[] supported, String expertise)
	{
		return startsWith(supported,expertise)>=0;
	}

	public static boolean startsAnyWithIgnoreCase(String[] supported, String expertise)
	{
		return startsWithIgnoreCase(supported,expertise)>=0;
	}

	public static int endsWith(String[] supported, String expertise)
	{
		if(expertise==null) return -1;
		if(supported==null) return 0;
		for(int i=0;i<supported.length;i++)
			if(supported[i].endsWith(expertise))
				return i;
		return -1;
	}

	public static int endsWithIgnoreCase(String[] supported, String expertise)
	{
		if(expertise==null) return -1;
		if(supported==null) return 0;
		for(int i=0;i<supported.length;i++)
			if(supported[i].toUpperCase().endsWith(expertise.toUpperCase()))
				return i;
		return -1;
	}

	public static boolean endsAnyWith(String[] supported, String expertise)
	{
		return endsWith(supported,expertise)>=0;
	}

	public static boolean endsAnyWithIgnoreCase(String[] supported, String expertise)
	{
		return endsWithIgnoreCase(supported,expertise)>=0;
	}

	public static Vector denumerate(Enumeration e)
	{
		Vector V=new Vector();
		for(;e.hasMoreElements();)
			V.addElement(e.nextElement());
		return V;
	}
	public static class IteratorWrapper<E> implements Iterator<E>
	{
		private E[] myArray;
		private int index=0;
		public IteratorWrapper(E[] wrapThis){myArray=wrapThis;}
		public boolean hasNext() { return index<myArray.length; }
		public E next() {
			if(index<myArray.length) return myArray[index++];
			throw new NoSuchElementException(); }
		public void remove(){}
	}
	public static Vector denumerate(Iterator e)
	{
		Vector V=new Vector();
		while(e.hasNext())
			V.add(e.next());
		return V;
	}
	public static ArrayList toArrayList(Iterator e)
	{
		ArrayList V=new ArrayList();
		while(e.hasNext())
			V.add(e.next());
		return V;
	}

	/** constant value representing an undefined/unimplemented miscText/parms format.*/
	//public static final String FORMAT_UNDEFINED="{UNDEFINED}";
	/** constant value representing an always empty miscText/parms format.*/
	//public static final String FORMAT_EMPTY="{EMPTY}";
}