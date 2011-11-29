package com.planet_ink.coffee_mud.Commands;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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
public class List extends StdCommand
{
	public List(){}

	private String[] access={"LIST"};
	public String[] getAccessWords(){return access;}

	public static void dumpThreadGroup(StringBuffer lines,ThreadGroup tGroup)
	{
		int ac = tGroup.activeCount();
		int agc = tGroup.activeGroupCount();
		Thread tArray[] = new Thread [ac+1];
		ThreadGroup tgArray[] = new ThreadGroup [agc+1];
		tGroup.enumerate(tArray,false);
		tGroup.enumerate(tgArray,false);
		lines.append(" ^HTGRP^?  ^H" + tGroup.getName() + "^?\n\r");
		for (int i = 0; i<ac; ++i) {
			if (tArray[i] != null) {
				if((tArray[i] instanceof Tickable)&&(((Tickable)tArray[i]).getTickStatus()==Tickable.TickStat.Not)) continue;
				lines.append(tArray[i].isAlive()? "  ok   " : " BAD!  ");
				lines.append(CMStrings.padRight(tArray[i].getName(),20)+": ");
				if(tArray[i] instanceof Session) {
					Session S=(Session)tArray[i];
					lines.append("Session status "+S.getStatus()+"-"+CMParms.combine(S.previousCMD(),0) + "\n\r"); }
				else if(tArray[i] instanceof Tickable) {
					Tickable T=(Tickable)tArray[i];
					lines.append("Tickable "+T.ID()+"-"+T.getTickStatus() + "\n\r"); }
				else {
					String status=CMLib.threads().getServiceThreadSummary(tArray[i]);
					lines.append("Thread "+tArray[i].getName() + status+"\n\r"); } } }
		if (agc > 0) {
			lines.append("{\n\r");
			for (int i = 0; i<agc; ++i) if (tgArray[i] != null) dumpThreadGroup(lines,tgArray[i]);
			lines.append("}\n\r"); }
	}
/*
	protected String reallyFindOneWays(MOB mob, Vector commands)
	{
		StringBuffer str=new StringBuffer("");
		try
		{
			for(Enumeration r=CMLib.map().rooms();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if(R.roomID().length()>0)
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Room R2=R.rawDoors()[d];
						if((R2!=null)&&(R2.rawDoors()[Directions.getOpDirectionCode(d)]!=R))
							str.append(CMStrings.padRight(R.roomID(),30)+": "+Directions.getDirectionName(d)+" to "+R2.roomID()+"\n\r");
					}
			}
		}catch(NoSuchElementException e){}
		if(str.length()==0) str.append("None!");
		if(CMParms.combine(commands,1).equalsIgnoreCase("log"))
			Log.rawSysOut(str.toString());
		return str.toString();
	}
*/

	public Vector<String> getMyCmdWords(MOB mob)
	{
		Vector<String> V=new Vector();
		if(CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
			for(ListOption option : allOptions)
				V.add(option.toString());
		else
			for(ListOption option : allOptions)
				if(option.isAllowed(mob)) V.add(option.toString());
		return V;
	}

	public ListOption getMyCmdCode(MOB mob, String s)
	{
		try{return ListOption.valueOf(s);}
		catch(IllegalArgumentException e){}
		return null;
	}

	public ListOption getAnyCode(MOB mob)
	{
		if(CMSecurity.isAllowed(mob,mob.location(),"LISTADMIN"))
			return allOptions[0];
		for(ListOption option : allOptions)
			if(option.isAllowed(mob)) return option;
		return null;
	}

	private enum ListOption
	{
		//Removed: Races, Classes, Staff, Skills, Diseases, Poisons, RealEstate, NoPurge, RaceCats
		RACES(new String[] {"CMDRACES"}){
			public void list(MOB mob, String rest){
				Iterator these=CMClass.Objects.RACE.all();
				boolean shortList=rest.equalsIgnoreCase("SHORT");
				StringBuffer lines=new StringBuffer("");
				if(!these.hasNext()) return;
				int column=0;
				if(shortList) {
					Vector raceNames=new Vector();
					for(;these.hasNext();) raceNames.addElement(((Race)these.next()).ID());
					lines.append(CMParms.toStringList(raceNames)); }
				else for(;these.hasNext();) {
					Race thisThang=(Race)these.next();
					if(++column>3) {
						lines.append("\n\r");
						column=1; }
					lines.append(CMStrings.padRight(thisThang.ID()+" ("+thisThang.racialCategory()+")",25)); }
				lines.append("\n\r");
				mob.session().wraplessPrintln(lines.toString()); } },
		RACECATS(new String[] {"CMDRACES"}){
			public void list(MOB mob, String rest){
				Iterator these=CMClass.Objects.RACE.all();
				boolean shortList=rest.equalsIgnoreCase("SHORT");
				StringBuffer lines=new StringBuffer("");
				if(!these.hasNext()) return;
				int column=0;
				Vector raceCats=new Vector();
				Race R=null;
				for(;these.hasNext();) {
					R=(Race)these.next();
					if(!raceCats.contains(R.racialCategory())) raceCats.addElement(R.racialCategory()); }
				Object[] sortedB=(new TreeSet(raceCats)).toArray();
				if(shortList) {
					String[] sortedC=new String[sortedB.length];
					for(int i=0;i<sortedB.length;i++) sortedC[i]=(String)sortedB[i];
					lines.append(CMParms.toStringList(sortedC)); }
				else for(int i=0;i<sortedB.length;i++) {
					String raceCat=(String)sortedB[i];
					if(++column>3) {
						lines.append("\n\r");
						column=1; }
					lines.append(CMStrings.padRight(raceCat,25)); }
				lines.append("\n\r");
				mob.session().wraplessPrintln(lines.toString()); } },
		ITEMS(new String[] {"CMDITEMS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMLib.lister().reallyList(CMClass.Objects.ITEM.all()).toString()); } },
		ARMOR(new String[] {"CMDITEMS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMLib.lister().reallyList(CMClass.Objects.WEARABLE.all()).toString());} },
		ENVRESOURCES(new String[] {"CMDITEMS","CMDROOMS","CMDAREAS"}){
			public void list(MOB mob, String rest){
				if(rest.equalsIgnoreCase("SHORT")) {
					mob.session().wraplessPrintln(CMParms.toStringList(RawMaterial.Resource.values()));
					return; }
				StringBuffer str=new StringBuffer("");
//				for(String S : RawMaterial.CODES.NAMES()) str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(S.toLowerCase()),16));
				str.append(CMStrings.padRight("Resource",15)+" ");
				str.append(CMStrings.padRight("Material",10)+" ");
				str.append(CMStrings.padRight("Val",3)+" ");
				str.append(CMStrings.padRight("Freq",4)+" ");
				str.append(CMStrings.padRight("Str",3)+" ");
				str.append(CMStrings.padRight("Density",7));
				for(RawMaterial.Resource R : RawMaterial.Resource.values()) {
					str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(R.toString()),16));
					str.append(CMStrings.padRight(CMStrings.capitalizeAndLower(R.material.toString()),11));
					str.append(CMStrings.padRight(""+R.value,4));
					str.append(CMStrings.padRight(""+R.frequency,5));
					str.append(CMStrings.padRight(""+R.hardness,4));
					str.append(CMStrings.padRight(""+R.density,8));
					str.append("\n\r"); }
				mob.session().wraplessPrintln(str.toString()); } },
		WEAPONS(new String[] {"CMDITEMS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMLib.lister().reallyList(CMClass.Objects.WEAPON.all()).toString());} },
		MOBS(new String[] {"CMDMOBS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMLib.lister().reallyList(CMClass.Objects.MOB.all()).toString());} },
		ROOMS(new String[] {"CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES"}){	//, CMDCLASSES
			public void list(MOB mob, String rest){
				StringBuilder S=new StringBuilder();
				for(Enumeration<Room> rooms=mob.location().getArea().getProperMap();rooms.hasMoreElements();){
					Room R=rooms.nextElement();
					S.append(CMStrings.padRightPreserve("^<LSTROOMID^>"+R.roomID()+"^</LSTROOMID^>",30)+": "+CMStrings.limit(R.displayText(),43)+"\n\r"); }
				mob.session().wraplessPrintln(S.toString()+"\n\r"); } },
		AREA(new String[] {"CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES"}){	//CMDCLASSES
			public void list(MOB mob, String rest){
				StringBuilder S=new StringBuilder();
				for(Enumeration<Room> rooms=mob.location().getArea().getProperMap();rooms.hasMoreElements();){
					Room R=rooms.nextElement();
					S.append(CMStrings.padRightPreserve(R.roomID(),30)+": "+R.ID()+"\n\r"); }
				mob.session().wraplessPrintln(S.toString()+"\n\r"); } },
		LOCALES(new String[] {"CMDROOMS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMLib.lister().reallyList(CMClass.Objects.LOCALE.all()).toString());} },
		BEHAVIORS(new String[] {"CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES"}){	//CMDCLASSES
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMLib.lister().reallyList(CMClass.Objects.BEHAVIOR.all()).toString());} },
		EXITS(new String[] {"CMDEXITS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMLib.lister().reallyList(CMClass.Objects.EXIT.all()).toString());} },
		PROPERTIES(new String[] {"CMDMOBS","CMDITEMS","CMDROOMS","CMDAREAS","CMDEXITS","CMDRACES"}){	//CMDCLASSES
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln("TODO, whoops");} },
		TICKS(new String[] {"LISTADMIN"}){
			public void list(MOB mob, String rest){
				StringBuffer msg=new StringBuffer("\n\r");
				boolean activeOnly=false;
				String mask=null;
				if("ACTIVE".startsWith(rest.toUpperCase())&&(rest.length()>0)) {
					activeOnly=true;
					rest=""; }
				msg.append(CMStrings.padRight("Grp",4)+CMStrings.padRight("Client",20)+" "+CMStrings.padRight("ID",3)+CMStrings.padRight("Status",8)+"\n\r");
				int col=0;
				int numGroups=CMath.s_int(CMLib.threads().tickInfo("tickGroupSize"));
				int whichTick=-1;
				if(CMath.isInteger(rest)&&(rest.length()>0)) whichTick=CMath.s_int(rest);
				else if(rest.length()>0) mask=rest.toUpperCase().trim();
				if((mask!=null)&&(mask.length()==0)) mask=null;
				for(int v=0;v<numGroups;v++) {
					int tickersSize=CMath.s_int(CMLib.threads().tickInfo("tickersSize"+v));
					if((whichTick<0)||(whichTick==v))
					for(int t=0;t<tickersSize;t++) {
						String status=CMLib.threads().tickInfo("tickerstatus"+v+"-"+t);
						boolean isActive=!status.equals("Not");
						if((!activeOnly)||(isActive)) {
							String name=CMLib.threads().tickInfo("tickerName"+v+"-"+t);
							if((mask==null)||(name.toUpperCase().indexOf(mask)>=0)) {
								if(((col++)>=2)||(activeOnly)) {
									msg.append("\n\r");
									col=1; }
								msg.append(CMStrings.padRight(""+v,4)+CMStrings.padRight(name,22)+" "+CMStrings.padRight("",3)+CMStrings.padRight(status,8)); } } } }
				mob.session().println(msg.toString()); } },
		BANNED(new String[] {"BAN"}){
			public void list(MOB mob, String rest){
				StringBuffer str=new StringBuffer("\n\rBanned names/ips:\n\r");
				Vector banned=Resources.getFileLineVector(Resources.getFileResource("banned.ini",false));
				if((banned!=null)&&(banned.size()>0))
				for(int b=0;b<banned.size();b++) str.append((b+1)+") "+((String)banned.elementAt(b))+"\n\r");
				mob.session().wraplessPrintln(str.toString());} },
		LOG(new String[] {"LISTADMIN"}){
			public void list(MOB mob, String rest){
				int pageBreak=((mob.playerStats()!=null)?mob.playerStats().getPageBreak():0);
				int lineNum=0;
				if(rest.length()==0) {
					Log.LogReader log=Log.instance().getLogReader();
					String line=log.nextLine();
					while((line!=null)&&(mob.session()!=null)&&(!mob.session().killFlag())) {
						mob.session().rawPrintln(line);
						if((pageBreak>0)&&(lineNum>=pageBreak))
							if(!pause(mob.session())) break;
							else lineNum=0;
						lineNum++;
						line=log.nextLine(); }
					log.close();
					return; }
				int start=0;
				int logSize=Log.instance().numLines();
				int end=logSize;
				Log.LogReader log=Log.instance().getLogReader();
				int i=rest.indexOf(" ");
				if(i>0){
					if(rest.substring(0,i).equalsIgnoreCase("first")) {
						if(CMath.isInteger(rest.substring(i+1))) end=CMath.s_int(rest.substring(i+1));
						else {
							mob.tell("Bad parameter format after.");
							return; } }
					else if(rest.substring(0,i).equalsIgnoreCase("last")) {
						if(CMath.isInteger(rest.substring(i+1))) start=end-CMath.s_int(rest.substring(i+1));
						else {
							mob.tell("Bad parameter format after.");
							return; } }
					else if(rest.substring(0,i).equalsIgnoreCase("skip")) {
						if(CMath.isInteger(rest.substring(i+1))) start=CMath.s_int(rest.substring(i+1));
						else {
							mob.tell("Bad parameter format after.");
							return; } } }
				if(end>=logSize) end=logSize;
				if(start<0) start=0;
				String line=log.nextLine();
				lineNum=0;
				int shownLineNum=0;
				while((line!=null)&&(mob.session()!=null)&&(!mob.session().killFlag())) {
					if((lineNum>start)&&(lineNum<=end)) {
						mob.session().rawPrintln(line);
						if((pageBreak>0)&&(shownLineNum>=pageBreak)) {
							if(!pause(mob.session())) break;
							else shownLineNum=0; }
						shownLineNum++; }
					lineNum++;
					line=log.nextLine(); }
				log.close(); } },
		USERS(new String[] {"CMDPLAYERS","STAT"}){
			public void list(MOB mob, String rest){
				StringBuffer head=new StringBuffer("");
				head.append("[");
				head.append(CMStrings.padRight("Race",8)+" ");
				head.append(CMStrings.padRight("Lvl",4)+" ");
		//		head.append(CMStrings.padRight("Hours",5)+" ");
				head.append(CMStrings.padRight("IP Address",17)+" ");
				head.append(CMStrings.padRight("Last",18)+" ");
				head.append("] Character name\n\r");
				for(Enumeration<MOB> players=CMLib.players().players();players.hasMoreElements();) {
					MOB player=players.nextElement();
					head.append("[");
					head.append(CMStrings.padRight("",8)+" ");	//player.body().race().name()
					head.append(CMStrings.padRight("",4)+" ");	//player.level() ?
		//			long age=Math.round(CMath.div(CMath.s_long(""+player.playerStats()),60.0));
		//			head.append(CMStrings.padRight(""+age,5)+" ");
					head.append(CMStrings.padRight(player.playerStats().lastIP(),17)+" ");
					head.append(CMStrings.padRight(CMLib.time().date2String(player.playerStats().lastDateTime()),18)+" ");
					head.append("] "+CMStrings.padRight("^<LSTUSER^>"+player.name()+"^</LSTUSER^>",15));
					head.append("\n\r"); }
				mob.tell(head.toString()); } },
		LINKAGES(new String[] {"CMDAREAS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln("TODO, whoops");} },
		REPORTS(new String[] {"LISTADMIN"}){
			public void list(MOB mob, String rest){
				mob.tell("\n\r^xCoffeeMud System Report:^.^N");
				try {
					System.gc();
					Thread.sleep(1500); }
				catch(Exception e){}
				StringBuffer buf=new StringBuffer("");
				long totalTime=System.currentTimeMillis()-CMSecurity.getStartTime();
				buf.append("The system has been running for ^H"+CMLib.english().returnTime(totalTime,0)+"^?.\n\r");
				long free=Runtime.getRuntime().freeMemory()/1000;
				long total=Runtime.getRuntime().totalMemory()/1000;
				buf.append("The system is utilizing ^H"+(total-free)+"^?kb out of ^H"+total+"^?kb.\n\r");
				buf.append("\n\r^xService Engine report:^.^N\n\r");
				String totalTickers=CMLib.threads().systemReport("totalTickers");
				String tickGroupSize=CMLib.threads().systemReport("TICKGROUPSIZE");
				long totalMillis=CMath.s_long(CMLib.threads().systemReport("totalMillis"));
				long totalTicks=CMath.s_long(CMLib.threads().systemReport("totalTicks"));
				String topGroupNumber=CMLib.threads().systemReport("topGroupNumber");
				long topGroupMillis=CMath.s_long(CMLib.threads().systemReport("topGroupMillis"));
				long topGroupTicks=CMath.s_long(CMLib.threads().systemReport("topGroupTicks"));
				long topObjectMillis=CMath.s_long(CMLib.threads().systemReport("topObjectMillis"));
				long topObjectTicks=CMath.s_long(CMLib.threads().systemReport("topObjectTicks"));
				buf.append("There are ^H"+totalTickers+"^? ticking objects in ^H"+tickGroupSize+"^? threads.\n\r");
				buf.append("The ticking objects have consumed: ^H"+CMLib.english().returnTime(totalMillis,totalTicks)+"^?.\n\r");
				buf.append("The most active group, #^H"+topGroupNumber+"^?, has consumed: ^H"+CMLib.english().returnTime(topGroupMillis,topGroupTicks)+"^?.\n\r");
				String topObjectClient=CMLib.threads().systemReport("topObjectClient");
				String topObjectGroup=CMLib.threads().systemReport("topObjectGroup");
				if(topObjectClient.length()>0) {
					buf.append("The most active object has been '^H"+topObjectClient+"^?', from group #^H"+topObjectGroup+"^?.\n\r");
					buf.append("That object has consumed: ^H"+CMLib.english().returnTime(topObjectMillis,topObjectTicks)+"^?.\n\r"); }
				buf.append("\n\r");
				buf.append("^xThread reports:^.^N\n\r");
				int threadNum=0;
				String threadName=CMLib.threads().systemReport("Thread"+threadNum+"name");
				while(threadName.trim().length()>0) {
					long saveThreadMilliTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"MilliTotal"));
					long saveThreadTickTotal=CMath.s_long(CMLib.threads().systemReport("Thread"+threadNum+"TickTotal"));
					buf.append("Thread '"+threadName+"' has consumed: ^H"+CMLib.english().returnTime(saveThreadMilliTotal,saveThreadTickTotal)+" ("+CMLib.threads().systemReport("Thread"+threadNum+"Status")+")^?.");
					buf.append("\n\r");
					threadNum++;
					threadName=CMLib.threads().systemReport("Thread"+threadNum+"name"); }
				buf.append("\n\r");
				buf.append("^xSession report:^.^N\n\r");
				long totalMOBMillis=CMath.s_long(CMLib.threads().systemReport("totalMOBMillis"));
				long totalMOBTicks=CMath.s_long(CMLib.threads().systemReport("totalMOBTicks"));
				buf.append("There are ^H"+CMLib.sessions().size()+"^? ticking players logged on.\n\r");
				buf.append("The ticking players have consumed: ^H"+CMLib.english().returnTime(totalMOBMillis,totalMOBTicks)+"^?.\n\r");
				long topMOBMillis=CMath.s_long(CMLib.threads().systemReport("topMOBMillis"));
				long topMOBTicks=CMath.s_long(CMLib.threads().systemReport("topMOBTicks"));
				String topMOBClient=CMLib.threads().systemReport("topMOBClient");
				if(topMOBClient.length()>0) {
					buf.append("The most active mob has been '^H"+topMOBClient+"^?'\n\r");
					buf.append("That mob has consumed: ^H"+CMLib.english().returnTime(topMOBMillis,topMOBTicks)+"^?.\n\r"); }
				mob.session().println(buf.toString()); } },
		THREADS(new String[] {"LISTADMIN"}){
			public void list(MOB mob, String rest){
				StringBuffer lines=new StringBuffer("^xStatus|Name                 ^.^?\n\r");
				try {
					ThreadGroup topTG = Thread.currentThread().getThreadGroup();
					while (topTG != null && topTG.getParent() != null) topTG = topTG.getParent();
					if (topTG != null) dumpThreadGroup(lines,topTG); }
				catch (Exception e) {
					lines.append ("\n\rBastards! Exception while listing threads: " + e.getMessage() + "\n\r"); }
				mob.session().wraplessPrintln(lines.toString()); } },
		RESOURCES(new String[] {"LOADUNLOAD"}){
			public void list(MOB mob, String rest){
				Vector keySet=Resources.findResourceKeys(rest);
				if(keySet.size()==1) {
					String key=(String)keySet.firstElement();
					StringBuffer str=new StringBuffer("^x"+key+"^?\n\r");
					Object o=Resources.getResource(key);
					if(o instanceof Vector) str.append(CMParms.toStringList((Vector)o));
					else if(o instanceof Hashtable) str.append(CMParms.toStringList((Hashtable)o));
					else if(o instanceof HashSet) str.append(CMParms.toStringList((HashSet)o));
					else if(o instanceof String[]) str.append(CMParms.toStringList((String[])o));
					else if(o instanceof boolean[]) str.append(CMParms.toStringList((boolean[])o));
					else if(o instanceof byte[]) str.append(CMParms.toStringList((byte[])o));
					else if(o instanceof char[]) str.append(CMParms.toStringList((char[])o));
					else if(o instanceof double[]) str.append(CMParms.toStringList((double[])o));
					else if(o instanceof int[]) str.append(CMParms.toStringList((int[])o));
					else if(o instanceof long[]) str.append(CMParms.toStringList((long[])o));
					else if(o!=null) str.append(o.toString());
					mob.session().println(str.toString());
					return; }
				Enumeration keys=keySet.elements();
				mob.session().println(CMLib.lister().reallyList2Cols(keys,-1,null).toString()); } },
		ONEWAYDOORS(new String[] {"CMDEXITS","CMDROOMS","CMDAREAS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln("TODO, whoops");} },
		MATERIALS(new String[] {"CMDITEMS","CMDROOMS","CMDAREAS"}){
			public void list(MOB mob, String rest){
				mob.session().wraplessPrintln(CMParms.toStringList(RawMaterial.Material.values())); } },
		CONTENTS(new String[] {"CMDITEMS","CMDMOBS","CMDROOMS","CMDAREAS"}){
			public void list(MOB mob, String rest){ /*
				Enumeration roomsToDo=null;
				if(rest.equalsIgnoreCase("area")) roomsToDo=mob.location().getArea().getMetroMap();
				else if(rest.trim().length()==0) roomsToDo=CMParms.makeVector(mob.location()).elements();
				else {
					Area A=CMLib.map().findArea(rest);
					if(A!=null) roomsToDo=A.getMetroMap();
					else {
						Room R=CMLib.map().getRoom(rest);
						if(R!=null) roomsToDo=CMParms.makeVector(mob.location()).elements();
						else {
							mob.tell("There's no such place as '"+rest+"'");
							return; } } }
				StringBuffer buf=new StringBuffer("");
				Room R=null;
				Room TR=null;
				Vector set=null;
				for(;roomsToDo.hasMoreElements();) {
					R=(Room)roomsToDo.nextElement();
					if(R.roomID().length()==0) continue;
					set=CMLib.database().DBReadRoomData(R.roomID(),false);
					if((set==null)||(set.size()==0)) buf.append("'"+R.roomID()+"' could not be read from the database!\n\r");
					else {
						TR=(Room)set.elements().nextElement();
						CMLib.database().DBReadContent(TR,set);
						buf.append("\n\r^NRoomID: "+CMLib.map().getExtendedRoomID(TR)+"\n\r");
						for(int m=0;m<TR.numInhabitants();m++) {
							MOB M=TR.fetchInhabitant(m);
							if(M==null) continue;
							buf.append("^M"+CMStrings.padRight(M.ID(),15)+": "+CMStrings.padRight(M.displayText(),35)+": "
										+CMStrings.padRight(M.envStats().level()+"",3)+": "
										+"^N\n\r");
							for(int i=0;i<M.numItems();i++) {
								Item I=M.getItem(i);
								if(I!=null) buf.append("    ^I"+CMStrings.padRight(I.ID(),15)
											+": "+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),35)+": "
											+CMStrings.padRight(I.envStats().level()+"",3)+": "
											+"^N"+((I.container()!=null)?I.Name():"")+"\n\r"); } }
						for(int i=0;i<TR.numItems();i++) {
							Item I=TR.getItem(i);
							if(I!=null) buf.append("^I"+CMStrings.padRight(I.ID(),15)+": "
										+CMStrings.padRight((I.displayText().length()>0?I.displayText():I.Name()),35)+": "
										+CMStrings.padRight(I.envStats().level()+"",3)+": "
										+"^N"+((I.container()!=null)?I.Name():"")+"\n\r"); }
						TR.destroy(); } }
				mob.session().wraplessPrintln(buf.toString()); */ } },
		HELPFILEREQUESTS(new String[] {"LISTADMIN"}){
			public void list(MOB mob, String rest){
				String fileName=Log.instance().getLogFilename("help");
				if(fileName==null) {
					mob.tell("This feature requires that help request log entries be directed to a file.");
					return; }
				CMFile f=new CMFile(fileName,mob,true);
				if((!f.exists())||(!f.canRead())) {
					mob.tell("File '"+f.getName()+"' does not exist.");
					return; }
				Vector V=Resources.getFileLineVector(f.text());
				Hashtable entries = new Hashtable();
				for(int v=0;v<V.size();v++) {
					String s=(String)V.elementAt(v);
					if(s.indexOf(" Help  Help")==19) {
						int x=s.indexOf("wanted help on",19);
						String helpEntry=s.substring(x+14).trim().toLowerCase();
						int[] sightings=(int[])entries.get(helpEntry);
						if(sightings==null) {
							sightings=new int[1];
							if(CMLib.help().getHelpText(helpEntry,mob,false)!=null) sightings[0]=-1;
							entries.put(helpEntry,sightings); }
						if(sightings[0]>=0) sightings[0]++;
						else sightings[0]--; } }
				Hashtable readyEntries = new Hashtable(entries.size());
				for(Enumeration e=entries.keys();e.hasMoreElements();) {
					Object key=e.nextElement();
					int[] val=(int[])entries.get(key);
					readyEntries.put(key,Integer.valueOf(val[0])); }
				DVector sightingsDV=DVector.toDVector(readyEntries);
				sightingsDV.sortBy(2);
				StringBuffer str=new StringBuffer("^HHelp entries, sorted by popularity: ^N\n\r");
				for(int d=0;d<sightingsDV.size();d++) str.append("^w"+CMStrings.padRight(sightingsDV.elementAt(d,2).toString(),4))
					   .append(" ")
					   .append(sightingsDV.elementAt(d,1).toString())
					   .append("\n\r");
				mob.session().wraplessPrint(str.toString()+"^N"); } },
		ACCOUNTS(new String[] {"CMDPLAYERS","STAT"}){
			public void list(MOB mob, String rest){
				StringBuffer head=new StringBuffer("");
				head.append("^X");
				head.append("[");
				head.append(CMStrings.padRight("Account",10)+" ");
				head.append(CMStrings.padRight("Last",18)+" ");
				head.append(CMStrings.padRight("IP Address",23)+" ");
				head.append("] Characters^.^N\n\r");
				Vector<PlayerAccount> allAccounts=CMLib.database().DBListAccounts(null);
				for(int u=0;u<allAccounts.size();u++) {
					PlayerAccount U=allAccounts.elementAt(u);
					StringBuffer line=new StringBuffer("");
					line.append("[");
					line.append(CMStrings.padRight(U.accountName(),10)+" ");
					line.append(CMStrings.padRight(CMLib.time().date2String(U.lastDateTime()),18)+" ");
					Enumeration<MOB> players = U.getLoadPlayers();
					Vector<String> pListsV = new Vector<String>();
					while(players.hasMoreElements()) pListsV.add(players.nextElement().name());
					line.append(CMStrings.padRight(U.lastIP(),23)+" ");
					line.append("] ");
					int len = line.length();
					head.append(line.toString());
					boolean notYet = true;
					for(String s : pListsV) {
						if(notYet) notYet=false;
						else head.append(CMStrings.repeat(" ", len));
						head.append(s);
						head.append("\n\r"); }
					if(pListsV.size()==0) head.append("\n\r"); }
				mob.tell(head.toString()); } },
		;
		private final String[] securityList;
		private ListOption(String[] list){securityList=list;}
		public abstract void list(MOB mob, String rest);
		public boolean isAllowed(MOB mob) {
			for(String S : securityList) if(CMSecurity.isAllowed(mob,mob.location(),S)) return true;
			return false; } }
	private static final ListOption[] allOptions=ListOption.values();

	public static boolean pause(Session sess) {
		if((sess==null)||(sess.killFlag())) return false;
		sess.out("<pause - enter>".toCharArray());
		try{ 
			String s=sess.blockingIn(); 
			if(s!=null)
			{
				s=s.toLowerCase();
				if(s.startsWith("qu")||s.startsWith("ex")||s.equals("x"))
					return false;
			}
		}catch(Exception e){return false;}
		return !sess.killFlag();
	}

	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("List what?");
			return false;
		}

		if(mob.session()==null) return false;

		String listWord=((String)commands.firstElement()).toUpperCase();
		String rest=(commands.size()>1)?CMParms.combine(commands,1):"";
		ListOption code=getMyCmdCode(mob, listWord);
		if((code==null)||(listWord.length()==0))
		{
			Vector<String> V=getMyCmdWords(mob);
			{
				StringBuffer str=new StringBuffer("");
				for(int v=0;v<V.size();v++)
					if(V.elementAt(v).length()>0)
					{
						str.append(V.elementAt(v));
						if(v==(V.size()-2))
							str.append(", or ");
						else
						if(v<(V.size()-1))
							str.append(", ");
					}
				mob.tell("You cannot list '"+listWord+"'.  Try "+str.toString()+".");
			}
			return false;
		}
		code.list(mob, rest);
//		case 17: s.wraplessPrintln(CMLib.lister().reallyList(CMClass.abilities(),Ability.ACODE_PROPERTY).toString()); break;
//		case 36: s.println(listLinkages(mob).toString()); break;
//		case 40: s.wraplessPrintln(reallyFindOneWays(mob,commands)); break;
		return false;
	}

	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return getAnyCode(mob)!=null;}

}
