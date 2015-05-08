package i5.dvita.webapplication.shared;
import java.io.Serializable;
import org.apache.commons.lang3.ArrayUtils;
public class WordEvolutionData  implements Serializable{
	/**
	 * 
	 */
	public static final long serialVersionUID = -7897650599712054415L;
	
	public Integer [] wordIDs;
	public Double [][]relevanceAtTime;
	
	
	public Integer [] intervalIDs;
	public String [] intervalStartDate;
	
}
