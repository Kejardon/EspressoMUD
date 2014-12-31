package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Go extends StdCommand
{
	public Go(){access=new String[]{"GO","WALK"};}
	
	//QueuedMove.newMove(exit, moveDistance)
	protected static class QueuedMove
	{
		protected static final ConcurrentLinkedQueue<QueuedMove> QMCache = new ConcurrentLinkedQueue();
		
		public ExitInstance lastExit;
		public int[] moveCommand;
		public QueuedMove(ExitInstance exit, int[] move){lastExit=exit; moveCommand=move;}
		protected QueuedMove(){}
		public static QueuedMove newMove(ExitInstance exit, int[] move)
		{
			QueuedMove queued = QMCache.poll();
			if(queued==null)
				return new QueuedMove(exit, move);
			queued.lastExit=exit;
			queued.moveCommand=move;
			return queued;
		}
		public void returnThis(){lastExit=null; moveCommand=null; QMCache.offer(this);}
	}

	public static boolean move(MOB mob,
						Room thisRoom,
						ExitInstance through,
						boolean nolook,
						boolean always)
	{
		if(mob==null || through==null) return false;
		Exit exit=through.getExit();
		Room destRoom=through.getDestination();
		ExitInstance entrance=exit.oppositeOf(through, destRoom);

		always|=CMSecurity.isAllowed(mob,"GOTO");
		//EnumSet enterCode=EnumSet.of(CMMsg.MsgCode.LEAVE);
		//if(always)
		//	enterCode.add(CMMsg.MsgCode.ALWAYS);
		//TODO: Include specific Exit description. Also have this call the mob's race to get a proper message for how to move.
		EnumSet<CMMsg.MsgCode> code=always?EnumSet.of(CMMsg.MsgCode.LEAVE,CMMsg.MsgCode.ALWAYS):EnumSet.of(CMMsg.MsgCode.LEAVE);
		CMMsg leaveMsg=CMClass.getMsg(mob,null,exit,code,"^[S-NAME] leave(s).");
		
		int gotDepart=0;
		int gotEntrance=0;
		boolean success=false;
		try{
			gotDepart=thisRoom.getLock(0);
			gotEntrance=(destRoom==null?0:destRoom.getLock(0));
			if((gotDepart!=2)&&(gotEntrance!=2))
				success=thisRoom.doAndReturnMsg(leaveMsg);
			if(success && destRoom!=null)
			{
				EnvMap.EnvLocation entranceLoc=null;
				boolean positions=destRoom.hasPositions();
				if(positions && entrance!=null)
					entranceLoc=destRoom.positionOf(entrance);
				if(entranceLoc==null)
				{
					if(positions)
						destRoom.placeHere(mob.body(), true, 0, 0, 0);
					else
						destRoom.bringHere(mob.body(), true);
				}
				else
					destRoom.placeHere(mob.body(), true, entranceLoc.x, entranceLoc.y, entranceLoc.z);
				if(!nolook)
					CMLib.commands().postLook(mob);
			}
		}finally{
			if(gotEntrance==1) destRoom.returnLock();
			if(gotDepart==1) thisRoom.returnLock();
		}
		return success;
	}

	public static int[] getMoveDistance(MOB mob, Vector<String> commands, int index)
	{
		int[] calculatedDistances=null;
		while(commands.size()>index)
		{
			int distance=getDistance(mob, commands, index);
			if(distance!=0)
			{
				if(distance<0)
				{
					distance=-distance;
					index++;
				}
				index++;
			}
			Directions.Dirs dir;
			if(commands.size()>index && (dir=Directions.getGoodDirectionCode(commands.get(index)))!=null)
			{
				if(calculatedDistances==null) calculatedDistances=new int[3];
				index++;
				doneDistance:
				if(distance==0)
				{
					if(commands.size()>index)
					{
						distance=getDistance(mob, commands, index);
						if(distance!=0)
						{
							if(distance<0)
							{
								distance=-distance;
								index++;
							}
							index++;
							break doneDistance;
						}
					}
					distance=1000;	//TODO: MOB setting for default distance/pace
				}
				//TODO: MOB setting for diagonal scaling
				calculatedDistances[0]+=dir.xMulti*distance;
				calculatedDistances[1]+=dir.yMulti*distance;
				calculatedDistances[2]+=dir.zMulti*distance;
				continue;
			}
			break;
		}
		return calculatedDistances;
	}
	protected static int getDistance(MOB mob, Vector<String> commands, int index)
	{
		float distanceRequest;
		//if(index>=commands.size())
		//	Log.errOut("Go",""+index+", "+commands.size()+", "+CMParms.combine(commands,0));
		String distanceString=commands.get(index);
		if(CMath.isNumber(distanceString))
		{
			distanceRequest=CMath.s_float(distanceString);
			if(distanceRequest<=0) return 0;
			//TODO: Check for special things like inches/feet/paces/miles (else assume paces.. or MOB default).
			//If found return a negative value, if not found return a positive value.
			//For now, just multiplying by 1000 to get standardized units.
			return (int)(distanceRequest*1000);
		}
		return 0;
	}
	
	@Override public boolean execute(MOB mob, MOB.QueuedCommand commands)
	{
		if(commands.cmdString.length()==0)
		{
			if(!(commands.data instanceof QueuedMove)) return false;
			int[] moveDistance=((QueuedMove)commands.data).moveCommand;
			if(moveDistance.length!=3) return false;
			
			Room R=mob.location();
			if(!R.hasPositions()) return false;
			EnvMap.EnvLocation mobLocation=(R.positionOf(mob.body()));
			if(mobLocation==null) throw new NullPointerException(mob.name()+" was not found in their current room!");
			EnvStats roomSize=R.getEnvObject().envStats();
			boolean leave=false;
			if(moveDistance[0]!=0 && Math.abs(mobLocation.x+moveDistance[0])>roomSize.width()/2)
				leave=true;
			if(moveDistance[1]!=0 && Math.abs(mobLocation.y+moveDistance[1])>roomSize.length()/2)
				leave=true;
			if(moveDistance[2]!=0 && Math.abs(mobLocation.z+moveDistance[2])>roomSize.height()/2)
				leave=true;
			if(leave)
			{
				int numExits=R.numExits();
				if(numExits==0)
				{
					mob.tell("You don't see any way out of here!");
					return false;
				}
				EnvMap.EnvLocation exitLocation=null;
				ExitInstance exit=null;
				found:
				if(numExits==1)
				{
					exit=R.getExitInstance(0);
					if(exit!=null && exit!=((QueuedMove)commands.data).lastExit)
						exitLocation=R.positionOf(exit);
				} else {
					ArrayList<EnvMap.EnvLocation> options=new ArrayList(numExits);
					long[] optionScores=new long[numExits];
					int option=0;
					for(Iterator<ExitInstance> iter=R.getAllExits();iter.hasNext();)
					{
						EnvMap.EnvLocation next=R.positionOf(iter.next());
						if(next==null || next.item==((QueuedMove)commands.data).lastExit) continue;
						long dotProduct=(long)(next.x-mobLocation.x)*moveDistance[0];
						dotProduct+=(long)(next.y-mobLocation.y)*moveDistance[1];
						dotProduct+=(long)(next.z-mobLocation.z)*moveDistance[2];
						if(dotProduct <= 0) continue;
						options.add(next);
						optionScores[option]=dotProduct;
						option++;
					}
					if(option==0)
					{
						mob.tell("That's far from this place and you're not sure of the best way to get there.");
						return false;
					}
					if(option==1)
					{
						exitLocation=options.get(0);
						break found;
					}
					long targetMag=moveDistance[0]*moveDistance[0];
					targetMag+=moveDistance[1]*moveDistance[1];
					targetMag+=moveDistance[2]*moveDistance[2];
					int lowestScore=-1;
					for(int i=0;i<option;i++)
					{
						EnvMap.EnvLocation exitLoc=options.get(i);
						long tempDist=exitLoc.x-mobLocation.x;
						long exitMag=tempDist*tempDist;
						tempDist=exitLoc.y-mobLocation.y;
						exitMag+=tempDist*tempDist;
						tempDist=exitLoc.z-mobLocation.z;
						exitMag+=tempDist*tempDist;
						optionScores[i]=exitMag*targetMag-optionScores[i];
						if((lowestScore==-1) || (optionScores[lowestScore] > optionScores[i]))
							lowestScore=i;
					}
					exitLocation=options.get(lowestScore);
				}
				if(exitLocation!=null)
				{
					moveDistance[0]-=(exitLocation.x-mobLocation.x);
					moveDistance[1]-=(exitLocation.y-mobLocation.y);
					moveDistance[2]-=(exitLocation.z-mobLocation.z);

					mob.goToThing(exitLocation, mobLocation, R);
					MOB.QueuedCommand qCom=MOB.QueuedCommand.newQC();
					qCom.command=this;
					qCom.cmdString="";
					qCom.data=QueuedMove.newMove(exit.getExit().oppositeOf(exit, exit.getDestination()), moveDistance);
					mob.enqueCommand(qCom, true);//TODO: queue command to go the remaining moveDistance
					return false;
				}
				return false;
			}
			mob.goDistance(moveDistance, mobLocation, R);
			return false;
		}
		String whereStr;
		{
			int i=commands.cmdString.indexOf(' ');
			if(i<0)
			{
				mob.tell("Go to where?");
				return false;
			}
			else
				whereStr=commands.cmdString.substring(i+1).trim();
		}
	//public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	//{
		//String whereStr=CMParms.combine(commands,1);
		Room R=mob.location();

//TODO: finish this when room grids are done.
/*
		Interactable E=null;
		if(R!=null)
			E=CMLib.english().fetchInteractable(whereStr, false, 1, R);
		if(E instanceof Rideable)
		{
			Command C=CMClass.getCommand("Enter");
			return C.execute(mob,commands,metaFlags);
		}
*/
		if(R.hasPositions())
		{
			EnvMap.EnvLocation mobLocation=(R.positionOf(mob.body()));
			if(mobLocation==null) throw new NullPointerException(mob.name()+" was not found in their current room!");
			
			int[] moveDistance = getMoveDistance(mob, CMParms.parse(commands.cmdString,1,-1), 0);
			if(moveDistance!=null)
			{
				EnvStats roomSize=R.getEnvObject().envStats();
				boolean leave=false;
				if(moveDistance[0]!=0 && Math.abs(mobLocation.x+moveDistance[0])>=roomSize.width()/2)
					leave=true;
				if(moveDistance[1]!=0 && Math.abs(mobLocation.y+moveDistance[1])>=roomSize.length()/2)
					leave=true;
				if(moveDistance[2]!=0 && Math.abs(mobLocation.z+moveDistance[2])>=roomSize.height()/2)
					leave=true;
				if(leave)
				{
					//If an exit CONTAINS target position it autowins (impossible if leave so doesn't matter)
					//If an exit intersects target vector and is on edge of room it autowins (TODO)
					//Get all exits' position
					//Autowin if only one exit exists
					//Dot product direction to exit with target vector
					//Remove anything with negative score
					//Autowin if only one exit is left
					//Calculate target direction's magnitude^2 (x^2+y^2+z^2)
					//Calculate each option's magnitude^2, multiply by direction's magnitude^2, subtract option's dot product
					//Lowest result wins
					int numExits=R.numExits();
					if(numExits==0)
					{
						mob.tell("You don't see any way out of here!");
						return false;
					}
					EnvMap.EnvLocation exitLocation=null;
					ExitInstance exit=null;
					found:
					if(numExits==1)
					{
						exit=R.getExitInstance(0);
						if(exit!=null)
							exitLocation=R.positionOf(exit);
					} else {
						ArrayList<EnvMap.EnvLocation> options=new ArrayList(numExits);
						long[] optionScores=new long[numExits];
						int option=0;
						for(Iterator<ExitInstance> iter=R.getAllExits();iter.hasNext();)
						{
							EnvMap.EnvLocation next=R.positionOf(iter.next());
							if(next==null) continue;
							long dotProduct=(long)(next.x-mobLocation.x)*moveDistance[0];
							dotProduct+=(long)(next.y-mobLocation.y)*moveDistance[1];
							dotProduct+=(long)(next.z-mobLocation.z)*moveDistance[2];
							if(dotProduct <= 0) continue;
							options.add(next);
							optionScores[option]=dotProduct;
							option++;
						}
						if(option==0)
						{
							mob.tell("That's far from this place and you're not sure of the best way to get there.");
							return false;
						}
						if(option==1)
						{
							exitLocation=options.get(0);
							break found;
						}
						long targetMag=moveDistance[0]*moveDistance[0];
						targetMag+=moveDistance[1]*moveDistance[1];
						targetMag+=moveDistance[2]*moveDistance[2];
						int lowestScore=-1;
						for(int i=0;i<option;i++)
						{
							EnvMap.EnvLocation exitLoc=options.get(i);
							long tempDist=exitLoc.x-mobLocation.x;
							long exitMag=tempDist*tempDist;
							tempDist=exitLoc.y-mobLocation.y;
							exitMag+=tempDist*tempDist;
							tempDist=exitLoc.z-mobLocation.z;
							exitMag+=tempDist*tempDist;
							optionScores[i]=exitMag*targetMag-optionScores[i];
							if((lowestScore==-1) || (optionScores[lowestScore] > optionScores[i]))
								lowestScore=i;
						}
						exitLocation=options.get(lowestScore);
					}
					if(exitLocation!=null)
					{
						moveDistance[0]-=(exitLocation.x-mobLocation.x);
						moveDistance[1]-=(exitLocation.y-mobLocation.y);
						moveDistance[2]-=(exitLocation.z-mobLocation.z);
						
						mob.goToThing(exitLocation, mobLocation, R);
						MOB.QueuedCommand qCom=MOB.QueuedCommand.newQC();
						qCom.command=this;
						qCom.cmdString="";
						qCom.data=QueuedMove.newMove(exit.getExit().oppositeOf(exit, exit.getDestination()), moveDistance);
						mob.enqueCommand(qCom, true);
						return false;
					}
					return false;
				}
				mob.goDistance(moveDistance, mobLocation, R);
				return false;
			}
			EnvMap.EnvLocation obj=R.findObject(whereStr, mob, mobLocation);
			if(obj!=null)
			{
				mob.goToThing(obj, mobLocation, R);
				return false;
			}
			
			mob.tell("You can't find "+whereStr+" here.");
		}
		else
		{
			ExitInstance map=R.getExitInstance(whereStr);
			if(map!=null)
				move(mob,R,map,false,false);
			else
				mob.tell("There is no exit like that.");
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_LOW_P_ACTION;}
	@Override public boolean canBeOrdered(){return true;}
}