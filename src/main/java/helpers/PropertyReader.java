package helpers;

import java.util.Properties;

public class PropertyReader {

	
	public static Properties PROPERTIES = new Properties();
	
	static {
		try {
		PROPERTIES.load(PropertyReader.class.getClassLoader().getResourceAsStream("people.properties"));
		}
		catch(Exception e)
		{
			System.out.println("Error while loading properties" + e + e.getMessage());
		}
		
	}
}
