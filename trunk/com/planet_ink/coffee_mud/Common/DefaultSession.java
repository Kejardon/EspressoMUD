package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Libraries.*;
import com.planet_ink.coffee_mud.Libraries.CharCreation.LoginResult;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/*
CoffeeMUD 5.6.2 copyright 2000-2010 Bo Zimmerman
EspressoMUD copyright 2011 Kejardon

Licensed under the Apache License, Version 2.0. You may obtain a copy of the license at
	http://www.apache.org/licenses/LICENSE-2.0
*/

public class DefaultSession extends Thread implements Session
{
	protected static final long DefaultPromptSleep=Long.MAX_VALUE;

	protected int status=0;
	protected int tries=0; //Number of consecutive failed logins
	protected Socket sock;
	protected BufferedReader in;
	protected PrintWriter out;
	protected OutputStream rawout;
	protected MOB mob;
	protected PlayerAccount acct=null;
	protected boolean killFlag=false;
	protected boolean needPrompt=false;
	protected boolean afkFlag=false;
	protected String afkMessage=null;
	protected StringBuilder input=new StringBuilder(256);
	protected ConcurrentLinkedQueue<String> output=new ConcurrentLinkedQueue<>();
	//private StringBuilder preliminaryInput=new StringBuilder("");
	//private StringBuilder fakeInput=null;
	//protected boolean waiting=false;
	protected static final int SOTIMEOUT=300;
	protected String previousCmd="";
	//protected String[] clookup=null;
	protected String lastColorStr="";
	protected String lastStr=null;
	protected int spamStack=0;
	protected CopyOnWriteArrayList<Session> snoops=new CopyOnWriteArrayList();
	protected CopyOnWriteArrayList<Session> snoopTargets=new CopyOnWriteArrayList();
	protected final LinkedList<String> prevMsgs=new LinkedList();
	protected StringBuffer curPrevMsg=null;

	//protected boolean lastWasCR=false;
	//protected boolean lastWasLF=false;
	protected boolean suspendCommandLine=false;

	private long lastPrompt=0;
	private long lastStart=System.currentTimeMillis();
	private long lastStop=System.currentTimeMillis();
	private long lastLoopTop=System.currentTimeMillis();
	private long onlineTime=System.currentTimeMillis();
	//private long lastPKFight=0;
	//private long lastNPCFight=0;
	//private long lastBlahCheck=0;
	private long milliTotal=0;
	//private long tickTotal=0;
	private long lastKeystroke=0;
	//private long promptLastShown=0;

	//private boolean[] serverTelnetCodes=new boolean[256];
	private boolean[] clientTelnetCodes=new boolean[256];
	protected DVector expectedTelnetCommands=new DVector(2);	//0==(byte[2])expected command, 1==(Long)timeout time for command. This is a list of requests to not respond to.
	protected String terminalType="UNKNOWN";
	protected long writeStartTime=0;

	//private final HashSet telnetSupportSet=new HashSet();
//	private static final HashSet mxpSupportSet=new HashSet();
//	private static final Hashtable mxpVersionInfo=new Hashtable();
	//private boolean bNextByteIs255=false;
	//private boolean connectionComplete=false;

	//private int currentColor='N';
	//private int lastColor=-1;
	protected static AtomicInteger sessionCounter=new AtomicInteger(0);
	//protected static int sessionCounter=0;
	
	protected final DVector pendingPrompts=new DVector(5);	//Integer, Wrapper/Thread, Future (sign of use/done), Cached message, IsBusy (True=thread is busy and not expecting input, False=expecting user input)
	protected boolean[] promptNumbers=new boolean[MAX_PROMPTS];
	public static final int MAX_PROMPTS=10; //0 special prompt, and normal prompts 1-9.

	protected volatile boolean activePrompt=false;
	protected SessionInputReader currentInputReader=NormalSIR;
	protected int inputMark=0;
	protected volatile boolean doingInput=false;
	protected long lastMSDP=0;
	protected boolean busyMSDP=false;
	protected boolean newMSDP=false;
	protected boolean[] MSDPIsNew=new boolean[MSDPOptions.size];
	protected boolean[] MSDPReporting=new boolean[MSDPOptions.size];

	protected boolean negotiated=false;
	protected byte support256=0;	//-1 means does not, 0 means unknown, 1 means does

	protected String lastTType;
	protected String ClientID="";
	protected String ClientVersion="";
	protected String PluginID="";
	protected String MXPVersion="";
	protected boolean enANSI=false;
	protected boolean enXTERM=false;
	//protected boolean enUTF8=false;
	protected boolean enSound=false;
	protected boolean enMXP=false;
	protected boolean enMSP=false;
	protected boolean enMSDP=false;
	protected boolean enATCP=false;
	protected boolean defaultDark=false;	//TODO: Make this get written to and be reported and stuff!
	protected boolean promptGA=false;

	protected final static byte[][] IACRequests={
		{(byte)TELNET_IAC, (byte)TELNET_SB, (byte)TELNET_TERMTYPE, (byte)1, (byte)TELNET_IAC, (byte)TELNET_SE},
		//{TELNET_IAC, TELNET_DO, TELNET_NAWS},	//NAWS. We don't really care about NAWS at the moment
		//{TELNET_IAC, TELNET_DO, TELNET_CHARSET},	//Only supporting one charset for the time being. also TELNET_CHARSET is not defined yet
		{(byte)TELNET_IAC, (byte)TELNET_WILL, (byte)TELNET_MSDP},
		{(byte)TELNET_IAC, (byte)TELNET_WILL, (byte)TELNET_MSSP},
		{(byte)TELNET_IAC, (byte)TELNET_DO, (byte)TELNET_ATCP},
		{(byte)TELNET_IAC, (byte)TELNET_WILL, (byte)TELNET_MSP},
		{(byte)TELNET_IAC, (byte)TELNET_DO, (byte)TELNET_MXP},
		//{TELNET_IAC, TELNET_WILL, TELNET_MCCP},	//TODO eventually: Reimplement compression
	};

	public static final String MSDP_START = String.valueOf(new char[]{TELNET_IAC, TELNET_SB, TELNET_MSDP, 1});
	
	public void sendMSDPDirect(String name, String value)
	{
		output.add(MSDP_START+name+((char)2)+value+IAC_END);
		/*
		int tempSize=name.length();
		char[] output=new char[tempSize+value.length()+7];
		output[0]=(char)TELNET_IAC;
		output[1]=(char)TELNET_SB;
		output[2]=(char)TELNET_MSDP;
		output[3]=(char)1;
		name.getChars(0, tempSize, output, 4);
		output[tempSize+4]=(char)2;
		value.getChars(0, value.length(), output, tempSize+5);
		output[output.length-2]=(char)TELNET_IAC;
		output[output.length-1]=(char)TELNET_SE;
		out.write(output);
		*/
	}
	private static final String ATCP_START = String.valueOf(new char[]{TELNET_IAC, TELNET_SB, TELNET_ATCP,'M','S','D','P','.'});
	public void sendATCPDirect(String name, String value)
	{
		output.add(ATCP_START+name+" "+value+IAC_END);
		/*
		int tempSize=name.length();
		char[] output=new char[tempSize+value.length()+11];
		output[0]=(char)TELNET_IAC;
		output[1]=(char)TELNET_SB;
		output[2]=(char)TELNET_ATCP;
		output[3]='M';
		output[4]='S';
		output[5]='D';
		output[6]='P';
		output[7]='.';
		name.getChars(0, tempSize, output, 8);
		output[tempSize+8]=' ';
		value.getChars(0, value.length(), output, tempSize+9);
		output[output.length-2]=(char)TELNET_IAC;
		output[output.length-1]=(char)TELNET_SE;
		out.write(output);
		*/
	}

	public static final String MSDPHeader=String.valueOf(new char[]{TELNET_IAC,TELNET_SB,TELNET_MSDP,1});
	public static final String ATCPHeader=String.valueOf(new char[]{TELNET_IAC,TELNET_SB,TELNET_ATCP,'M','S','D','P','.'});
	public static final String IAC_END=String.valueOf(new char[]{TELNET_IAC,TELNET_SE});
	public void sendMSDP(String name, String value)
	{
		if(enMSDP)
		{
			output.add(MSDPHeader+name+((char)2)+value+IAC_END);
			/*
			int tempSize=name.length();
			char[] output=new char[tempSize+value.length()+7];
			output[0]=(char)TELNET_IAC;
			output[1]=(char)TELNET_SB;
			output[2]=(char)TELNET_MSDP;
			output[3]=(char)1;
			name.getChars(0, tempSize, output, 4);
			output[tempSize+4]=(char)2;
			value.getChars(0, value.length(), output, tempSize+5);
			output[output.length-2]=(char)TELNET_IAC;
			output[output.length-1]=(char)TELNET_SE;
			out.write(output);
			*/
		}
		else if(enATCP)
		{
			output.add(ATCPHeader+name+' '+value+IAC_END);
			/*
			int tempSize=name.length();
			char[] output=new char[tempSize+value.length()+11];
			output[0]=(char)TELNET_IAC;
			output[1]=(char)TELNET_SB;
			output[2]=(char)TELNET_ATCP;
			output[3]='M';
			output[4]='S';
			output[5]='D';
			output[6]='P';
			output[7]='.';
			name.getChars(0, tempSize, output, 8);
			output[tempSize+8]=' ';
			value.getChars(0, value.length(), output, tempSize+9);
			output[output.length-2]=(char)TELNET_IAC;
			output[output.length-1]=(char)TELNET_SE;
			out.write(output);
			*/
		}
	}
	@Override public void setMSDPNew(String S)
	{
		MSDPOptions O;
		try{O=MSDPOptions.valueOf(S);}
		catch(IllegalArgumentException e){
			e.printStackTrace();
			Log.errOut("DefaultSession","Invalid MSDP tag: "+S);
			Log.errOut("DefaultSession",e);
			return;
		}
		MSDPIsNew[O.ordinal()]=true;
		if(MSDPReporting[O.ordinal()])
			newMSDP=true;
	}
	@Override public void setMSDPNew(int i)
	{
		MSDPIsNew[i]=true;
		if(MSDPReporting[i])
			newMSDP=true;
	}
	public enum MSDPOptions	//TODO/NOTE: Check and make sure everything that should set MSDPIsNew for these does so
	{
		SERVER_ID(false, false, false)
		{public String value(DefaultSession S){return CMProps.Strings.MUDNAME.property();}},
		SERVER_TIME(false, false, false)
		{public String value(DefaultSession S){return ""+System.currentTimeMillis();}},
		CHARACTER_NAME(false, false, true)
		{public String value(DefaultSession S){return (S.mob==null)?"":S.mob.name();}},
		//HEALTH(false, false, true)
		//HEALTH_MAX(false, false, true)
		//MANA(false, false, true)
		//MANA_MAX(false, false, true)
		//RACE(false, false, true)
		//LEVEL(false, false, true)
		//STR(false, false, true)
		//INT(false, false, true)
		//CON(false, false, true)
		//WIL(false, false, true)
		//PRE(false, false, true)
		//REA(false, false, true)
		//OBS(false, false, true)
		//OPPONENT_NAME(false, false, true)
		//AREA_NAME(false, false, true)
		//ROOM_NAME(false, false, true)
		//ROOM_EXITS(false, false, true)
		//WORLD_TIME(false, false, true)
		CLIENT_ID(true, false, true)
		{public String value(DefaultSession S){return S.ClientID;}
		 public void writeValue(DefaultSession S, String str){if(S.ClientID==""){S.ClientID=str; S.setMSDPNew(ordinal());}}},
		CLIENT_VERSION(true, false, true)
		{public String value(DefaultSession S){return S.ClientVersion;}
		 public void writeValue(DefaultSession S, String str){if(S.ClientVersion==""){S.ClientID=str; S.setMSDPNew(ordinal());}}},
		PLUGIN_ID(true, false, true)
		{public String value(DefaultSession S){return S.PluginID;}
		 public void writeValue(DefaultSession S, String str){S.PluginID=str; S.setMSDPNew(ordinal());}},
		DARKER_DISPLAY(true, false, true)
		{public String value(DefaultSession S){return ""+(S.defaultDark?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.defaultDark=false;
			 else if(str.equals("1")) S.defaultDark=true;
			 S.setMSDPNew(ordinal());}},
		ANSI_COLORS(true, false, true)
		{public String value(DefaultSession S){return ""+(S.enANSI?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.enANSI=false;
			 else if(str.equals("1")) S.enANSI=true;
			 S.setMSDPNew(ordinal());}},
		PROMPT_GA(true, false, true)
		{public String value(DefaultSession S){return ""+(S.promptGA?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.promptGA=false;
			 else if(str.equals("1")) S.promptGA=true;
			 S.setMSDPNew(ordinal());}},
		ATCP(true, false, true)
		{public String value(DefaultSession S){return ""+(S.enATCP?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.enATCP=false;
			 else if(str.equals("1")) S.enATCP=true;
			 S.setMSDPNew(ordinal());}},
		XTERM_256_COLORS(true, false, true)
		{public String value(DefaultSession S){return ""+(S.enXTERM?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.enXTERM=false;
			 else if(str.equals("1")) S.enXTERM=true;
			 S.setMSDPNew(ordinal());}},
		/*UTF_8(true, false, true)
		{public String value(DefaultSession S){return ""+(S.enUTF8?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.enUTF8=false;
			 else if(str.equals("1")) s.enUTF8=true;
			 S.setMSDPNew(ordinal());}},
		*/
		SOUND(true, false, true)
		{public String value(DefaultSession S){return ""+(S.enSound?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.enSound=false;
			 else if(str.equals("1")) S.enSound=true;
			 S.setMSDPNew(ordinal());}},
		MXP(true, false, true)
		{public String value(DefaultSession S){return ""+(S.enMXP?1:0);}
		 public void writeValue(DefaultSession S, String str){
			 if(str.equals("0")) S.enMXP=false;
			 else if(str.equals("1")) S.enMXP=true;
			 S.setMSDPNew(ordinal());}},
		//BUTTON_1(?, true, ?);
		//GAUGE_1(?, true, ?);
		;
		public final boolean userCanWrite;
		public final boolean GUI;
		public final boolean local;
		//public int localIndex;
		public static final int size=values().length;
		public abstract String value(DefaultSession S);
		private MSDPOptions(boolean write, boolean gui, boolean loc)
		{
			userCanWrite=write;
			GUI=gui;
			local=loc;
		}
		public void writeValue(DefaultSession S, String str){};
		
		protected static MSDPOptions[] savedArray=values();
		public static MSDPOptions fromOrdinal(int i){return savedArray[i];} 
	}

	@Override public String ID(){return "DefaultSession";}
	@Override public DefaultSession newInstance(){return new DefaultSession();}
	@Override public void initializeClass(){}
	//public boolean isFake() { return false;}
	@Override public DefaultSession copyOf(){ try{ Object O=this.clone(); return (DefaultSession)O;}catch(CloneNotSupportedException e){return newInstance();} }
	@Override public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}

	public DefaultSession()
	{
		super("DefaultSession."+sessionCounter.getAndIncrement());
	}

	@Override public void handlePromptFor(CommandCallWrap command, int type)
	{
		synchronized(pendingPrompts)
		{
			checkPendingPrompts();
			if(type==2)
			{
				if(!promptNumbers[0])
				{
					pendingPrompts.addRow(0, command, CMClass.threadPool.submit(command), null, Boolean.TRUE);
					promptNumbers[0]=true;
				}
				return;
			}
			//else { }
			Integer I=null;
			for(int i=1;i<MAX_PROMPTS;i++)
			{
				if(promptNumbers[i]) continue;
				I=i;
				break;
			}
			if(I==null) {rawPrint("You have too many active prompts, finish some first.\r\n"); return;}
			Future<Void> doneCheck=CMClass.threadPool.submit(command);
			pendingPrompts.addRow(I, command, doneCheck, null, Boolean.TRUE);
			promptNumbers[I]=true;
		}
	}
	@Override
	public void catchPromptFor(CommandCallWrap command)
	{
		synchronized(pendingPrompts)
		{
			for(int i=pendingPrompts.size()-1;i>=0;i--)
			{
				if(checkPendingPrompt(i) && (pendingPrompts.elementAt(i, 1)==command))
				{
					pendingPrompts.setElementAt(i, 1, Thread.currentThread());
					return;
				}
			}
		}
	}
	@Override
	public boolean handlePromptFor(Thread thread, int type, Future response)
	{
		synchronized(pendingPrompts)
		{
			checkPendingPrompts();
			if(type==2)
			{
				if(!promptNumbers[0])
				{
					pendingPrompts.addRow(0, thread, response, null, Boolean.TRUE);
					promptNumbers[0]=true;
					return true;
				}
				return false;
			}
			//else { }
			Integer I=null;
			for(int i=1;i<MAX_PROMPTS;i++)
			{
				if(promptNumbers[i]) continue;
				I=i;
				break;
			}
			if(I==null) {rawPrint("You have too many active prompts, finish some first.\r\n"); return false;}
			pendingPrompts.addRow(I, thread, response, null, Boolean.TRUE);
			promptNumbers[I]=true;
			return true;
		}
	}
	@Override public String prompt(String Message, String Default, long maxTime)
	{
		String Msg=prompt(Message,maxTime).trim();
		if(Msg.length()==0)
			return Default;
		return Msg;
	}
	@Override public String prompt(String Message, String Default)
	{
		String Msg=prompt(Message,-1).trim();
		if(Msg.length()==0)
			return Default;
		return Msg;
	}
	@Override public String prompt(String Message, long maxTime)
	{
		Thread caller=Thread.currentThread();
		Object[] promptData=null;
		synchronized(pendingPrompts)
		{
			for(int i=pendingPrompts.size()-1;i>=0;i--)
			{
				if(checkPendingPrompt(i) && (((Thread)pendingPrompts.elementAt(i, 1))==caller))
				{
					promptData=pendingPrompts.elementsAt(i);
					break;
				}
			}
		}
		try{
			int index=(Integer)promptData[0];
			//if(promptData==null)	//This is an active prompt. Hijack flushInput.
			if(index==0)
			{
				//print(Message);
				activePrompt=true;
				//long stopTime=System.currentTimeMillis()+(maxTime>0?maxTime:DefaultPromptSleep);
				//while((activePrompt)&&(stopTime<System.currentTimeMillis()))
				//{
				//	if(in.ready())
				//		while(currentInputReader.checkInput(this));	//Wait until a flushInput from reading input clears activePrompt
				//	else
				//		try{Thread.sleep(50);}catch(Exception e){}
				//}
				//if(index==0&&activePrompt)
				//{
				//	activePrompt=false;
				//	return "";
				//}
				//input now has our data.
				//Message=input.toString();
				//input.setLength(0);
				//return Message;
			}
			else
			{
				Message="-- Prompt "+index+" --\r\n"+Message+"\r\n-- End Prompt "+index+" --";
			}
			promptData[3]=Message;
			promptData[4]=Boolean.FALSE;
			print(Message);
			try{Thread.sleep(maxTime>0?maxTime:DefaultPromptSleep);}catch(InterruptedException e){}
			if(promptData[4]==Boolean.FALSE)
			{
				promptData[4]=Boolean.TRUE;
				if(index==0)
				{
					activePrompt=false;
				}
				else
				{
					print("\r\n-- Prompt "+index+" has expired --\r\n");
				}
				return "";
			}
			return (String)promptData[3];
		}
		catch(Exception e)
		{
			Log.errOut("DefaultSession",e);
			rawPrint("Oops!\r\n");
		}
		return "";
	}
	public static class ManualFuture<E> implements Future<E>
	{
		protected boolean done=false;
		@Override public boolean cancel(boolean evenIfRunning) {done=true; return true;}
		@Override public E get(){return null;}
		@Override public E get(long time, TimeUnit unit){return null;}
		@Override public boolean isCancelled(){return false;}
		@Override public boolean isDone(){return done;}
	}
	@Override public String newPrompt(String Message, long maxTime)
	{
		Thread caller=Thread.currentThread();
		Object[] promptData=null;
		Future<Void> doneCheck=null;
		synchronized(pendingPrompts)
		{
			checkPendingPrompts();
			Integer I=null;
			for(int i=1;i<MAX_PROMPTS;i++)
			{
				if(promptNumbers[i]) continue;
				I=i;
				break;
			}
			if(I==null) {rawPrint("You have too many active prompts, finish some first.\r\n"); return "";}
			doneCheck=new ManualFuture<Void>();
			//pendingPrompts.addRow(I, caller, doneCheck, null, Boolean.TRUE);
			promptData=new Object[]{I, caller, doneCheck, null, Boolean.TRUE};
			pendingPrompts.addRow(promptData);
			promptNumbers[I]=true;
		}
		try{
			Message="-- Prompt "+promptData[0]+" --\r\n"+Message+"\r\n-- End Prompt "+promptData[0]+" --";
			promptData[3]=Message;
			promptData[4]=Boolean.FALSE;
			print(Message);
			try{Thread.sleep(maxTime>0?maxTime:DefaultPromptSleep);}catch(InterruptedException e){}
			if(promptData[4]==Boolean.FALSE)
			{
				promptData[4]=Boolean.TRUE;
				print("\r\n-- Prompt "+promptData[0]+" has expired --\r\n");
				return "";
			}
			return (String)promptData[3];
		}
		catch(Exception e){}
		finally{doneCheck.cancel(false);}
		return "";
	}
	@Override public String prompt(String Message)
	{ return prompt(Message, -1); }

	@Override public void initializeSession(Socket s, String introTextStr)
	{
		sock=s;
		try
		{
			sock.setSoTimeout(SOTIMEOUT);
			rawout=sock.getOutputStream();
			InputStream rawin=sock.getInputStream();
			//setClientTelnetMode(TELNET_TERMTYPE,true);
			negotiateTelnetMode(rawout,TELNET_TERMTYPE);
			out = new PrintWriter(new OutputStreamWriter(rawout,CMProps.Strings.CHARSETOUTPUT.property()));
			in = new BufferedReader(new InputStreamReader(rawin,CMProps.Strings.CHARSETINPUT.property()));

			Thread.sleep(500);
			if(introTextStr!=null)
				print(introTextStr);
		//	connectionComplete=true;
		}
		catch(Exception e) {Log.errOut("DefaultSession",e);}
	}

	private void negotiateTelnetMode(OutputStream out, int optionCode)
	throws IOException
	{
		if(CMSecurity.isDebugging("TELNET"))
			Log.debugOut("Session","Sent sub-option: "+Session.TELNET_DESCS[optionCode]);
		byte[] stream=(optionCode==TELNET_TERMTYPE?
		  (new byte[]{(byte)TELNET_IAC,(byte)TELNET_SB,(byte)optionCode,(byte)1,(byte)TELNET_IAC,(byte)TELNET_SE}):
		  (new byte[]{(byte)TELNET_IAC,(byte)TELNET_SB,(byte)optionCode,(byte)TELNET_IAC,(byte)TELNET_SE}));
		out.write(stream);
		out.flush();
	}

	//Gotta be careful about this, make sure cases don't overlap.
	@Override public void setOther(int otherCode, boolean onOff)
	{
		switch(otherCode)
		{
			case PlayerStats.ATT_DARKENDISPLAY: defaultDark=onOff; setMSDPNew(MSDPOptions.DARKER_DISPLAY.ordinal()); break;
		}
	}
	@Override public void setClientTelnetMode(int telnetCode, boolean onOff)
	{
		clientTelnetCodes[telnetCode]=onOff;
		switch(telnetCode)
		{
			//TODO
			case TELNET_ANSI: enANSI=onOff; setMSDPNew(MSDPOptions.ANSI_COLORS.ordinal()); break;
			case TELNET_GA: promptGA=onOff; setMSDPNew(MSDPOptions.PROMPT_GA.ordinal()); break;
			case TELNET_MXP: enMXP=onOff; setMSDPNew(MSDPOptions.MXP.ordinal()); break;
			case TELNET_MSP: enMSP=onOff; break;
			case TELNET_ATCP: enATCP=onOff; setMSDPNew(MSDPOptions.ATCP.ordinal()); break;
		}
	}
	@Override public boolean clientTelnetMode(int telnetCode)
	{ return clientTelnetCodes[telnetCode]; }
	public static final String IACWill=String.valueOf(new char[]{TELNET_IAC,TELNET_WILL});
	public static final String IACWont=String.valueOf(new char[]{TELNET_IAC,TELNET_WONT});
	@Override public void changeTelnetMode(int telnetCode, boolean onOff) 
	{
		/*
		char[] command={(char)TELNET_IAC,onOff?(char)TELNET_WILL:(char)TELNET_WONT,(char)telnetCode};
		out.write(command);
		out.flush();
		*/
		String command=(onOff?(IACWill):(IACWont))+(char)telnetCode;
		output.add(command);
		if(CMSecurity.isDebugging("TELNET")) Log.debugOut("Session","Sent: "+(onOff?"Will":"Won't")+" "+Session.TELNET_DESCS[telnetCode]);
		//setServerTelnetMode(telnetCode,onOff);
	}
	private static final String IAC_START=String.valueOf(new char[]{TELNET_IAC,TELNET_SB});
	@Override public void negotiateTelnetMode(int telnetCode)
	{
		/*
		char[] command=(telnetCode==TELNET_TERMTYPE?
		  (new char[]{(char)TELNET_IAC,(char)TELNET_SB,(char)telnetCode,(char)1,(char)TELNET_IAC,(char)TELNET_SE}):
		  (new char[]{(char)TELNET_IAC,(char)TELNET_SB,(char)telnetCode,(char)TELNET_IAC,(char)TELNET_SE}));
		out.write(command);
		out.flush();
		*/
		String command=(telnetCode==TELNET_TERMTYPE?(IAC_START+((char)telnetCode)+((char)1)+IAC_END):(IAC_START+((char)telnetCode)+IAC_END));
		output.add(command);
		if(CMSecurity.isDebugging("TELNET")) Log.debugOut("Session","Negotiate-Sent: "+Session.TELNET_DESCS[telnetCode]);
	}

	@Override public void initTelnetMode(int mobbitmap)
	{
		setClientTelnetMode(TELNET_ANSI,(mobbitmap&PlayerStats.ATT_ANSI)>0);
/*		boolean changedSomething=false;
		boolean mxpSet=(!CMSecurity.isDisabled("MXP"))&&((mobbitmap&PlayerStats.ATT_MXP)>0);
		if(mxpSet!=clientTelnetMode(TELNET_MXP))
		{ changeTelnetMode(TELNET_MXP,!clientTelnetMode(TELNET_MXP)); changedSomething=true;}
		boolean mspSet=(!CMSecurity.isDisabled("MSP"))&&((mobbitmap&PlayerStats.ATT_SOUND)>0);
		if(mspSet!=clientTelnetMode(TELNET_MSP))
		{ changeTelnetMode(TELNET_MSP,!clientTelnetMode(TELNET_MSP)); changedSomething=true;}
		try{if(changedSomething) blockingIn(500);}catch(Exception e){}
*/
	}


	@Override public long getTotalMillis(){ return milliTotal;}
	@Override public long getIdleMillis(){ return System.currentTimeMillis()-lastKeystroke;}
	@Override public long getMillisOnline(){ return System.currentTimeMillis()-onlineTime;}

	@Override public long lastLoopTime(){ return lastLoopTop;}
	@Override public void updateLoopTime(){ lastLoopTop=System.currentTimeMillis();}
	//public long getLastPKFight(){return lastPKFight;}
	//public void setLastPKFight(){lastPKFight=System.currentTimeMillis();}
	//public long getLastNPCFight(){return lastNPCFight;}
	//public void setLastNPCFight(){lastNPCFight=System.currentTimeMillis();}
	@Override public LinkedList<String> getLastMsgs(){synchronized(prevMsgs){return (LinkedList)prevMsgs.clone();}}

	@Override public String getTerminalType(){ return terminalType;}
	@Override public MOB mob(){return mob;}
	@Override public void setMob(MOB newmob){ mob=newmob; setMSDPNew(MSDPOptions.CHARACTER_NAME.ordinal());}
	@Override public void setAccount(PlayerAccount account){acct=account;}
	@Override public int getWrap(){return ((mob!=null)&&(mob.playerStats()!=null))?mob.playerStats().getWrap():78;}
	public int getPageBreak(){return ((mob!=null)&&(mob.playerStats()!=null))?mob.playerStats().getPageBreak():-1;}
	@Override public boolean killFlag(){return killFlag;}
	public void setKillFlag(boolean truefalse){killFlag=truefalse;}
	@Override public String previousCMD(){return previousCmd;}
	@Override public void startBeingSnoopedBy(Session S)
	{
		snoops.addIfAbsent(S);
	}
	@Override public void stopBeingSnoopedBy(Session S)
	{
		snoops.remove(S);
	}
	@Override public boolean amBeingSnoopedBy(Session S)
	{
		if(S==null) return snoops.isEmpty();
		return(snoops.contains(S));
	}
	@Override public void copySnoops(Session ses)
	{
		if(ses instanceof DefaultSession)
		{
			DefaultSession S=(DefaultSession)ses;
			S.snoops=(CopyOnWriteArrayList)snoops.clone();
			S.snoopTargets=(CopyOnWriteArrayList)snoopTargets.clone();
			for(Session source : snoops)
			{
				source.stopSnoopingOn(this);
				source.startSnoopingOn(S);
			}
			for(Session source : snoopTargets)
			{
				source.stopBeingSnoopedBy(this);
				source.startBeingSnoopedBy(S);
			}
		}
	}
	@Override public void startSnoopingOn(Session S)
	{
		if(snoopTargets.addIfAbsent(S))
			S.startBeingSnoopedBy(this);
	}
	@Override public boolean stopSnoopingOn(Session S)
	{
		S.stopBeingSnoopedBy(this);
		return snoopTargets.remove(S);
	}
	@Override public Iterator<Session> snoopTargets()
	{
		return snoopTargets.iterator();
	}

	private int metaFlags() {
		return (snoops.size()>0)?Command.METAFLAG_SNOOPED:0;
	}
	
	@Override public void setPreviousCmd(String cmds)
	{
		if((cmds!=null)&&((cmds.length()==0)||(cmds.charAt(0)=='!')))
			return;
		previousCmd=cmds;
	}

	@Override public boolean afkFlag(){return afkFlag;}
	@Override public void setAfkFlag(boolean truefalse)
	{
		if(afkFlag==truefalse) return;
		afkFlag=truefalse;
		if(afkFlag)
			println("\r\nYou are now listed as AFK.");
		else
		{
			afkMessage=null;
			println("\r\nYou are no longer AFK.");
		}
	}
	@Override public String afkMessage()
	{
		if(mob==null) return "";
		if((afkMessage==null)||(CMStrings.removeColors(afkMessage).trim().length()==0))
			return mob.name()+" is AFK at the moment.";
		return afkMessage;
	}
	@Override public void setAFKMessage(String str){afkMessage=str;}

	protected void errorOut(Exception t)
	{
		Log.errOut("Session",t);
		CMLib.sessions().removeElement(this);
		killFlag=true;
	}

	public long getWriteStartTime(){return writeStartTime;}
	@Override public boolean isLockedUpWriting(){
		long time=writeStartTime;
		if(time==0) return false;
		return ((System.currentTimeMillis()-time)>10000);
	}

	@Override public void addOut(String msg)
	{
		output.add(msg);
	}
	private void out(String c){
		try{
			if((out!=null)&&(c!=null)&&(!c.isEmpty()))
			{
				{
					writeStartTime=System.currentTimeMillis()+c.length();
					out.write(c);
					if(out.checkError())
						kill(true);
				}
			}
		}
		catch(Exception ioe){ killFlag=true;}
		finally{writeStartTime=0;}
	}
	@Override public void onlyPrint(String msg){onlyPrint(msg,false);}
	@Override public void onlyPrint(String msg, boolean noCache)
	{
		if((out==null)||(msg==null)) return;
		try
		{
			if(snoops.size()>0)
			{
				String msgColored=msg.replace("\r\n",CMLib.coffeeFilter().colorOnlyFilter("\r\n^Z"+((mob==null)?"?":mob.name())+":^N ",this));
				for(Session snooper : snoops)
					snooper.onlyPrint(msgColored,noCache);
			}

			if(msg.endsWith("\r\n")
			&&(msg.equals(lastStr))
			&&(msg.length()>2)
			&&(msg.indexOf("\n")==(msg.length()-2)))
			{ spamStack++; return; }
			else if(spamStack>0)
			{
				if(spamStack>1)
					lastStr=lastStr.substring(0,lastStr.length()-2)+"("+spamStack+")"+lastStr.substring(lastStr.length()-2);
				output.add(lastStr);
			}

			spamStack=0;
			if(msg.startsWith("\r\n")&&(msg.length()>2))
				lastStr=msg.substring(2);
			else
				lastStr=msg;

			// handle line cache --
			if(!noCache)
			for(int i=0;i<msg.length();i++)
			{
				if(curPrevMsg==null) curPrevMsg=new StringBuffer("");
				if(msg.charAt(i)=='\r') continue;
				if(msg.charAt(i)=='\n')
				{
					if(curPrevMsg.toString().trim().length()>0)
					{
						synchronized(prevMsgs)
						{
							while(prevMsgs.size()>=MAX_PREVMSGS)
								prevMsgs.removeFirst();
							prevMsgs.add(curPrevMsg.toString());
							curPrevMsg.setLength(0);
						}
					}
					continue;
				}
				curPrevMsg.append(msg.charAt(i));
			}
			output.add(msg);
		}
		catch(java.lang.NullPointerException e){}
	}
	@Override public void rawPrint(String msg)
	{ if(msg==null)return;
	  onlyPrint((needPrompt?msg:("\r\n"+msg)),false);
	  needPrompt=true;
	}

	@Override public void print(String msg)
	{
		onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false),false);
	}

	@Override public void rawPrintln(String msg)
	{ if(msg==null)return; rawPrint(msg+"\r\n");}

	@Override public void stdPrint(String msg)
	{ rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false)); }

	@Override public void print(Interactable src, Interactable trg, CMObject tol, String msg)
	{ onlyPrint((CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)),false);}

	@Override public void stdPrint(Interactable src, Interactable trg, CMObject tol, String msg)
	{ rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,trg,msg,false)); }

	@Override public void println(String msg)
	{ if(msg==null)return; print(msg+"\r\n");}

	@Override public void wraplessPrintln(String msg)
	{ if(msg==null)return;
	  onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,true)+"\r\n",false);
	  needPrompt=true;
	}

	@Override public void wraplessPrint(String msg)
	{ onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,true),false);
	  needPrompt=true;
	}

	@Override public void colorOnlyPrintln(String msg)
	{ colorOnlyPrint(msg,false);}
	@Override public void colorOnlyPrintln(String msg, boolean noCache)
	{ if(msg==null)return;
	  onlyPrint(CMLib.coffeeFilter().colorOnlyFilter(msg,this)+"\r\n",noCache);
	  needPrompt=true;
	}

	@Override public void colorOnlyPrint(String msg)
	{ colorOnlyPrint(msg,false);}
	@Override public void colorOnlyPrint(String msg, boolean noCache)
	{ onlyPrint(CMLib.coffeeFilter().colorOnlyFilter(msg,this),noCache);
	  needPrompt=true;
	}

	@Override public void stdPrintln(String msg)
	{ if(msg==null)return;
	  rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,mob,mob,null,msg,false)+"\r\n");
	}

	@Override public void println(Interactable src, Interactable trg, CMObject tol, String msg)
	{ if(msg==null)return;
	  onlyPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)+"\r\n",false);
	}

	@Override public void stdPrintln(Interactable src,Interactable trg, CMObject tol, String msg)
	{ if(msg==null)return;
	  rawPrint(CMLib.coffeeFilter().fullOutFilter(this,mob,src,trg,tol,msg,false)+"\r\n");
	}

	@Override public void setPromptFlag(boolean truefalse)
	{
		needPrompt=truefalse;
	}

	//Last color should be left to messages, not this.
	//If no ANSI, simply return "";
	//check for 0-7, I/i, B/b, U/u, get proper escape code. Set last color.
	@Override public String getColor(char c)
	{
		if(!enANSI) return "";
		switch(c)
		{
			case '0': case '1': case '2': case '3':
			case '4': case '5': case '6': case '7':
				return CMColor.ASCI_COLORS[(c-'0')+(defaultDark?0:8)];
			case '.': return CMColor.COLOR_NONE;
			case 'I': return CMColor.COLOR_ITALIC;
			case 'i': return CMColor.COLOR_ENDITALIC;
			case 'U': return CMColor.COLOR_UNDERLINE;
			case 'u': return CMColor.COLOR_ENDUNDERLINE;
			//default: fall through
		}
			
		//TODO: Check for override for this session
		CMColor.ColorCode color=CMColor.ColorCode.get(c);
		return color==null?"":color.DefaultWholeString;
	}
	/*
	If no ANSI, return "";
	Check format (f/F b/B, ### or 0-255), return "" if invalid. Calculate color(s) otherwise.
		Format: L###, L==F or B, #==0-5. (must have 3 digits)
				l#, L==f or b, #==0-255. (must have 1 to 3 digits)
				May have both an f and b (any combination of cases), not two f's (any case) or b's (any case).
		Result: "\033[(38;5;x);(48;5;y)m" (parts in () are optional - first is foreground, last is background, either may be replaced by 'default' (x9))
				x/y may be raw value from lowercase, or calculated value 16+(36*digit0)+(6*digit1)+(digit2) from uppercase
	Check if xterm, simplify color if not, else use full color.
	*/
	@Override public String getColor(String S, String oldColor)
	{
		if(!enANSI) return "";
		int foreColor=-1;
		int backColor=-1;
		char first=S.charAt(0);
		if((first=='f')||(first=='F'))
		{
			foreColor=getColorFromTag(S, Character.isUpperCase(first));
			if(foreColor<0) return "";
			if(foreColor>=65536)
			{
				S=S.substring(foreColor/65536);
				foreColor=(foreColor&255);
				first=S.charAt(0);
				if((first!='b')&&(first!='B')) return "";
				backColor=getColorFromTag(S, Character.isUpperCase(first));
				if((backColor<0)||(backColor>=65536)) return "";
			}
		}
		else
		{
			backColor=getColorFromTag(S, Character.isUpperCase(first));
			if(backColor<0) return "";
			if(backColor>=65536)
			{
				S=S.substring(backColor/65536);
				backColor=(backColor&255);
				first=S.charAt(0);
				if((first!='f')&&(first!='F')) return "";
				foreColor=getColorFromTag(S, Character.isUpperCase(first));
				if((foreColor<0)||(foreColor>=65536)) return "";
			}
		}
		if(foreColor<0) foreColor=getColorFromEscape(oldColor, false);
		if(backColor<0) backColor=getColorFromEscape(oldColor, true);
		//boolean doXTerm=enXTERM;	//Make sure no thread confusion happens
		StringBuilder colorString;//=new StringBuffer(doXTerm?24:12);
		if(enXTERM)
		{
			colorString=new StringBuilder(22);
			colorString.append("\033[");
			if(foreColor==-1)
				colorString.append("39");
			else
				colorString.append("38;5;").append(foreColor);
			if(backColor==-1)
				colorString.append(";49");
			else
				colorString.append(";48;5;").append(backColor);
			colorString.append('m');
			return colorString.toString();
		}
		//else { }
		colorString=new StringBuilder(12);
		colorString.append("\033[3");
		if(foreColor==-1)
			colorString.append('9');
		else
			colorString.append(CMColor.xTermToANSI[foreColor]);
		colorString.append(";4");
		if(backColor==-1)
			colorString.append('9');
		else
			colorString.append(CMColor.xTermToANSINoBold[backColor]);
		colorString.append('m');
		return colorString.toString();
	}
	//S will always be a valid ANSI escape - most likely something like \033[38;5;10;48;5;212m or \033[1;32;47m
	//assume xterm, usually return 0-255
	//if last color was 'default', return -1
	protected int getColorFromEscape(String S, boolean background)
	{
		Vector<String> values=CMParms.parseSemicolons(S.substring(2, S.length()-1), false);
		int tag;
		boolean bold=false;
		if(background) for(int i=0;i<values.size();i++) try
		{
			tag=Integer.parseInt(values.get(i));
			if(tag==1) bold=true;
			if((tag>=40)&&(tag<=49))
			{
				if(tag==49) return -1;
				if((tag==48)&&(Integer.parseInt(values.get(++i))==5))
				{
					tag=Integer.parseInt(values.get(++i));
					if((tag>=0)&&(tag<=255)) return tag;
				}
				return tag-40+(bold?8:0);
			}
		} catch(Exception e){}
		else for(int i=0;i<values.size();i++) try
		{
			tag=Integer.parseInt(values.get(i));
			if(tag==1) bold=true;
			if((tag>=30)&&(tag<=39))
			{
				if(tag==39) return -1;
				if((tag==38)&&(Integer.parseInt(values.get(++i))==5))
				{
					tag=Integer.parseInt(values.get(++i));
					if((tag>=0)&&(tag<=255)) return tag;
				}
				return tag-30+(bold?8:0);
			}
		} catch(Exception e){}
		return -1;
	}
	//-1 : Invalid S
	//0-255 : Fully parsed value
	//(0-255) + (65536*n) : Partially parsed, skip n letters in S to get the next part.
	protected int getColorFromTag(String S, boolean RGBFormat)
	{
		if(RGBFormat)
		{
			if(S.length()<4) return -1;
			int R=S.charAt(1)-'0';
			int G=S.charAt(2)-'0';
			int B=S.charAt(3)-'0';
			boolean longer=S.length()>4;
			if((R<0)||(R>5)||(G<0)||(G>5)||(B<0)||(B>5)) return -1;
			return 16+36*R+6*G+B+(longer?65536*4:0);
		}
		int i=1;
		int color=0;
		for(;(i<5)&&(i<S.length());i++)
		{
			char nextChar=S.charAt(i);
			if(Character.isDigit(nextChar))
			{
				if(i==5) return -1;
				color=color*10+nextChar-'0';
			}
			else
			{
				if((color>255)||(i==1)) return -1;
				color+=65536*i;
				break;
			}
		}
		if((color>255)&&(color<65536)) return -1;
		return color;
	}

	//TODO


	/*
	have protected variable input, a StringBuilder.
	have protected static interface of SessionInputReader
	have protected static variables that implement SessionInputReader with a new anonymous class
	NormalSIR - If IAC, goto IAC1. If \r goto Return, if \n flush. If escape, goto escape check and mark spot in input.
	IACValue1SIR - Got an IAC. May be 255 (give input a 255), else flush input, 251-254 (write to input, goto IAC2), 250 (write to input, goto IACFull), 246(respond " \b"?, goto normal), anything else ignore goto normal.
	IACValue2SIR - Got a request/notification. Get last byte and set changes accordingly.
	IACFullSIR - Only really care about termtype? TODOish
	//ReturnSIR - special: If not ready, flush input and goto normal. Else get next, if \n eat it, flush input, if not \n add to input.
	Not doing ReturnSIR; NormalSIR can handle it without needing to switch around objects
	EscapeSIR - If format wrong... abandon immediately, toss all so-far-collected data? First should be [, then either digits, ;, or m. If anything else, delete everything up to and including the first anything else. If m, leave alone. Goto normal when done.
	*/
	protected static interface SessionInputReader{public boolean checkInput(DefaultSession S) throws IOException;}
	protected static SessionInputReader MXPSIR=new SessionInputReader() {
	private String getSub(String tag, StringBuilder source, int fromHere) {
		int i=source.indexOf(tag, fromHere);
		if(i<0) return null;
		fromHere=i+tag.length();
		int size=source.length();
		if(fromHere>=size) return null;
		if(source.charAt(i)=='\"') fromHere++;
		for(i=fromHere;i<size;i++)
		{
			char ch=source.charAt(i);
			if((!Character.isLetterOrDigit(ch))&&(ch!='.'))
				break;
		}
		return source.substring(fromHere, i);
	}
	public boolean checkInput(DefaultSession S) throws IOException {
		//ESC[#z was just eaten, S.inputMark should point to start of MXP data. Stop when mark is 1000 bytes ago or a >, whichever comes first
		stopLoop:
		while(S.in.ready())
		{
			int next=S.in.read();
			switch(next)
			{
				case -1: //TODO: This should stop the session because something is wrong. Not sure the best way to stop the session, probably need to check if it is already stopped.
					return false;
				case '>':
					String str;
					if( (str=getSub("CLIENT=", S.input, S.inputMark))!=null)
					{
						S.ClientID=str;
						S.setMSDPNew(MSDPOptions.CLIENT_ID.ordinal());
					}
					subIf:
					if( (str=getSub("VERSION=", S.input, S.inputMark))!=null)
					{
						S.ClientVersion=str;
						S.setMSDPNew(MSDPOptions.CLIENT_VERSION.ordinal());
						String ClientID=S.ClientID;
						if(ClientID.equalsIgnoreCase("MUSHCLIENT"))	//4.02 and later is good
						{
							if(CMStrings.versionCheck(str, new int[]{4, 2})>=0)
							{
								S.support256=1;
								S.enXTERM=true;
								S.setMSDPNew(MSDPOptions.XTERM_256_COLORS.ordinal());
							}
							else
								S.support256=-1;
						}
						else if(ClientID.equalsIgnoreCase("CMUD"))	//3.04 and later is good
						{
							if(CMStrings.versionCheck(str, new int[]{3, 4})>=0)
							{
								S.support256=1;
								S.enXTERM=true;
								S.setMSDPNew(MSDPOptions.XTERM_256_COLORS.ordinal());
							}
							else
								S.support256=-1;
						}
						else if(ClientID.equalsIgnoreCase("ATLANTIS"))	//Atlantis supports 256 colors before it support MXP
						{
							S.support256=1;
							S.enXTERM=true;
							S.setMSDPNew(MSDPOptions.XTERM_256_COLORS.ordinal());
						}
					}
					if( (str=getSub("MXP=", S.input, S.inputMark))!=null)
					{
						S.MXPVersion=str;
					}
					if(S.MXPVersion.length()>0)
					{
						S.output.add("\t[F210][\toINFO\t[F210]]\tn MXP version "+S.MXPVersion+" detected and enabled.\r\n");
					}
					S.input.setLength(S.inputMark);
					S.currentInputReader=DefaultSession.NormalSIR;
					break stopLoop;
				default:
					if(S.inputMark+1000<S.input.length())
					{
						S.input.setLength(S.inputMark);
						S.currentInputReader=DefaultSession.NormalSIR;
						break stopLoop;
					}
					S.input.append((char)next);
			}
		}
		return S.in.ready();
	} };
	protected static SessionInputReader IACValue1SIR=new SessionInputReader() {
	public boolean checkInput(DefaultSession S) throws IOException {
		int next=S.in.read();
		switch(next)
		{
			case -1: //TODO: This should stop the session because something is wrong. Not sure the best way to stop the session, probably need to check if it is already stopped.
				return false;
			case 255:
				S.input.append((char)next);
//				S.input.append((char)next);	//IMPORTANT NOTE: Output filter MUST check for FF being sent out and double it when it's not a command!
				S.currentInputReader=DefaultSession.NormalSIR;
				break;
			case TELNET_AYT:
//				if(S.input.length()>0)
//					S.flushInput();
//				else
					S.output.add(" \b");	//I don't really know why " \b" exactly. This is what CoffeeMUD does though.
				S.currentInputReader=DefaultSession.NormalSIR;
				break;
			case TELNET_DO:
			case TELNET_DONT:
			case TELNET_WILL:
			case TELNET_WONT:
				S.inputMark=S.input.length();
				S.input.append((char)next);
				S.currentInputReader=DefaultSession.IACValue2SIR;
				break;
			case TELNET_SB:
				S.inputMark=S.input.length();
				S.currentInputReader=DefaultSession.IACFullSIR;
				break;
			default:
				S.currentInputReader=DefaultSession.NormalSIR;
		}
		return S.in.ready();
	} };
	protected static SessionInputReader IACFullSIR=new SessionInputReader() {
	public void doMSDP(DefaultSession S, String MSDPVar, String MSDPVal) {
		if(MSDPVar.equals("SEND"))
		{
			MSDPOptions O=null;
			try{O=MSDPOptions.valueOf(MSDPVal);}
			catch(IllegalArgumentException e){return;}
			S.sendMSDP(MSDPVar, O.value(S));
		}
		else if(MSDPVar.equals("REPORT"))
		{
			MSDPOptions O=null;
			try{O=MSDPOptions.valueOf(MSDPVal);}
			catch(IllegalArgumentException e){return;}
			S.MSDPIsNew[O.ordinal()]=true;
			S.MSDPReporting[O.ordinal()]=true;
		}
		else if(MSDPVar.equals("RESET"))
		{
			if((MSDPVal.equals("REPORTABLE_VARIABLES"))||
			  (MSDPVal.equals("REPORTED_VARIABLES")))
			for(int i=0; i<S.MSDPIsNew.length; i++)
			{
				S.MSDPIsNew[i]=false;
				S.MSDPReporting[i]=false;
			}
		}
		else if(MSDPVar.equals("UNREPORT"))
		{
			MSDPOptions O=null;
			try{O=MSDPOptions.valueOf(MSDPVal);}
			catch(IllegalArgumentException e){return;}
			S.MSDPIsNew[O.ordinal()]=false;
			S.MSDPReporting[O.ordinal()]=false;
		}
		else if(MSDPVar.equals("LIST"))
		{
			if(MSDPVal.equals("COMMANDS"))
				S.sendMSDP("COMMANDS", "LIST REPORT RESET SEND UNREPORT");
			else if(MSDPVal.equals("LISTS"))
				S.sendMSDP("LISTS", "COMMANDS LISTS CONFIGURABLE_VARIABLES REPORTABLE_VARIABLES REPORTED_VARIABLES SENDABLE_VARIABLES");	//GUI_VARIABLES
			else if((MSDPVal.equals("SENDABLE_VARIABLES"))||(MSDPVal.equals("REPORTABLE_VARIABLES")))
			{
				if(S.enMSDP)
				{
					StringBuilder listedVariable=new StringBuilder((char)5);
					for(MSDPOptions O : MSDPOptions.values())
					{
						if(!O.GUI)
							listedVariable.append((char)2+O.name());
					}
					listedVariable.append((char)6);
					S.sendMSDPDirect(MSDPVal, listedVariable.toString());
				}
				else if(S.enATCP)
				{
					StringBuilder listedVariable=new StringBuilder("");
					for(MSDPOptions O : MSDPOptions.values())
					{
						if(!O.GUI)
						{
							if(listedVariable.length()>0)
								listedVariable.append(' ');
							listedVariable.append(O.name());
						}
					}
					S.sendATCPDirect(MSDPVal, listedVariable.toString());
				}
			}
			else if(MSDPVal.equals("REPORTED_VARIABLES"))
			{
				if(S.enMSDP)
				{
					StringBuilder listedVariable=new StringBuilder((char)5);
					for(MSDPOptions O : MSDPOptions.values())
					{
						if(S.MSDPReporting[O.ordinal()])
							listedVariable.append((char)2+O.name());
					}
					listedVariable.append((char)6);
					S.sendMSDPDirect(MSDPVal, listedVariable.toString());
				}
				else if(S.enATCP)
				{
					StringBuilder listedVariable=new StringBuilder("");
					for(MSDPOptions O : MSDPOptions.values())
					{
						if(S.MSDPReporting[O.ordinal()])
						{
							if(listedVariable.length()>0)
								listedVariable.append(' ');
							listedVariable.append(O.name());
						}
					}
					S.sendATCPDirect(MSDPVal, listedVariable.toString());
				}
			}
			else if(MSDPVal.equals("CONFIGURABLE_VARIABLES"))
			{
				if(S.enMSDP)
				{
					StringBuilder listedVariable=new StringBuilder((char)5);
					for(MSDPOptions O : MSDPOptions.values())
					{
						if(O.userCanWrite)
							listedVariable.append((char)2+O.name());
					}
					listedVariable.append((char)6);
					S.sendMSDPDirect(MSDPVal, listedVariable.toString());
				}
				else if(S.enATCP)
				{
					StringBuilder listedVariable=new StringBuilder("");
					for(MSDPOptions O : MSDPOptions.values())
					{
						if(O.userCanWrite)
						{
							if(listedVariable.length()>0)
								listedVariable.append(' ');
							listedVariable.append(O.name());
						}
					}
					S.sendATCPDirect(MSDPVal, listedVariable.toString());
				}
			}
			//else if(MSDPVal.equals("GUI_VARIABLES"))
		}
		else
		{
			MSDPOptions O=null;
			try{O=MSDPOptions.valueOf(MSDPVar);}
			catch(IllegalArgumentException e){return;}
			O.writeValue(S, MSDPVal);
		}
	}
	public boolean checkInput(DefaultSession S) throws IOException {
		stopLoop:
		while(S.in.ready())
		{
			int next=S.in.read();
			switch(next)
			{
				case -1:
					return false;	//TODO: This should stop the session because something is wrong. Not sure the best way to stop the session, probably need to check if it is already stopped.
				case 255:
					if(!S.in.ready())	//fancy handling in case of problems popping up here
					{
						while((!S.in.ready())&&(!S.killFlag))
							try{Thread.sleep(50);}catch(Exception e){}
						if(!S.in.ready()) return false;
					}
					int argument=S.in.read();
					if(argument==TELNET_SE)
					{
						char command=S.input.charAt(S.inputMark);
						switch(command)
						{
							case TELNET_TERMTYPE:
							{
								String newID=S.input.substring(S.inputMark+1);
								if(S.ClientID=="")	//yaaaay intentional use of improper comparison
								{
									S.ClientID=newID;
									S.setMSDPNew(MSDPOptions.CLIENT_ID.ordinal());
									if(newID.equals("ANSI"))
									{
										S.lastTType=newID;
										break;
									}
								}
								if(S.lastTType==null || ((!S.lastTType.equals(newID))&&(!S.ClientID.equals(newID))))
								{
									S.lastTType=newID;
									if(newID.indexOf("-256color")>=0)
									{
										S.enXTERM=true;
										S.setMSDPNew(MSDPOptions.XTERM_256_COLORS.ordinal());
										S.support256=1;
									}
									S.output.add(IAC_START+((char)TELNET_TERMTYPE)+((char)1)+IAC_END); //(char)TELNET_IAC+(char)TELNET_SB+(char)TELNET_TERMTYPE+(char)1+(char)TELNET_IAC+(char)TELNET_SE);
								}
								if(newID.startsWith("Mudlet"))
								{
									S.ClientID="Mudlet";
									S.setMSDPNew(MSDPOptions.CLIENT_ID.ordinal());
									if(newID.length()>7)
									{
										S.ClientVersion=new String(newID.substring(7));
										S.setMSDPNew(MSDPOptions.CLIENT_VERSION.ordinal());
										if(CMStrings.versionCheck(S.ClientVersion, new int[]{1, 1})>=0)
										{
											S.support256=1;
											S.enXTERM=true;
											S.setMSDPNew(MSDPOptions.XTERM_256_COLORS.ordinal());
										}
										else
											S.support256=-1;
									}
								}
								else if(newID.equals("EMACS-RINZAI"))
								{
									S.support256=1;
									S.enXTERM=true;
									S.setMSDPNew(MSDPOptions.XTERM_256_COLORS.ordinal());
								}
								else if(newID.startsWith("DecafMUD"))
								{
									S.support256=1;
									S.enXTERM=true;
									S.setMSDPNew(MSDPOptions.XTERM_256_COLORS.ordinal());
									S.ClientID="DecafMUD";
									S.setMSDPNew(MSDPOptions.CLIENT_ID.ordinal());
									if(newID.length()>9)
									{
										S.ClientVersion=new String(newID.substring(9));
										S.setMSDPNew(MSDPOptions.CLIENT_VERSION.ordinal());
									}
								}
								else if(newID.equals("ZMUD"))
									S.support256=-1;
								break;
							}
							//case TELNET_NAWS:	//Not used atm
							//case TELNET_CHARSET:	//Only default atm
							case TELNET_MSDP:
							{
								String MSDPVar=null;
								String MSDPVal;
								int stop=S.input.length();
								int type=0;
								int previous=0;
								for(int i=S.inputMark;i<stop;i++)
								{
									char nextChar=S.input.charAt(i);
									if((nextChar==1)||(nextChar==2))
									{
										if(type==1)
										{
											MSDPVar=S.input.substring(previous, i);
										}
										else if(MSDPVar!=null)
										{
											MSDPVal=S.input.substring(previous, i);
											doMSDP(S, MSDPVar, MSDPVal);
										}
										previous=i+1;
										type=nextChar;
									}
								}
								if(type==2&&previous<stop&&MSDPVar!=null)
								{
									MSDPVal=S.input.substring(previous);
									doMSDP(S, MSDPVar, MSDPVal);
								}
								break;
							}
							case TELNET_ATCP:
							{
								String MSDPVar=null;
								String MSDPVal;
								int stop=S.input.length();
								int type=0;
								int previous=0;
								for(int i=S.inputMark;i<stop;i++)
								{
									char nextChar=S.input.charAt(i);
									if((nextChar=='@')||(nextChar==' '))
									{
										if(type=='@')
										{
											MSDPVar=S.input.substring(previous, i);
										}
										else if(MSDPVar!=null)
										{
											MSDPVal=S.input.substring(previous, i);
											doMSDP(S, MSDPVar, MSDPVal);
										}
										previous=i+1;
										type=nextChar;
									}
								}
								if(type==' '&&previous<stop&&MSDPVar!=null)
								{
									MSDPVal=S.input.substring(previous);
									doMSDP(S, MSDPVar, MSDPVal);
								}
								break;
							}
						}
						S.input.setLength(S.inputMark);
						S.currentInputReader=DefaultSession.NormalSIR;
						break stopLoop;
					}
					if(argument==255)
					{
						S.input.append((char)argument);
						continue;
					}
					if(argument==-1)
						return false;	//TODO: This should stop the session because something is wrong. Not sure the best way to stop the session, probably need to check if it is already stopped.
					//If we get here, the client is doing something completely terribly wrong. Falling through for now
					
				default:
					S.input.append(next);
			}
		}
		return S.in.ready();
	} };
	public static final String MXP_ON_RESPONSE=IAC_START+(char)TELNET_MXP+IAC_END+"\033[7z";
	public static final String MXP_WILL=""+(char)TELNET_IAC+(char)TELNET_WILL+(char)TELNET_MXP;
	protected static SessionInputReader IACValue2SIR=new SessionInputReader() {
	public boolean checkInput(DefaultSession S) throws IOException {
		int type=S.in.read();
		switch(type)
		{
			case -1:
				return false;	//TODO: This should stop the session because something is wrong. Not sure the best way to stop the session, probably need to check if it is already stopped.
			case TELNET_TERMTYPE:
				if(!S.negotiated)
				{
					if(S.input.charAt(S.inputMark)==TELNET_WILL)
					{
						S.negotiated=true;
						for(int i=0;i<DefaultSession.IACRequests.length;i++)
							S.rawout.write(DefaultSession.IACRequests[i]);
					}
					else if(S.input.charAt(S.inputMark)==TELNET_WONT)
					{
						S.negotiated=true;
						for(int i=1;i<DefaultSession.IACRequests.length;i++)
							S.rawout.write(DefaultSession.IACRequests[i]);
					}
				}
				S.input.setLength(S.inputMark);
				S.currentInputReader=DefaultSession.NormalSIR;
				break;
			case TELNET_MSDP:
				if(S.input.charAt(S.inputMark)==TELNET_DO)
				{
					S.enMSDP=true;
					S.sendMSDP("SERVER_ID", MSDPOptions.SERVER_ID.value(S));
				}
				else if(S.input.charAt(S.inputMark)==TELNET_DONT)
					S.enMSDP=false;
				break;
			case TELNET_MSSP:
				if(S.input.charAt(S.inputMark)==TELNET_DO)
					S.output.add(CMProps.getMSSPIAC());
				break;
/*			case TELNET_MCCP:
				//Not supported yet. TODO eventually
*/
			case TELNET_MSP:
				if(S.input.charAt(S.inputMark)==TELNET_DO)
					S.enMSP=true;
				else if(S.input.charAt(S.inputMark)==TELNET_DONT)
					S.enMSP=false;
				break;
			case TELNET_MXP:
				char command=S.input.charAt(S.inputMark);
				if((command==TELNET_DO)||(command==TELNET_WILL))
				{
					S.output.add(MXP_ON_RESPONSE);	//Effectively disables the need for <, >, and & handling.
					S.enMXP=true;
					S.setMSDPNew(MSDPOptions.MXP.ordinal());
				}
				else if(command==TELNET_WONT)
					S.output.add(MXP_WILL);
				else
				{
					S.enMXP=false;
					S.setMSDPNew(MSDPOptions.MXP.ordinal());
				}
				break;
			case TELNET_ATCP:
				if(S.input.charAt(S.inputMark)==TELNET_WILL)
				{
					if(!S.enMSDP)
					{
						S.enATCP=true;
						S.sendMSDP("SERVER_ID", MSDPOptions.SERVER_ID.value(S));
					}
				}
				else if(S.input.charAt(S.inputMark)==TELNET_WONT)
					S.enATCP=false;
				break;
		}
		S.input.setLength(S.inputMark);
		S.currentInputReader=DefaultSession.NormalSIR;
		return S.in.ready();
	} };
	protected static SessionInputReader NormalSIR=new SessionInputReader() {
	public boolean checkInput(DefaultSession S) throws IOException {
		stopLoop:
		while(S.in.ready())
		{
			int next=S.in.read();
			switch(next)
			{
				case -1: //TODO: This should stop the session because something is wrong. Not sure the best way to stop the session, probably need to check if it is already stopped.
					return false;
				case 27:	//Escape. Mark, make sure it is not an MXP escape. If it is, eat it, flush all previous input. Else keep going.
					S.inputMark=S.input.length();
					S.input.append((char)next);
					S.currentInputReader=DefaultSession.EscapeSIR;
					break stopLoop;
				case 255:	//IAC. If next is not IAC, flush all previous input.
					S.currentInputReader=DefaultSession.IACValue1SIR;
					break stopLoop;
				case 13:	//\r
					if(S.in.ready())
					{
						S.in.mark(2);
						next=S.in.read();
						if(next!='\n')
							S.in.reset();
					}
					//Fall through to \n regardless what happens
				case 10:	//\n
					if((next!=13)&&(S.in.ready()))
					{
						S.in.mark(2);
						next=S.in.read();
						if(next!='\r')
							S.in.reset();
					}
					S.flushInput();
					break;
				default:
					S.input.append((char)next);
			}
		}
		return S.in.ready();
	} };
	protected static SessionInputReader EscapeSIR=new SessionInputReader() {
	private boolean validEscape(String S) {
			if(S.length()==0) return true;
			//First two (ESC, [) and last (m) have been checked and are not here. Need to check to see: Does not end with ;, no ints too large
			boolean wasN8=false;	//was38 or was48
			boolean wasN8s5=false;	//was38;5 or was48;5
			while(true)
			{
//				if((S.charAt(0)=='0')&&(Character.isDigit(S.charAt(1)))) return false;
				int sub=0;
				if(wasN8s5)
				{
					while(Character.isDigit(S.charAt(sub))&&S.length()>sub)
						if(++sub>3) return false;
					if(sub==0) return false;
					if((sub==3)&&(CMath.s_int(S.substring(0,3))>255)) return false;
					wasN8s5=false;
				}
				else
				{
					while(Character.isDigit(S.charAt(sub))&&S.length()>sub)
						if(++sub>2) return false;
					if(sub==0) return false;
					int value=CMath.s_int(S.substring(0,sub));
					if(wasN8)
					{
						//NOTE: This should change if there are other valid values for 38/48
						if(value!=5) return false;
						wasN8s5=true;
						wasN8=false;
					}
					else
					{
						if(value>65) return false;
						if(value==38||value==48) wasN8=true;
					}
				}
				if(S.length()==sub) return true;
				S=S.substring(sub+1);
				if(S.length()==0) return false;
			}
		}
	public boolean checkInput(DefaultSession S) throws IOException{
		if(S.inputMark+1==S.input.length())
		{
			int next=S.in.read();
			if(next!='[')
			{
				S.currentInputReader=DefaultSession.NormalSIR;
				S.input.setLength(S.inputMark);
				return S.in.ready();
			}
			S.input.append((char)next);
		}
		stopLoop:
		while(S.in.ready())
		{
			int next=S.in.read();
			switch(next)
			{
				case -1: //TODO: This should stop the session because something is wrong. Not sure the best way to stop the session, probably need to check if it is already stopped.
					return false;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case ';':
					S.input.append((char)next);
					break;
				case 'z':
					if((S.inputMark==S.input.length()-3)	//ignore ESC and [, possible digit, z was not written
					  &&(Character.isDigit(S.input.charAt(S.inputMark+2))))
						S.currentInputReader=DefaultSession.MXPSIR;
					else
						S.currentInputReader=DefaultSession.NormalSIR;
					S.input.setLength(S.inputMark);
					break stopLoop;
				case 'm':
					if(validEscape(S.input.substring(S.inputMark+2, S.input.length())))
					{
						S.input.append((char)next);
						S.currentInputReader=DefaultSession.NormalSIR;
						break stopLoop;
					}
					//if invalid, fall through to below which eats the whole escape sequence
				default:
					S.currentInputReader=DefaultSession.NormalSIR;
					S.input.setLength(S.inputMark);
					break stopLoop;
			}
		}
		return S.in.ready();
	} };

	//Do it even if size is 0
	public void flushInput()
	{
		String thisInput=input.toString().trim();
		//if(!activePrompt)
			input.setLength(0);
		if(clientTelnetCodes[TELNET_ECHO])
			output.add(thisInput);
		if(snoops.size()>0)
		{
			//get chararray, write it
			String mobName=(mob==null?"?":mob.name());
			//"\033[1;33;41m", ":\033[0;37m " : 10+9=19 chars
			//char[] msgColored=new char[thisInput.length()+mobName.length()+19];
			//"\033[1;33;41m".getChars(0,10, msgColored, 0);
			//mobName.getChars(0, mobName.length(), msgColored, 10);
			//":\033[0;37m ".getChars(0, 9, msgColored, 10+mobName.length());
			String msgColored="\033[1;33;41m"+mobName+":\033[0;37m "+thisInput;
			//thisInput.getChars(0, thisInput.length(), msgColored, 19+mobName.length());
			for(Session snooper : snoops)
			{
				if(snooper instanceof DefaultSession)
					((DefaultSession)snooper).output.add(msgColored);
			}
		}
		lastKeystroke=System.currentTimeMillis();
		if(thisInput.length()>0)
			synchronized(prevMsgs){prevMsgs.add(thisInput);}
		setAfkFlag(false);
		int answersPrompt=activePrompt?0:-1;
		if((thisInput.length()>0)&&(Character.isDigit(thisInput.charAt(0))))
		{
			int index=1;
			while(thisInput.length()>index && Character.isDigit(thisInput.charAt(index))) index++;
			answersPrompt=(index==thisInput.length()?CMath.s_int(thisInput):CMath.s_int(thisInput.substring(0,index)));
			thisInput=thisInput.substring(index).trim();
		}
		doneInput:
		if(answersPrompt>=0)
		{
			//int answersPrompt;
			//int index=thisInput.indexOf(' ');
			//answersPrompt=(index==-1?CMath.s_int(thisInput):CMath.s_int(thisInput.substring(0,index)));
			//if(answersPrompt<=0) break doneInput;
			Object[] promptData;
			synchronized(pendingPrompts)
			{
				checkPendingPrompts();
				promptData=pendingPrompts.elementsAt(answersPrompt);
			}
			if(promptData==null)
			{
				rawPrint("There is no prompt associated with that number.");
			}
			else if((Boolean)promptData[4])
			{
				rawPrint("That prompt is not currently waiting for input.");
			}
			else if(thisInput.isEmpty() && answersPrompt!=0)
			{
				if(promptData[3]!=null)
					print((String)promptData[3]);
				else //Probably a bug.
					rawPrint("There is no previous message from this prompt!");
				//if(index!=-1)
				//	rawPrint("That prompt is not currently waiting for input.");
				//if(promptData[3]!=null)
				//	print((String)promptData[3]);	//Should it be the below? Or should the below be saved? Gotta format it anyways the first time so...
				//	//print("-- Prompt "+answersPrompt+" --\r\n"+((String)promptData[3])+"\r\n-- End Prompt "+answersPrompt+" --");
				//else if(index==-1)	//Kinda worth noting: This kinda signifies the user predicted a prompt before it happened..
				//	rawPrint("There is no previous method from this prompt!");
			}
			else
			{
				promptData[3]=thisInput;
				promptData[4]=Boolean.TRUE;
				((Thread)promptData[1]).interrupt();
			}
		}
		else
		{
			int spaceIndex=thisInput.indexOf(' ');
			String firstWord=(spaceIndex<0?thisInput:thisInput.substring(0,spaceIndex));
			if(firstWord.length()==0) break doneInput;
			//waiting=false;	//?
			PlayerStats pstats=mob.playerStats();
			String alias=(pstats!=null)?pstats.getAlias(firstWord):"";
			if(alias.length()>0)
			{
				//ArrayList<String> ALL_CMDS=new ArrayList();
				thisInput=thisInput.substring(spaceIndex+1);
				Vector<String> all_stuff=CMParms.parseSquiggleDelimited(alias,true);
				for(int a=0;a<all_stuff.size();a++)
				{
					String CMDS=all_stuff.get(a)+thisInput;
					setPreviousCmd(CMDS);
					milliTotal+=(lastStop-lastStart);
					lastStart=System.currentTimeMillis();
					rawPrintln(CMDS);
					if(mob!=null)
						mob.enqueCommand(CMDS,metaFlags());
					lastStop=System.currentTimeMillis();
				}
			}
			else
			{
				setPreviousCmd(thisInput);
				milliTotal+=(lastStop-lastStart);
				lastStart=System.currentTimeMillis();
				if(mob!=null)
					mob.enqueCommand(thisInput,metaFlags());
				lastStop=System.currentTimeMillis();
			}
		}
		needPrompt=true;
	}

	@Override public boolean confirm(String Message, String Default, long maxTime)
	{
		if(Default.toUpperCase().startsWith("T")) Default="Y";
		String YN=Default;
		try{YN=choose(Message,"YN",Default,maxTime);}
		catch(IOException e){}
		if(YN.equals("Y"))
			return true;
		return false;
	}
	@Override public boolean confirm(String Message, String Default)
	{
		if(Default.toUpperCase().startsWith("T")) Default="Y";
		String YN=Default;
		try{YN=choose(Message,"YN",Default,-1);}
		catch(IOException e){}
		if(YN.equals("Y"))
			return true;
		return false;
	}

	@Override public String choose(String Message, String Choices, String Default)
	throws IOException
	{ return choose(Message,Choices,Default,-1);}

	@Override public String choose(String Message, String Choices, String Default, long maxTime)
	throws IOException
	{
		String YN="";
		while((YN.equals(""))||(!Choices.contains(YN))&&(!killFlag))
		{
			YN=prompt(Message, maxTime);
			if(YN==null){ return Default.toUpperCase(); }
			YN=YN.trim().toUpperCase();
			if(YN.equals("")){ return Default.toUpperCase(); }
			if(YN.length()>1) YN=YN.substring(0,1);
		}
		return YN;
	}

	@Override public void kill(boolean killThread)
	{
		killFlag=true;
		saveMOBTime();
		if(killThread)
		{
			CMLib.sessions().removeElement(this);
			closeSocks();
			status=Session.STATUS_LOGOUT11;
			CMLib.killThread(this,1000,1);
		}
		else
			status=Session.STATUS_LOGOUT4;
	}

	public void showPrompt()
	{
	//	promptLastShown=System.currentTimeMillis();
		MOB Mob=mob;
		if(Mob==null) return;
		if(Mob.playerStats()==null) return;
/*		StringBuffer buf=new StringBuffer("");
		if(clientTelnetMode(Session.TELNET_MXP))
			buf.append("^<!EN Hp '"+mob().charStats().getPoints(CharStats.Points.HIT)
					+"'^>^<!EN MaxHp '"+mob().charStats().getMaxPoints(CharStats.Points.HIT)
					+"'^>^<!EN Mana '"+mob().charStats().getPoints(CharStats.Points.MANA)
					+"'^>^<!EN MaxMana '"+mob().charStats().getMaxPoints(CharStats.Points.MANA)
					+"'^>^<!EN Focus '"+mob().charStats().getPoints(CharStats.Points.FOCUS)
					+"'^>^<!EN TargetFocus '"+mob().charStats().getMaxPoints(CharStats.Points.FOCUS)
					+"'^>^\r\n\r\n");
		buf.append(CMLib.utensils().builtPrompt(mob));
		print(CMLib.utensils().builtPrompt(mob)+".^.");
*/
		print(CMLib.utensils().builtPrompt(Mob)+(promptGA?("^.^N"+(char)TELNET_IAC+(char)TELNET_GA):("^.^N")));
	}

	protected void closeSocks()
	{
		try
		{
			if(sock!=null)
			{
				status=Session.STATUS_LOGOUT5;
				if(out!=null) out.flush();
				status=Session.STATUS_LOGOUT6;
				if(sock!=null) sock.shutdownInput();
				status=Session.STATUS_LOGOUT7;
				if(sock!=null) sock.shutdownOutput();
				status=Session.STATUS_LOGOUT8;
				if(out!=null) out.close();
				status=Session.STATUS_LOGOUT9;
				if(sock!=null) sock.close();
				status=Session.STATUS_LOGOUT10;
			}
			in=null;
			out=null;
			sock=null;
		}
		catch(IOException e){}
	}

	@Override
	public byte[] getByteAddress()
	{
		try { return sock.getInetAddress().getAddress(); }
		catch (Exception e) { return null; }
	}

	@Override
	public String getAddress()
	{
		try { return sock.getInetAddress().getHostAddress(); }
		catch (Exception e) { return "Unknown (Excpt "+e.getMessage() + ")"; }
	}

	private void saveMOBTime()
	{
		MOB M=mob;
		if(M!=null)
		{
			PlayerStats pstats=M.playerStats();
			if(pstats!=null)
				pstats.setLastDateTime(System.currentTimeMillis());
		}
	}
	@Override public int getStatus(){return status;}
	@Override public void logout()
	{
		if((mob==null)||(mob.playerStats()==null))
			kill(false);
		else
		{
			saveMOBTime();
			mob=null;
		}
	}

	public static abstract class PromptableCall<T> implements Callable<T>
	{
		public DefaultSession session;
		public Future<T> future;
		public PromptableCall(DefaultSession sess)
		{
			session=sess;
		}
		
		@Override public T call()
		{
			session.handlePromptFor(Thread.currentThread(), promptType(), future);
			try {return innerCall();}
			catch (Exception e){Log.errOut("PromptableCall", e);}
			session.kill(false);
			return defaultReturn();
		}
		public abstract T innerCall() throws java.io.IOException;
		public abstract T defaultReturn();
		public abstract int promptType();
	}
	private static class LoginCall extends PromptableCall<LoginResult>
	{
		public LoginCall(DefaultSession sess) { super(sess); }
		@Override public LoginResult innerCall() throws java.io.IOException
		{ return CMLib.login().login(session); }
		@Override public LoginResult defaultReturn(){return LoginResult.NO_LOGIN;}
		@Override public int promptType(){return 2;}
	}
	private static class CharSelectCall extends PromptableCall<LoginResult>
	{
		public PlayerAccount account;
		public CharSelectCall(PlayerAccount acct, DefaultSession sess)
		{
			super(sess);
			this.account=acct;
		}
		@Override public LoginResult innerCall() throws java.io.IOException
		{ return CMLib.login().selectAccountCharacter(account, session); }
		@Override public LoginResult defaultReturn(){return LoginResult.NO_LOGIN;}
		@Override public int promptType(){return 2;}
	}
	public Future<LoginResult> loginAttempt()
	{
		return CMClass.threadPool.submit(new LoginCall(this));
	}
	public Future<LoginResult> selectChar()
	{
		return CMClass.threadPool.submit(new CharSelectCall(acct, this));
	}
	/**
	 * Handle login status
	 * 
	 * @param login Future result of current login attempt
	 * @return true if user has just logged out, otherwise false
	 */
	private boolean handleStatus(Future<LoginResult> login) throws InterruptedException,ExecutionException
	{
		while(true)
		{
			switch(status)
			{
				case STATUS_LOGIN: {
					status=Session.STATUS_LOGIN1;
					login=loginAttempt(); } //Fall through
				case STATUS_LOGIN1: {
					if(login.isDone())
					{
						LoginResult result=login.get();
						if(result==LoginResult.NO_LOGIN)
						{
							//status=STATUS_LOGOUT;
							return true;
						}
						else if(result==LoginResult.ACCOUNT_LOGIN)
						{
							tries=0;
							//TODO: Load account settings (i.e. ansi/xterm/whatever) here? I think reloadTerminal does that?
							CMLib.login().reloadTerminal(acct,this);
							status=STATUS_ACCOUNTMENU;
							login=selectChar();
						}
						else
						{
							tries=0;
							status=STATUS_LOGIN2;
						}
						continue;
					}
					return false; }
				case STATUS_ACCOUNTMENU: {
					if(login.isDone())
					{
						LoginResult result=login.get();
						if(result==LoginResult.NO_LOGIN)
						{
							status=STATUS_LOGOUT;
							return true;
						}
						else
						{
							tries=0;
							status=STATUS_LOGIN2;
						}
						continue; //goto status
					}
					return false; }
				case STATUS_LOGIN2: {
					if((mob!=null)&&(mob.playerStats()!=null))
						acct=mob.playerStats().getAccount();
					CMLib.login().reloadTerminal(mob);
					if((!killFlag)&&(mob!=null))
					{
						StringBuilder loginMsg=new StringBuilder("");
						loginMsg.append(getAddress())
								.append(" ").append(terminalType)
								.append(((mob.playerStats().hasBits(PlayerStats.ATT_MXP))&&clientTelnetMode(Session.TELNET_MXP))?" MXP":"")
//								.append((clientTelnetMode(Session.TELNET_COMPRESS)||clientTelnetMode(Session.TELNET_COMPRESS2))?" CMP":"")
								.append(((mob.playerStats().hasBits(PlayerStats.ATT_ANSI))&&clientTelnetMode(Session.TELNET_ANSI))?" ANSI":"")
								.append(", login: ").append(mob.name());
						Log.sysOut("Session",loginMsg.toString());
						CMMsg loginMessage = CMClass.getMsg(mob,null,(Vector)null,EnumSet.of(CMMsg.MsgCode.LOGIN),null);
						if(!CMLib.map().sendGlobalMessage(mob,EnumSet.of(CMMsg.MsgCode.LOGIN),loginMessage))
							killFlag=true;
						loginMessage.returnMsg();
					}
					needPrompt=true;
					status=STATUS_OK;
					return false; }
				case STATUS_OK: { //Check mob?
					if(mob==null)
					{
						if(acct!=null)
						{
							CMLib.login().reloadTerminal(acct,this);
							status=STATUS_ACCOUNTMENU;
							login=selectChar();
							continue;
						}
						else
						{
							//status=STATUS_LOGOUT;
							return true;
						}
					}
					return false; }
				default: { //This shouldn't happen but just in case
					//status=STATUS_LOGOUT;
					return true;
				}
			}
		}
	}
	@Override
	public void run()
	{
		try
		{
			while((!killFlag)&&((++tries)<5))
			{
				Future<LoginResult> login=null;
				mob=null;	//Don't think this is needed but playing safe
				status=Session.STATUS_LOGIN;
				while((!killFlag))
				{
					if(handleStatus(login)) break;
					while((!killFlag)&&(CMLib.threads().isAllSuspended())&&(!CMSecurity.isASysOp(mob)))
						try{Thread.sleep(2000);}catch(InterruptedException e){}
					lastLoopTop=System.currentTimeMillis();
					setMSDPNew(MSDPOptions.SERVER_TIME.ordinal());
					if((enMSDP)&&(lastMSDP<lastLoopTop-1000)&&(newMSDP))	//saving a bit instead of calling System
					{
						lastMSDP=lastLoopTop;
						newMSDP=false;
						for(int i=MSDPOptions.size-1; i>=0; i--)
						{
							if(!MSDPReporting[i]) continue;
							if(MSDPIsNew[i]) continue;
							MSDPIsNew[i]=false;
							MSDPOptions O=MSDPOptions.fromOrdinal(i);
							sendMSDP(O.toString(), O.value(this));
						}
					}
					if(in.ready())
						while(currentInputReader.checkInput(this));

					if(!afkFlag)
					{
						if(System.currentTimeMillis()-lastKeystroke>=600000)
							setAfkFlag(true);
					}
					if((needPrompt)&&(lastPrompt+1000<System.currentTimeMillis()))
					{
						showPrompt();
						needPrompt=false;
						lastPrompt=System.currentTimeMillis();
					}
					try{Thread.sleep(100);}catch(InterruptedException e){}
				}
				status=STATUS_LOGOUT;
				previousCmd=""; // will let system know you are back in login menu
			}
		}
		catch(Exception t) //IO and Socket exceptions at least, possibly others
		{
			if(!Log.isMaskedErrMsg(t.getMessage())&&((!killFlag)||(sock.isConnected())))
				errorOut(t);
		}
		status=Session.STATUS_LOGOUT3;

		if(mob!=null)
		{
			String name=mob.name();
			if(name.trim().length()==0) name="Unknown";
			Vector<String> channels=CMLib.channels().getFlaggedChannelNames(CMChannels.ChannelFlag.LOGOFFS);
			for(int i=0;i<channels.size();i++)
				CMLib.commands().postChannel(channels.elementAt(i),name+" has logged out",true);
			CMLib.login().notifyFriends(mob,"^X"+mob.name()+" has logged off.^.^?");

			// the player quit message!
			mob.playerStats().setLastDateTime(System.currentTimeMillis());
			Log.sysOut("Session",getAddress()+" logout: "+name);
			mob.setSession(null);
			mob=null;
		}

		status=Session.STATUS_LOGOUT4;
		killFlag=true;
		//waiting=false;
		needPrompt=false;
		acct=null;
		snoops.clear();

		closeSocks();

		status=Session.STATUS_LOGOUT11;
		CMLib.sessions().removeElement(this);

		status=Session.STATUS_LOGOUTFINAL;
	}
	protected void checkPendingPrompts()
	{
		Future<Void> prompt;
		for(int i=pendingPrompts.size()-1;i>=0;i--)
			if((prompt = (Future<Void>)pendingPrompts.elementAt(i, 2)).isDone())
			{
				promptNumbers[(Integer)pendingPrompts.removeElementsAt(i)[0]]=false;
				try {prompt.get();}
				catch(ExecutionException | InterruptedException e)
				{
					Log.errOut("Prompt",e.getCause());
				}
			}
	}
	protected boolean checkPendingPrompt(int i)
	{
		Future<Void> prompt;
		if((prompt = (Future<Void>)pendingPrompts.elementAt(i, 2)).isDone())
		{
			promptNumbers[(Integer)pendingPrompts.removeElementsAt(i)[0]]=false;
			try {prompt.get();}
			catch(ExecutionException | InterruptedException e)
			{
				Log.errOut("Prompt",e.getCause());
			}
			return false;
		}
		return true;
	}

}