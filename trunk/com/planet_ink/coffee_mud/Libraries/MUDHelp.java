package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.nio.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;
import java.io.IOException;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class MUDHelp extends StdLibrary
{
	public String ID(){return "MUDHelp";}

	public String getHelpText(String helpStr, MOB forMOB, boolean favorAHelp)
	{ return getHelpText(helpStr, forMOB, favorAHelp, false);}
	
	public String getHelpText(String helpStr, MOB forMOB, boolean favorAHelp, boolean noFix)
	{
		if(helpStr.length()==0) return null;
		String thisTag=null;
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

	public Vector<String> getTopics(boolean archonHelp, boolean standardHelp)
	{
		Vector<String> reverseList=new Vector();
		Properties rHelpFile=null;
		if(archonHelp)
			rHelpFile=getArcHelpFile();
		if(standardHelp)
		{
			if(rHelpFile!=null)
			for(Enumeration<String> e=(Enumeration)rHelpFile.keys();e.hasMoreElements();)
			{
				String ptop = e.nextElement();
				String thisTag=rHelpFile.getProperty(ptop);
				if //((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)||
				  (rHelpFile.getProperty(thisTag)==null)
					reverseList.add(ptop);
			}
			rHelpFile=getHelpFile();
		}
		if(rHelpFile!=null)
		for(Enumeration<String> e=(Enumeration)rHelpFile.keys();e.hasMoreElements();)
		{
			String ptop = e.nextElement();
			String thisTag=rHelpFile.getProperty(ptop);
			if //((thisTag==null)||(thisTag.length()==0)||(thisTag.length()>=35)||
			  (rHelpFile.getProperty(thisTag)==null)
				reverseList.add(ptop);
		}
		Collections.sort(reverseList);
		return reverseList;
	}
	
	public String fixHelp(String tag, String str, MOB forMOB)
	{
		/*boolean worldCurrency=str.startsWith("<CURRENCIES>");
		if(str.startsWith("<CURRENCY>")||worldCurrency)
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
				help.append("\r\n"+CMStrings.padRight("World Currencies",20)+":");
			for(Enumeration e=currencies.elements();e.hasMoreElements();)
			{
				String currency=(String)e.nextElement();
				if(worldCurrency)
					help.append("\r\n"+CMStrings.padRight("Currency",20)+":");
				else
					help.append("\r\n"+CMStrings.padRight("Local Currency",20)+":");
				if(currency.length()==0)
					help.append("default");
				else
					help.append(CMStrings.capitalizeAndLower(currency));
				BeanCounter.MoneyDenomination denoms[]=CMLib.beanCounter().getCurrencySet(currency);
				for(int d=0;d<denoms.length;d++)
				{
					if(denoms[d].abbr.length()>0)
						help.append("\r\n"+CMStrings.padRight(denoms[d].name+" ("+denoms[d].abbr+")",20)+":");
					else
						help.append("\r\n"+CMStrings.padRight(denoms[d].name,20)+":");
					if(denoms[d].value==CMLib.beanCounter().getLowestDenomination(currency))
						help.append(" (exchange rate is "+denoms[d].value+" of base)");
					else
						help.append(" "+CMLib.beanCounter().getConvertableDescription(currency,denoms[d].value));
				}
				help.append("\r\n");
			}
			help.append(str);
			str=help.toString();
		}*/
		if(str.startsWith("<RACE>"))
		{
			str=str.substring(6);
			Race R=CMClass.RACE.get(tag);
			if(R==null) R=CMClass.RACE.get(tag.replace('_',' '));
			if(R!=null)
			{
				StringBuilder prepend=new StringBuilder("");
				int wrap = 0;
				if((forMOB!=null)&&(forMOB.session()!=null))
					wrap=forMOB.session().getWrap();
				if(wrap <=0 ) wrap=78;
				prepend.append("^HRace Name : ^N"+R.name()+" ^H(^N"+R.racialCategory()+"^H)^N");
				prepend.append("\r\n");
				
				String s=R.getStatAdjDesc();
				prepend.append(columnHelper("^HStat Mods.:^N",s,wrap));
				s=""; //R.getSensesChgDesc();
				//if(R.getDispositionChgDesc().length()>0)
				//	s+=((s.length()>0)?", ":"")+R.getDispositionChgDesc();
				//if(R.getAbilitiesDesc().length()>0)
				//	s+=((s.length()>0)?", ":"")+R.getAbilitiesDesc();
				//prepend.append(columnHelper("^HAbilities :^N",s,wrap));
				prepend.append(columnHelper("^HLanguages :^N",R.getLanguagesDesc(),wrap));
				//prepend.append(columnHelper("^HLife Exp. :^N",R.getAgingChart()[Race.AGE_ANCIENT]+" years",wrap));
				prepend.append("^HDesc.     : ^N");
				str=prepend.toString()+"\r\n"+str;
			}
		}
		/*try{
			if(str!=null)
				return CMLib.httpUtils().doVirtualPage(str);
		}catch(com.planet_ink.coffee_mud.core.exceptions.HTTPRedirectException x){}*/
		return str;
	}

	public String getHelpText(String helpStr, Properties rHelpFile, MOB forMOB)
	{ return getHelpText(helpStr,rHelpFile,forMOB,false);}
	public String getHelpText(String helpStr, Properties rHelpFile, MOB forMOB, boolean noFix)
	{
		helpStr=helpStr.trim().toUpperCase();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		String thisTag=null;

		//Race R=CMClass.RACE.get(helpStr);

		if(thisTag==null) thisTag=rHelpFile.getProperty(helpStr);
		boolean areaTag=(thisTag==null)&&helpStr.startsWith("AREAHELP_");
		boolean found=((thisTag!=null)&&(thisTag.length()>0));

		if(!found)
		{
			String ahelpStr=helpStr.replace("_"," ").trim();
			if(areaTag) ahelpStr=ahelpStr.substring(9);
			for(Iterator<Area> e=CMLib.map().areas();e.hasNext();)
			{
				Area A=e.next();
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
			String ahelpStr=helpStr.replace("_"," ").trim();
			// INEXACT searches start here
			if(!found)
				for(Enumeration<String> e=(Enumeration)rHelpFile.keys();e.hasMoreElements();)
				{
					String key=e.nextElement();
					if(key.startsWith(helpStr))
					{
						thisTag=rHelpFile.getProperty(key);
						helpStr=key;
						found=true;
						break;
					}
				}
			if(!found)
				for(Enumeration<String> e=(Enumeration)rHelpFile.keys();e.hasMoreElements();)
				{
					String key=e.nextElement();
					if(CMLib.english().containsString(key,helpStr))
					{
						thisTag=rHelpFile.getProperty(key);
						helpStr=key;
						found=true;
						break;
					}
				}
			if(!found)
				for(Iterator<Area> e=CMLib.map().areas();e.hasNext();)
				{
					Area A=e.next();
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
			else if(thisOtherTag!=null)
			{
				helpStr=thisTag;
				thisTag=thisOtherTag;
			}
			else
				break;
		}
		
		// the area exception
		//if((thisTag==null)||(thisTag.length()==0))
		//	if(CMLib.map().getArea(helpStr.trim())!=null)
		//		return new StringBuilder(CMLib.map().getArea(helpStr.trim()).getAreaStats().toString());
		
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
				thisTag=thisTag.replace("[CHANNEL]",s.toUpperCase());
				thisTag=thisTag.replace("[channel]",s.toLowerCase());
				String extra = no?"":CMLib.channels().getExtraChannelDesc(s);
				thisTag=thisTag.replace("[EXTRA]",extra);
				return thisTag;
			}
		}
		
		if((thisTag==null)||(thisTag.length()==0))
			return null;
		if(noFix) return thisTag;
		return fixHelp(helpStr,thisTag,forMOB);
	}

	public String getHelpList(String helpStr, 
								   Properties rHelpFile1, 
								   Properties rHelpFile2, 
								   MOB forMOB)
	{
		helpStr=helpStr.toUpperCase().trim();
		if(helpStr.indexOf(" ")>=0)
			helpStr=helpStr.replace(' ','_');
		Vector<String> matches=new Vector();

		for(Enumeration<String> e=(Enumeration)rHelpFile1.keys();e.hasMoreElements();)
		{
			String key=e.nextElement();
			String prop=rHelpFile1.getProperty(key,"");
			if((key.indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
				matches.add(key);
		}
		if(rHelpFile2!=null)
		for(Enumeration<String> e=(Enumeration)rHelpFile2.keys();e.hasMoreElements();)
		{
			String key=e.nextElement();
			String prop=rHelpFile1.getProperty(key,"");
			if((key.indexOf(helpStr)>=0)||(CMLib.english().containsString(prop,helpStr)))
				matches.add(key);
		}
		if(matches.size()==0)
			return "";
		return CMLib.lister().fourColumns(matches).toString();
	}
	
	public Properties getArcHelpFile()
	{
		Properties arcHelpFile=(Properties)Resources.getResource("ARCHON HELP FILE");
		if(arcHelpFile==null)
		//try
		{
			arcHelpFile=new Properties();
			CMFile directory=new CMFile("resources/help/",null,true);
			if((directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(String item : list)
				{
					if((item!=null)&&(item.toUpperCase().endsWith(".INI"))&&(item.toUpperCase().startsWith("ARC_")))
						{}
						//arcHelpFile.load(new BufferedInputStream(new FileInputStream(CMFile.getProperExistingPath("resources/help/"+item))));
						//TODO
				}
			}
			//DVector suspiciousPairs=suspiciousTags(arcHelpFile);
			//for(int d=0;d<suspiciousPairs.size();d++)
			//	Syst/em.out.pri/ntln(suspiciousPairs.elementAt(d,1)+": "+suspiciousPairs.elementAt(d,2));
			/*for(Enumeration<String> e=arcHelpFile.keys();e.hasMoreElements();)
			{
				String key=e.nextElement();
				String entry=arcHelpFile.get(key);
				int x=entry.indexOf("<ZAP=");
				if(x<0) continue;
				int y=entry.indexOf(">",x);
				if(y<(x+6)) continue;
				String word=entry.substring(x+5,y).trim();
				entry=entry.substring(0,x)+CMLib.masking().maskHelp("\r\n",word)+entry.substring(y+1);
				arcHelpFile.remove(key);
				arcHelpFile.put(key,entry);
			}*/
			Resources.submitResource("ARCHON HELP FILE",arcHelpFile);
		}
		/*
		catch(IOException e)
		{
			Log.errOut("MUDHelp",e);
			return new Properties();
		}
		*/
		return arcHelpFile;
	}

	public Properties getHelpFile()
	{
		Properties helpFile=(Properties)Resources.getResource("MAIN HELP FILE");
		if(helpFile==null)
		//try
		{
			helpFile=new Properties();
			CMFile directory=new CMFile("resources/help/",null,true);
			if((directory.canRead())&&(directory.isDirectory()))
			{
				String[] list=directory.list();
				for(String item : list)
					if((item!=null)&&(item.toUpperCase().endsWith(".INI"))&&(!item.toUpperCase().startsWith("ARC_")))
						{}
						//helpFile.load(new BufferedInputStream(new FileInputStream(CMFile.getProperExistingPath("resources/help/"+item))));
						//TODO
			}
			//DVector suspiciousPairs=suspiciousTags(helpFile);
			//for(int d=0;d<suspiciousPairs.size();d++)
			//	Syst/em.out.pri/ntln(suspiciousPairs.elementAt(d,1)+": "+suspiciousPairs.elementAt(d,2));
			Resources.submitResource("MAIN HELP FILE",helpFile);
		}
		/*
		catch(IOException e)
		{
			Log.errOut("MUDHelp",e);
			return new Properties();
		}
		*/
		return helpFile;
	}
	
	public boolean shutdown() {
		unloadHelpFile(null);
		return true;
	}
	public void unloadHelpFile(MOB mob)
	{
		Resources.removeResource("PLAYER TOPICS");
		Resources.removeResource("ARCHON TOPICS");
		Resources.removeResource("help/help.txt");
		Resources.removeResource("help/accts.txt");
		Resources.removeResource("text/races.txt");
		Resources.removeResource("text/newacct.txt");
		Resources.removeResource("text/selchar.txt");
		Resources.removeResource("text/newchar.txt");
		Resources.removeResource("text/doneacct.txt");
		Resources.removeResource("text/stats.txt");
		Resources.removeResource("text/classes.txt");
		Resources.removeResource("help/arc_help.txt");
		Resources.removeResource("MAIN HELP FILE");
		Resources.removeResource("ARCHON HELP FILE");

		// also the intro page
		CMFile introDir=new CMFile("resources/text",null,false,true);
		if(introDir.isDirectory())
		{
			CMFile[] files=introDir.listFiles();
			for(int f=0;f<files.length;f++)
				if(files[f].getName().toLowerCase().startsWith("intro")
				&&files[f].getName().toLowerCase().endsWith(".txt"))
					Resources.removeResource("text/"+files[f].getName());
		}

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
			prepend.append(CMStrings.padRight(word, 12)).append(s).append("\r\n");
			word=" ";
		}
		return prepend.toString();
	}

	/*protected DVector suspiciousTags(Properties p)
	{
		String k=null;
		DVector pairs=new DVector(2);
		for(Enumeration e=p.keys();e.hasMoreElements();)
		{
			k=(String)e.nextElement();
			for(int i=0;i<k.length();i++)
				if(Character.isLowerCase(k.charAt(i)))
				{
					pairs.addRow(k,p.get(k));
					break;
				}
		}
		return pairs;
	}*/
}
