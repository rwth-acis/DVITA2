package i5.dvita.commons;

import java.io.Serializable;

public class ACL implements Serializable
{
	private static final long serialVersionUID = -212125378900142053L;
	
	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_ANALYSIS_ADMIN = "ANALYSIS_ADMIN";
	public static final String ROLE_USER = "USER";
	public static final String ROLE_GUEST= "GUEST";
	
	public static final String OPERATION_CONTROL_PANEL = "CONTROL_PANEL";
	public static final String OPERATION_LOGIN = "LOGIN";
	public static final String OPERATION_REQUEST_ANALYSIS = "REQUEST_ANALYSIS";
	public static final String OPERATION_INVOKE_ANALYSIS = "INVOKE_ANALYSIS";
	public static final String OPERATION_CONFIGURATION = "CONFIGURATION";
}
