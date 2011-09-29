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


	/**
	 *  Prints all the Post Parameters Key and Value Pairs
	 * @param parameters Map of POST parameters 
	 */
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
	    System.out.println("Mage Calculation Completed  ... ");

		return result;
	}	
	
	/**
	 * This method will generate a given input file with with specificied input arguments....
	 * 
	 * @param directory		The given directory of the input and output file
	 * @param inputFileName	File with headers
	 * @param parameters	Map of POST parameters
	 * @param outputFileName File for output to be used by the PERL optMAGE
	 * @param isSingle		If this all the parameters are single (i.e no Array)
	 * @throws IOException
	 */
	protected void generate(String directory, String inputFileName, Map<String, String[]> parameters, String outputFileName, boolean isSingle) throws IOException{
		
		// Get the headers as a String Array
		String[] headers = TextFile.getLinesAsArray(directory+inputFileName) ;
		Integer length;
		if (isSingle){ length = 1; }
		else { length = Integer.parseInt(parameters.get(MageEditor.mutationCount)[0]); }
		
		// Intialize the strigns for concatentation
		String header= "";
		String params= "";
		String [] lines= new String[length]; 
		
		// Initialize lines
		for (int ii=0; ii<length; ii++){ lines[ii] = "";}
		
		// Get all the values from the parameters map and format them properly
		//int record=0;
		for( String hh: headers){
			header += hh+"	";
			for (int ii= 0; ii<length ;ii++ ){
				lines[ii] += parameters.get(hh)[ii]+"	";
				//System.out.println("DEBUG " +lines[ii]);
			}
			//System.out.print(Arrays.toString(lines));
		//	record++;
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
