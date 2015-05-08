package i5.dvita.tools.similardocumentcomputation;

import java.lang.Math;

public class JensenShanonDivergenz {

	public static  Double JSD(Double[]P, Double[] topicproportionsOtherDoc){
		double[]Middleware=new double[P.length];
		for(int i=0;i<P.length;i++){
			Middleware[i]= (P[i]+topicproportionsOtherDoc[i])/2.0;
		}
		return ((KLD(P,Middleware)/2.0) +(KLD(topicproportionsOtherDoc,Middleware))/2.0);

	}
	
	private static double  KLD(Double[] topicproportionsOtherDoc, double[] Middleware) {
		
		double result = 0.0;
	for(int i=0;i<topicproportionsOtherDoc.length;i++){
		if(topicproportionsOtherDoc[i]==0) continue;
		result+=(topicproportionsOtherDoc[i]*Math.log(topicproportionsOtherDoc[i]/Middleware[i]));
		}
	return result;
	}
	
}
