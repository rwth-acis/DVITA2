package i5.dvita.webapplication.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import i5.dvita.webapplication.shared.DocumentData;
import i5.dvita.webapplication.shared.DocumentInfo;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface DocumentServiceAsync {
	
	void relatedDocuments(int TOPICID, int limit,int topictime, AsyncCallback<DocumentInfo []> callback)throws IllegalArgumentException;
	
	void getDocumentData(int docid, AsyncCallback<DocumentData> callback) throws IllegalArgumentException;

	void similarDocuments(int docid,int time,
			AsyncCallback<DocumentInfo[] > asyncCallback);

	
	void documentSearch(String words, AsyncCallback<DocumentInfo[]> asyncCallback) throws IllegalArgumentException;
}
