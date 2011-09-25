package magelets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 
 * @author Samir Ahmed
 *
 */
@SuppressWarnings("serial")
public abstract class Magelet extends HttpServlet {

	protected String result="";
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


	protected void printPostParameter(Map<String,String[]> parameters){
		for (Entry<String, String[]> entry : parameters.entrySet())
		{
		    System.out.println(entry.getKey() + " | " + entry.getValue()[0]);
		}
	}
	
	/**
	 * 
	 * @param filePath		Directory with the desired parameters
	 * @param parameters	Map of all the POST request Parameters
	 * @return				True if valid POST request, false if INVALID
	 * @throws IOException 
	 */
	protected boolean validate(String filePath, Map<String,String[]> parameters) throws IOException{
		String [] headers = magelets.TextFile.getLinesAsArray(filePath);
		System.out.println("Required Headers - "+ Arrays.toString(headers));
		boolean isValid = true;
		
		for (String ss:  headers){
			if (parameters.containsKey(ss)){
				System.out.println("Parameter Header Found - "+ss+" - "+Arrays.toString(parameters.get(ss)));
			}
			else {
				isValid = false; 
			}
		}
		
		// Return true if Valid, false if Not
		return isValid;
	}
	
	/**
	 * 
	 * @param request	Post request
	 * @return	Returns the parameters as Map
	 */
	protected Map<String,String[]> load(HttpServletRequest request){
		
		@SuppressWarnings("unchecked")
		final Map<String,String[]> parameters = request.getParameterMap();
		
		return parameters;
	}
	
	/**
	 * 
	 * @param relativeFilePath	String contain the current File path
	 * @return	String of the real File path
	 */
	protected String getDirectory(String relativeFilePath){
		return this.getServletContext().getRealPath(relativeFilePath);
	}
	
	/**
	 * 
	 * @param directory
	 * @param scriptName
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected String execute(String directory, String scriptName) throws IOException, InterruptedException {
		// Create a process builder to execute perl scripts
	    ProcessBuilder pb = new ProcessBuilder("perl",scriptName);
	    ProcessBuilder pb2 = new ProcessBuilder("open",directory);
	    pb.directory(new File(directory));
	    
	    Process p = pb.start();
	    p.waitFor();
	    pb2.start();
	    System.out.print("Mage Calculation Completed  ... ");

		return result;
	}	
	
	protected void generate(String directory, String inputFileName, Map<String, String[]> parameters, String outputFileName) throws IOException{
		
		// Get the headers as a String Array
		String[] headers = TextFile.getLinesAsArray(directory+inputFileName) ;
		Integer length = Integer.parseInt(parameters.get(MageEditor.mutationCount)[0]);
		
		// Intialize the strigns for concatentation
		String header= "";
		String params= "";
		String [] lines= new String[length]; 
		
		// Initialize lines
		for (int ii=0; ii<length; ii++){ lines[ii] = "";}
		
		// Get all the values from the parameters map and format them properly
		for( String hh: headers){
			header += hh+"	";
			for (int ii = 0; ii<length ;ii++ ){
				lines[ii] += parameters.get(hh)[ii]+"	";
			}
		}
		
		// Remove the last tabs
		header = header.substring(0, header.length()-1);
		
		// Assemble the parameter array into a single properly formated string
		for (String line: lines){
			line = line.substring(0, line.length()-1);
			params += line+"\n";
		}
		
		// Return the parameter file
		TextFile.write(directory, outputFileName, header+"\n"+params);
	}
	
/*	protected void genINTPUTTARGETS(String directory, String inputFileName, Map<String, String[]> parameters, String outputFileName){
		String print_header = "Gene Name	Sense	Replicore	LP	RP	Mutation	Sequence\n";
		
		String start = parameters.get("start")[0];
		String end = parameters.get("end")[0];
		String mutation = parameters.get("mutation")[0];
		String mutationSequence = parameters.get("mutatedSequence")[0];
		
		String print_parameters = "pps	-	2	"+start+"\t"+end+"\t"+mutation+"\t"+mutationSequence+"\n";
		System.out.println(print_parameters);
		
		TextFile.write(directory, inputFileName, print_header +"\n" + print_parameters); 
	}
*/	
	
	/**
	 * Method for generating and printing a FASTA file from a given DNA sequence.
	 * 
	 * @param directory			The directory to which the file will be printed
	 * @param outputFileName	The desired file to be produced, ensure it ends with .fasta
	 * @param dnaSequence	The desired sequence to made into a .fasta file
	 * @throws IOException
	 */
	protected void generateFASTA(String directory, String outputFileName, String dnaSequence) throws IOException{
		String fasta = ">\n"+dnaSequence+"\n";		
		TextFile.write(directory, outputFileName, fasta);
	}

	
}
