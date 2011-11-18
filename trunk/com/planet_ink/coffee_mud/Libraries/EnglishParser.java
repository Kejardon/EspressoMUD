package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.exceptions.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
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


import java.io.IOException;
import java.util.*;
import java.util.regex.*;

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
public class EnglishParser extends StdLibrary implements EnglishParsing
{
	public String ID(){return "EnglishParser";}
	private final static String[] articles={"a","an","all of","some one","a pair of","one of","all","the","some"};
	public static boolean[] PUNCTUATION_TABLE=null;
	public final static char[] ALL_CHRS="ALL".toCharArray();
	
	public boolean isAnArticle(String s)
	{
		for(int a=0;a<articles.length;a++)
			if(s.toLowerCase().equals(articles[a]))
				return true;
		return false;
	}

	public String cleanArticles(String s)
	{
		boolean didSomething=true;
		while(didSomething)
		{
			didSomething=false;
			String lowStr=s.toLowerCase();
			for(int a=0;a<articles.length;a++)
				if(lowStr.startsWith(articles[a]+" "))
					return s.substring(articles[a].length()+1);
		}
		return s;
	}

	
	public String startWithAorAn(String str)
	{
		if((str==null)||(str.length()==0)) return str;
		String uppStr=str.toUpperCase();
		if((!uppStr.startsWith("A "))
		&&(!uppStr.startsWith("AN "))
		&&(!uppStr.startsWith("THE "))
		&&(!uppStr.startsWith("SOME ")))
		{
			if("AEIOU".indexOf(uppStr.charAt(0))>=0) 
				return "an "+str;
			return "a "+str;
		}
		return str;
	}
	
	
	public String insertUnColoredAdjective(String str, String adjective)
	{
		if(str.length()==0) 
			return str;
		str=CMStrings.removeColors(str.trim());
		String uppStr=str.toUpperCase();
		if((uppStr.startsWith("A "))
		||(uppStr.startsWith("AN ")))
		{
			if("aeiouAEIOU".indexOf(adjective.charAt(0))>=0) 
				return "an "+adjective+" "+str.substring(2).trim();
			return "a "+adjective+" "+str.substring(2).trim();
		}
		if((!uppStr.startsWith("THE "))
		&&(!uppStr.startsWith("SOME ")))
		{
			if("aeiouAEIOU".indexOf(adjective.charAt(0))>=0) 
				return "an "+adjective+" "+str.trim();
			return "a "+adjective+" "+str.trim();
		}
		int x=str.indexOf(' ');
		return str.substring(0,x)+" "+adjective+" "+str.substring(x+1);
	}
	
	public Object findCommand(MOB mob, Vector commands)
	{
		if((mob==null)
		||(commands==null)
		||(mob.location()==null)
		||(commands.size()==0))
			return null;

		String firstWord=((String)commands.elementAt(0)).toUpperCase();

		if((firstWord.length()>1)&&(!Character.isLetterOrDigit(firstWord.charAt(0))))
		{
			commands.insertElementAt(((String)commands.elementAt(0)).substring(1),1);
			commands.setElementAt(""+firstWord.charAt(0),0);
			firstWord=""+firstWord.charAt(0);
		}

		// first, exacting pass
		Command C=CMClass.Objects.COMMAND.getCommand(firstWord,true);
		if((C!=null)
		&&(C.securityCheck(mob))
		&&(!CMSecurity.isDisabled("COMMAND_"+CMClass.classID(C).toUpperCase())))
			return C;
/*
		Ability A=getToEvoke(mob,(Vector)commands.clone());
		if((A!=null)
		&&(!CMSecurity.isDisabled("ABILITY_"+A.ID().toUpperCase())))
			return A;

		if(getAnEvokeWord(mob,firstWord)!=null)
			return null;
		Social social=CMLib.socials().fetchSocial(commands,true,true);
		if(social!=null) return social;
*/

		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).equalsIgnoreCase(firstWord))
			{
				C=CMClass.Objects.COMMAND.getCommand("CHANNEL", true);
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+CMLib.channels().getChannelName(c)).equalsIgnoreCase(firstWord))
			{
				C=CMClass.Objects.COMMAND.getCommand("NOCHANNEL", true);
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}

		// second, inexacting pass
		//commands comes inexactly after ables
		//because of CA, PR, etc..
		C=CMClass.Objects.COMMAND.getCommand(firstWord,false);
		if((C!=null)
		&&(C.securityCheck(mob))
		&&(!CMSecurity.isDisabled("COMMAND_"+CMClass.classID(C).toUpperCase())))
			return C;

/*		social=CMLib.socials().fetchSocial(commands,false,true);
		if(social!=null)
		{
			commands.setElementAt(social.baseName(),0);
			return social;
		}
*/
		for(int c=0;c<CMLib.channels().getNumChannels();c++)
		{
			if(CMLib.channels().getChannelName(c).startsWith(firstWord))
			{
				commands.setElementAt(CMLib.channels().getChannelName(c),0);
				C=CMClass.Objects.COMMAND.getCommand("Channel", true);
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
			else
			if(("NO"+CMLib.channels().getChannelName(c)).startsWith(firstWord))
			{
				commands.setElementAt("NO"+CMLib.channels().getChannelName(c),0);
				C=CMClass.Objects.COMMAND.getCommand("NoChannel", true);
				if((C!=null)&&(C.securityCheck(mob))) return C;
			}
		}

		return null;
	}


	public boolean[] PUNCTUATION_TABLE() {
		if(PUNCTUATION_TABLE==null)
		{
			boolean[] PUNCTUATION_TEMP_TABLE=new boolean[255];
			for(int c=0;c<255;c++)
				switch(c)
				{
				case '`': case '~': case '!': case '@': case '#': case '$': case '%':
				case '^': case '&': case '*': case '(': case ')': case '_': case '-':
				case '+': case '=': case '[': case ']': case '{': case '}': case '\\':
				case '|': case ';': case ':': case '\'': case '\"': case ',': case '<':
				case '.': case '>': case '/': case '?':
					PUNCTUATION_TEMP_TABLE[c]=true;
					break;
				default:
					PUNCTUATION_TEMP_TABLE[c]=false;
				}
			PUNCTUATION_TABLE=PUNCTUATION_TEMP_TABLE;
		}
		return PUNCTUATION_TABLE;
	}
	
	public String stripPunctuation(String str)
	{
		if((str==null)||(str.length()==0)) return str;
		boolean puncFound=false;
		PUNCTUATION_TABLE();
		for(int x=0;x<str.length();x++)
			if(isPunctuation((byte)str.charAt(x)))
			{
				puncFound=true;
				break;
			}
		if(!puncFound) return str;
		char[] strc=str.toCharArray();
		char[] str2=new char[strc.length];
		int s=0;
		for(int x=0;x<strc.length;x++)
			if(!isPunctuation((byte)strc[x]))
			{
				str2[s]=strc[x];
				s++;
			}
		return new String(str2,0,s);
	}
	
	private boolean isPunctuation(byte b) {
		if((b<0)||(b>255)) return false;
		return PUNCTUATION_TABLE[b];
	}
	
	public boolean equalsPunctuationless(char[] strC, char[] str2C)
	{
		if((strC.length==0)&&(str2C.length==0)) return true;
		PUNCTUATION_TABLE();
		int s1=0;
		int s2=0;
		int s1len=strC.length;
		while((s1len>0)&&(Character.isWhitespace(strC[s1len-1])||isPunctuation((byte)strC[s1len-1])))
			s1len--;
		int s2len=str2C.length;
		while((s2len>0)&&(Character.isWhitespace(str2C[s2len-1])||isPunctuation((byte)str2C[s2len-1])))
			s2len--;
		while(s1<s1len)
		{
			while((s1<s1len)&&(isPunctuation((byte)strC[s1])))
				s1++;
			while((s2<s2len)&&(isPunctuation((byte)str2C[s2])))
				s2++;
			if(s1==s1len)
			{
				if(s2==s2len)
					return true;
				return false;
			}
			if(s2==s2len)
				return false;
			if(strC[s1]!=str2C[s2])
				return false;
			s1++;
			s2++;
		}
		if(s2==s2len)
			return true;
		return false;
	}
	
	public boolean containsString(String toSrchStr, String srchStr)
	{
		if((toSrchStr==null)||(srchStr==null)) return false;
		if((toSrchStr.length()==0)&&(srchStr.length()>0)) return false;
		char[] srchC=srchStr.toCharArray();
		char[] toSrchC=toSrchStr.toCharArray();
		for(int c=0;c<srchC.length;c++)
			srchC[c]=Character.toUpperCase(srchC[c]);
		for(int c=0;c<toSrchC.length;c++)
			toSrchC[c]=Character.toUpperCase(toSrchC[c]);
		if(java.util.Arrays.equals(srchC,ALL_CHRS)) return true;
		if(java.util.Arrays.equals(srchC,toSrchC)) return true;
		if(equalsPunctuationless(srchC,toSrchC)) return true;
		
		boolean topOnly=false;
		if((srchC.length>1)&&(srchC[0]=='$'))
		{
			srchC=new String(srchC,1,srchC.length-1).toCharArray();
			topOnly=true;
		}
		int tos=0;
		boolean found=false;
		while((!found)&&(tos<toSrchC.length))
		{
			for(int x=0;x<srchC.length;x++)
			{
				if(tos>=toSrchC.length)
				{
					if(srchC[x]=='$')
						found=true;
					break;
				}

				switch(toSrchC[tos])
				{
				case '^':
					tos=tos+2;
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';':
					tos++;
					break;
				}
				switch(srchC[x])
				{
				case '^': x=x+2;
					break;
				case ',':
				case '?':
				case '!':
				case '.':
				case ';': x++;
					break;
				}
				if(x<srchC.length)
				{
					if(tos<toSrchC.length)
					{
						if(srchC[x]!=toSrchC[tos])
							break;
						else
						if(x==(srchC.length-1))
						   found=true;
						else
							tos++;
					}
					else
					if(srchC[x]=='$')
						found=true;
					else
						break;
				}
				else
				{
					found=true;
					break;
				}
			}
			if((topOnly)&&(!found)) break;
			while((!found)&&(tos<toSrchC.length)&&(Character.isLetter(toSrchC[tos])))
				tos++;
			tos++;
		}
		return found;
	}

	public String bumpDotNumber(String srchStr)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return srchStr;
		if(((Boolean)flags[FLAG_ALL]).booleanValue())
			return srchStr;
		if(((Integer)flags[FLAG_DOT]).intValue()==0)
			return "1."+((String)flags[FLAG_STR]);
		return (((Integer)flags[FLAG_DOT]).intValue()+1)+"."+((String)flags[FLAG_STR]);
	}

	public Object[] fetchFlags(String srchStr)
	{
		if(srchStr.length()==0) return null;
		srchStr=srchStr.toUpperCase();
		if((srchStr.length()<2)||(srchStr.equals("THE")))
		   return null;
		Object[] flags=new Object[3];

		boolean allFlag=false;
		if(srchStr.startsWith("ALL "))
		{
			srchStr=srchStr.substring(4);
			allFlag=true;
		}
		else
		if(srchStr.equals("ALL"))
			allFlag=true;

		int dot=srchStr.lastIndexOf(".");
		int occurrance=0;
		if(dot>0)
		{
			String sub=srchStr.substring(dot+1);
			occurrance=CMath.s_int(sub);
			if(occurrance>0)
				srchStr=srchStr.substring(0,dot);
			else
			{
				dot=srchStr.indexOf(".");
				sub=srchStr.substring(0,dot);
				occurrance=CMath.s_int(sub);
				if(occurrance>0)
					srchStr=srchStr.substring(dot+1);
				else
					occurrance=0;
			}
		}
		flags[0]=srchStr;
		flags[1]=Integer.valueOf(occurrance);
		flags[2]=Boolean.valueOf(allFlag);
		return flags;
	}

	public CMObject fetchObject(Vector<? extends CMObject> list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		CMObject thisThang=null;
		if(exactOnly)
		{
			if(srchStr.startsWith("$")) srchStr=srchStr.substring(1);
			if(srchStr.endsWith("$")) srchStr=srchStr.substring(0,srchStr.length()-1);
			try
			{
				for(int i=0;i<list.size();i++)
				{
					thisThang=list.elementAt(i);
					if(thisThang.ID().equalsIgnoreCase(srchStr))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					thisThang=list.elementAt(i);
					if(containsString(thisThang.ID(),srchStr))
						if((--myOccurrance)<=0)
							return thisThang;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return null;
	}

	public Interactable fetchInteractable(Vector<? extends Interactable> list, String srchStr, boolean exactOnly)
	{ return fetchInteractable(list.toArray(), srchStr, exactOnly); }
	protected boolean thingCheck(Vector<? extends Interactable> V, Interactable thisThang, String srchStr, boolean allFlag, boolean exact, int maxDepth, int[] numLeft)
	{
		if(exact)
		{
			if((thisThang.ID().equalsIgnoreCase(srchStr)||thisThang.name().equalsIgnoreCase(srchStr))
				&&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0))))
			{
				V.add(thisThang);
				if((--numLeft[0])<=0) return true;
			}
		}
		else
		{
			if(containsString(thisThang.name(),srchStr)
			   &&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0))))
			{
				V.add(thisThang);
				if((--numLeft[0])<=0) return true;
			}
		}
		//NOTE: Not sure if this line is a good idea!
		if((thisThang instanceof Body)&&(((Body)thisThang).mob()!=null))
			if(thingCheck(V, ((Body)thisThang).mob(), srchStr, allFlag, exact, 0, numLeft))
				return true;
		if(maxDepth>0)
		{
			ItemCollection col=ItemCollection.DefaultItemCol.getFrom(thisThang);
			if(col!=null)
				if(thingArrayCheck(V, (Item[])col.allItems().toArray(), srchStr, allFlag, exact, maxDepth-1, numLeft))
					return true;
			if(thisThang instanceof Room)
			{
				Room R=(Room)thisThang;
				for(int i=0;i<R.numExits();i++)
				{
					Exit E=R.getExit(i);
					if((E!=null)&&(thingCheck(E, srchStr, allFlag, exact, maxDepth-1, numLeft)))
						return true;
				}
			}
		}
		return false;
	}
	protected boolean thingArrayCheck(Vector<? extends Interactable> V, Interactable[] stuff, String srchStr, boolean allFlag, boolean exact, int maxDepth, int[] numLeft)
	{
		for(Interactable I : stuff)
		{
			if(I==null) continue;
			if(thingCheck(V, I, srchStr, allFlag, exact, maxDepth, numLeft)) return true;
		}
		return false;
	}
	public Vector<Interactable> fetchInteractables(String srchStr, boolean exactOnly, int maxDepth, int toFind, Object... list);
	{
		Vector<? extends Interactable> V=new Vector();
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;
		srchStr=(String)flags[FLAG_STR];
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		int[] numLeft={toFind};
		if(srchStr.startsWith("$")) srchStr=srchStr.substring(1);
		if(srchStr.endsWith("$")) srchStr=srchStr.substring(0,srchStr.length()-1);
		for(Object O : list)
		{
			if(O instanceof Interactable)
				{ if(thingCheck(V, (Interactable)O, srchStr, allFlag, exactOnly, maxDepth, numLeft)) return V; }
			else if((maxDepth>0)&&(O instanceof ItemCollection))
				{ if(thingArrayCheck(V, (Interactable[])((ItemCollection)O).allItems().toArray(), srchStr, allFlag, exactOnly, maxDepth-1, numLeft)) return V; }
			else if((maxDepth>0)&&(O instanceof ItemCollection.ItemHolder))
				{ if(thingArrayCheck(V, (Interactable[])((ItemCollection.ItemHolder)O).getItemCollection().allItems().toArray(), srchStr, allFlag, exactOnly, maxDepth-1, numLeft)) return V; }
			else if(O instanceof Vector)
				{ if(thingArrayCheck(V, (Interactable[])((Vector)O).toArray(), srchStr, allFlag, exactOnly, maxDepth, numLeft)) return V; }
		}
		return V;
	}
	public Interactable fetchInteractable(String srchStr, boolean exactOnly, int maxDepth, Object... list)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=(String)flags[FLAG_STR];
		int[] myOccurrance={((Integer)flags[FLAG_DOT]).intValue()};
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		Interactable thisThang=null;
		if(srchStr.startsWith("$")) srchStr=srchStr.substring(1);
		if(srchStr.endsWith("$")) srchStr=srchStr.substring(0,srchStr.length()-1);
		for(Object O : list)
		{
			if(O instanceof Interactable)
				thisThang=thingCheck((Interactable)O, srchStr, allFlag, exactOnly, myOccurrance, maxDepth);
			else if((maxDepth>0)&&(O instanceof ItemCollection))
				thisThang=thingArrayCheck((Interactable[])((ItemCollection)O).allItems().toArray(), srchStr, allFlag, exactOnly, myOccurrance, maxDepth-1);
			else if((maxDepth>0)&&(O instanceof ItemCollection.ItemHolder))
				thisThang=thingArrayCheck((Interactable[])((ItemCollection.ItemHolder)O).getItemCollection().allItems().toArray(), srchStr, allFlag, exactOnly, myOccurrance, maxDepth-1);
			else if(O instanceof Vector)
				thisThang=thingArrayCheck((Interactable[])((Vector)O).toArray(), srchStr, allFlag, exactOnly, myOccurrance, maxDepth);
			if(thisThang!=null) return thisThang;
		}
		return null;
	}
	protected Interactable thingCheck(Interactable thisThang, String srchStr, boolean allFlag, boolean exact, int[] myOccurrance, int maxDepth)
	{
		if(exact)
		{
			if(thisThang.ID().equalsIgnoreCase(srchStr)||thisThang.name().equalsIgnoreCase(srchStr))
				if((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0)))
					if((--myOccurrance[0])<=0)
						return thisThang;
		}
		else
		{
			if(containsString(thisThang.name(),srchStr)
			   &&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0))))
				if((--myOccurrance[0])<=0)
					return thisThang;
		}
		//NOTE: Not sure if this line is a good idea! Will check mobs instead of just bodys, but not mobs inventories
		if((thisThang instanceof Body)&&(((Body)thisThang).mob()!=null))
			return thingCheck(((Body)thisThang).mob(), srchStr, allFlag, exact, myOccurrance, 0);
		if(maxDepth>0)
		{
			Interactable found=null;
			ItemCollection col=ItemCollection.DefaultItemCol.getFrom(thisThang);
			if(col!=null)
			{
				found=thingArrayCheck((Item[])col.allItems().toArray(), srchStr, allFlag, exact, myOccurrance, maxDepth-1);
				if(found!=null) return found;
			}
			if(thisThang instanceof Room)
			{
				Room R=(Room)thisThang;
				for(int i=0;i<R.numExits();i++)
				{
					Exit E=R.getExit(i);
					if(E!=null) found=thingCheck(E, srchStr, allFlag, exact, myOccurrance, maxDepth-1);
					if(found!=null) return found;
				}
			}
		}
		return null;
	}
	protected Interactable thingArrayCheck(Interactable[] stuff, String srchStr, boolean allFlag, boolean exact, int[] myOccurrance, int maxDepth)
	{
		Interactable thing=null;
		for(Interactable I : stuff)
		{
			if(I==null) continue;
			if((thing=thingCheck(I, srchStr, allFlag, exact, myOccurrance, maxDepth))!=null)
				break;
		}
		return thing;
	}

	public Vector<Interactable> fetchInteractables(Vector<? extends Interactable> list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		Vector matches=new Vector(1);
		if(flags==null) return matches;
		
		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		Interactable thisThang=null;
		if(exactOnly)
		{
			if(srchStr.startsWith("$")) srchStr=srchStr.substring(1);
			if(srchStr.endsWith("$")) srchStr=srchStr.substring(0,srchStr.length()-1);
			try
			{
				for(int i=0;i<list.size();i++)
				{
					thisThang=list.elementAt(i);
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||thisThang.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0)))
							if((--myOccurrance)<=0)
								matches.addElement(thisThang);
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					thisThang=list.elementAt(i);
					if(containsString(thisThang.name(),srchStr)
					   &&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0))))
						if((--myOccurrance)<=0)
							matches.addElement(thisThang);
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			if(matches.size()==0)
			{
				myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
				try
				{
					for(int i=0;i<list.size();i++)
					{
						thisThang=list.elementAt(i);
						if((containsString(thisThang.displayText(),srchStr)
							||((thisThang instanceof MOB)&&containsString(((MOB)thisThang).genericName(),srchStr))))
								if((--myOccurrance)<=0)
									matches.addElement(thisThang);
					}
				}
				catch(java.lang.ArrayIndexOutOfBoundsException x){}
			}
		}
		return matches;
	}
	public int getContextNumber(Object[] list, Interactable E){ return getContextNumber(CMParms.makeVector(list),E);}
	public int getContextNumber(Vector<? extends Interactable> list, Interactable E)
	{
		if(list==null) return 0;
		Vector<? extends Interactable> V=(Vector)list.clone();
		int context=1;
		for(int v=0;v<V.size();v++)
			if(V.elementAt(v).name().equalsIgnoreCase(E.name()))
			{
				if(V.elementAt(v)==E)
					return context<2?0:context;
				if((!(V.elementAt(v) instanceof Item))
				||(!(E instanceof Item))
				||(((Item)E).container()==((Item)V.elementAt(v)).container()))
					context++;
			}
		return -1;
	}
	public int getContextSameNumber(Object[] list, Interactable E){ return getContextSameNumber(CMParms.makeVector(list),E);}
	public int getContextSameNumber(Vector<? extends Interactable> list, Interactable E)
	{
		if(list==null) return 0;
		Vector<? extends Interactable> V=(Vector)list.clone();
		int context=1;
		for(int v=0;v<V.size();v++)
			if(V.elementAt(v).name().equalsIgnoreCase(E.name()))
			{
				if(E.sameAs(V.elementAt(v)))
					return context<2?0:context;
				if((!(V.elementAt(v) instanceof Item))
				||(!(E instanceof Item))
				||(((Item)E).container()==((Item)V.elementAt(v)).container()))
					context++;
			}
		return -1;
	}
	public String getContextName(Object[] list, Interactable E){ return getContextName(CMParms.makeVector(list),E);}
	public String getContextName(Vector<? extends Interactable> list, Interactable E)
	{
		if(list==null) return E.name();
		int number=getContextNumber(list,E);
		if(number<0) return null;
		if(number<2) return E.name();
		return E.name()+"."+number;
	}

	public String getContextSameName(Object[] list, Interactable E){ return getContextName(CMParms.makeVector(list),E);}
	public String getContextSameName(Vector list, Interactable E)
	{
		if(list==null) return E.name();
		int number=getContextSameNumber(list,E);
		if(number<0) return null;
		if(number<2) return E.name();
		return E.name()+"."+number;
	}

	public Interactable fetchInteractable(Interactable[] list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		if(exactOnly)
		{
			if(srchStr.startsWith("$")) srchStr=srchStr.substring(1);
			if(srchStr.endsWith("$")) srchStr=srchStr.substring(0,srchStr.length()-1);
			for(Interactable thisThang : list)
				if(   (thisThang!=null)
					&&(thisThang.ID().equalsIgnoreCase(srchStr)||thisThang.name().equalsIgnoreCase(srchStr))
					&&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0)))
					&&((--myOccurrance)<=0) )
					return thisThang;
		}
		else
		{
			for(Interactable thisThang : list)
				if(   (thisThang!=null)
					&&(containsString(thisThang.name(),srchStr))
					&&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0)))
					&&((--myOccurrance)<=0) )
					return thisThang;
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(Interactable thisThang : list)
				if(   (thisThang!=null)
					&&((containsString(thisThang.displayText(),srchStr))
						||((thisThang instanceof MOB)&&containsString(((MOB)thisThang).genericName(),srchStr)))
					&&((--myOccurrance)<=0) )
					return thisThang;
		}
		return null;
	}

	//TODO: In general this routine should be improved, lots of potential problems from it
	public Vector fetchItemList(Interactable from,
								MOB mob,
								Item container,
								Vector commands,
								boolean visionMatters)	//Currently this is ignored!
	{
		int addendum=1;
		String addendumStr="";
		Vector V=new Vector();

		int maxToItem=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int((String)commands.firstElement())>0))
		{
			maxToItem=CMath.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
		}

		String name=CMParms.combine(commands,0);
		boolean allFlag=(commands.size()>0)?((String)commands.elementAt(0)).equalsIgnoreCase("all"):false;
		if(name.toUpperCase().startsWith("ALL.")){ allFlag=true; name="ALL "+name.substring(4);}
		if(name.toUpperCase().endsWith(".ALL")){ allFlag=true; name="ALL "+name.substring(0,name.length()-4);}
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToItem)))
		{
			doBugFix=false;
			Interactable item=null;
			if(from instanceof MOB)
				item=((MOB)from).fetchInventory(name+addendumStr);
			else
			if(from instanceof Room)	//TODO: This should check room exits too.
				item=((Room)from).fetchItem(name+addendumStr);
			if((item!=null)
			&&(item instanceof Item)
//			&&((!visionMatters)||(CMLib.flags().canBeSeenBy(item.getEnvObject(),mob)))
			&&(!V.contains(item)))
				V.addElement(item);
			if(item==null) break;
			addendumStr="."+(++addendum);
		}
		return V;
	}

	public int getPartitionIndex(Vector<String> commands, String partitionName)
	{
		for(int i=1; i<commands.size()-1; i++)
			if(partitionName.equalsIgnoreCase(commands.get(i)))
			{
				commands.remove(i);
				return i;
			}
		return -1;
	}
	public int getPartitionIndex(Vector<String> commands, String partitionName, int defaultTo)
	{
		for(int i=1; i<commands.size()-1; i++)
			if(partitionName.equalsIgnoreCase(commands.get(i)))
			{
				commands.remove(i);
				return i;
			}
		return defaultTo;
	}

	public Vector<Container> possibleContainers(MOB mob, Vector commands, boolean withContentOnly)
	{
		Vector<Container> V=new Vector();
		if(commands.size()==1)
			return V;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>0;i--)
			if(((String)commands.elementAt(i)).equalsIgnoreCase("from"))
			{
				fromDex=i;
				containerDex=i+1;
				if(((containerDex+1)<commands.size())
				&&((((String)commands.elementAt(containerDex)).equalsIgnoreCase("all"))
				||(CMath.s_int((String)commands.elementAt(containerDex))>0)))
					containerDex++;
				break;
			}

		String possibleContainerID=CMParms.combine(commands,containerDex);

		boolean allFlag=false;
		String preWord="";
		if(possibleContainerID.equalsIgnoreCase("all"))
			allFlag=true;
		else
		if(containerDex>1)
			preWord=(String)commands.elementAt(containerDex-1);

		int maxContained=Integer.MAX_VALUE;
		if(CMath.s_int(preWord)>0)
		{
			maxContained=CMath.s_int(preWord);
			commands.setElementAt("all",containerDex-1);
			containerDex--;
			preWord="all";
		}

		if(preWord.equalsIgnoreCase("all")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID;}
		else
		if(possibleContainerID.toUpperCase().startsWith("ALL.")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(4);}
		else
		if(possibleContainerID.toUpperCase().endsWith(".ALL")){ allFlag=true; possibleContainerID="ALL "+possibleContainerID.substring(0,possibleContainerID.length()-4);}

		int addendum=1;
		String addendumStr="";
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxContained)))
		{
			doBugFix=false;
			Interactable thisThang=mob.location().fetchItem(possibleContainerID+addendumStr);
			ItemCollection o=ItemCollection.DefaultItemCol.getFrom(thisThang);
			if((o!=null)
			&&((!withContentOnly)||(o.numItems()>0)))
//			&&(CMLib.flags().canBeSeenBy(thisThang.getEnvObject(),mob)||mob.isMine(thisThang))
			{
				V.addElement(thisThang);
				if(V.size()==1)
				{
					while((fromDex>=0)&&(commands.size()>fromDex))
						commands.removeElementAt(fromDex);
					while(commands.size()>containerDex)
						commands.removeElementAt(containerDex);
					preWord="";
				}
			}
			if(thisThang==null)
				return V;
			addendumStr="."+(++addendum);
		}
		return V;
	}

	public Container possibleContainer(MOB mob, Vector commands, boolean withStuff)
	{
		if(commands.size()==1)
			return null;

		int fromDex=-1;
		int containerDex=commands.size()-1;
		for(int i=commands.size()-2;i>=1;i--)
			if(((String)commands.elementAt(i)).equalsIgnoreCase("from"))
			{ fromDex=i; containerDex=i+1;  break;}
		String possibleContainerID=CMParms.combine(commands,containerDex);

		Interactable thisThang=mob.location().fetchItem(possibleContainerID);
		ItemCollection o=ItemCollection.DefaultItemCol.getFrom(thisThang);
		if((o!=null)
		&&((!withStuff)||(o.numItems()>0)))
		{
			while((fromDex>=0)&&(commands.size()>fromDex))
				commands.removeElementAt(fromDex);
			while(commands.size()>containerDex)
				commands.removeElementAt(containerDex);
			return (Container)thisThang;
		}
		return null;
	}

	public String returnTime(long millis, long ticks)
	{
		String avg="";
		if(ticks>0)
			avg=", Average="+(millis/ticks)+"ms";
		if(millis<1000) return millis+"ms"+avg;
		long seconds=millis/1000;
		millis-=(seconds*1000);
		if(seconds<60) return seconds+"s "+millis+"ms"+avg;
		long minutes=seconds/60;
		seconds-=(minutes*60);
		if(minutes<60) return minutes+"m "+seconds+"s "+millis+"ms"+avg;
		long hours=minutes/60;
		minutes-=(hours*60);
		if(hours<24) return hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
		long days=hours/24;
		hours-=(days*24);
		return days+"d "+hours+"h "+minutes+"m "+seconds+"s "+millis+"ms"+avg;
	}

	//Return max_value if not specified. Return a value + command "all" if specified. Return -1 if fail.
	//TODO: Reprogram this and make it sensible.
	public int calculateMaxToGive(MOB mob, Vector commands, boolean breakPackages, Interactable checkWhat, boolean getOnly)
	{
		int maxToGive=Integer.MAX_VALUE;
		if((commands.size()>1)
//		&&(numPossibleGold(mob,CMParms.combine(commands,0))==0)
		&&(CMath.s_int((String)commands.firstElement())>0))
		{
			maxToGive=CMath.s_int((String)commands.firstElement());
			commands.setElementAt("all",0);
			if(breakPackages)
			{
				String packCheckName=CMParms.combine(commands,1);
				Interactable fromWhat=null;
				if(checkWhat instanceof MOB)
					fromWhat=mob.fetchInventory(packCheckName);
				else
				if(checkWhat instanceof Room)
					fromWhat=((Room)checkWhat).fetchItem(packCheckName);
				if(fromWhat instanceof Item)
				{
					int max=3000;
					if(maxToGive>max)
					{
						mob.tell("You can only handle "+max+" at a time.");
						return -1;
					}
//					Interactable toWhat=CMLib.materials().unbundle((Item)fromWhat,maxToGive);
					if(getOnly&&mob.isMine(fromWhat)&&mob.isMine(toWhat))
					{
						mob.tell("Ok");
						return -1;
					}
					else
					if(commands.size()==1)
						commands.addElement(toWhat.name());
					else
					{
						Object o=commands.firstElement();
						commands.clear();
						commands.addElement(o);
						commands.addElement(toWhat.name());
					}
				}
			}
		}
		return maxToGive;
	}

/*
	public boolean evokedBy(Ability thisAbility, String thisWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
				return true;
		}
		return false;
	}
	private String collapsedName(Ability thisAbility)
	{
		int x=thisAbility.name().indexOf(" ");
		if(x>=0)
			return CMStrings.replaceAll(thisAbility.name()," ","");
		return thisAbility.Name();
	}
	public boolean evokedBy(Ability thisAbility, String thisWord, String secondWord)
	{
		for(int i=0;i<thisAbility.triggerStrings().length;i++)
		{
			if(thisAbility.triggerStrings()[i].equalsIgnoreCase(thisWord))
			{
				if(((thisAbility.name().toUpperCase().startsWith(secondWord)))
				||(collapsedName(thisAbility).toUpperCase().startsWith(secondWord)))
					return true;
			}
		}
		return false;
	}
	public String getAnEvokeWord(MOB mob, String word)
	{
		return null;
	}
	public Ability getToEvoke(MOB mob, Vector commands)
	{
		return null;
	}
	public boolean preEvoke(MOB mob, Vector commands, int secondsElapsed, double actionsRemaining)
	{
		commands=(Vector)commands.clone();
		Ability evokableAbility=getToEvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell("You don't know how to do that.");
			return false;
		}
		return evokableAbility.preInvoke(mob,commands,null,false,0,secondsElapsed,actionsRemaining);
	}
	public void evoke(MOB mob, Vector commands)
	{
		Ability evokableAbility=getToEvoke(mob,commands);
		if(evokableAbility==null)
		{
			mob.tell("You don't know how to do that.");
			return;
		}
		evokableAbility.invoke(mob,commands,null,false,0);
	}
	public String numPossibleGoldCurrency(Interactable mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			long num=CMath.s_long(itemID);
			if(mine instanceof MOB)
			{
				Vector V=CMLib.beanCounter().getStandardCurrency((MOB)mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
					if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
						return ((Coins)V.elementAt(v)).getCurrency();
				V=CMLib.beanCounter().getStandardCurrency((MOB)mine,null);
				for(int v=0;v<V.size();v++)
					if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
						return ((Coins)V.elementAt(v)).getCurrency();
			}
			return CMLib.beanCounter().getCurrency(mine);
		}
		Vector V=CMParms.parse(itemID);
		if((V.size()>1)&&(CMath.isInteger((String)V.firstElement())))
			return matchAnyCurrencySet(CMParms.combine(V,1));
		else
		if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
			return matchAnyCurrencySet(CMParms.combine(V,1));
		else
		if(V.size()>0)
			return matchAnyCurrencySet(CMParms.combine(V,0));
		return CMLib.beanCounter().getCurrency(mine);
	}
	public Object[] parseMoneyStringSDL(MOB mob, String amount, String correctCurrency)
	{
		double b=0;
		String myCurrency=CMLib.beanCounter().getCurrency(mob);
		double denomination=1.0;
		if(correctCurrency==null) correctCurrency=myCurrency;
		if(amount.length()>0)
		{
			myCurrency=numPossibleGoldCurrency(mob,amount);
			if(myCurrency!=null)
			{
				denomination=CMLib.english().numPossibleGoldDenomination(null,correctCurrency,amount);
				long num=CMLib.english().numPossibleGold(null,amount);
				b=denomination*num;
			}
			else
				myCurrency=CMLib.beanCounter().getCurrency(mob);
		}
		return new Object[]{myCurrency,Double.valueOf(denomination),Long.valueOf(Math.round(b/denomination))};
	}
	public double numPossibleGoldDenomination(Environmental mine, String currency, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			long num=CMath.s_long(itemID);
			if(mine instanceof MOB)
			{
				Vector V=CMLib.beanCounter().getStandardCurrency((MOB)mine,currency);
				for(int v=0;v<V.size();v++)
					if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
						return ((Coins)V.elementAt(v)).getDenomination();
			}
			return CMLib.beanCounter().getLowestDenomination(currency);
		}
		Vector V=CMParms.parse(itemID);
		if((V.size()>1)&&(CMath.isInteger((String)V.firstElement())))
			return matchAnyDenomination(currency,CMParms.combine(V,1));
		else
		if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
			return matchAnyDenomination(currency,CMParms.combine(V,1));
		else
		if(V.size()>0)
			return matchAnyDenomination(currency,CMParms.combine(V,0));
		return 0;
	}
	public long numPossibleGold(MOB mine, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		if(CMath.isInteger(itemID))
		{
			long num=CMath.s_long(itemID);
			if(mine!=null)
			{
				Vector V=CMLib.beanCounter().getStandardCurrency(mine,CMLib.beanCounter().getCurrency(mine));
				for(int v=0;v<V.size();v++)
					if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
						return num;
				V=CMLib.beanCounter().getStandardCurrency(mine,null);
				for(int v=0;v<V.size();v++)
					if(((Coins)V.elementAt(v)).getNumberOfCoins()>=num)
						return num;
			}
			return CMath.s_long(itemID);
		}
		Vector V=CMParms.parse(itemID);
		if((V.size()>1)
		&&((CMath.isInteger((String)V.firstElement()))
		&&(matchAnyCurrencySet(CMParms.combine(V,1))!=null)))
			return CMath.s_long((String)V.firstElement());
		else
		if((V.size()>1)&&(((String)V.firstElement()).equalsIgnoreCase("all")))
		{
			String currency=matchAnyCurrencySet(CMParms.combine(V,1));
			if(currency!=null)
			{
				if(mine!=null)
				{
					Vector V2=CMLib.beanCounter().getStandardCurrency(mine,currency);
					double denomination=matchAnyDenomination(currency,CMParms.combine(V,1));
					Coins C=null;
					for(int v2=0;v2<V2.size();v2++)
					{
						C=(Coins)V2.elementAt(v2);
						if(C.getDenomination()==denomination)
							return C.getNumberOfCoins();
					}
				}
				return 1;
			}
		}
		else
		if((V.size()>0)&&(matchAnyCurrencySet(CMParms.combine(V,0))!=null))
			return 1;
		return 0;
	}
	public Item bestPossibleGold(MOB mob, Container container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		double denomination=0.0;
		String currency=CMLib.beanCounter().getCurrency(mob);
		if(CMath.isInteger(itemID))
		{
			gold=CMath.s_long(itemID);
			double totalAmount=CMLib.beanCounter().getTotalAbsoluteValue(mob,currency);
			double bestDenomination=CMLib.beanCounter().getBestDenomination(currency,(int)gold,totalAmount);
			if(bestDenomination==0.0)
			{
				bestDenomination=CMLib.beanCounter().getBestDenomination(null,(int)gold,totalAmount);
				if(bestDenomination>0.0)
					currency=null;
			}
			if(bestDenomination==0.0)
				return null;
			denomination=bestDenomination;
		}
		else
		{
			Vector V=CMParms.parse(itemID);
			if(V.size()<1) return null;
			if((!CMath.isInteger((String)V.firstElement()))
			&&(!((String)V.firstElement()).equalsIgnoreCase("all")))
				V.insertElementAt("1",0);
			Item I=mob.fetchInventory(container,CMParms.combine(V,1));
			if(I instanceof Coins)
			{
				if(((String)V.firstElement()).equalsIgnoreCase("all"))
					gold=((Coins)I).getNumberOfCoins();
				else
					gold=CMath.s_long((String)V.firstElement());
				currency=((Coins)I).getCurrency();
				denomination=((Coins)I).getDenomination();
			}
			else
				return null;
		}
		if(gold>0)
		{
			double amt = CMLib.beanCounter().getTotalAbsoluteValue(mob, currency);
			if(amt>=(denomination*gold))
			{
				double expectedAmt = amt - (denomination*gold);
				CMLib.beanCounter().subtractMoney(mob,currency,denomination,denomination*gold);
				double newAmt = CMLib.beanCounter().getTotalAbsoluteValue(mob, currency);
				if(newAmt > expectedAmt)
					CMLib.beanCounter().subtractMoney(mob,currency,(newAmt - expectedAmt));
				Coins C=(Coins)CMClass.getItem("StdCoins");
				C.setCurrency(currency);
				C.setDenomination(denomination);
				C.setNumberOfCoins(gold);
//				C.recoverEnvStats();
				//TODO: This will need to be improved.
				mob.getItemCollection().addItem(C);
				return C;
			}
			mob.tell("You don't have that much "+CMLib.beanCounter().getDenominationName(currency,denomination)+".");
			Vector V=CMLib.beanCounter().getStandardCurrency(mob,currency);
			for(int v=0;v<V.size();v++)
				if(((Coins)V.elementAt(v)).getDenomination()==denomination)
					return (Item)V.elementAt(v);
		}
		return null;
	}
	public String matchAnyCurrencySet(String itemID)
	{
		Vector V=CMLib.beanCounter().getAllCurrencies();
		Vector V2=null;
		for(int v=0;v<V.size();v++)
		{
			V2=CMLib.beanCounter().getDenominationNameSet((String)V.elementAt(v));
			for(int v2=0;v2<V2.size();v2++)
			{
				String s=(String)V2.elementAt(v2);
				if(s.toLowerCase().endsWith("(s)"))
					s=s.substring(0,s.length()-3)+"s";
				if(containsString(s,itemID))
					return (String)V.elementAt(v);
			}
		}
		return null;
	}

	public double matchAnyDenomination(String currency, String itemID)
	{
		MoneyLibrary.CMCurrency DV=CMLib.beanCounter().getCurrencySet(currency);
		itemID=itemID.toUpperCase();
		String s=null;
		if(DV!=null)
		{
			MoneyLibrary.MoneyDenomination MD=DV.find(itemID);
			if(MD!=null) return MD.value();
		}
		return 0.0;
	}
	public Item possibleRoomGold(MOB seer, Room room, Item container, String itemID)
	{
		if(itemID.toUpperCase().trim().startsWith("A PILE OF "))
			itemID=itemID.substring(10);
		long gold=0;
		if(CMath.isInteger(itemID))
		{
			gold=CMath.s_long(itemID);
			itemID="";
		}
		else
		{
			Vector V=CMParms.parse(itemID);
			if((V.size()>1)&&(CMath.isInteger((String)V.firstElement())))
				gold=CMath.s_long((String)V.firstElement());
			else
				return null;
			itemID=CMParms.combine(V,1);
		}
		if(gold>0)
		{
			for(int i=0;i<room.getItemCollection().numItems();i++)
			{
				Item I=room.getItemCollection().getItem(i);
				if((I.container()==container)
				&&(I instanceof Coins)
//				&&(CMLib.flags().canBeSeenBy(I.getEnvObject(),seer))
				&&((itemID.length()==0)||(containsString(I.name(),itemID))))
				{
					if(((Coins)I).getNumberOfCoins()<=gold)
						return I;
					((Coins)I).setNumberOfCoins(((Coins)I).getNumberOfCoins()-gold);
					Coins C=(Coins)CMClass.Objects.ITEM.getNew("StdCoins");
					C.setCurrency(((Coins)I).getCurrency());
					C.setNumberOfCoins(gold);
					C.setDenomination(((Coins)I).getDenomination());
					C.setContainer(container);
//					C.recoverEnvStats();
					room.getItemCollection().addItem(C);
					return C;
				}
			}
		}
		return null;
	}
	public int fetchInteractableIndex(Vector<? extends Interactable> list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return -1;

		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();
		Interactable thisThang=null;
		if(exactOnly)
		{
			if(srchStr.startsWith("$")) srchStr=srchStr.substring(1);
			if(srchStr.endsWith("$")) srchStr=srchStr.substring(0,srchStr.length()-1);
			try
			{
				for(int i=0;i<list.size();i++)
				{
					thisThang=list.elementAt(i);
					if(thisThang.ID().equalsIgnoreCase(srchStr)
					   ||thisThang.name().equalsIgnoreCase(srchStr))
						if((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0)))
							if((--myOccurrance)<=0)
								return i;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					thisThang=list.elementAt(i);
					if(containsString(thisThang.name(),srchStr)
					   &&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0))))
						if((--myOccurrance)<=0)
							return i;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			try
			{
				for(int i=0;i<list.size();i++)
				{
					thisThang=list.elementAt(i);
					if((containsString(thisThang.displayText(),srchStr)
						||((thisThang instanceof MOB)&&containsString(((MOB)thisThang).genericName(),srchStr))))
							if((--myOccurrance)<=0)
								return i;
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException x){}
		}
		return -1;
	}
	public Interactable fetchInteractable(Hashtable<String, Interactable> list, String srchStr, boolean exactOnly)
	{
		Object[] flags=fetchFlags(srchStr);
		if(flags==null) return null;

		srchStr=(String)flags[FLAG_STR];
		int myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
		boolean allFlag=((Boolean)flags[FLAG_ALL]).booleanValue();

		if(list.get(srchStr)!=null)
			return list.get(srchStr);
		Interactable thisThang=null;
		if(exactOnly)
		{
			if(srchStr.startsWith("$")) srchStr=srchStr.substring(1);
			if(srchStr.endsWith("$")) srchStr=srchStr.substring(0,srchStr.length()-1);
			for(Enumeration<Interactable> e=list.elements();e.hasMoreElements();)
			{
				thisThang=e.nextElement();
				if(thisThang.ID().equalsIgnoreCase(srchStr)
				||thisThang.name().equalsIgnoreCase(srchStr))
					if((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0)))
						if((--myOccurrance)<=0)
							return thisThang;
			}
		}
		else
		{
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(Enumeration<Interactable> e=list.elements();e.hasMoreElements();)
			{
				thisThang=e.nextElement();
				if((containsString(thisThang.name(),srchStr))
				&&((!allFlag)||((thisThang.displayText()!=null)&&(thisThang.displayText().length()>0))))
					if((--myOccurrance)<=0)
						return thisThang;
			}
			myOccurrance=((Integer)flags[FLAG_DOT]).intValue();
			for(Enumeration<Interactable> e=list.elements();e.hasMoreElements();)
			{
				thisThang=e.nextElement();
				if((containsString(thisThang.displayText(),srchStr))
				||((thisThang instanceof MOB)&&containsString(((MOB)thisThang).genericName(),srchStr)))
					if((--myOccurrance)<=0)
						return thisThang;
			}
		}
		return null;
	}
*/
}
