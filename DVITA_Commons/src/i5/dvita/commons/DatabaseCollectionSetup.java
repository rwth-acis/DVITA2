package i5.dvita.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCollectionSetup implements Serializable
{
	private static final long serialVersionUID = 2124588123709576626L;
	
	public DatabaseConfigurationRawdata databaseConfigurationRawdata = new DatabaseConfigurationRawdata();
	public List<DatabaseConfigurationAnalysis> databaseConfigurationAnalyses = new ArrayList<DatabaseConfigurationAnalysis>();
}
