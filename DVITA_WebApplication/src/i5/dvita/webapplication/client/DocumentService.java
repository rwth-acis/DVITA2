package i5.dvita.webapplication.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import i5.dvita.webapplication.shared.DocumentData;
import i5.dvita.webapplication.shared.DocumentInfo;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("document")
public interface DocumentService extends RemoteService {
	public DocumentData getDocumentData(int docid) throws IllegalArgumentException;
	DocumentInfo [] relatedDocuments(int TOPICID, int limit, int topictime) throws IllegalArgumentException;
	DocumentInfo [] similarDocuments(int docid, int time) throws IllegalArgumentException;
	
	DocumentInfo[] documentSearch(String string)throws IllegalArgumentException;


}
