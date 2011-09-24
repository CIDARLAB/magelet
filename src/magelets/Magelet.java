package magelets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author Samir Ahmed
 *
 */
@SuppressWarnings("serial")
public abstract class Magelet extends GenericServlet {
	
	// Constructor
	public Magelet(){
		super();
	}
	
	/**
	 * 
	 * @param request	An HTTP Get Request
	 * @param response	Responds with Connection GMT UTC Timestamp
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
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
	 * 
	 * @param filePath		Directory with the desired parameters
	 * @param parameters	Map of all the POST request Parameters
	 * @return				True if valid POST request, false if INVALID
	 */
	abstract public boolean validate(String filePath, Map<String,String[]> parameters);
	
	/**
	 * Abstraction intended for concrete method to generate all the necessary input files
	 */
	abstract public void generate();
	
	/**
	 * Executes the MAGE as a system call to an external process
	 * @return 	A String with the output files from the MAGE execution
	 */
	abstract public String execute();
}
