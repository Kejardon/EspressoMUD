package com.planet_ink.coffee_mud.Libraries;
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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.*;

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
public class MUDHelp extends StdLibrary implements HelpLibrary
{
	public String ID(){return "MUDHelp";}

	public StringBuilder getHelpText(String helpStr, MOB forMOB, boolean favorAHelp)
	{ return getHelpText(helpStr, forMOB, favorAHelp, false);}
	
	public StringBuilder getHelpText(String helpStr, MOB forMOB, boolean favorAHelp, boolean noFix)
	{
		if(helpStr.length()==0) return null;
		StringBuilder thisTag=null;
		if(favorAHelp)
		{
			if(getArcHelpFile().size()>0)
				thisTag=getHelpText(helpStr,getArcHelpFile(),forMOB,noFix);
			if(thisTag==null)
			{
				if(getHelpFile().size()==0) return null;
				thisTag=getHelpText(helpStr,getHelpFile(),forMOB,noFix);
			}
		}
		else
		{
			if(getHelpFile().size()>0)
				thisTag=getHelpText(helpStr,getHelpFile(),forMOB,noFix);
			if(thisTag==null)
			{
				if(getArcHelpFile().size()==0) return null;
				thisTag=getHelpText(helpStr,getArcHelpFile(),forMOB,noFix);
			}
		}
		return thisTag;
	}
	public void addHelpEntry(String ID, String text, boolean archon)
	{
		if(archon)
			getArcHelpFile().put(ID.toUpperCase(),text);
		else
			getHelpFile().put(ID.toUpperCase(),text);
	}

	public Vector getTopics(boolean archonHelp, boolean standardHelp)
	{
		Vector reverseList=new Vector();
		Properties rHelpFile=null;
		if(archonHelp)
			rHelpFile=getArcHelpFile();
		if(standardHelp)
		{
			if(rHelpFile==null)
				rHelpFile=getHelpFile();
			else
			{
				for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
				{
					String ptop = (String)e.nextElement();
					String thisTag=rHelpFile.getProperty(ptop);
					if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
						|| (rHelpFile.getProperty(thisTag)== null) )
							reverseList.addElement(ptop);
				}
				rHelpFile=getHelpFile();
			}
		}
		if(rHelpFile!=null)
		for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
		{
			String ptop = (String)e.nextElement();
			String thisTag=rHelpFile.getProperty(ptop);
			if ((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)
				|| (rHelpFile.getProperty(thisTag)== null) )
					reverseList.addElement(ptop);
		}
		Collections.sort(reverseList);
		return reverseList;
	}
	
	public String fixHelp(String tag, String str, MOB forMOB)
	{
		boolean worldCurrency=str.startsWith("<CURRENCIES>");
/*		if(str.startsWith("<CURRENCY>")||worldCurrency)
		{
			str=str.substring(worldCurrency?12:10);
			Vector currencies=new Vector();
			if((forMOB==null)||(forMOB.location()==null)||(worldCurrency))
			{
				worldCurrency=true;
				for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				{
					String currency=CMLib.beanCounter().getCurrency((Area)e.nextElement());
					if(!currencies.contains(currency))
						currencies.addElement(currency);
				}
			}
			else
				currencies.addElement(CMLib.beanCounter().getCurrency(forMOB.location()));
			StringBuilder help=new StringBuilder("");
			if(worldCurrency)
				help.append("\n\r"+CMStrings.padRight("World Currencies",20)+":");
			for(Enumeration e=currencies.elements();e.hasMoreElements();)
			{
				String currency=(String)e.nextElement();
				if(worldCurrency)
					help.append("\n\r"+CMStrings.padRight("Currency",20)+":");
				else
					help.append("\n\r"+CMStrings.padRight("Local Currency",20)+":");
				if(currency.length()==0)
					help.append("default");
				else
					help.append(CMStrings.capitalizeAndLower(currency));
				MoneyLibrary.MoneyDenomination denoms[]=CMLib.beanCounter().getCurrencySet(currency);
				for(int d=0;d<denoms.length;d++)
				{
					if(denoms[d].abbr.length()>0)
						help.append("\n\r"+CMStrings.padRight(denoms[d].name+" ("+denoms[d].abbr+")",20)+":");
					else
						help.append("\n\r"+CMStrings.padRight(denoms[d].name,20)+":");
					if(denoms[d].value==CMLib.beanCounter().getLowestDenomination(currency))
						help.append(" (exchange rate is "+denoms[d].value+" of base)");
					else
						help.append(" "+CMLib.beanCounter().getConvertableDescription(currency,denoms[d].value));
				}
				help.append("\n\r");
			}
			help.append(str);
			str=help.toString();
		}
*/		if(str.startsWith("<RACE>"))
		{
			str=str.substring(6);
			Race R=(Race)CMClass.Objects.RACE.get(tag);
			if(R==null) R=(Race)CMClass.Objects.RACE.get(tag.replace('_',' '));
			if(R!=null)
			{
				StringBuilder prepend=new StringBuilder("");
				int wrap = 0;
				if((forMOB!=null)&&(forMOB.session()!=null))
					wrap=forMOB.session().getWrap();
				if(wrap <=0 ) wrap=78;
				prepend.append("^HRace Name : ^N"+R.name()+" ^H(^N"+R.racialCategory()+"^H)^N");
				prepend.append("\n\r");
				
				String s=R.getStatAdjDesc();
				prepend.append(columnHelper("^HStat Mods.:^N",s,wrap));
				s=R.getSensesChgDesc();
				if(R.getDispositionChgDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getDispositionChgDesc();
				if(R.getAbilitiesDesc().length()>0)
					s+=((s.length()>0)?", ":"")+R.getAbilitiesDesc();
				prepend.append(columnHelper("^HAbilities :^N",s,wrap));
				prepend.append(columnHelper("^HLanguages :^N",R.getLanguagesDesc(),wrap));
//				prepend.append(columnHelper("^HLife Exp. :^N",R.getAgingChart()[Race.AGE_ANCIENT]+" years",wrap));
				prepend.append("^HDesc.     : ^N");
				str=prepend.toString()+"\n\r"+str;
			}
		}
		try{
			if(str!=null)
				return CMLib.httpUtils().doVirtualPage(str);
		}catch(com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException x){}
		return str;
	}

	public StringBuilder getHelpText(String helpStr, Properties rHelpFile, MOB forMOB)
	{ return getHelpText(helpStr,rHelpFile,forMOB,false);}
	public StringBuilder getHelpText(String helpStr, Properties rHelpFile, MOB forMOB, boolean noFix)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		String thisTag=null;

		Race R=(Race)CMClass.Objects.RACE.get(helpStr.toUpperCase());
		
		boolean found=false;
		if(thisTag==null) thisTag=rHelpFile.getProperty(helpStr);
		boolean areaTag=(thisTag==null)&&helpStr.startsWith("AREAHELP_");
		if(thisTag==null){thisTag=rHelpFile.getProperty("SPELL_"+helpStr); if(thisTag!=null) helpStr="SPELL_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PRAYER_"+helpStr); if(thisTag!=null) helpStr="PRAYER_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("SONG_"+helpStr); if(thisTag!=null) helpStr="SONG_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("DANCE_"+helpStr); if(thisTag!=null) helpStr="DANCE_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PLAY_"+helpStr); if(thisTag!=null) helpStr="PLAY_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("CHANT_"+helpStr); if(thisTag!=null) helpStr="CHANT_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("BEHAVIOR_"+helpStr); if(thisTag!=null) helpStr="BEHAVIOR_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("POWER_"+helpStr); if(thisTag!=null) helpStr="POWER_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("SKILL_"+helpStr); if(thisTag!=null) helpStr="SKILL_"+helpStr;}
		if(thisTag==null){thisTag=rHelpFile.getProperty("PROP_"+helpStr); if(thisTag!=null) helpStr="PROP_"+helpStr;}
		found=((thisTag!=null)&&(thisTag.length()>0));

		if(!found)
		{
			String ahelpStr=helpStr.replaceAll("_"," ").trim();
			if(areaTag) ahelpStr=ahelpStr.substring(9);
			for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
			{
				Area A=(Area)e.nextElement();
				if(A.name().equalsIgnoreCase(ahelpStr))
				{
					helpStr=A.name();
					found=true;
					areaTag=true;
					break;
				}
			}
		}
		
		if((!areaTag)&&(!found))
		{
			String ahelpStr=helpStr.replaceAll("_"," ").trim();
/*			if(!found)
			{ 
				String s=CMLib.socials().getSocialsHelp(forMOB,helpStr.toUpperCase(), true);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}
*/			// INEXACT searches start here
			if(!found)
				for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
				{
					String key=((String)e.nextElement()).toUpperCase();
					if(key.startsWith(helpStr))
					{
						thisTag=rHelpFile.getProperty(key);
						helpStr=key;
						found=true;
						break;
					}
				}
/*			if(!found)
			{
				String currency=CMLib.english().matchAnyCurrencySet(ahelpStr);
				if(currency!=null)
				{
					double denom=CMLib.english().matchAnyDenomination(currency,ahelpStr);
					if(denom>0.0)
					{
						Coins C2=CMLib.beanCounter().makeCurrency(currency,denom,1);
						if((C2!=null)&&(C2.description().length()>0))
							return new StringBuilder(C2.name()+" is "+C2.description().toLowerCase());
					}
				}
			}
*/
			if(!found)
				for(Enumeration e=rHelpFile.keys();e.hasMoreElements();)
				{
					String key=((String)e.nextElement()).toUpperCase();
					if(CMLib.english().containsString(key,helpStr))
					{
						thisTag=rHelpFile.getProperty(key);
						helpStr=key;
						found=true;
						break;
					}
				}
			
/*			if(!found)
			{ 
				String s=CMLib.socials().getSocialsHelp(forMOB,helpStr.toUpperCase(), false);
				if(s!=null)
				{
					thisTag=s;
					helpStr=helpStr.toUpperCase();
					found=true;
				}
			}
*/
			if(!found)
				for(Enumeration e=CMLib.map().areas();e.hasMoreElements();)
				{
					Area A=(Area)e.nextElement();
					if(CMLib.english().containsString(A.name(),ahelpStr))
					{
						helpStr=A.name();
						break;
					}
				}
		}
		while((thisTag!=null)&&(thisTag.length()>0)&&(thisTag.length()<31)&&(!areaTag))
		{
			String thisOtherTag=rHelpFile.getProperty(thisTag);
			if((thisOtherTag!=null)&&(thisOtherTag.equals(thisTag)))
				thisTag=null;
			else
			if(thisOtherTag!=null)
			{
				helpStr=thisTag;
				thisTag=thisOtherTag;
			}
			else
				break;
		}
		
		// the area exception
//		if((thisTag==null)||(thisTag.length()==0))
//			if(CMLib.map().getArea(helpStr.trim())!=null)
//				return new StringBuilder(CMLib.map().getArea(helpStr.trim()).getAreaStats().toString());
		
		// internal exceptions
		if((thisTag==null)||(thisTag.length()==0))
		{
			String s=CMLib.channels().getChannelName(helpStr.trim());
			boolean no=false;
			if(((s==null)||(s.length()==0))
			&&(helpStr.toLowerCase().startsWith("no")))
			{
				s=CMLib.channels().getChannelName(helpStr.trim().substring(2));
				no=true;
			}
			if((s!=null)&&(s.length()>0))
			{
				if(no)
					thisTag=rHelpFile.getProperty("NOCHANNEL");
				else
					thisTag=rHelpFile.getProperty("CHANNEL");
				thisTag=CMStrings.replaceAll(thisTag,"[CHANNEL]",s.toUpperCase());
				thisTag=CMStrings.replaceAll(thisTag,"[channel]",s.toLowerCase());
				String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
				thisTag=CMStrings.replaceAll(thisTag,"[EXTRA]",extra);
				return new StringBuilder(thisTag);
			}
		}
		
		if((thisTag==null)||(thisTag.length()==0))
			return null;
		if(noFix) return new StringBuilder(thisTag);
		return new StringBuilder(fixHelp(helpStr,thisTag,forMOB));
	}

	public StringBuilder getHelpList(String helpStr, 
								   Properties rHelpFile1, 
								   Properties rHelpFile2, 
								   MOB forMOB)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		Vector matches=new Vector();

		for(Enumeration e=rHelpFile1.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			String prop=rHelpFile1.getProperty(key,"");
			if((key.toUpperCase().indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
				matches.addElement(key.toUpperCase());
		}
		if(rHelpFile2!=null)
		for(Enumeration e=rHelpFile2.keys();e.hasMoreElements();)
		{
			String key=(String)e.nextElement();
			String prop=rHelpFile1.getProperty(key,"");
			if((key.toUpperCase().indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
				matches.addElement(key.toUpperCase());
		}
		if(matches.size()==0)
			return new StringBuilder("");
		return CMLib.lister().fourColumns(matches);
	}
	
	public Properties getArcHelpFile()
	{
		try
		{
			Properties arcHelpFile=(Properties)Resources.getResource("ARCHON HELP FILE");
			if(arcHelpFile==null)
			{
				arcHelpFile=new Properties();
				CMFile directory=new CMFile(Resources.buildResourcePath("help"),null,true);
				if((directory.canRead())&&(directory.isDirectory()))
				{
					String[] list=directory.list();
					for(int l=0;l<list.length;l++)
					{
						String item=list[l];
						if((item!=null)
						&&(item.length()>0)
						&&item.toUpperCase().endsWith(".INI")
						&&(item.toUpperCase().startsWith("ARC_")))
							arcHelpFile.load(new ByteArrayInputStream(new CMFile(Resources.buildResourcePath("help")+item,null,true).raw()));
					}
				}
				//DVector suspiciousPairs=suspiciousTags(arcHelpFile);
				//for(int d=0;d<suspiciousPairs.size();d++)
				//	Syst/em.out.pri/ntln(suspiciousPairs.elementAt(d,1)+": "+suspiciousPairs.elementAt(d,2));
				for(Enumeration e=arcHelpFile.keys();e.hasMoreElements();)
				{
					String key=(String)e.nextElement();
					String entry=(String)arcHelpFile.get(key);
					int x=entry.indexOf("<ZAP=");
					if(x>=0)
					{
						int y=entry.indexOf(">",x);
						if(y>(x+5))
						{
							String word=entry.substring(x+5,y).trim();
							entry=entry.substring(0,x)+CMLib.masking().maskHelp("\n\r",word)+entry.substring(y+1);
							arcHelpFile.remove(key);
							arcHelpFile.put(key,entry);
						}
					}
				}
				Resources.submitResource("ARCHON HELP FILE",arcHelpFile);
			}
			return arcHelpFile;
		}
		catch(IOException e)
		{
			Log.errOut("MUDHelp",e);
		}
		return new Properties();
	}

	protected DVector suspiciousTags(Properties p)
	{
		String k=null;
		DVector pairs=new DVector(2);
		for(Enumeration e=p.keys();e.hasMoreElements();)
		{
			k=(String)e.nextElement();
			for(int i=0;i<k.length();i++)
				if(Character.isLowerCase(k.charAt(i)))
				{
					pairs.addElement(k,p.get(k));
					break;
				}
		}
		return pairs;
	}
	
	public Properties getHelpFile()
	{
		try
		{
			Properties helpFile=(Properties)Resources.getResource("MAIN HELP FILE");
			if(helpFile==null)
			{
				helpFile=new Properties();
				CMFile directory=new CMFile(Resources.buildResourcePath("help"),null,true);
				if((directory.canRead())&&(directory.isDirectory()))
				{
					String[] list=directory.list();
					for(int l=0;l<list.length;l++)
					{
						String item=list[l];
						if((item!=null)
						&&(item.length()>0)
						&&item.toUpperCase().endsWith(".INI")
						&&(!item.toUpperCase().startsWith("ARC_")))
							helpFile.load(new ByteArrayInputStream(new CMFile(Resources.buildResourcePath("help")+item,null,true).raw()));
					}
				}
				//DVector suspiciousPairs=suspiciousTags(helpFile);
				//for(int d=0;d<suspiciousPairs.size();d++)
				//	Syst/em.out.pri/ntln(suspiciousPairs.elementAt(d,1)+": "+suspiciousPairs.elementAt(d,2));
				Resources.submitResource("MAIN HELP FILE",helpFile);
			}
			return helpFile;
		}
		catch(IOException e)
		{
			Log.errOut("MUDHelp",e);
		}
		return new Properties();
	}
	
	public boolean shutdown() {
		unloadHelpFile(null);
		return true;
	}
	public void unloadHelpFile(MOB mob)
	{
		if(Resources.getResource("PLAYER TOPICS")!=null)
			Resources.removeResource("PLAYER TOPICS");
		if(Resources.getResource("ARCHON TOPICS")!=null)
			Resources.removeResource("ARCHON TOPICS");
		if(Resources.getResource("help/help.txt")!=null)
			Resources.removeResource("help/help.txt");
		if(Resources.getResource("help/accts.txt")!=null)
			Resources.removeResource("help/accts.txt");
		if(Resources.getResource("text/races.txt")!=null)
			Resources.removeResource("text/races.txt");
		if(Resources.getResource("text/newacct.txt")!=null)
			Resources.removeResource("text/newacct.txt");
		if(Resources.getResource("text/selchar.txt")!=null)
			Resources.removeResource("text/selchar.txt");
		if(Resources.getResource("text/newchar.txt")!=null)
			Resources.removeResource("text/newchar.txt");
		if(Resources.getResource("text/doneacct.txt")!=null)
			Resources.removeResource("text/doneacct.txt");
		if(Resources.getResource("text/stats.txt")!=null)
			Resources.removeResource("text/stats.txt");
		if(Resources.getResource("text/classes.txt")!=null)
			Resources.removeResource("text/classes.txt");
		if(Resources.getResource("help/arc_help.txt")!=null)
			Resources.removeResource("help/arc_help.txt");
		if(Resources.getResource("MAIN HELP FILE")!=null)
			Resources.removeResource("MAIN HELP FILE");
		if(Resources.getResource("ARCHON HELP FILE")!=null)
			Resources.removeResource("ARCHON HELP FILE");

		// also the intro page
		CMFile introDir=new CMFile(Resources.makeFileResourceName("text"),null,false,true);
		if(introDir.isDirectory())
		{
			CMFile[] files=introDir.listFiles();
			for(int f=0;f<files.length;f++)
				if(files[f].getName().toLowerCase().startsWith("intro")
				&&files[f].getName().toLowerCase().endsWith(".txt"))
					Resources.removeResource("text/"+files[f].getName());
		}

		if(Resources.getResource("text/offline.txt")!=null)
			Resources.removeResource("text/offline.txt");

		if(mob!=null)
			mob.tell("Help files unloaded. Next HELP, AHELP, new char will reload.");
	}
	protected String columnHelper(String word, String msg, int wrap)
	{
		StringBuilder prepend = new StringBuilder("");
		String[] maxStats = CMLib.coffeeFilter().wrapOnlyFilter(msg,wrap-12);
		for(String s : maxStats)
		{
			prepend.append(CMStrings.padRight(word, 12)).append(s).append("\n\r");
			word=" ";
		}
		return prepend.toString();
	}
}
