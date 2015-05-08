package i5.dvita.dts;

import java.io.IOException;
import java.net.ServerSocket;

public class DVITAToolServer
{
	public static void main(String[] args) 
	{
		Configuration serverConfig = Configuration.getInstance();
		ServerSocket serverSocket = null;
		
		System.out.println("Starting DVITAToolServer...");
		
		if (!serverConfig.readConfigFile(args))
		{
			// if we can't read the configuration file we just quit here
			// exception handling and further information about the error are given by readConfigFile
			System.out.println("Server exit with errors.");
			System.exit(-1);
		}

        try
        {
    		// create ServerSocket, depending on given IP
    		if (null == serverConfig.getIP())
    		{
    			serverSocket = new ServerSocket(serverConfig.getPort());
    		}
    		else
    		{
    			serverSocket = new ServerSocket(serverConfig.getPort(), 50, serverConfig.getIP());
    		}
    		
    		if (null == Configuration.getInstance().getLoggerFolderPath())
    		{
    			System.out.println("Can not access/create log folder. All messages are now printed to stdout.");
    			System.out.println("(you can add logfolder=pathtologfolder to the configuration file. The folder will be created if not exists)");
    		}
    		else
    		{
    			System.out.println("For further messages check log files(accessible in adminGUI within DVITA)");
    		}
    		
    		System.out.println("");
    		System.out.println("Startup successfull!");
    		System.out.println("Now i'm waiting for clients [:");
    		

    		
    		// the basic DTS execution loop: wait for clients and delegate them
            while (true)
            {
                new ClientCommunicationThread(serverSocket.accept()).start();
            }
        }
        catch (IOException e)
        {
            System.err.println("Could not listen on port " + serverConfig.getPort());
            System.exit(-1);
        }		
	}	
}
