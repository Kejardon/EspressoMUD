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
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/
//TODO: Redo stuff here using nio? Test in misc.java
@SuppressWarnings("unchecked")
public class CMFile
{
/*	public enum Flags
	{
		NOWRITELOCAL, //DIRECTORY, NOREADLOCAL, HIDDEN, 
	} */
	//public EnumSet<Flags> Savable=EnumSet.of(Flags.DIRECTORY, Flags.HIDDEN);
	//public static final int VFS_MASK_MASKSAVABLE=1+2+4;

	private static final char pathSeparator=File.separatorChar;
	private static final Charset inCharSet = Charset.defaultCharset();
	private static final Charset outCharSet = Charset.defaultCharset();

	private boolean logErrors=false;

	//private EnumSet vfsBits=EnumSet.noneOf(Flags.class);
	private boolean mayAccess=false;
	private String localPath=null;
	private String path=null;
	private String name=null;
	//private String author=null;
	private MOB accessor=null;
	//private long modifiedDateTime=System.currentTimeMillis();
	private File localFile=null;
	//private String parentDir=null;

	public CMFile(String filename, MOB user, boolean pleaseLogErrors)
	{ super(); buildCMFile(filename,user,pleaseLogErrors,false);}
	public CMFile(String filename, MOB user, boolean pleaseLogErrors, boolean forceAllow)
	{ super(); buildCMFile(filename,user,pleaseLogErrors,forceAllow);}

	public static String getProperExistingPath(String absolutePath)
	{
		absolutePath=localizeFilename(absolutePath);
		File localFile=new File(absolutePath);
		if(!localFile.exists())
		{
			File localDir=new File(".");
			int endZ=-1;
			boolean found=true;
			while((!localFile.exists())&&(endZ<absolutePath.length())&&(localDir.exists())&&(localDir.isDirectory())&&(found))
			{
				int startZ=endZ+1;
				endZ=absolutePath.indexOf(pathSeparator,startZ);
				if(endZ<0)
					endZ=absolutePath.length();
				String[] files=localDir.list();
				found=false;
				String altPath=null;
				for(int f=0;f<files.length;f++)
				{
					if(files[f].equalsIgnoreCase(absolutePath.substring(startZ,endZ)))
					{
						found=true;
						if(files[f].equals(absolutePath.substring(startZ,endZ)))
						{
							altPath=null;
							break;
						}
						altPath=files[f];
					}
				}
				if(found)
				{
					if(altPath!=null)
						absolutePath=absolutePath.substring(0,startZ)+altPath+((endZ<absolutePath.length())?absolutePath.substring(endZ):"");
					if(endZ!=absolutePath.length())
						localDir=new File(localDir.getAbsolutePath()+pathSeparator+absolutePath.substring(startZ,endZ));
				}
			}
		}
		return absolutePath;
	}
	private void buildCMFile(String absolutePath, MOB user, boolean pleaseLogErrors, boolean forceAllow)
	{
		accessor=user;
		localFile=null;
		logErrors=pleaseLogErrors;
		//if(accessor!=null) author=accessor.name();
		absolutePath=vfsifyFilename(absolutePath);
		name=absolutePath;
		int x=absolutePath.lastIndexOf('/');
		path="";
		if(x>=0)
		{
			path=absolutePath.substring(0,x);
			name=absolutePath.substring(x+1);
		}
		//parentDir=path;
		localPath=path.replace('/',pathSeparator);
		// fill in all we can
		//vfsBits=EnumSet.noneOf(Flags.class);
		String ioPath=getIOReadableLocalPathAndName();
		localFile=new File(ioPath);
		if(!localFile.exists())
		{
			File localDir=new File(".");
			int endZ=-1;
			boolean found=true;
			while((!localFile.exists())&&(endZ<ioPath.length())&&(localDir.exists())&&(localDir.isDirectory())&&(found))
			{
				int startZ=endZ+1;
				endZ=ioPath.indexOf(pathSeparator,startZ);
				if(endZ<0)
					endZ=ioPath.length();
				String[] files=localDir.list();
				found=false;
				String altPath=null;
				for(int f=0;f<files.length;f++)
				{
					if(files[f].equalsIgnoreCase(ioPath.substring(startZ,endZ)))
					{
						found=true;
						if(files[f].equals(ioPath.substring(startZ,endZ)))
						{
							altPath=null;
							break;
						}
						altPath=files[f];
					}
				}
				if(found)
				{
					if(altPath!=null)
						ioPath=ioPath.substring(0,startZ)+altPath+((endZ<ioPath.length())?ioPath.substring(endZ):"");
					if(endZ!=ioPath.length())
						localDir=new File(localDir.getAbsolutePath()+pathSeparator+ioPath.substring(startZ,endZ));
				}
			}
			int lastSep=ioPath.lastIndexOf(pathSeparator);
			if(lastSep>=0)
			{
				localPath=ioPath.substring(0,lastSep);
				path=localPath.replace(pathSeparator,'/');
				name=ioPath.substring(lastSep+1);
			}
			else
				name=ioPath;
			localFile=new File(getIOReadableLocalPathAndName());

		}

		//modifiedDateTime=localFile.lastModified();
		//if(localFile.isHidden()) vfsBits.add(Flags.HIDDEN);

		//boolean isADirectory=((localFile.exists()&&(localFile.isDirectory()));
		//boolean allowedToTraverseAsDirectory=isADirectory&&((accessor==null)||CMSecurity.canTraverseDir(accessor,absolutePath));
		mayAccess=((forceAllow)||(accessor==null)||(CMSecurity.canAccessFile(accessor,absolutePath)));
		/*boolean allowedToReadLocal=//(localFile!=null)&&
									//(localFile.exists())&&
									(allowedToWriteLocal||isADirectory);

		if(!allowedToReadLocal) vfsBits.add(Flags.NOREADLOCAL);
*/
		//if(!allowedToWriteLocal) vfsBits.add(Flags.NOWRITELOCAL);

		//if(isADirectory) vfsBits.add(Flags.DIRECTORY);
	}

	public CMFile getParent(){return new CMFile(path,accessor,false,false);}

	public boolean mustOverwrite()
	{
		if(localFile.isDirectory())
			return true;
		return canRead();
	}

	public boolean canRead()
	{
		if(!mayAccess) return false;
		return localFile.exists();
	}
	public boolean canWrite()
	{
		return mayAccess;
	}

	public boolean isDirectory(){return localFile.isDirectory();}
	public boolean exists(){ return localFile.exists();}
	//public boolean isFile(){return canRead()&&(!vfsBits.contains(Flags.DIRECTORY));}
	public long lastModified(){return localFile.lastModified();}
	public String accessor(){return ((accessor!=null)?accessor.name():"SYS_UNK");}
	//public boolean canLocalEquiv(){return (!vfsBits.contains(Flags.NOREADLOCAL));}
	public String getName(){return name;}	//NOTE: Not 100% sure this will equal(localFile.name())
	public String getAbsolutePath(){return "/"+getLocalPathAndName();}
	public String getLocalPathAndName()
	{
		if(localPath.length()==0)
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
		if(s.trim().length()==0) return ".";	//CAn this even happen?
		return s;
	}

	public boolean delete()
	{
		if(mayAccess)
			return localFile.delete();
		return false;
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
			reader=new BufferedReader(new InputStreamReader(new FileInputStream(getIOReadableLocalPathAndName()),inCharSet));
			while(reader.ready())
			{
				String line=reader.readLine();
				if(line!=null)
				{
					buf.append(line);
					buf.append("\r\n");
					continue;
				}
				else break;
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
			if ( reader != null )
				try
				{
					reader.close();
					reader = null;
				}
				catch ( final IOException ignore ) {}
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
			F=new BufferedReader(new InputStreamReader(new FileInputStream(getIOReadableLocalPathAndName()),inCharSet));
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
			if ( F != null )
				try
				{
					F.close();
					F = null;
				}
				catch ( final IOException ignore ){}
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
			if ( fileIn != null )
				try
				{
					fileIn.close();
					fileIn = null;
				}
				catch ( final IOException ignore ){}
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

	//This currently only is given StringBuffers
	public boolean saveRaw(CharSequence data)
	{
		if(data==null)
		{
			Log.errOut("CMFile","Unable to save file '"+getLocalPathAndName()+"': No Data.");
			return false;
		}
		if((localFile.isDirectory())||(!mayAccess))
		{
			Log.errOut("CMFile","Access error saving file '"+getLocalPathAndName()+"'.");
			return false;
		}

		FileOutputStream FW = null;
		try
		{
			ByteBuffer O=outCharSet.encode(CharBuffer.wrap(data));
			FW=new FileOutputStream(localFile,false);
			FileChannel FC=FW.getChannel();
			FC.write(O);
			FW.close();
			FW=null;
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			if ( FW != null )
				try
				{
					FW.close();
					FW = null;
				}
				catch ( final IOException ignore ){}
		}
		return false;
	}
	public boolean saveRaw(byte[] data)
	{
		if(data==null)
		{
			Log.errOut("CMFile","Unable to save file '"+getLocalPathAndName()+"': No Data.");
			return false;
		}
		if((localFile.isDirectory())||(!mayAccess))
		{
			Log.errOut("CMFile","Access error saving file '"+getLocalPathAndName()+"'.");
			return false;
		}

		FileOutputStream FW = null;
		try
		{
			ByteBuffer O=ByteBuffer.wrap(data);
			FW=new FileOutputStream(localFile,false);
			FileChannel FC=FW.getChannel();
			FC.write(O);
			FW.close();
			FW=null;
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			if ( FW != null )
				try
				{
					FW.close();
					FW = null;
				}
				catch ( final IOException ignore ){}
		}
		return false;
	}

	public boolean saveText(CharSequence data){ return saveText(data,false);}
	public boolean saveText(CharSequence data, boolean append)
	{
		if(data==null)
		{
			Log.errOut("CMFile","Unable to save file '"+getLocalPathAndName()+"': No Data.");
			return false;
		}
		if((localFile.isDirectory())||(!mayAccess))
		{
			Log.errOut("CMFile","Access error saving file '"+getLocalPathAndName()+"'.");
			return false;
		}

		//StringBuffer O=saveBufNormalize((data instanceof StringBuffer)?data:new StringBuffer(data));
		FileOutputStream FW = null;
		try
		{
			ByteBuffer O=outCharSet.encode(CharBuffer.wrap(data));
			FW=new FileOutputStream(localFile,append);
			FileChannel FC=FW.getChannel();
			FC.write(O);
			FW.close();
			FW=null;
			return true;
		}
		catch(IOException e)
		{
			Log.errOut("CMFile","Error Saving '"+getIOReadableLocalPathAndName()+"': "+e.getMessage());
		}
		finally
		{
			if ( FW != null )
				try
				{
					FW.close();
					FW = null;
				}
				catch ( final IOException ignore ) { }
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
		if(!mayAccess)
		{
			Log.errOut("CMFile","Access error making directory '"+getLocalPathAndName()+"'.");
			return false;
		}
		if(localFile.mkdirs())
			return true;
		Log.errOut("CMFile","Unable to mkdir '"+localFile.getAbsolutePath()+"'.");
		return false;
	}

	public String[] list()
	{
		return localFile.list();
	}

	public CMFile[] listFiles()
	{
		File[] list=localFile.listFiles();
		if(list==null) return null;
		CMFile[] finalDir=new CMFile[list.length];
		File F2;
		String subPath=getVFSPathAndName()+"/";
		for(int i=0;i<list.length;i++)
		{
			F2=list[i];
			CMFile CF=new CMFile(subPath+F2.getName(),accessor,false);
			finalDir[i]=CF;
		}
		return finalDir;
	}

	public static String localizeFilename(String filename)
	{
		filename=filename.trim();
		if(filename.startsWith("//"))
			filename=filename.substring(2);
		if((filename.length()>2)
		&&(Character.isLetter(filename.charAt(0))
		&&(filename.charAt(1)==':')))
			filename=filename.substring(2);
		while(filename.startsWith("/")) filename=filename.substring(1);
		while(filename.startsWith("\\")) filename=filename.substring(1);
		while(filename.endsWith("/"))
			filename=filename.substring(0,filename.length()-1);
		while(filename.endsWith("\\"))
			filename=filename.substring(0,filename.length()-1);
		if(pathSeparator=='\\')
			return filename.replace('/',pathSeparator);
		return filename.replace('\\',pathSeparator);
	}
	public static String vfsifyFilename(String filename)
	{
		filename=filename.trim();
		if(filename.startsWith("//"))
			filename=filename.substring(2);
		if((filename.length()>2)
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

	//Converts all ends of lines to \n. Efficient/standardized file saving I guess.
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

/*	//Not used at the moment, can't think of when I'd use it, so disabling it for the time being.
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
*/
}