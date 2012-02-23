

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import magelets.MageEditor;
import magelets.Magelet;

/**
 * Servlet implementation class Merlin
 */
public final class Merlin extends Magelet {
	
	private static final long serialVersionUID = 1L;
    
	private static final String validHeaders = "validate.txt";
    private static final String inputParameterHeaders = "inputParameterHeaders.txt";
    private static final String inputParameterFileName = "INPUTparam.txt";
    private static final String servletFolder ="/optMage_1/";
    private static final String inputTargetHeaders = "inputTargetHeaders.txt";   
    private static final String inputTargetFileName = "INPUTtarg.txt";
    private static final String genomeFileName = "genome.fasta";   
    
    
    /**
     * @see Magelet#Magelet()
     */
    public Merlin() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		

		// Set response type and create an output printer
		response.setContentType("text");
		PrintWriter out = response.getWriter();
		
		// Get Servlet Directory
		String directory = getDirectory(servletFolder) + "/";
		System.out.println("POST Received");
		
		// Load and validate parameters
		Map<String,String[]> parameters = load(request);
		
		//System.out.println("Start Array = " + parameters.get("start").length);
		
		try{
			if (validate(directory+validHeaders,parameters)) {
				
				// Generate the inputParameterFile
				generate(directory, inputParameterHeaders, parameters, inputParameterFileName, true);
				generate(directory, inputTargetHeaders, parameters, inputTargetFileName, false);
				generateFASTA(directory, genomeFileName,  parameters.get(MageEditor.dnaSequence)[0] );
				
				// Create a New Instance of Merlin
				mage.Core.Merlin merlin = new mage.Core.Merlin(directory, inputParameterFileName, inputParameterFileName);
				
				// Enable Plotting
                mage.Core.Merlin.plot = true;
				
				// Enable Switches
				mage.Switches.Blast.method = 2;
				
				// Run the optimization
				merlin.optimize();
				
				// Read the MAGE results;
			    this.result= "MERLIN!";
			    System.out.println(result);
			    
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
