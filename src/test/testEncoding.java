package test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class testEncoding {
	
	public static void main(String[] args) throws UnsupportedEncodingException
	{
		System.out.println( "Hello " + URLEncoder.encode("Hello \nWorld","ISO-8859-1") );
	}
}
