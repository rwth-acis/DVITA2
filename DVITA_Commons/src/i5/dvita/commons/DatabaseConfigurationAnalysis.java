package i5.dvita.commons;


import java.sql.Timestamp;

public class DatabaseConfigurationAnalysis extends DatabaseConnection
{
	private static final long serialVersionUID = 581305734016915405L;

	public DatabaseConfigurationRawdata databaseConfigurationRawdata = null;
	public Integer connectionId = null;
	
	public Integer id = null;
	public Granularity gran;
	public int NumberTopics = -1;
	public Timestamp rangeStart;
	public Timestamp rangeEnd;	
	public String metaTitle = null;
	public String metaDescription = null;
	public Integer status = null;
	
	/* gehe maximal x zeitpunkte in vergangenheit bzw. zukunft (in Thesis: Delta Parameter) */
	public int similarDocsTimeShift = 1;
	
	/* zu jeden dieser zeitpunkte finde die y ähnlichsten dokumente */
	public int similarDocsCount = 10;
	
	public static enum Granularity {
	    /* 1 */ YEARLY,  
	    /* 2 */ MONTHLY, 
	    /* 3 */ WEEKLY, 
	    /* 4 */ DAYLY, 
	    /* 5 */ QUARTERYEAR, 
	    /* 6 */ HALFYEAR,
	    /* 7 */ FIVEYEARS,
	    /* 8 */ DECADE 
	}
}
