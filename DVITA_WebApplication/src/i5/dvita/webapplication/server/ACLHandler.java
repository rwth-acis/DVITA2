package i5.dvita.webapplication.server;

import i5.dvita.commons.ACLRole;
import i5.dvita.dbaccess.DBAccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACLHandler
{
	static public List<String> getOperations(String role)
	{
		DBAccess dbAccess = new DBAccess();
		List<String> operations = new ArrayList<String>();
		HashMap<Integer, ACLRole> aclRoles = dbAccess.getACLRoles();
			
		for (Map.Entry<Integer, ACLRole> entry : aclRoles.entrySet())
		{
			if (entry.getValue().getRole().equals(role))
			{
				operations.add(entry.getValue().getOperation());
			}
		}
		
		return operations;
	}
}
