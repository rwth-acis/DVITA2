package i5.dvita.commons;

import java.io.Serializable;

public class DatabaseConnection implements Serializable
{
	private static final long serialVersionUID = -7733795906571530332L;
	
	public Integer id = null;
	public String name = null;
	public String description = null;
	public String server = null;
	public Integer port = null;
	public String databasename = null;
	public String user = null;
	public String passwort = null;
	public String tablePrefix = null;
	public String schema = null;
	
	// 1 = MYSQL
	// 2 = DB2
	// 3 = ORACLE
	public Integer type = null;
}
