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
/**
 * Source
 * A Vector containing Environmentals. Anything that's directly responsible for triggering the msg.
 *
 * Target
 * Any one Environmental. With how messages typically work it's best to have each target have
 * its own message, instead of handling a lump of targets together.
 * 
 * Tool
 * A Vector containing CMObjects. Anything that's directly used for triggering the msg.
 *
 * Source Code
 * EnumSet of CMMsg values.
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


	/* helpful message groupings
	public static final int MSK_CAST_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC;
	public static final int MSK_CAST_MALICIOUS_VERBAL=MASK_SOUND|MASK_MOUTH|MASK_MAGIC|MASK_MALICIOUS;
	public static final int MSK_CAST_SOMANTIC=MASK_HANDS|MASK_MAGIC;
	public static final int MSK_CAST_MALICIOUS_SOMANTIC=MASK_HANDS|MASK_MAGIC|MASK_MALICIOUS;
	public static final int MSK_HAGGLE=MASK_HANDS|MASK_SOUND|MASK_MOUTH;
	public static final int MSK_CAST=MSK_CAST_VERBAL|MSK_CAST_SOMANTIC;
	public static final int MSK_CAST_MALICIOUS=MSK_CAST_MALICIOUS_VERBAL|MSK_CAST_MALICIOUS_SOMANTIC;
	public static final int MSK_MALICIOUS_MOVE=MASK_MALICIOUS|MASK_MOVE|MASK_SOUND;
	// all major messages
	public static final int NO_EFFECT=0;
	public static final int MSG_AREAAFFECT=MASK_ALWAYS|TYP_AREAAFFECT;
	public static final int MSG_PUSH=MASK_HANDS|TYP_PUSH;
	public static final int MSG_PULL=MASK_HANDS|TYP_PULL;
	public static final int MSG_RECALL=MASK_SOUND|TYP_RECALL; // speak precludes animals
	public static final int MSG_OPEN=MASK_HANDS|TYP_OPEN;
	public static final int MSG_CLOSE=MASK_HANDS|TYP_CLOSE;
	public static final int MSG_PUT=MASK_HANDS|TYP_PUT;
	public static final int MSG_GET=MASK_HANDS|TYP_GET;
	public static final int MSG_UNLOCK=MASK_HANDS|TYP_UNLOCK;
	public static final int MSG_LOCK=MASK_HANDS|TYP_LOCK;
	public static final int MSG_WIELD=MASK_HANDS|TYP_WIELD;
	public static final int MSG_GIVE=MASK_HANDS|TYP_GIVE;
	public static final int MSG_BUY=MSK_HAGGLE|TYP_BUY;
	public static final int MSG_SELL=MSK_HAGGLE|TYP_SELL;
	public static final int MSG_DROP=MASK_HANDS|TYP_DROP;
	public static final int MSG_WEAR=MASK_HANDS|TYP_WEAR;
	public static final int MSG_FILL=MASK_HANDS|MASK_MOVE|MASK_SOUND|TYP_FILL;
	public static final int MSG_DELICATE_SMALL_HANDS_ACT=MASK_HANDS|MASK_DELICATE|TYP_DELICATE_HANDS_ACT;
	public static final int MSG_DELICATE_HANDS_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_DELICATE_HANDS_ACT;
	public static final int MSG_THIEF_ACT=MASK_HANDS|MASK_MOVE|MASK_DELICATE|TYP_JUSTICE;
	public static final int MSG_VALUE=MSK_HAGGLE|TYP_VALUE;
	public static final int MSG_HOLD=MASK_HANDS|TYP_HOLD;
	public static final int MSG_NOISYMOVEMENT=MASK_HANDS|MASK_SOUND|MASK_MOVE|TYP_NOISYMOVEMENT;
	public static final int MSG_QUIETMOVEMENT=MASK_HANDS|MASK_MOVE|TYP_QUIETMOVEMENT;
	public static final int MSG_RELOAD=MASK_HANDS|TYP_RELOAD;
	public static final int MSG_WEAPONATTACK=MASK_HANDS|MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_WEAPONATTACK;
	public static final int MSG_LOOK=MASK_EYES|TYP_LOOK;
	public static final int MSG_READ=MASK_EYES|TYP_READ;
	public static final int MSG_NOISE=MASK_SOUND|TYP_NOISE;
	public static final int MSG_SPEAK=MASK_SOUND|MASK_MOUTH|TYP_SPEAK;
	public static final int MSG_CAST_VERBAL_SPELL=MSK_CAST_VERBAL|TYP_CAST_SPELL;
	public static final int MSG_LIST=MASK_SOUND|MASK_MOUTH|TYP_LIST;
	public static final int MSG_EAT=MASK_HANDS|MASK_MOUTH|TYP_EAT;
	public static final int MSG_ENTER=MASK_MOVE|MASK_SOUND|TYP_ENTER;
	public static final int MSG_CAST_ATTACK_VERBAL_SPELL=MSK_CAST_MALICIOUS_VERBAL|TYP_CAST_SPELL;
	public static final int MSG_LEAVE=MASK_MOVE|MASK_SOUND|TYP_LEAVE;
	public static final int MSG_SLEEP=MASK_MOVE|TYP_SLEEP;
	public static final int MSG_SIT=MASK_MOVE|TYP_SIT;
	public static final int MSG_STAND=MASK_MOVE|TYP_STAND;
	public static final int MSG_FLEE=MASK_MOVE|MASK_SOUND|TYP_FLEE;
	public static final int MSG_CAST_SOMANTIC_SPELL=MSK_CAST_SOMANTIC|TYP_CAST_SPELL;
	public static final int MSG_CAST_ATTACK_SOMANTIC_SPELL=MSK_CAST_MALICIOUS_SOMANTIC|TYP_CAST_SPELL;
	public static final int MSG_CAST=MSK_CAST|TYP_CAST_SPELL;
	public static final int MSG_CAST_MALICIOUS=MSK_CAST_MALICIOUS|TYP_CAST_SPELL;
	public static final int MSG_OK_ACTION=MASK_SOUND|MASK_ALWAYS|TYP_OK_ACTION;
	public static final int MSG_OK_VISUAL=MASK_ALWAYS|TYP_OK_VISUAL;
	public static final int MSG_DRINK=MASK_HANDS|MASK_MOUTH|TYP_DRINK;
	public static final int MSG_HANDS=MASK_HANDS|TYP_HANDS;
	public static final int MSG_EMOTE=MASK_SOUND|MASK_HANDS|TYP_EMOTE;
	public static final int MSG_FOLLOW=MASK_ALWAYS|TYP_FOLLOW;
	public static final int MSG_NOFOLLOW=MASK_ALWAYS|TYP_NOFOLLOW;
	public static final int MSG_WRITE=MASK_HANDS|TYP_WRITE;
	public static final int MSG_MOUNT=MASK_MOVE|MASK_SOUND|TYP_MOUNT;
	public static final int MSG_DISMOUNT=MASK_MOVE|MASK_SOUND|TYP_DISMOUNT;
	public static final int MSG_SERVE=MASK_MOUTH|MASK_SOUND|TYP_SERVE;
	public static final int MSG_REBUKE=MASK_MOUTH|MASK_SOUND|TYP_REBUKE;
	public static final int MSG_ADVANCE=MASK_MOVE|MASK_SOUND|MASK_MALICIOUS|TYP_ADVANCE;
	public static final int MSG_DEATH=MASK_SOUND|MASK_ALWAYS|TYP_DEATH;
	public static final int MSG_WITHDRAW=MASK_HANDS|TYP_WITHDRAW;
	public static final int MSG_DEPOSIT=MASK_HANDS|TYP_DEPOSIT;
	public static final int MSG_QUIT=MASK_ALWAYS|TYP_QUIT;
	public static final int MSG_SHUTDOWN=MASK_ALWAYS|TYP_SHUTDOWN;
	public static final int MSG_VIEW=MASK_SOUND|MASK_MOUTH|TYP_VIEW;
	public static final int MSG_RETIRE=MASK_ALWAYS|TYP_RETIRE;
	public static final int MSG_RETREAT=MASK_MOVE|MASK_SOUND|TYP_RETREAT;
	public static final int MSG_PANIC=MASK_MOVE|MASK_SOUND|TYP_PANIC;
	public static final int MSG_THROW=MASK_HANDS|MASK_SOUND|TYP_THROW;
	public static final int MSG_EXTINGUISH=MASK_HANDS|TYP_EXTINGUISH;
	public static final int MSG_TELL=MASK_ALWAYS|TYP_TELL;
	public static final int MSG_SITMOVE=MASK_MOVE|TYP_SITMOVE;
	public static final int MSG_KNOCK=MASK_HANDS|MASK_SOUND|TYP_KNOCK;
	public static final int MSG_TEACH=MASK_HANDS|MASK_SOUND|MASK_MOUTH|MASK_MOVE|TYP_TEACH;
	public static final int MSG_REMOVE=MASK_HANDS|TYP_REMOVE;
	public static final int MSG_DAMAGE=MASK_ALWAYS|TYP_DAMAGE;
	public static final int MSG_HEALING=MASK_ALWAYS|TYP_HEALING;
	public static final int MSG_ROOMRESET=MASK_ALWAYS|TYP_ROOMRESET;
	public static final int MSG_SNIFF=MASK_HANDS|TYP_SNIFF;
	public static final int MSG_ACTIVATE=MASK_HANDS|TYP_ACTIVATE;
	public static final int MSG_DEACTIVATE=MASK_HANDS|TYP_DEACTIVATE;
	public static final int MSG_LOGIN=MASK_ALWAYS|TYP_LOGIN;
	public static final int MSG_LEVEL=MASK_ALWAYS|TYP_LEVEL;
	public static final int MSG_EXAMINE=MASK_EYES|TYP_EXAMINE;
	public static final int MSG_ORDER=MASK_SOUND|MASK_MOUTH|TYP_ORDER;
	public static final int MSG_EXPIRE=MASK_ALWAYS|TYP_EXPIRE;
	public static final int MSG_BORROW=MASK_HANDS|TYP_BORROW;
	public static final int MSG_EAT_GROUND=MASK_MOUTH|TYP_EAT;
	public static final int MSG_HUH=MASK_ALWAYS|TYP_HUH;
	public static final int MSG_BRINGTOLIFE=MASK_ALWAYS|TYP_LIFE;
	public static final int MSG_LAYDOWN=MASK_MOVE|TYP_LAYDOWN;
*/
}
