package com.planet_ink.coffee_mud.Common.interfaces;
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
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
/**
 * Source
 * A Vector containing Interactables. Anything that's directly responsible for triggering the msg, usually.
 *
 * Target
 * Any one Interactable. With how messages typically work it's best to have each target have
 * its own message, instead of handling a lump of targets together.
 * 
 * Tool
 * A Vector containing CMObjects. Anything that's directly used for triggering the msg.
 *
 * Source Code
 * EnumSet of CMMsg values that the source is expected to respond/listen to.
 * 
 * Source Message
 * This is the string which the source MOBs will see should the event occur successfully.
 *
 * Target Code
 * Target Message
 * Others Code
 * Others Message
 * Same for target and unrelated mobs.
 * 
 * Debated changes (TODO):
 * Make a 'message type' code that holds the main MsgCode for the message for things to know how to react to the message.
 * Make seperate messages for visual/audio observation and a default message. 3 flags for each mob observing it: visual, audio, default (observed-but-null goes to default).
 * Add a dedicated 'obviousness' value for how close/observant people have to be to see/hear it (maybe 2 if seperate visual/audio messages).
 * Add a HashSet of previously called entities, if they've already been called (okMessage/executeMsg) do not call it again. Just prevent infinite loops of listeners. This miiiight not be necessary with careful design...
 * 
 */
@SuppressWarnings("unchecked")
public interface CMMsg extends CMCommon
{
	public static final EnumSet<MsgCode> NO_EFFECT=EnumSet.noneOf(MsgCode.class);
	
	public EnumSet<MsgCode> targetCode();
	public void setTargetCode(EnumSet<MsgCode> codes);
	public boolean addTargetCode(MsgCode code); //Return true if the Enum was new and added.
	public boolean removeTargetCode(MsgCode code); //Return true if the Enum was found and removed.
	public boolean hasTargetCode(MsgCode... code);
	public String targetMessage();
	public void setTargetMessage(String str);
	public boolean isTarget(Interactable E);
	public void setTarget(Interactable E);
	public Interactable target();

	public EnumSet<MsgCode> sourceCode();
	public void setSourceCode(EnumSet<MsgCode> codes);
	public boolean addSourceCode(MsgCode code); //Return true if the Enum was new and added.
	public boolean removeSourceCode(MsgCode code); //Return true if the Enum was found and removed.
	public boolean hasSourceCode(MsgCode... code);
	public String sourceMessage();
	public void setSourceMessage(String str);

	public EnumSet<MsgCode> othersCode();
	public void setOthersCode(EnumSet<MsgCode> codes);
	public boolean addOthersCode(MsgCode code); //Return true if the Enum was new and added.
	public boolean removeOthersCode(MsgCode code); //Return true if the Enum was found and removed.
	public boolean hasOthersCode(MsgCode... code);
	public String othersMessage();
	public void setOthersMessage(String str);
	public boolean isOthers(Interactable E);

	public Vector<CMObject> tool();
	public CMObject firstTool();
	public void setTools(Vector<CMObject> V);
	public boolean addTool(CMObject E);
	public boolean removeTool(CMObject E);
	public boolean isTool(CMObject E);
	public CMObject[] toolArr();

	public Vector<Interactable> source();
	public Interactable firstSource();
	public void setSource(Vector<Interactable> V);
	public boolean addSource(Interactable E);
	public boolean removeSource(Interactable E);
	public boolean isSource(Interactable E);
	public Interactable[] sourceArr();

	//Tempting to make this an array. Hm.
	public int value();
	public void setValue(int amount);

	//<ListenHolder.OKChecker> ? Probably.
	//Also probably have an internal sorted list with a container object holding the OKChecker and priority.
	public Vector<ListenHolder.MsgListener> responders();
	public void addResponse(ListenHolder.MsgListener E, int priority);
	public boolean handleResponses();

	public Vector<CMMsg> trailerMsgs();
	public void addTrailerMsg(CMMsg msg);

	public enum MsgCode
	{
		//Dropped codes:
		//RECALL, WIELD, TELL, BUY, SELL, DEPOSIT, WITHDRAW, WAND, HIT, PANIC, TELL
		//TEACH, EXPCHANGE, ROOMRESET, LEVEL, BORROW, EXPIRE
		//CHANNEL
		//OPTIMIZE
		
		//'Major' codes. More flags than message definitions
		HANDS, MOVE, EYES, MOUTH,
		SOUND, SNIFF, VISUAL,
		ALWAYS, MAGIC, DELICATE, MALICIOUS, CHANNEL,
		//'Minor' codes. Kinda grouped together and organized
		//Give might be same as Put? probably not.
		UNLOCK, LOCK, OPEN, CLOSE, PUSH, PULL, THROW, DROP, PUT, GET, GIVE,
		ENTER, LEAVE, SLEEP, CRAWL, SIT, LAYDOWN, STAND, MOUNT, DISMOUNT, ADVANCE, RETREAT,
		WEAR, REMOVE, ACTIVATE, DEACTIVATE, RELOAD, KNOCK, EXTINGUISH,
		FILL, EAT, DRINK,
		LOOK, EXAMINE, READ, WRITE, SPEAK, CAST, EMOTE, ORDER,
		FIRE, COLD, WATER, GAS, MIND, JUSTICE, ACID, ELECTRIC, POISON, PARALYZE, UNDEAD, DISEASE,
		ATTACK, DAMAGE, HEALING, DEATH, LIFE,
		QUIT, SHUTDOWN, RETIRE, HUH, LOGIN
	}
/* MsgCode standards. * means optional, may be empty/null. By default, Message reserved for visual(or sound) component, value is unused.
UNLOCK/LOCK/OPEN/CLOSE/PUSH/PULL/ACTIVATE/DEACTIVATE/EXTINGUISH:
Source* providing power/action, target is the lock/closeable/whatever, tool* is what is used to do the action(limb, key, spell, sword...).
	Value* is obviousness? For UNLOCK/LOCK/ACTIVATE/DEACTIVATE only probably
GET: Same as UNLOCK, source is also recipient of object and necessary (value* is obviousness).
KNOCK: Same as UNLOCK, value is how hard the object is hit.
THROW: Source providing power/action(first object, must exist, be null if none. IMPORTANT) then list* of details(i.e. limbs or spells doing the throwing), target is target, tool is list of things thrown
GIVE/RELOAD/MOUNT: Same as THROW but value* is obviousness.
	What should the target be for GIVE? :x or specifically what limb/tool is the recipient using?
DISMOUNT: Same as MOUNT but optional target* (if null, immediately below source. If specified, link immediately above target that leads to source)
PUT: Same as GIVE but target is optional (no target means the ground where the mob is at)
FILL: Same as THROW (tool is what it is filled with/by) but value is an optional amount..? What to do about obviousness... this is probably important for assassins poisoning drinks!
DROP: Same as THROW but optional target.
EAT/DRINK: Basically same as THROW, value* specifies amount to eat/drink.
WEAR/REMOVE: Basically same as THROW. Target(optional for remove) is the limb it's worn on (or if mob, find best-fit limb to put it on), tool is what's being put on (first is first handled(inmost for wear, outmost for remove)).
ENTER/LEAVE: Source is the thing(s) moving, target is the thing being entered/left, tool* is the spell used/object ridden. Value is obviousness.
SLEEP/CRAWL/SIT/LAYDOWN/STAND: Source is the thing(s) sleeping, target* is the location/object being slept on. Tool is always empty I guess, value* is obviousness.
: Source* is providing power/action, target is what is being mounted, tool is what is mounting. Value* is obviousness.
ADVANCE/RETREAT: Source is the thing(s) moving, target is what is being approached. Tool* would be a list of waypoints? Probably not. Distance calculated on the fly.
	Will probably change/think this over. Mechanics aren't ready for it yet anyways. TODO
LOOK/EXAMINE/READ: Basically the same as ENTER/LEAVE
WRITE: Source* providing power/action, target is what is being written on, tool* is utensil. Target message is the text written. Value* is obviousness.
	Actually, I kinda want to rehaul a lot of writing stuff. Maybe legible text should be its own CMObject. Worth considering for later, TODO.
SPEAK/EMOTE: Source* providing power/action, target* is who/what it's directed at, tool* is whatever used. Value* is the intensity(loudness for sound, obviousness for emote).
ORDER: Subflag on Speak or Emote probably.
CAST: Source* providing power/mana, target* is target (always resisting), tool is spell. Value* is obviousness.
FIRE-DISEASE: Subflags. Mainly used for ATTACK for special-case things probably.
ATTACK/HEALING: Source* providing power/action, target is target, tool is how they are attacked/healed.
DAMAGE: Subflag for attack. Alternatively, by itself to damage without a chance to dodge (if so value is raw damage).
DEATH: Source is thing that dies. Tool* is direct cause(s) of death?
LIFE: Source is thing that came to life. Tool* is what caused it?
QUIT/RETIRE/LOGIN/HUH: Source is player that did something. (Should HUH be a flag? Might make it just use mob.tell or something)
SHUTDOWN: I dunno. Can't think of anything actually important for a shutdown message.
*/
}