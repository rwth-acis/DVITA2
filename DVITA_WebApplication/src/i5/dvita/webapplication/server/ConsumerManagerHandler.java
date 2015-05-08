package i5.dvita.webapplication.server;

import java.util.HashMap;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryInformation;

public class ConsumerManagerHandler
{
	private HashMap<String, ConsumerManager> _manager = new HashMap<String, ConsumerManager>();
	private HashMap<String, DiscoveryInformation> _discovery = new HashMap<String, DiscoveryInformation>();
	
	private static ConsumerManagerHandler _instance = null;
	
	private ConsumerManagerHandler() { }
	
	public static ConsumerManagerHandler getInstance()
	{
		if (null == _instance)
		{
			_instance = new ConsumerManagerHandler();
		}
		
		return _instance;
	}
	
	public ConsumerManager getManager(String authUID)
	{
		return _manager.get(authUID);
	}
	
	public void setManager(String authUID, ConsumerManager manager)
	{
		_manager.put(authUID, manager);
	}
	
	public void removeManager(String authUID)
	{
		_manager.remove(authUID);
	}

	public DiscoveryInformation getDiscovery(String authUID)
	{
		return _discovery.get(authUID);
	}

	public void setDiscovery(String authUID, DiscoveryInformation discoveryInformation)
	{
		_discovery.put(authUID, discoveryInformation);
	}
	
	public void removeDiscovery(String authUID)
	{
		_discovery.remove(authUID);
	}
}
