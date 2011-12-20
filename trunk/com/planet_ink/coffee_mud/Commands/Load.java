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
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.*;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class Load extends StdCommand
{
	public Load(){}

	private String[] access={"LOAD"};
	public String[] getAccessWords(){return access;}
	public boolean execute(MOB mob, Vector commands, int metaFlags)
		throws java.io.IOException
	{
		String list="RESOURCE, "+CMParms.toStringList(CMClass.Objects.values());
		if(commands.size()<3)
		{
			mob.tell("LOAD what? Use LOAD "+list+" [CLASS NAME]");
			return false;
		}
		String what=(String)commands.elementAt(1);
		String name=CMParms.combine(commands,2);
		if(what.equalsIgnoreCase("RESOURCE"))
		{
			CMFile F=new CMFile(name,mob,true);
			if((!F.exists())||(!F.canRead()))
				mob.tell("File '"+name+"' could not be accessed.");
			else
			{
				StringBuffer buf=Resources.getFileResource(name,true); // enforces its own security
				if((buf==null)||(buf.length()==0))
					mob.tell("Resource '"+name+"' was not found.");
				else
					mob.tell("Resource '"+name+"' was loaded.");
			}
		}
		else
		if(CMSecurity.isASysOp(mob))
		{
			try
			{
				if(name.toUpperCase().endsWith(".JAVA"))
				{
					while(name.startsWith("/")) name=name.substring(1);
					Class<?> C=null;
					Object CO=null;
					try{
						C=Class.forName("com.sun.tools.javac.Main", true, CMClass.instance());
						if(C!=null) CO=C.newInstance();
					}catch(Exception e){
						Log.errOut("Load",e.getMessage());
					}
					ByteArrayOutputStream bout=new ByteArrayOutputStream();
					PrintWriter pout=new PrintWriter(new OutputStreamWriter(bout)); 
					if(CO==null)
					{
						mob.tell("Unable to instantiate compiler.  You might try including your Java JDK's lib/tools.jar in your classpath next time you boot the mud.");
						return false;
					}
					String[] args=new String[]{name};
					if(C!=null)
					{
						java.lang.reflect.Method M=C.getMethod("compile",new Class[]{args.getClass(),PrintWriter.class});
						Object returnVal=M.invoke(CO,new Object[]{args,pout});
						if((returnVal instanceof Integer)&&(((Integer)returnVal).intValue()!=0))
						{
							mob.tell("Compile failed:");
							if(mob.session()!=null)
								mob.session().rawOut(bout.toString());
							return false;
						}
					}
					name=name.substring(0,name.length()-5)+".class";
				}
				
				String unloadClassName=name;
				if(unloadClassName.toUpperCase().endsWith(".CLASS"))
					unloadClassName=unloadClassName.substring(0,unloadClassName.length()-6);
				unloadClassName=unloadClassName.replace('\\','.');
				unloadClassName=unloadClassName.replace('/','.');
				
				if(what.equalsIgnoreCase("CLASS"))
				{
					Object O=CMClass.getClass(unloadClassName);
					if(O!=null)
					{
						CMClass.Objects x=CMClass.classCode(O);
						if(x!=null) what=x.toString();
					}
				}
				if(CMClass.valueOf(CMClass.Objects.class, what)==null)
					mob.tell("Don't know how to load a '"+what+"'.  Try one of the following: "+list);
				else
				{
					Object O=CMClass.getClass(unloadClassName);
					if((O instanceof CMObject)
					&&(name.toUpperCase().endsWith(".CLASS"))
					&&(CMClass.classCode(what).remove((CMObject)O))) //delClass(what,(CMObject)O))) 
						mob.tell(unloadClassName+" was unloaded.");
					if(CMClass.loadClass(what,name,false))
					{
						mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+" was successfully loaded.");
						return true;
					}
				}
			}
			catch(java.lang.Error err)
			{
				mob.tell(err.getMessage());
			}
			catch(Throwable t)
			{
				Log.errOut("Load",t.getClass().getName()+": "+t.getMessage());
			}
			mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+" was not loaded.");
		}
		return false;
	}
	
	public boolean canBeOrdered(){return true;}
	public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,mob.location(),"LOADUNLOAD");}
}