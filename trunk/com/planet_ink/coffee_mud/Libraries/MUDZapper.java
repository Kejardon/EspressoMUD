package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.lang.ref.SoftReference;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class MUDZapper extends StdLibrary
{
	public static final String DEFAULT_MASK_HELP =
		"+SYSOP (allow archons to bypass the rules)  \r\n"
		+"-SYSOP (always <WORD> archons)  \r\n"
		+"+SUBOP (allow archons or area staff to bypass the rules)  \r\n"
		+"-SUBOP (always <WORD> archons and area staff)  \r\n"
		+"-PLAYER (<WORD> all players) \r\n"
		+"-MOB (<WORD> all mobs/npcs)  \r\n"
		+"-thief -mage  -ranger (<WORD> only listed classes)\r\n"
		+"-RACE (<WORD> all races)  \r\n"
		+"-RACE +elf +dwarf +human +half +gnome (create exceptions)  \r\n"
		+"-elf -dwarf -human -half -gnome (<WORD> only listed races)  \r\n"
		+"-RACECAT (<WORD> all racial categories)  \r\n"
		+"-RACECAT +elf +insect +humanoid +canine +gnome (create exceptions)  \r\n"
		+"+RACECAT (do not <WORD> all racial categories)  \r\n"
		+"+RACECAT -elf -insect -humanoid -canine -gnome (create exceptions)  \r\n"
		+"-GENDER (<WORD> all genders)  \r\n"
		+"-GENDER +male +female +neuter (create exceptions)  \r\n"
		+"-male -female -neuter (<WORD> only listed genders)  \r\n"
		+"-SECURITY (<WORD> all security flags, even a lack of a security) \r\n"
		+"-SECURITY +cmdrooms +area cmditems etc..  (create exceptions)\r\n"
		+"+SECURITY (do not <WORD> any or no expertises) \r\n"
		+"+SECURITY -cmdrooms +area cmditems, etc.. (create exceptions) \r\n"
		+"-NAMES (<WORD> everyone) \r\n"
		+"-NAMES +bob \"+my name\" etc.. (create name exceptions) \r\n"
		+"+NAMES (do not <WORD> anyone who has a name) \r\n"
		+"+NAMES -bob \"-my name\" etc.. (create name exceptions) \r\n"
		+"-STR X (<WORD> those with strength greater than X)  \r\n"
		+"+STR X (<WORD> those with strength less than X)  \r\n"
		+"-INT X (<WORD> those with intelligence greater than X)  \r\n"
		+"+INT X (<WORD> those with intelligence less than X)  \r\n"
		+"-WIS X (<WORD> those with wisdom greater than X)  \r\n"
		+"+WIS X (<WORD> those with wisdom less than X)  \r\n"
		+"-CON X (<WORD> those with constitution greater than X)  \r\n"
		+"+CON X (<WORD> those with constitution less than X)  \r\n"
		+"-CHA X (<WORD> those with charisma greater than X)  \r\n"
		+"+CHA X (<WORD> those with charisma less than X)  \r\n"
		+"-DEX X (<WORD> those with dexterity greater than X)  \r\n"
		+"+DEX X (<WORD> those with dexterity less than X) \r\n"
		+"+-ADJSTR...ADJCON (Same as above, but uses current values) \r\n"
		+"-AREA (<WORD> in all areas) \r\n"
		+"-AREA \"+my areaname\" etc.. (create exceptions) \r\n"
		+"+AREA (do not <WORD> any areas) \r\n"
		+"+AREA \"-my areaname\" etc.. (create exceptions) \r\n"
		+"-HOME (<WORD> in all home/beacon areas) \r\n"
		+"-HOME \"+my home/beacon areaname\" etc.. (create exceptions) \r\n"
		+"+HOME (do not <WORD> any home/beacon areas) \r\n"
		+"+HOME \"-my home/beacon areaname\" etc.. (create exceptions) \r\n"
		+"-ITEM \"+item name\" etc... (<WORD> only those with an item name) \r\n"
		+"-WORN \"+item name\" etc... (<WORD> only those wearing item name) \r\n"
		+"-EFFECTS (<WORD> anyone, even no effects) \r\n"
		+"-EFFECTS +Sleep \"+Wood Chopping\" etc.. (create name exceptions) \r\n"
		+"+EFFECTS (do not <WORD> anyone, even non effected people) \r\n"
		+"+EFFECTS -Sleep \"-Wood Chopping\" etc.. (create name exceptions) \r\n"
		+"-MATERIAL \"+WOODEN\" etc.. (<WORD> only items of added materials) \r\n"
		+"+MATERIAL \"-WOODEN\" etc.. (Do not <WORD> items of -materials) \r\n"
		+"-RESOURCES \"+OAK\" etc.. (<WORD> only items of added resources) \r\n"
		+"+RESOURCES \"-OAK\" etc.. (Do not <WORD> items of -resources) \r\n"
		+"-JAVACLASS \"+GENMOB\" etc.. (<WORD> only objects of +java class) \r\n"
		+"+JAVACLASS \"-GENITEM\" etc.. (Do not <WORD> objs of -classes) \r\n"
		+"-VALUE X (<WORD> those with value or money less than X)  \r\n"
		+"+VALUE X (<WORD> those with value or money greater than X) \r\n"
		+"-WEIGHT X (<WORD> those weighing less than X)  \r\n"
		+"+WEIGHT X (<WORD> those weighing more than X) \r\n"
		+"-ARMOR X (<WORD> those with armor bonus less than X)  \r\n"
		+"+ARMOR X (<WORD> those with armor bonus more than X) \r\n"
		+"-DAMAGE X (<WORD> those with damage bonus less than X)  \r\n"
		+"+DAMAGE X (<WORD> those with damage bonus more than X) \r\n"
		+"-ATTACK X (<WORD> those with attack bonus less than X)  \r\n"
		+"+ATTACK X (<WORD> those with attack bonus more than X) \r\n"
		+"-WORNON \"+TORSO\" etc.. (<WORD> only items wearable on +locations) \r\n"
		+"+WORNON \"-NECK\" etc.. (Do not <WORD> items wearable on -locations) \r\n"
		+"-DISPOSITION \"+ISHIDDEN\" etc.. (<WORD> only with +dispositions) \r\n"
		+"+DISPOSITION \"-ISHIDDEN\" etc.. (Do not <WORD> only with sub disp) \r\n"
		+"-SENSES \"+CANSEEDARK\" etc.. (<WORD> only those with +senses) \r\n"
		+"+SENSES \"-CANSEEDARK\" etc.. (Do not <WORD> those with -senses) \r\n"
		+"-HOUR +X (<WORD> always, unless the hour is X)  \r\n"
		+"+HOUR -X (<WORD> those only when the hour is X) \r\n"
		+"-SEASON +FALL (<WORD> those only when season is FALL)  \r\n"
		+"+SEASON -SPRING (<WORD> those whenever the season is SPRING) \r\n"
		+"-MONTH +X (<WORD> those only when month number is X)  \r\n"
		+"+MONTH -X (<WORD> those whenever the month number is X) \r\n"
		+"-DAY +X (<WORD> those only when day number is X)  \r\n"
		+"+DAY -X (<WORD> those whenever the day number is X)  \r\n"
		+"-WEATHER +DROUGHT (<WORD> those only when weather is DROUGHT)  \r\n"
		+"+WEATHER -BLIZZARD (<WORD> those whenever the weather is BLIZZARD) \r\n"
		+"-CHANCE 50 (<WORD> the given % of the time)";

	public String ID(){return "MUDZapper";}

	protected MOB nonCrashingMOB=null;
	protected MOB nonCrashingMOB(){
		if(nonCrashingMOB!=null)
			return nonCrashingMOB;
		nonCrashingMOB=CMClass.CREATURE.get("StdMOB");
		return nonCrashingMOB;
	}

	protected Item nonCrashingItem=null;
	protected Item nonCrashingItem(MOB mob){
		if(mob.getItemCollection().numItems()>0)
		{
			Item I = mob.getItemCollection().getItem(0);
			if(I!=null) return I;
		}
		if(nonCrashingItem!=null)
			return nonCrashingItem;
		nonCrashingItem=CMClass.ITEM.get("StdItem");
		return nonCrashingItem;
	}

	public String rawMaskHelp(){return DEFAULT_MASK_HELP;}

	protected Vector preCompiled(String str)
	{
		Hashtable<String, SoftReference<Vector>> H=(Hashtable)Resources.getResource("SYSTEM_HASHED_MASKS");
		if(H==null) synchronized(this)
		{
			H=(Hashtable)Resources.getResource("SYSTEM_HASHED_MASKS");
			if(H==null)
				{H=new Hashtable(); Resources.submitResource("SYSTEM_HASHED_MASKS",H);}
		}
		SoftReference<Vector> ref=H.get(str.trim());
		Vector V=null;
		if(ref!=null)
			V=ref.get();
		if(V==null)
		{
			V=maskCompile(str);
			H.put(str,new SoftReference(V));
		}
		return V;
	}

	public enum Mask
	{
		NAME, PLAYER, NPC, WEIGHT, EFFECT, SECURITY, SYSOP, //VALUE, 
		AREA, 
		HOUR, SEASON, MONTH, DAY, 
		JAVACLASS, 
		CHANCE,
		ALWAYS;
	}

	/*public String maskHelp(String word)
	{
		if((word==null)||(word.length()==0))
			word="disallow";
		String tag="HELPMASK_"+word;
		String helpVersion=Resources.getResource(tag);	//Case sensitive resource!
		if(helpVersion==null)
		{
			helpVersion=rawMaskHelp().replace("<WORD>",word);
			Resources.submitResource(tag, helpVersion);
		}
		return helpVersion;
	}*/

	protected int determineSeason(String str)
	{
		str=str.toUpperCase().trim();
		if(str.length()==0) return -1;
		for(int i=0;i<TimeClock.SEASON_DESCS.length;i++)
			if(TimeClock.SEASON_DESCS[i].startsWith(str))
				return i;
		return -1;
	}
	
	protected boolean fromHereEqual(Vector<String> V, char plusMinus, int fromHere, String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=V.get(v);
			if(str.length()==0) continue;
			if(CMClass.valueOf(Mask.class, str)!=null)
				return false;
			if(str.equalsIgnoreCase(plusMinus+find)) return true;
		}
		return false;
	}

	protected boolean fromHereStartsWith(Vector<String> V, char plusMinus, int fromHere, String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=V.get(v);
			if(str.length()==0) continue;
			if(CMClass.valueOf(Mask.class, str)!=null)
				return false;
			if(str.startsWith(plusMinus+find)) return true;
		}
		return false;
	}

	protected boolean fromHereEndsWith(Vector<String> V, char plusMinus, int fromHere, String find)
	{
		for(int v=fromHere;v<V.size();v++)
		{
			String str=V.get(v);
			if(str.length()==0) continue;
			if(CMClass.valueOf(Mask.class, str)!=null)
				return false;
			if((str.charAt(0)==plusMinus)&&str.endsWith(find))
				return true;
		}
		return false;
	}

	public String maskDesc(String text){return maskDesc(text,false);}

	public String maskDesc(String text, boolean skipFirstWord)
	{
		if(text.trim().length()==0) return "Anyone";
		StringBuilder buf=new StringBuilder("");
		Vector<String> V=CMParms.parse(text.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			String str=V.get(v);
			int val=-1;
			boolean positive=true;
			if(str.startsWith("+"))
				str=str.substring(1);
			else if(str.startsWith("-"))
			{
				str=str.substring(1);
				positive=false;
			}
			Mask m=(Mask)CMClass.valueOf(Mask.class, str);
			if(m!=null) switch(m)
			{
				case ALWAYS:
					if(positive)
						buf.append("Always allowed. ");
					else
						buf.append("Never allowed. ");
					break;
				case SECURITY:
					if(positive)
						buf.append((skipFirstWord?"The":"Requires")+" following security flag(s): ");
					else
						buf.append("Disallows the following security flag(s): ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("SECURITY"))
							break;
						buf.append(str2+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case HOUR:
					if(positive)
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following time(s) of the day: ");
					else
						buf.append("Disallowed during the following time(s) of the day: ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("HOUR"))
							break;
						buf.append(CMath.s_int(str2.trim())+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case SEASON: // +season
					if(positive)
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following season(s): ");
					else
						buf.append("Disallowed during the following season(s): ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("SEASON"))
							break;
						if(CMath.isInteger(str2.trim()))
						{
							int season=CMath.s_int(str2.trim());
							if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
								buf.append(TimeClock.SEASON_DESCS[season]+", ");
						}
						else
						{
							int season=determineSeason(str2.trim());
							if((season>=0)&&(season<TimeClock.SEASON_DESCS.length))
								buf.append(TimeClock.SEASON_DESCS[season]+", ");
						}
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case MONTH:
					if(positive)
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"during the following month(s): ");
					else
						buf.append("Disallowed during the following month(s): ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("MONTH"))
							break;
						buf.append(CMath.s_int(str2.trim())+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case DAY:
					if(positive)
						buf.append((skipFirstWord?"Only ":"Allowed only ")+"on the following day(s) of the month: ");
					else
						buf.append("Disallowed during the following day(s) of the month: ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("DAY"))
							break;
						buf.append(CMath.s_int(str2.trim())+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case JAVACLASS:
					if(positive)
						buf.append((skipFirstWord?"B":"Requires b")+"eing of the following type: ");
					else
						buf.append("Disallows being of the following type: ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("JAVACLASS"))
							break;
						buf.append(str2+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case NAME:
					if(positive)
						buf.append((skipFirstWord?"The":"Requires")+" following name(s): ");
					else
						buf.append("Disallows the following mob/player name(s): ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("NAME"))
							break;
						buf.append(str2+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case PLAYER:
					buf.append("Requires players.  ");
					break;
				case NPC:
					buf.append("Requires mobs/npcs.  ");
					break;
				case CHANCE:
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					buf.append((skipFirstWord?"":"Allowed ")+" "+val+"% of the time.  ");
					break;
/*				case VALUE:
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					if(positive)
						buf.append((skipFirstWord?"A":"Requires a")+" value of at least "+val+".  ");
					else
						buf.append((skipFirstWord?"A":"Requires a")+" value of at most "+val+".  ");
					break; */
				case WEIGHT:
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					if(positive)
						buf.append((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at least "+val+".  ");
					else
						buf.append((skipFirstWord?"A":"Requires a")+" weight/encumbrance of at most "+val+".  ");
					break;
				case AREA: // +Area
					if(positive)
						buf.append((skipFirstWord?"The":"Requires the")+" following area(s): ");
					else
						buf.append("Disallows the following area(s): ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("AREA"))
							break;
						buf.append(str2+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
				case EFFECT:
					if(positive)
						buf.append((skipFirstWord?"P":"Requires p")+"articipation in the following activities/effect(s): ");
					else
						buf.append("Disallows the following activities/effect(s): ");
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("EFFECT"))
							break;
						Effect A=CMClass.EFFECT.get(str2);
						if(A!=null)
							buf.append(A.ID()+", ");
//						else
//							buf.append(str2+", ");
					}
					if(buf.toString().endsWith(", "))
						buf.setLength(buf.length()-2);
					buf.append(".  ");
					break;
			}
		}

		if(buf.length()==0) buf.append("Anyone.");
		return buf.toString();
	}

	public boolean syntaxCheck(String mask, Vector errorSink)
	{
		if(mask.trim().length()==0) return true;
		Vector<String> V=CMParms.parse(mask.toUpperCase());
		for(int v=0;v<V.size();v++)
		{
			String str=V.get(v);
			if(CMClass.valueOf(Mask.class, str)!=null) return true;
		}
		errorSink.add("No valid zapper codes found.");
		return false;
	}

	public Vector maskCompile(String text)
	{
		Vector buf=new Vector();
		if(text.trim().length()==0) return buf;
		Vector<String> V=CMParms.parse(text.toUpperCase());
		boolean buildItemFlag=false;
		boolean buildRoomFlag=false;
		for(int v=0;v<V.size();v++)
		{
			String str=V.get(v);
			int val=-1;
			boolean positive=true;
			if(str.startsWith("+"))
				str=str.substring(1);
			else if(str.startsWith("-"))
			{
				str=str.substring(1);
				positive=false;
			}
			Vector entry=new Vector();
			buf.add(entry);
			entry.add(Boolean.valueOf(positive));
			Mask m=(Mask)CMClass.valueOf(Mask.class, str);
			if(m!=null) switch(m)
			{
				case EFFECT:
					entry.add(m);
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("EFFECT"))
						{
							v=v2-1;
							break;
						}
						else
						{
							CMObject A=CMClass.EFFECT.get(str2);
							if(A==null) A=CMClass.BEHAVIOR.get(str2);
							if(A!=null)
								entry.add(A.ID());
						}
						v=V.size();
					}
					break;
				case SECURITY:
				case AREA:
					buildRoomFlag=true;
				case NAME:
				case JAVACLASS:
					entry.add(m);
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals(m.toString()))
						{
							v=v2-1;
							break;
						}
						else
						entry.add(str2);
						v=V.size();
					}
					break;
				case SEASON:
					entry.add(m);
					buildRoomFlag=true;
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals("SEASON"))
						{
							v=v2-1;
							break;
						}
						else
						{
							if(CMath.isInteger(str2.trim()))
								entry.add(Integer.valueOf(CMath.s_int(str2.trim())));
							else
							if(determineSeason(str2.trim())>=0)
								entry.add(Integer.valueOf(determineSeason(str2.trim())));
						}
						v=V.size();
					}
					break;
				case HOUR:
				case MONTH:
				case DAY:
					entry.add(m);
					buildRoomFlag=true;
					for(int v2=v+1;v2<V.size();v2++)
					{
						String str2=V.get(v2);
						if(str2.equals(m.toString()))
						{
							v=v2-1;
							break;
						}
						else
						entry.add(Integer.valueOf(CMath.s_int(str2.trim())));
						v=V.size();
					}
					break;
				case ALWAYS:
				case SYSOP:
				case PLAYER:
				case NPC:
					entry.add(m);
					break;
//				case VALUE:
//					buildItemFlag=true;
				case CHANCE:
				case WEIGHT:
					val=((++v)<V.size())?CMath.s_int(V.get(v)):0;
					entry.add(m);
					entry.add(Integer.valueOf(val));
					break;
			}
		}
		for(int b=0;b<buf.size();b++)
			if(buf.get(b) instanceof Vector)
				((Vector)buf.get(b)).trimToSize();
		if(buf.size()>0)
			buf.add(0, new boolean[]{buildItemFlag,buildRoomFlag});
		else
			buf.add(new boolean[]{buildItemFlag,buildRoomFlag});
		buf.trimToSize();
		return buf;
	}
/*
	protected Room outdoorRoom(Area A)
	{
		Room R=null;
		for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
		{
			R=(Room)e.nextElement();
			if((R.domainType()&Room.INDOORS)==0) return R;
		}
		return A.getRandomMetroRoom();
	}

*/
	//TODO: Should work on this code eventually, make it not need noncrashing stuff
	public boolean maskCheck(String text, Interactable E, boolean actual){ return maskCheck(preCompiled(text),E,actual);}
	public boolean maskCheck(Vector cset, Interactable E, boolean actual)
	{
		if(E==null) return true;
		if((cset==null)||(cset.size()<2)) return true;
		MOB mob=(E instanceof MOB)?(MOB)E:nonCrashingMOB();
		boolean[] flags=(boolean[])cset.firstElement();
		Item item=flags[0]?((E instanceof Item)?(Item)E:nonCrashingItem(mob)):null;
		Room room = flags[1]?((E instanceof Area)?((Area)E).getRandomMetroRoom():CMLib.map().roomLocation(E)):null;
		if((mob==null)||(flags[0]&&(item==null))) 
			return false;
		for(int c=1;c<cset.size();c++)
		{
			Vector V=(Vector)cset.get(c);
			if(V.size()>0) try
			{
			boolean positive=true;
			if(V.get(0) instanceof Boolean) positive=((Boolean)V.remove(0)).booleanValue();
			switch((Mask)V.firstElement())
			{
			case ALWAYS:
				if(positive)
					return true;
				else
					return false;
			case SYSOP:
				if(positive)
				{
					if(CMSecurity.isASysOp(mob))
						return true;
				}
				else if(CMSecurity.isASysOp(mob))
					return false;
				break;
			case SECURITY:
				if(positive)
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(CMSecurity.isAllowed(mob,(String)V.get(v)))
						{ found=true; break;}
					if(!found) return false;
				}
				else
				{
					for(int v=1;v<V.size();v++)
						if(CMSecurity.isAllowed(mob,(String)V.get(v)))
						{ return false;}
				}
				break;
			case NAME:
				if(positive)
				{
					boolean found=false;
					String name=E.name();
					for(int v=1;v<V.size();v++)
						if(name.equalsIgnoreCase((String)V.get(v)))
						{ found=true; break; }
					if(!found) return false;
				}
				else
				{
					String name=E.name();
					for(int v=1;v<V.size();v++)
						if(name.equalsIgnoreCase((String)V.get(v)))
						{ return false;}
				}
				break;
			case PLAYER:
				if(mob.isMonster()) return false;
				break;
			case NPC:
				if(!mob.isMonster()) return false;
				break;
			case HOUR:
				if(positive)
				{
					boolean found=false;
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getTimeOfDay()==((Integer)V.get(v)).intValue())
						{ found=true; break;}
					if(!found) return false;
				}
				else
				{
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getTimeOfDay()==((Integer)V.get(v)).intValue())
							return false;
				}
				break;
			case SEASON:
				if(positive)
				{
					boolean found=false;
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getSeasonCode()==((Integer)V.get(v)).intValue())
						{ found=true; break;}
					if(!found) return false;
				}
				else
				{
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getSeasonCode()==((Integer)V.get(v)).intValue())
							return false;
				}
				break;
			case MONTH:
				if(positive)
				{
					boolean found=false;
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getMonth()==((Integer)V.get(v)).intValue())
						{ found=true; break;}
					if(!found) return false;
				}
				else
				{
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getMonth()==((Integer)V.get(v)).intValue())
							return false;
				}
				break;
			case DAY:
				if(positive)
				{
					boolean found=false;
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getDayOfMonth()==((Integer)V.get(v)).intValue())
						{ found=true; break;}
					if(!found) return false;
				}
				else
				{
					if(room!=null)
					for(int v=1;v<V.size();v++)
						if(room.getArea().getTimeObj().getDayOfMonth()==((Integer)V.get(v)).intValue())
							return false;
				}
				break;
			case JAVACLASS:
				if(positive)
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(E.ID().equalsIgnoreCase((String)V.get(v)))
						{ found=true; break;}
					if(!found) return false;
				}
				else
				{
					for(int v=1;v<V.size();v++)
						if(E.ID().equalsIgnoreCase((String)V.get(v)))
						{ return false;}
				}
				break;
			case EFFECT:
				if(positive)
				{
					boolean found=false;
					for(int v=1;v<V.size();v++)
						if(E.fetchEffect((String)V.get(v))!=null)
						{   found=true; break;}
					if(!found)
					for(int v=1;v<V.size();v++)
						if(E.fetchBehavior((String)V.get(v))!=null)
						{   found=true; break;}
					if(!found) return false;
				}
				else
				{
					for(int v=1;v<V.size();v++)
						if(E.fetchEffect((String)V.get(v))!=null)
							return false;
					for(int v=1;v<V.size();v++)
						if(E.fetchBehavior((String)V.get(v))!=null)
							return false;
				}
				break;
			case CHANCE:
				if((V.size()>1)&&(CMLib.dice().rollPercentage()>(((Integer)V.get(1)).intValue())))
					return false;
				break;
			case WEIGHT:
				if(positive)
				{
					if((V.size()>1)&&(E.getEnvObject().envStats().weight()<(((Integer)V.get(1)).intValue())))
						return false;
				}
				else if((V.size()>1)&&(E.getEnvObject().envStats().weight()>(((Integer)V.get(1)).intValue())))
					return false;
				break;
/*			case VALUE:
				if(positive)
				{
					if(E instanceof MOB)
					{
						if((V.size()>1)&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)<(((Integer)V.get(1)).intValue())))
						   return false;
					}
					else
					{
						if((V.size()>1)&&(item.baseGoldValue()<(((Integer)V.get(1)).intValue())))
							return false;
					}
				}
				else
				{
					if(E instanceof MOB)
					{
						if((V.size()>1)&&(CMLib.beanCounter().getTotalAbsoluteValueAllCurrencies(mob)>(((Integer)V.get(1)).intValue())))
							return false;
					}
					else
					{
						if((V.size()>1)&&(item.baseGoldValue()>(((Integer)V.get(1)).intValue())))
							return false;
					}
				}
				break; */
			case AREA:
				if(positive)
				{
					boolean found=false;
					if(room!=null)
						for(int v=1;v<V.size();v++)
							if(room.getArea().name().equalsIgnoreCase((String)V.get(v)))
							{ found=true; break;}
					if(!found) return false;
				}
				else
				{
					if(room!=null)
						for(int v=1;v<V.size();v++)
							if(room.getArea().name().equalsIgnoreCase((String)V.get(v)))
							{ return false;}
					break;
				}
			}
			}catch(NullPointerException n){}
		}
		return true;
	}
}