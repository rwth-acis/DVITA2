package i5.dvita.webapplication.shared;

import java.io.Serializable;

public class DocumentInfo implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = 3792339475194534132L;
/**
 * Hier werden alle Ausgaben der Serverseite gespeichert  
 * und anschlieﬂend von cleint wiederbenutzt
 */
	public Integer docID;
	public String docTitle;
	public String docDate;
	public Double[] topicProportions;
	public Integer [] topicIDs;
	public Integer intervalID;
	
	
	
}
