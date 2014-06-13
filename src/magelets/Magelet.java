package magelets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
	protected HashMap<String, String[]> map;

	// Constructor
	public Magelet(){
		super();
		this.map =  new HashMap<String, String[]>();
		this.map.put(MageEditor.ERROR,new String [] {"Default Error!"});
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
		//System.out.println("Required Headers - "+ Arrays.toString(headers));
		boolean isValid = true;

		for (String ss:  headers){
			if (parameters.containsKey(ss)){
				if (!ss.equals("genome")){
					System.out.println("Parameter Header Found - "+ss+" - "+Arrays.toString(parameters.get(ss)));
				}
				else
				{
					System.out.println("Parameter Header Found - "+ss+" - "+parameters.get(ss)[0].toString().length());
				}
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
		//ProcessBuilder pb2 = new ProcessBuilder("open",directory);
		pb.directory(new File(directory));

		Process p = pb.start();
		p.waitFor();
		//pb2.start();
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
	protected void generate(String directory, String outputFileName, String parameterKey, Map<String, String[]> parameters) throws IOException{

		//Print out the parameter
		TextFile.write(directory, outputFileName, parameters.get(parameterKey)[0]);
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


	// Creates an application/x-www-form-urlencoded String for a response
	protected String buildURLfromMap  () throws UnsupportedEncodingException
	{
		StringBuilder sb =  new StringBuilder();

		for (Map.Entry<String, String[]> entry : this.map.entrySet())
		{

			sb.append(  URLEncoder.encode(entry.getKey(), "ISO-8859-1") ); // URLEncoder.encode(entry.getKey(),"ISO-8859-1")
			sb.append("=");
			StringBuilder array = new StringBuilder();
			for ( String ss : entry.getValue()) { array.append( URLEncoder.encode(ss, "ISO-8859-1") +MageEditor.DELIMITER);}
			sb.append(array.toString());
			sb.append("&");
		}

		// Remove the last character
		sb.deleteCharAt(sb.length()-1);

		// return the string
		return sb.toString();
	}
}
