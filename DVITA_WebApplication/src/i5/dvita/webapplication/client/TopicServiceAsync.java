package i5.dvita.webapplication.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import i5.dvita.webapplication.shared.ThemeRiverData;
import i5.dvita.webapplication.shared.TopicLabels;


public interface TopicServiceAsync {


	void getTopicCurrent(Integer[]Topics, AsyncCallback<ThemeRiverData[]> callback) throws IllegalArgumentException;
	void getTopicList(AsyncCallback<TopicLabels> callback) throws IllegalArgumentException;
	void topicSearch(String textitem, AsyncCallback<Integer[]> asyncCallback);
	void getTimeintervals(AsyncCallback<String[][]> asyncCallback);
	void topicRanking(int buttonTyp, AsyncCallback<Integer[]> asyncCallback);
	
	
	
}
