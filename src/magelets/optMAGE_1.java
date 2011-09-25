package magelets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class optMAGE_1
 */
public class optMAGE_1 extends Magelet {
	
	private static final long serialVersionUID = 1L;
    
	private static final String validHeaders = "/validate.txt";
    private static final String inputParameterHeaders = "/headers.txt";
    private static final String script = "optMAGEv0.9.pl";
    private static final String oligoFile = "/OUToligos.txt";
    private static final String dumpFile = "/OUTdump.txt";
    private static final String inputParameterFileName = "/INPUTparam.txt";
    private static final String servletFolder ="/optMage_1/";
    
    
    /**
     * @see Magelet#Magelet()
     */
    public optMAGE_1() {
        super();
        
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Set response type and create an output printer
		response.setContentType("text");
		PrintWriter out = response.getWriter();
		
		// Get Servlet Directory
		String directory = getDirectory(servletFolder);
		System.out.println("POST Received");
		
		// Load and validate parameters
		Map<String,String[]> parameters = load(request);
		try{
			if (validate(directory+validHeaders,parameters)) {
				
				// Generate the inputParameterFile
				generate(directory, inputParameterHeaders, parameters, inputParameterFileName);
				
				// Execute the optMAGE script
				execute(directory, script);
				
				// Read the MAGE results;
			    this.result= TextFile.read(directory+oligoFile);
			    System.out.println(result);
			    
			    // Delete the files we just created
			    TextFile.delete(directory+dumpFile);
			    TextFile.delete(directory+oligoFile);
			    TextFile.delete(directory+inputParameterFileName);
			}
			else { this.result = "Invalid Request Parameters"; }
		}
		catch (Exception EE){ EE.printStackTrace();}
		finally{
			
			// Print to console and return the results
			out.write(this.result);
			System.out.println(this.result);
			
			// Close the output Stream.
			out.close();
		}
		
	}
}
