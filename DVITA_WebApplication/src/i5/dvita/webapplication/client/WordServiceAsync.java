package i5.dvita.webapplication.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import i5.dvita.webapplication.shared.WordData;
import i5.dvita.webapplication.shared.WordEvolutionData;


public interface WordServiceAsync {


	void getWordEvolution(Integer[] wordsId, int topicID, AsyncCallback<WordEvolutionData> asyncCallback) throws IllegalArgumentException;
	void bestWords(int topic, int i, long topictime,
			AsyncCallback<WordData> asyncCallback) throws IllegalArgumentException;


	
}
