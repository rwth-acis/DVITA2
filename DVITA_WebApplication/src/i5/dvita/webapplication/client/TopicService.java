package i5.dvita.webapplication.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import i5.dvita.webapplication.shared.ThemeRiverData;
import i5.dvita.webapplication.shared.TopicLabels;

@RemoteServiceRelativePath("topic")
public interface TopicService extends RemoteService{
	
	ThemeRiverData [] getTopicCurrent(Integer [] Topics) throws IllegalArgumentException;

	TopicLabels getTopicList() throws IllegalArgumentException;
	Integer[] topicSearch(String textitem) throws IllegalArgumentException;

	String [][] getTimeintervals()throws IllegalArgumentException;

	Integer[] topicRanking(int rankType) throws IllegalArgumentException;

	
	
	
}