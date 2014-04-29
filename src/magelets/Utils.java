package magelets;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**A class to handle passing HTTP requests to mage.Tools.OutputTools
 * 
 * @author Michael Quintin
 *
 */
public class Utils extends Magelet{
	
	private static final String validHeaders = "validate.txt";
	private static final String inputParameterFileName = "INPUTparam.txt";
	private static final String servletFolder ="/optMage_1/";
	private static final String inputTargetFileName = "INPUTtarg.txt";
	private static final String genomeFileName = "genome.fasta";   
    private static final String oligoFile = "OUToligos.txt";



	public Utils(){
		super();
	}
	
	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set response type and create an output printer
		response.setContentType("text");
		PrintWriter out = response.getWriter();

		// Get Servlet Directory

		System.out.println("POST Received");

		// Load and validate parameters
		Map<String,String[]> parameters = load(request);

		//System.out.println("Start Array = " + parameters.get("start").length);

		String user_id = parameters.get(MageEditor.USERID)[0];
		String directory = getDirectory(servletFolder)+"/copy"+user_id+"/" ;
		
		try{
			if (validateheaders(parameters)){
				String type = map.get("requesttype")[0];
				if (type.equals("mascpcr")){
					//TODO handle mascpr request
					//what's in the Oligo file???
					
					//write the MASCPCR primer file
					//List<Oligo> 
					
					//String filecontents = mage.Tools.OutputTools.getMASCPCRPrimerFileContents(pool);
				}
				else if (type.equals("dsdna")){
					//TODO handle dsdna request
				}
				else if (type.equals("diversity")){
					//TODO handle diversity request
				}
				
			}
			else{
				this.map.remove(MageEditor.ERROR);
				this.map.put(MageEditor.ERROR, new String[]{ "Invalid Parameters"});
				this.result = this.buildURLfromMap();		
			}	
		}
		catch (Exception EE){ 
			EE.printStackTrace();
			this.map.remove(MageEditor.ERROR);
			this.map.put(MageEditor.ERROR, new String[] {EE.toString()});
			this.result = this.buildURLfromMap();
		}
	}
	
	/**The request must contain a "requesttype" of either "dsdna","mascpcr", or "diversity"
	 * which each also have their own requirements
	 * 
	 * @param parameters
	 * @return is the request valid
	 */
	private boolean validateheaders(Map<String,String[]> parameters){
		Set<String> headers = parameters.keySet();
		String type = "ERROR"; //this should be an enum...
		if (!headers.contains("requesttype")){
			return false;
		}
		else{
			type = parameters.get("requesttype")[0];
		}
		//...because you can't use switch statements with strings until Java 7
		if ( type.equals("mascpcr")){
			return true;
		}
		else if (type.equals("diversity")){
			return (headers.contains("cycles"));
		}
		else if (type.equals("dsdna")){
			return (headers.contains("left") && headers.contains("right") && headers.contains("seq"));
		}
		else return false;
	}
	
}
