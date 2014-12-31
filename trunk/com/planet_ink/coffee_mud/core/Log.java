package com.planet_ink.coffee_mud.core;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class Log
{
	private static Log logs=null;
	public Log(){ if(logs==null) logs=this; }
	public static Log instance()
	{
		//Log log=logs;
		if(logs==null) 
			return new Log();
		return logs;
	}
	public static Log newInstance(){
		logs=new Log();
		return logs;
	}
	
	/** final date format for headers */
	public static SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMdd.HHmm.ssSS");
	/** SPACES for headers */
	private static final String SPACES="                                                                                               ";

	/** always to "log" */
	private PrintWriter fileOutWriter=null;
	/** always to systemout */
	private PrintWriter systemOutWriter=new PrintWriter(System.out,true);
	/** The fully qualified file path */
	private String filePath = "";

	private Hashtable WRITERS=new Hashtable();

	/**
	 * Optional method to determine if message is a masked
	 * out throwable message type.
	 *
	 * <br><br><b>Usage:</b> if(isMaskedErrMsg(errException.getMessage()))
	 * @param str the message
	 * @return boolean TRUE if masked out.
	 */
	public static boolean isMaskedErrMsg(String str)
	{
		if(str==null) return false;
		str=str.toLowerCase();
		for(int i=0;i<maskErrMsgs.length;i++)
			if(str.indexOf(maskErrMsgs[i])>=0)
				return true;
		return false;
	}

	private boolean isWriterOn(String name)
	{
		String flag=prop(name);
		if(flag==null) return true;
		if(flag.length()==0) return false;
		if(flag.startsWith("OFF")) return false;
		return true;
	}
	
	public String getLogFilename(String name)
	{
		String flag=prop(name.toUpperCase().trim());
		if(flag.startsWith("OWNFILE"))
			return "mud_"+name.toLowerCase()+".log";
		if((flag.startsWith("FILE"))||(flag.startsWith("BOTH")))
			return "mud.log";
		return null;
	}
	
	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param name code string
	 * @return PrintWriter the writer
	 */
	private PrintWriter getWriter(String name)
	{
		PrintWriter writer=(PrintWriter)WRITERS.get(name);
		if(writer!=null) return writer;
		String flag=prop(name);
		if(flag==null)
		{
			writer=systemOutWriter;
			WRITERS.put(name, writer);
		}
		else if(flag.length()>0)
		{
			if(flag.startsWith("OFF")) return null;
			if(flag.startsWith("ON"))
			{
				writer=systemOutWriter;
			}
			else if((flag.startsWith("FILE"))||(flag.startsWith("BOTH")))
			{
				writer=fileOutWriter;
			}
			else if(flag.startsWith("OWNFILE"))
			{
				File fileOut=new File("mud_"+name.toLowerCase()+".log");
				try
				{
					filePath = fileOut.getAbsolutePath();
					FileOutputStream fileStream=new FileOutputStream(fileOut,true);
					writer=new PrintWriter(fileStream,true);
					WRITERS.put(name, writer);
				}
				catch(IOException e)
				{
					Log.errOut("Log",e);
				}
			}
		}
		return writer;
	}

	/**
	 * Returns an appropriate writer for the given ON, OFF, FILE, or OWNFILE
	 *
	 * <br><br><b>Usage:</b> PrintWriter W=getWriter("BOTH");
	 * @param name code string
	 * @return PrintWriter the writer
	 */
	private String prop(String type)
	{
		String s=System.getProperty("LOG.MUD_"+type.toUpperCase().trim());
		if(s==null) s="";
		return s;
	}

	/**
	* Reset all of the log files
	* ON, OFF, FILE, BOTH
	* <br><br><b>Usage:</b>  CMProps.Initialize("ON","OFF");
	* @param newSYSMSGS code string to describe info msgs
	* @param newERRMSGS code string to describe error msgs
	* @param newWARNMSGS code string to describe warning msgs
	* @param newDBGMSGS code string to describe debug msgs
	* @param newHLPMSGS code string to describe help msgs
	*/
	public void setLogOutput(String newSYSMSGS,
							 String newINFOMSGS,
							 String newERRMSGS,
							 String newWARNMSGS,
							 String newDBGMSGS,
							 String newHLPMSGS,
							 String newKILMSGS,
							 String newCBTMSGS)
	{
		System.setProperty("LOG.MUD_SYS",newSYSMSGS);
		System.setProperty("LOG.MUD_INFO",newINFOMSGS);
		System.setProperty("LOG.MUD_ERROR",newERRMSGS);
		System.setProperty("LOG.MUD_WARN",newWARNMSGS);
		System.setProperty("LOG.MUD_DEBUG",newDBGMSGS);
		System.setProperty("LOG.MUD_HELP",newHLPMSGS);
		System.setProperty("LOG.MUD_KILLS",newKILMSGS);
		System.setProperty("LOG.MUD_COMBAT",newCBTMSGS);
		WRITERS.clear();
	}

	public void startLogFiles(int numberOfLogs)
	{
		try
		{
			// initializes the logging objects
			if(numberOfLogs>1)
			{
				try{
					String name="mud"+(numberOfLogs-1)+".log";
					File f=new File(name);
					if(f.exists())
						f.delete();
				}catch(Exception e){}
				for(int i=numberOfLogs-1;i>0;i--)
				{
					String inum=(i>0)?(""+i):"";
					String inumm1=(i>1)?(""+(i-1)):"";
					try{
						File f=new File("mud"+inumm1+".log");
						if(f.exists())
							f.renameTo(new File("mud"+inum+".log"));
					}catch(Exception e){}
				}
			}
			String name="mud.log";
			File fileOut=new File(name);
			filePath = fileOut.getAbsolutePath();
			FileOutputStream fileStream=new FileOutputStream(fileOut);
			fileOutWriter=new PrintWriter(fileStream,true);
			System.setErr(new PrintStream(fileStream));
			WRITERS.clear();
		}
		catch(IOException e)
		{
			System.out.println("NO OPEN LOG: "+e.toString());
		}
	}

	public static class LogReader
	{
		protected BufferedReader reader = null;
		public String nextLine()
		{
			if(reader==null)
			{
				try
				{
					FileReader F=new FileReader("mud.log");
					reader = new BufferedReader(F);
				}
				catch(Exception e)
				{
					Log.errOut("Log",e.getMessage());
					return null;
				}
			}
			String line=null;
			try {
				if(reader.ready())
					line=reader.readLine();
			}
			catch ( final IOException ignore ){}
			if(line==null) close();
			return line;
		}
		public void close()
		{
			if ( reader != null )
			{
				try
				{
					reader.close();
					reader = null;
				}
				catch ( final IOException ignore ){}
			}
		}
	}
	
	public int numLines()
	{
		int num=0;
		try
		{
			FileReader F=new FileReader("mud.log");
			BufferedReader reader = new BufferedReader(F);
			String line="";
			while((line!=null)&&(reader.ready()))
			{ line=reader.readLine(); num++;}
		}
		catch(Exception e)
		{
			Log.errOut("Log",e.getMessage());
		}
		return num;
	}
	
	//no reason to call this really...
	public LogReader getLogReader()
	{
		return new LogReader();
	}
	public StringBuffer getLog()
	{
		StringBuffer buf=new StringBuffer("");
		BufferedReader reader = null;
		try
		{
			FileReader F=new FileReader("mud.log");
			reader = new BufferedReader(F);
			String line="";
			while((line!=null)&&(reader.ready()))
			{
				line=reader.readLine();
				if(line!=null)
					buf.append(line+"\r\n");
			}
		}
		catch(Exception e)
		{
			Log.errOut("Log",e.getMessage());
		}
		finally
		{
			if ( reader != null )
			{
				try
				{
					reader.close();
					reader = null;
				}
				catch ( final IOException ignore ) { }
			}
		}
		return buf;
	}
	/**
	* Start all of the log files
	*
	* <br><br><b>Usage:</b>  path = getLogLocation();
	* @return the string representation of the file path
	*/
	public String getLogLocation()
	{
		return filePath;
	}

	/**
	* Will be used to create a standardized log header for file logs
	*
	* <br><br><b>Usage:</b> SysOutWriter.println(getLogHeader(S,"Info",Module, Message));
	* @param Obj Session object
	* @param Type Type of information
	* @param Module The module name
	* @param Message The message to print
	* @return String The header and message, formatted
	*/
	private static String getLogHeader(String Type, String Module, String Message)
	{
		String date=dateFormat.format(Calendar.getInstance().getTime());
		StringBuffer Header=new StringBuffer((date+SPACES).substring(0,20));
		Header.append((Type+SPACES).substring(0,6));
		Header.append((Module+SPACES).substring(0,13));
		Header.append(Message);
		return Header.toString();
	}

	public static void infoOut(String Out) { infoOut("UNKN",Out); }
	public static void sysOut(String Out){ sysOut("UNKN",Out); }
	public static void debugOut(String Out){ debugOut("UNKN",Out); }
	public static void errOut(String Out){ errOut("UNKN",Out); }
	public static void warnOut(String Out){ warnOut("UNKN",Out); }
	public static void helpOut(String Out) { helpOut("UNKN",Out); }
	public static void killsOut(String Out) { killsOut("UNKN",Out); }
	public static void combatOut(String Out) { combatOut("UNKN",Out); }
	public static void sysOut(String Module, String Message){ standardOut("Sys",Module,Message);}
	public static void infoOut(String Module, String Message){ standardOut("Info",Module,Message);}
	public static void errOut(String Module, String Message){ standardOut("Error",Module,Message);}
	public static void warnOut(String Module, String Message){ standardOut("Warn",Module,Message);}
	public static void debugOut(String Module, String Message){ standardOut("Debug",Module,Message);}
	public static void helpOut(String Module, String Message){ standardOut("Help",Module,Message);}
	public static void killsOut(String Module, String Message){ standardOut("Kills",Module,Message);}
	public static void combatOut(String Module, String Message){ standardOut("Combat",Module,Message);}
	public static void debugOut(String Module, Exception e){ shortExOut("Debug",Module,e);}
	public static void errOut(String Module, Throwable e){ standardExOut("Error",Module,e);}
	public static void warnOut(String Module, Throwable e){ standardExOut("Warn",Module,e);}
	public static void rawSysOut(String Message){rawStandardOut("Sys",Message);}

	/**
	* Handles long exception logging entries.  Sends them to System.out,
	* the log file, or nowhere.
	*
	* <br><br><b>Usage:</b> standardExOut("UNKN",Out);
	* @param Type The channel to print to
	* @param Module The module to print
	* @param e	The exception whose string one wishes to print
	*/
	public static void standardExOut(String Type, String Module, Throwable e)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=logs.getWriter(Type);
			if(outWriter!=null)
			{
				if(e!=null)
				{
					outWriter.println(getLogHeader(Type,Module, e.getMessage()));
					e.printStackTrace(outWriter);
					outWriter.flush();
				}
				else
					outWriter.println(getLogHeader(Type,Module,"Null/Unknown error occurred."));
				if(logs.prop(Type).startsWith("BOTH"))
				{
					if(e!=null)
					{
						System.out.println(getLogHeader(Type,Module, e.getMessage()));
						e.printStackTrace(System.out);
						System.out.flush();
					}
					else
						System.out.println(getLogHeader(Type,Module,"Null/Unknown error occurred."));
				}
				logs.close(outWriter);
			}
		}
	}

	/**
	* Handles error logging entries.  Sends them to System.out,
	* the log file, or nowhere.
	*
	* <br><br><b>Usage:</b> shortExOut("Info","UNKN",Out);
	* @param Type The type of channel
	* @param Module The message to print
	* @param e	The exception whose string one wishes to print
	*/
	public static void shortExOut(String Type, String Module, Exception e)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=logs.getWriter(Type);
			if(outWriter!=null)
			{
				outWriter.println(getLogHeader(Type,Module, e.getMessage()));
				e.printStackTrace(outWriter);
				outWriter.flush();
				if(logs.prop(Type).startsWith("BOTH"))
				{
					System.out.println(getLogHeader(Type,Module, e.getMessage()));
					e.printStackTrace(System.out);
					System.out.flush();
				}
				logs.close(outWriter);
			}
		}
	}

	/**
	* Handles raw info logging entries.  Sends them to System.out,
	* the log file, or nowhere.
	*
	* <br><br><b>Usage:</b> rawStandardOut("Info","REQ-OUT:"+REQ);
	* @param Type The type of message
	* @param Message The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public static void rawStandardOut(String Type, String Message)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=logs.getWriter(Type);
			if(outWriter!=null)
			{
				outWriter.println(Message);
				outWriter.flush();
				if(logs.prop(Type).startsWith("BOTH"))
					System.out.println(Message);
				logs.close(outWriter);
			}
		}
	}

	/**
	* Handles debug logging entries.  Sends them to System.out,
	* the log file, or nowhere.
	*
	* <br><br><b>Usage:</b> standardOut("Info","UNKN",Out);
	* @param Type The type of writer
	* @param Module The file name
	* @param Message The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	private static void standardOut(String Type, String Module, String Message)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=logs.getWriter(Type);
			if(outWriter!=null)
			{
				outWriter.println(getLogHeader(Type,Module, Message));
				outWriter.flush();
				if(logs.prop(Type).startsWith("BOTH"))
					System.out.println(getLogHeader(Type,Module, Message));
				logs.close(outWriter);
			}
		}
	}

	/**
	* Handles debug timing entries.  Sends them to System.out,
	* the log file, or nowhere.
	*
	* <br><br><b>Usage:</b> timeOut("Info","UNKN",Out);
	* @param Type Channel name
	* @param Module The file name
	* @param Message The message to print
	* @param priority The priority of the message, high is less priority, 0=always
	*/
	public static void timeOut(String Type, String Module, String Message)
	{
		synchronized(Type.intern())
		{
			PrintWriter outWriter=logs.getWriter(Type);
			if(outWriter!=null)
			{
				Calendar C=Calendar.getInstance();
				Message=C.get(Calendar.MINUTE)+":"+C.get(Calendar.SECOND)+":"+C.get(Calendar.MILLISECOND)+": "+Message;
				outWriter.println(getLogHeader("-time-",Module, Message));
				outWriter.flush();
				if(logs.prop(Type).startsWith("BOTH"))
					System.out.println(getLogHeader("-time-",Module, Message));
				logs.close(outWriter);
			}
		}
	}

	/**
	 * Close the given printwriter, if its an "ownfile".
	 */
	private PrintWriter close(PrintWriter pr)
	{
		if(pr==null) return null;
		if((pr!=systemOutWriter)
		&&(pr!=fileOutWriter))
			pr.close();
		return null;
	}

	/**
	 * Shut down this class forever
	 */
	public void close()
	{
		fileOutWriter.close();
		fileOutWriter=null;
	}

	public static boolean errorChannelOn() { return logs.isWriterOn("error");}
	public static boolean helpChannelOn() { return logs.isWriterOn("help");}
	public static boolean debugChannelOn() { return logs.isWriterOn("debug");}
	public static boolean infoChannelOn() { return logs.isWriterOn("info");}
	public static boolean warnChannelOn() { return logs.isWriterOn("warning");}
	public static boolean killsChannelOn() { return logs.isWriterOn("kills");}
	public static boolean combatChannelOn() { return logs.isWriterOn("combat");}
	
	/** totally optional, this is the list of maskable error message types.  Useful for internet apps */
	private final static String[] maskErrMsgs={
		"broken pipe",
		"reset by peer",
		"socket closed",
		"connection abort",
		"connection reset",
		"network is unreachable",
		"jvm_recv",
		"timed out",
		"stream closed",
		"no route to host",
		"protocol not available"
	};
}