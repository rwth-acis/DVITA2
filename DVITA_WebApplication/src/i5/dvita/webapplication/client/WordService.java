package i5.dvita.webapplication.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import i5.dvita.webapplication.shared.WordData;
import i5.dvita.webapplication.shared.WordEvolutionData;
import org.apache.commons.lang3.ArrayUtils;

@RemoteServiceRelativePath("word")
public interface WordService extends RemoteService{
	
	WordEvolutionData getWordEvolution(Integer[] wordsId, int topicID) throws IllegalArgumentException;
	WordData bestWords(int topic, int i, long topictime) throws IllegalArgumentException;
	
}