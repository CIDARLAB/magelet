package magelets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class magelet
 */
public class Mage_Test extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Mage_Test() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		// Generate Current Time in UTC format
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		f.setTimeZone(TimeZone.getTimeZone("UTC"));

		// Print confirmation response.
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.write("Connected : "+f.format(new Date()));
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		// Set the response type to text/html
		response.setContentType("text/html");
		
		// Create a print writer
		PrintWriter out = response.getWriter();
		
		// Extract parameters into a Map, supress type safety warning unchecked
		@SuppressWarnings("unchecked")
		final Map<String,String[]> parameters = request.getParameterMap();
		
		try{
			if(validate(parameters)) { 
				out.write("Valid Parameters"); 
				Mage_Test_Executor mte = new Mage_Test_Executor(this, parameters);
				out.write(mte.execute());	
			}
			else {out.write("Invalid Request"); }
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}
	
	/*Need a function here to validate the INPUTTARG file*/
	
	private boolean validate(Map<String,String[]> parameters) throws IOException{
		
		String filestr = read(getServletContext().getRealPath("/optMage/optMage_Validate.txt"));
		String [] headers = filestr.split("\n");
		boolean valid = true;

		for (Entry<String, String[]> entry : parameters.entrySet())
		{
		    System.out.println(entry.getKey() + "/" + entry.getValue()[0]);
		}
		
		for (String ss:  headers){
			if (parameters.containsKey(ss)){
				System.out.println("Parameter Header Found - "+ss+" - "+parameters.get(ss)[0]);
			}
			else {
				valid = false; 
			}
		}
		return valid;
		}
	
	/* Straight forward Read file util,,, TODO:  Move this to a utils package*/
	public static String read(String file) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (file));
		String line  = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}
		return stringBuilder.toString();
	}
	
}
