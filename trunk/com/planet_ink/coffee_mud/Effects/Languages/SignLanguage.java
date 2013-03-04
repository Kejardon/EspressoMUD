package com.planet_ink.coffee_mud.Effects.Languages;


import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class SignLanguage extends StdLanguage
{
	public String ID() { return "SignLanguage"; }
	public String name(){ return "Sign Language";}
	public String writtenName() { return "Braille";}
	public static Vector wordLists=null;
	public SignLanguage()
	{
		super();
	}

	public Vector translationVector(String language)
	{
		return wordLists;
	}

	protected boolean processSourceMessage(CMMsg msg, String str, int numToMess)
	{
		if(msg.sourceMessage()==null) return true;
		int wordStart=msg.sourceMessage().indexOf('\'');
		if(wordStart<0) return true;
		String wordsSaid=CMStrings.getSayFromMessage(msg.sourceMessage());
		if(numToMess>0) wordsSaid=messChars(ID(),wordsSaid,numToMess);
		String fullMsgStr = CMStrings.substituteSayInMessage(msg.sourceMessage(),wordsSaid);
		wordStart=fullMsgStr.indexOf('\'');
		String startFullMsg=fullMsgStr.substring(0,wordStart);
		if(startFullMsg.indexOf("YELL^s")>0)
		{
			if(msg.firstSource() instanceof MOB)
				((MOB)msg.firstSource()).tell("You can't yell in sign language.");
			return false;
		}
		String oldStartFullMsg = startFullMsg;
		startFullMsg = startFullMsg.replace("say^s", "sign^s");
		startFullMsg = startFullMsg.replace("ask^s", "sign^s askingly");
		startFullMsg = startFullMsg.replace("exclaim^s", "sign^s excitedly");
		if(oldStartFullMsg.equals(startFullMsg))
		{
			int x=startFullMsg.toLowerCase().lastIndexOf("^s");
			if(x<0) 
				x=startFullMsg.trim().length();
			else
				x+=3;
			startFullMsg = startFullMsg.substring(0,x)+" in sign" +startFullMsg.substring(x);
		}
		
		msg.addTool(this);
		msg.removeSourceCode(CMMsg.MsgCode.SOUND);
		msg.addSourceCode(CMMsg.MsgCode.MOVE);
		return true;
	}
	
	protected boolean processNonSourceMessages(CMMsg msg, String str, int numToMess)
	{
		String fullOtherMsgStr=(msg.othersMessage()==null)?msg.targetMessage():msg.othersMessage();
		if(fullOtherMsgStr==null) return true;
		int wordStart=fullOtherMsgStr.indexOf('\'');
		if(wordStart<0) return true;
		String startFullMsg=fullOtherMsgStr.substring(0,wordStart);
		String verb = "sign^s";
		switch(CMLib.dice().roll(1, 20, 0))
		{
		case 1: case 2: case 3: case 4: case 5: verb="gesture^s"; break;
		case 6: verb="wave^s"; break;
		case 7: case 8: verb="gesticulate^s"; break;
		case 9: verb="wave^s ^[S-HIS-HER] fingers"; break;
		case 10: verb="wiggle^s ^[S-HIS-HER] hands"; break;
		case 11: case 12: verb="wave^s ^[S-HIS-HER] hands"; break;
		case 13: verb="wiggle^s ^[S-HIS-HER] fingers"; break;
		}
		String oldStartFullMsg = startFullMsg;
		startFullMsg = startFullMsg.replace("tell^s", verb);
		startFullMsg = startFullMsg.replace("say^s", verb);
		startFullMsg = startFullMsg.replace("ask^s", verb+" askingly");
		startFullMsg = startFullMsg.replace("exclaim^s", verb+" excitedly");
		if(oldStartFullMsg.equals(startFullMsg))
		{
			int x=startFullMsg.toLowerCase().lastIndexOf("^s");
			if(x<0) 
				x=startFullMsg.trim().length();
			else
				x+=3;
			startFullMsg = startFullMsg.substring(0,x)+" in "+verb+startFullMsg.substring(x);
		}
		startFullMsg=startFullMsg.trim() + ".";
		msg.addTool(this);
		msg.removeSourceCode(CMMsg.MsgCode.SOUND);
		msg.addSourceCode(CMMsg.MsgCode.MOVE);
		msg.setTargetMessage(startFullMsg);
		msg.setOthersMessage(startFullMsg);
		return true;
	}
	
	protected boolean translateOthersMessage(CMMsg msg, String sourceWords)
	{
		if((msg.othersMessage()!=null)&&(sourceWords!=null)&&(sourceWords!=null))
		{
			String otherMes=msg.othersMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.firstSource(),msg.target(),msg.firstTool(),otherMes,false);
			msg.addTrailerMsg(((MOB)affected).location(), CMClass.getMsg(msg.firstSource(),(MOB)affected,null,CMMsg.NO_EFFECT,null,msg.othersCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}
	
	protected boolean translateTargetMessage(CMMsg msg, String sourceWords)
	{
		if(msg.isTarget((MOB)affected)&&(msg.targetMessage()!=null))
		{
			String otherMes=msg.targetMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			if(msg.target()!=null)
				otherMes=CMLib.coffeeFilter().fullOutFilter(null,(MOB)affected,msg.firstSource(),msg.target(),msg.firstTool(),otherMes,false);
			msg.addTrailerMsg(((MOB)affected).location(), CMClass.getMsg(msg.firstSource(),(MOB)affected,null,CMMsg.NO_EFFECT,null,msg.targetCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")",CMMsg.NO_EFFECT,null));
			return true;
		}
		return false;
	}

	protected boolean translateChannelMessage(CMMsg msg, String sourceWords)
	{
		if(msg.hasSourceCode(CMMsg.MsgCode.CHANNEL)&&(msg.othersMessage()!=null))
		{
			String otherMes=msg.othersMessage();
			if((otherMes.lastIndexOf('\'')==otherMes.indexOf('\'')))
				otherMes=otherMes.replace('.', ' ')+'\''+sourceWords+'\'';
			msg.addTrailerMsg(((MOB)affected).location(), CMClass.getMsg(msg.source().clone(),null,null,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null,msg.othersCode(),CMStrings.substituteSayInMessage(otherMes,sourceWords)+" (translated from "+name()+")"));
			return true;
		}
		return false;
	}
	
}
