package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.io.*;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Load extends StdCommand
{
	public Load(){access=new String[]{"LOAD"};}
	protected String list;
	protected String list()
	{
		if(list==null)
		{
			StringBuilder listBuild=new StringBuilder("RESOURCE");
			for(CMClass.Objects obj : CMClass.Objects.values())
			{
				listBuild.append(", ").append(obj.name);
			}
			list=listBuild.toString();
		}
		return list;
	}

	@Override public boolean execute(MOB mob, Vector<String> commands, int metaFlags)
	{
		if(commands.size()<3)
		{
			mob.tell("LOAD what? Use LOAD "+list()+" [CLASS NAME]");
			return false;
		}
		String what=commands.elementAt(1);
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
/*
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
*/
				String unloadClassName=name;
				if(unloadClassName.toUpperCase().endsWith(".CLASS"))
					unloadClassName=unloadClassName.substring(0,unloadClassName.length()-6);
				unloadClassName=unloadClassName.replace('\\','.');
				unloadClassName=unloadClassName.replace('/','.');
				
				CMClass.Objects classType=null;
				if(what.equalsIgnoreCase("CLASS"))
				{
					CMObject O=CMClass.getClass(unloadClassName);
					if(O!=null)
						classType=CMClass.classCode(O);
				}
				else
					classType=CMClass.classCode(what);
				if(classType==null)
					mob.tell("Don't know how to load a '"+what+"'.  Try one of the following: "+list());
				else
				{
					if(CMClass.loadListToObj(classType, name, classType.ancestor, false))
					{
						mob.tell(what+" "+name+" was successfuly loaded.");
						return false;
					}
/*					CMObject O=CMClass.getClass(unloadClassName);
					if((O!=null)&&
					   //(name.toUpperCase().endsWith(".CLASS"))&&
					   (classType.remove(O)))
						mob.tell(unloadClassName+" was unloaded.");
					if(CMClass.loadClass(what,name,false))
					{
						mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+" was successfully loaded.");
						return true;
					} */
				}
			}
			catch(java.lang.Error err)
			{
				mob.tell(err.getMessage());
			}
			catch(Throwable t)
			{
				Log.errOut("Load",t);
			}
			mob.tell(CMStrings.capitalizeAndLower(what)+" "+name+" was not loaded.");
		}
		return false;
	}

	@Override public int commandType(MOB mob, String cmds){return CT_SYSTEM;}
	@Override public boolean canBeOrdered(){return true;}
	@Override public boolean securityCheck(MOB mob){return CMSecurity.isAllowed(mob,"LOADUNLOAD");}
}