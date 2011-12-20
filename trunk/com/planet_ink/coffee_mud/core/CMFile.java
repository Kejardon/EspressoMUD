package com.planet_ink.coffee_mud.core;
//import com.planet_ink.coffee_mud.core.database.DBConnections;
import com.planet_ink.coffee_mud.core.interfaces.*;
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
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
@SuppressWarnings("unchecked")
public class CMFile
{
	public enum Flags
	{
		DIRECTORY, HIDDEN, NOREADLOCAL, NOWRITELOCAL
	}
	public EnumSet<Flags> Savable=EnumSet.of(Flags.DIRECTORY, Flags.HIDDEN);
	public static final int VFS_MASK_MASKSAVABLE=1+2+4;

	private static final char pathSeparator=File.separatorChar;
	private static final String inCharSet = Charset.defaultCharset().name();
	private static final String outCharSet = Charset.defaultCharset().name();

	private boolean logErrors=false;

	private EnumSet vfsBits=EnumSet.noneOf(Flags.class);
	private String localPath=null;
	private String path=null;
	private String name=null;
	private String author=null;
	private MOB accessor=null;
	private long modifiedDateTime=System.currentTimeMillis();
	private File localFile=null;
	private String parentDir=null;

	public CMFile(String filename, MOB user, boolean pleaseLogErrors)
	{ super(); buildCMFile(filename,user,pleaseLogErrors,false);}
	public CMFile(String filename, MOB user, boolean pleaseLogErrors, boolean forceAllow)
	{ super(); buildCMFile(filename,user,pleaseLogErrors,forceAllow);}
	public CMFile (String currentPath, String filename, MOB user, boolean pleaseLogErrors)
	{ super(); buildCMFile(incorporateBaseDir(currentPath,filename),user,pleaseLogErrors,false); }
	public CMFile (String currentPath, String filename, MOB user, boolean pleaseLogErrors, boolean forceAllow)
	{ super(); buildCMFile(incorporateBaseDir(currentPath,filename),user,pleaseLogErrors,forceAllow); }

	private void buildCMFile(String absolutePath, MOB user, boolean pleaseLogErrors, boolean forceAllow)
	{
		accessor=user;
		localFile=null;
		logErrors=pleaseLogErrors;
		if(accessor!=null) author=accessor.name();
		absolutePath=vfsifyFilename(absolutePath);
		name=absolutePath;
		int x=absolutePath.lastIndexOf('/');
		path="";
		if(x>=0)
		{
			path=absolutePath.substring(0,x);
			name=absolutePath.substring(x+1);
		}
		parentDir=path;
		localPath=path.replace('/',pathSeparator);
		// fill in all we can
		vfsBits=EnumSet.noneOf(Flags.class);
		String ioPath=getIOReadableLocalPathAndName();
		localFile=new File(ioPath);
		if(!localFile.exists())
		{
			File localDir=new File(".");
			int endZ=-1;
			boolean found=true;
			if((localDir.exists())&&(localDir.isDirectory()))
				parentDir="//"+localPath;
			while((!localFile.exists())&&(endZ<ioPath.length())&&(localDir.exists())&&(localDir.isDirectory())&&(found))
			{
				int startZ=endZ+1;
				endZ=ioPath.indexOf(pathSeparator,startZ);
				if(endZ<0)
					endZ=ioPath.length();
				String[] files=localDir.list();
				found=false;
				for(int f=0;f<files.length;f++)
				{
					if(files[f].equalsIgnoreCase(ioPath.substring(startZ,endZ)))
					{
						if(!files[f].equals(ioPath.substring(startZ,endZ)))
							ioPath=ioPath.substring(0,startZ)+files[f]+((endZ<ioPath.length())?ioPath.substring(endZ):"");
						found=true;
						break;
					}
				}
				if(found)
				{
					if(endZ==ioPath.length())
					{
						int lastSep=ioPath.lastIndexOf(pathSeparator);
						if(lastSep>=0)
						{
							localPath=ioPath.substring(0,lastSep);
							name=ioPath.substring(lastSep+1);
						}
						else
							name=ioPath;
						localFile=new File(getIOReadableLocalPathAndName());
					}
					else
						localDir=new File(localDir.getAbsolutePath()+pathSeparator+ioPath.substring(startZ,endZ));
				}
			}
		}

		modifiedDateTime=localFile.lastModified();
		if(localFile.isHidden()) vfsBits.add(Flags.HIDDEN);

		boolean isADirectory=((localFile!=null)&&(localFile.exists())&&(localFile.isDirectory()));
		boolean allowedToTraverseAsDirectory=isADirectory
										   &&((accessor==null)||CMSecurity.canTraverseDir(accessor,accessor.location(),absolutePath));
		boolean allowedToWriteLocal=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,accessor.location(),absolutePath,false)));
		boolean allowedToReadLocal=(localFile!=null)
									&&(localFile.exists())
									&&(allowedToWriteLocal||allowedToTraverseAsDirectory);

		if(!allowedToReadLocal) vfsBits.add(Flags.NOREADLOCAL);

		if(!allowedToWriteLocal) vfsBits.add(Flags.NOWRITELOCAL);

		if(allowedToTraverseAsDirectory)  vfsBits.add(Flags.DIRECTORY);
	}

	public CMFile getParent(){return new CMFile(path,accessor,false,false);}

	public boolean mustOverwrite()
	{
		if(!isDirectory())
			return canRead();
		return ((localFile!=null)&&(localFile.isDirectory()));
	}

	public boolean canRead()
	{
		if(!exists()) return false;
		if(vfsBits.contains(Flags.NOREADLOCAL))
			return false;
		return true;
	}
	public boolean canWrite()
	{
		if(vfsBits.contains(Flags.NOWRITELOCAL))
			return false;
		return true;
	}

	public boolean isDirectory(){return exists()&&vfsBits.contains(Flags.DIRECTORY);}
	public boolean exists(){ return !(vfsBits.contains(Flags.NOREADLOCAL));}
	public boolean isFile(){return canRead()&&(!vfsBits.contains(Flags.DIRECTORY));}
	public long lastModified(){return modifiedDateTime;}
	public String author(){return ((author!=null))?author:"SYS_UNK";}
	public boolean canLocalEquiv(){return (!vfsBits.contains(Flags.NOREADLOCAL));}
	public String getName(){return name;}
	public String getAbsolutePath(){return "/"+getLocalPathAndName();}
	public String getLocalPathAndName()
	{
		if(path.length()==0)
			return name;
		return localPath+pathSeparator+name;
	}
    public String getVFSPathAndName()
    {
        if(path.length()==0)
            return name;
        return path+'/'+name;
    }
	public String getIOReadableLocalPathAndName()
	{
		String s=getLocalPathAndName();
		if(s.trim().length()==0) return ".";
		return s;
	}

	public boolean mayDeleteIfDirectory()
	{
		if(!isDirectory()) return true;
		if((localFile!=null)&&(localFile.isDirectory())&&(localFile.list().length>0))
			return false;
		return true;
	}

	public boolean deleteLocal()
	{
		if(!exists()) return false;
		if(!canWrite()) return false;
		if(!mayDeleteIfDirectory()) return false;
		if((canLocalEquiv())&&(localFile!=null))
			return localFile.delete();
		return false;
	}

	public boolean delete()
	{
		if(!exists()) return false;
		if(!canWrite()) return false;
		if(!mayDeleteIfDirectory()) return false;
		return deleteLocal();
	}

	public StringBuffer text()
	{
		StringBuffer buf=new StringBuffer("");
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getLocalPathAndName()+"'.");
			return buf;
		}

		BufferedReader reader = null;
		try
		{
			reader=new BufferedReader(
				   new InputStreamReader(
				   new FileInputStream(
				   	getIOReadableLocalPathAndName()
			),inCharSet));
			String line="";
			while((line!=null)&&(reader.ready()))
			{
				line=reader.readLine();
				if(line!=null)
				{
					buf.append(line);
					buf.append("\n\r");
				}
			}
		}
		catch(Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
			return buf;
		}
		finally
		{
			try
			{
				if ( reader != null )
				{
					reader.close();
					reader = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	public StringBuffer textUnformatted()
	{
		StringBuffer buf=new StringBuffer("");
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getLocalPathAndName()+"'.");
			return buf;
		}
		Reader F = null;
		try
		{
			F=new InputStreamReader(
		 	  new FileInputStream(
			   	getIOReadableLocalPathAndName()
			 ),inCharSet);
			char c=' ';
			while(F.ready())
			{
				c=(char)F.read();
				if(c<0) break;
				buf.append(c);
			}
		}
		catch(Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
		}
		finally
		{
			try
			{
				if ( F != null )
				{
					F.close();
					F = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	public byte[] raw()
	{
		byte[] buf=new byte[0];
		if(!canRead())
		{
			if(logErrors)
				Log.errOut("CMFile","Access error on file '"+getLocalPathAndName()+"'.");
			return buf;
		}
		DataInputStream fileIn = null;
		try
		{
			fileIn = new DataInputStream( new BufferedInputStream( new FileInputStream(getIOReadableLocalPathAndName()) ) );
			buf = new byte [ fileIn.available() ];
			fileIn.readFully(buf);
			fileIn.close();
		}
		catch(Exception e)
		{
			if(logErrors)
				Log.errOut("CMFile",e.getMessage());
		}
		finally
		{
			try
			{
				if ( fileIn != null )
				{
					fileIn.close();
					fileIn = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return buf;
	}

	public StringBuffer textVersion(byte[] bytes)
	{
		StringBuffer text=new StringBuffer(CMStrings.bytesToStr(bytes));
		for(int i=0;i<text.length();i++)
			if((text.charAt(i)<0)||(text.charAt(i)>127))
				return null;
		return text;
	}

	public boolean saveRaw(Object data)
	{
		if(data==null)
		{
			Log.errOut("CMFile","Unable to save file '"+getLocalPathAndName()+"': No Data.");
			return false;
		}
		if((vfsBits.contains(Flags.DIRECTORY))
		||(!canWrite()))
		{
			Log.errOut("CMFile","Access error saving file '"+getLocalPathAndName()+"'.");
			return false;
		}

		Object O=null;
		if(data instanceof String)
			O=new StringBuffer((String)data);
		else
		if(data instanceof StringBuffer)
			O=(StringBuffer)data;
		else
		if(data instanceof byte[])
		{
			StringBuffer test=textVersion((byte[])data);
			if(test!=null)
				O=test;
			else
				O=(byte[])data;
		}
		else
			O=new StringBuffer(data.toString());

		FileOutputStream FW = null;
		try
		{
			File F=new File(getIOReadableLocalPathAndName());
			if(O instanceof StringBuffer)
				O=CMStrings.strToBytes(((StringBuffer)O).toString());
			if(O instanceof String)
				O=CMStrings.strToBytes(((String)O));
			if(O instanceof byte[])
			{
				FW=new FileOutputStream(F,false);
				FW.write((byte[])O);
				FW.flush();
				FW.close();
			}
			vfsBits.remove(Flags.NOREADLOCAL);
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			try
			{
				if ( FW != null )
				{
					FW.close();
					FW = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return false;
	}

	public boolean saveText(Object data){ return saveText(data,false);}
	public boolean saveText(Object data, boolean append)
	{
		if(data==null)
		{
			Log.errOut("CMFile","Unable to save file '"+getLocalPathAndName()+"': No Data.");
			return false;
		}
		if((vfsBits.contains(Flags.DIRECTORY))||(!canWrite()))
		{
			Log.errOut("CMFile","Access error saving file '"+getLocalPathAndName()+"'.");
			return false;
		}

		StringBuffer O=null;
		if(data instanceof String)
			O=new StringBuffer((String)data);
		else
		if(data instanceof StringBuffer)
			O=(StringBuffer)data;
		else
		if(data instanceof byte[])
			O=new StringBuffer(CMStrings.bytesToStr((byte[])data));
		else
			O=new StringBuffer(data.toString());

		FileWriter FW = null;
		try
		{
			File F=new File(getIOReadableLocalPathAndName());
			FW=new FileWriter(F,append);
			FW.write(saveBufNormalize(O).toString());
			FW.close();
			vfsBits.remove(Flags.NOREADLOCAL);
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			try
			{
				if ( FW != null )
				{
					FW.close();
					FW = null;
				}
			}
			catch ( final IOException ignore )
			{

			}
		}
		return false;
	}

	public boolean mkdir()
	{
		if(mustOverwrite())
		{
			Log.errOut("CMFile","File exists '"+getLocalPathAndName()+"'.");
			return false;
		}
		if(!canWrite())
		{
			Log.errOut("CMFile","Access error making directory '"+getLocalPathAndName()+"'.");
			return false;
		}
		String fullPath=getIOReadableLocalPathAndName();
		File F=new File(fullPath);
		File PF=F.getParentFile();
		Vector parents=new Vector();
		while(PF!=null)
		{
			parents.addElement(PF);
			PF=PF.getParentFile();
		}
		for(int p=parents.size()-1;p>=0;p--)
		{
			PF=(File)parents.elementAt(p);
			if((PF.exists())&&(PF.isDirectory())) continue;
			if((PF.exists()&&(!PF.isDirectory()))||(!PF.mkdir()))
			{
				Log.errOut("CMFile","Unable to mkdir '"+PF.getAbsolutePath()+"'.");
				return false;
			}
		}
		if(F.exists())
		{
			Log.errOut("CMFile","File exists '"+fullPath+"'.");
			return false;
		}
		if(F.mkdir())
		{
			vfsBits.remove(Flags.NOREADLOCAL);
			vfsBits.add(Flags.DIRECTORY);
			vfsBits.remove(Flags.NOWRITELOCAL);
			return true;
		}
		return false;
	}

	public String[] list()
	{
		if(!isDirectory()) return new String[0];
		CMFile[] CF=listFiles();
		String[] list=new String[CF.length];
		for(int f=0;f<CF.length;f++)
			list[f]=CF[f].getName();
		return list;
	}

	public boolean isLocalDirectory()
	{
		return (localFile!=null)&&(localFile.isDirectory());
	}

	public CMFile[] listFiles()
	{
		if((!isDirectory())||(!canRead()))
			return new CMFile[0];
		String prefix="//";
		Vector dir=new Vector();
		Vector fcheck=new Vector();
		String thisDir=getIOReadableLocalPathAndName();
		File F=new File(thisDir);
		if(F.isDirectory())
		{
			File[] list=F.listFiles();
			File F2=null;
			for(int l=0;l<list.length;l++)
			{
				F2=list[l];
				String thisPath=vfsifyFilename(thisDir)+"/"+F2.getName();
				String thisName=F2.getName();
				CMFile CF=new CMFile(prefix+thisPath,accessor,false);
				if((CF.canRead())
				&&(!fcheck.contains(thisName.toUpperCase())))
					dir.addElement(CF);
			}
		}

		CMFile[] finalDir=new CMFile[dir.size()];
		for(int f=0;f<dir.size();f++)
			finalDir[f]=(CMFile)dir.elementAt(f);
		return finalDir;
	}

	public static String vfsifyFilename(String filename)
	{
		filename=filename.trim();
		if(filename.startsWith("::"))
			filename=filename.substring(2);
		if(filename.startsWith("//"))
			filename=filename.substring(2);
		if((filename.length()>3)
		&&(Character.isLetter(filename.charAt(0))
		&&(filename.charAt(1)==':')))
			filename=filename.substring(2);
		while(filename.startsWith("/")) filename=filename.substring(1);
		while(filename.startsWith("\\")) filename=filename.substring(1);
		while(filename.endsWith("/"))
			filename=filename.substring(0,filename.length()-1);
		while(filename.endsWith("\\"))
			filename=filename.substring(0,filename.length()-1);
		return filename.replace(pathSeparator,'/');
	}

	private StringBuffer saveBufNormalize(StringBuffer myRsc)
	{
		for(int i=0;i<myRsc.length();i++)
			if(myRsc.charAt(i)=='\n')
			{
				for(i=myRsc.length()-1;i>=0;i--)
					if(myRsc.charAt(i)=='\r')
						myRsc.deleteCharAt(i);
				return myRsc;
			}
		for(int i=0;i<myRsc.length();i++)
			if(myRsc.charAt(i)=='\r')
				myRsc.setCharAt(i,'\n');
		return myRsc;
	}

	private static String incorporateBaseDir(String currentPath, String filename)
	{
		String starter="";
		if(filename.startsWith("::")||filename.startsWith("//"))
		{
			starter=filename.substring(0,2);
			filename=filename.substring(2);
		}
		if(!filename.startsWith("/"))
		{
			boolean didSomething=true;
			while(didSomething)
			{
				didSomething=false;
				if(filename.startsWith(".."))
				{
					filename=filename.substring(2);
					int x=currentPath.lastIndexOf("/");
					if(x>=0)
						currentPath=currentPath.substring(0,x);
					else
						currentPath="";
					didSomething=true;
				}
				if((filename.startsWith("."))&&(!(filename.startsWith(".."))))
				{
					filename=filename.substring(1);
					didSomething=true;
				}
				while(filename.startsWith("/")) filename=filename.substring(1);
			}
			if((currentPath.length()>0)&&(filename.length()>0))
				filename=currentPath+"/"+filename;
			else
			if(currentPath.length()>0)
				filename=currentPath;
		}
		return starter+filename;
	}

	public static CMFile[] getFileList(String currentPath, String filename, MOB user, boolean recurse, boolean expandDirs)
	{ return getFileList(incorporateBaseDir(currentPath,filename),user,recurse,expandDirs);}
	public static CMFile[] getFileList(String parse, MOB user, boolean recurse, boolean expandDirs)
	{
		CMFile dirTest=new CMFile(parse,user,false);
		if((dirTest.exists())&&(dirTest.isDirectory())&&(dirTest.canRead())&&(!recurse))
		{ return expandDirs?dirTest.listFiles():new CMFile[]{dirTest};}
		String vsPath=vfsifyFilename(parse);
		String fixedName=vsPath;
		int x=vsPath.lastIndexOf('/');
		String fixedPath="";
		if(x>=0)
		{
			fixedPath=vsPath.substring(0,x);
			fixedName=vsPath.substring(x+1);
		}
		CMFile dir=new CMFile("//"+fixedPath,user,false);
		if((!dir.exists())||(!dir.isDirectory())||(!dir.canRead()))
			return null;
		Vector set=new Vector();
		CMFile[] cset=dir.listFiles();
		fixedName=fixedName.toUpperCase();
		for(int c=0;c<cset.length;c++)
		{
			if((recurse)&&(cset[c].isDirectory())&&(cset[c].canRead()))
			{
				CMFile[] CF2=getFileList(cset[c].getLocalPathAndName()+"/"+fixedName,user,true,expandDirs);
				for(int cf2=0;cf2<CF2.length;cf2++)
					set.addElement(CF2[cf2]);
			}
			String name=cset[c].getName().toUpperCase();
			boolean ismatch=true;
			if((!name.equalsIgnoreCase(fixedName))
				&&(fixedName.length()>0))
			for(int f=0,n=0;f<fixedName.length();f++,n++)
				if(fixedName.charAt(f)=='?')
				{
					if(n>=name.length()){ ismatch=false; break; }
				}
				else
				if(fixedName.charAt(f)=='*')
				{
					if(f==fixedName.length()-1) break;
					char mustMatchC=fixedName.charAt(f+1);
					for(;n<name.length();n++)
						if(name.charAt(n)==mustMatchC)
							break;
					if((n<name.length())&&(name.charAt(n)==mustMatchC))
					{ n--; continue;}
					ismatch=false;
					break;
				}
				else
				if((n>=name.length())||(fixedName.charAt(f)!=name.charAt(n)))
				{ ismatch=false; break; }
			if(ismatch) set.addElement(cset[c]);
		}
		if(set.size()==1)
		{
			dirTest=(CMFile)set.firstElement();
			if((dirTest.exists())&&(dirTest.isDirectory())&&(dirTest.canRead())&&(!recurse))
			{ return expandDirs?dirTest.listFiles():new CMFile[]{dirTest};}
		}
		cset=new CMFile[set.size()];
		for(int s=0;s<set.size();s++)
			cset[s]=(CMFile)set.elementAt(s);
		return cset;
	}
}