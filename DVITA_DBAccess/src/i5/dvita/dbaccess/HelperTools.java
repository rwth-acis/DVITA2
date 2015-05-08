package i5.dvita.dbaccess;

public class HelperTools 
{
	public static String implode(String[] stringArray)
	{
		StringBuilder builder = new StringBuilder();
		
		for (String s : stringArray)
		{
			builder.append(s);
			builder.append(",");
		}
		
		builder.deleteCharAt(builder.length()-1);
		
		return builder.toString();
	}
	
	public static String implode(Integer[] stringArray)
	{
		StringBuilder builder = new StringBuilder();
		
		for (Integer i : stringArray)
		{
			builder.append(i);
			builder.append(",");
		}
		
		builder.deleteCharAt(builder.length()-1);
		
		return builder.toString();
	}
	
	/*public static Integer[] parseToInteger(String[] stringArray)
	{
		Inte
	}*/
}
