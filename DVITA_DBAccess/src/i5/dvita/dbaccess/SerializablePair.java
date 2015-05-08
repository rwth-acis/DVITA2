package i5.dvita.dbaccess;

import java.io.Serializable;

public class SerializablePair<A extends Serializable,B extends Serializable> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2831696607388259933L;
	public A first;
	public B second;
	

}
