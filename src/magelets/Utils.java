package magelets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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
	
	private static final long serialVersionUID = 1L;
	
	//private static final String validHeaders = "validate.txt";
	//private static final String inputParameterFileName = "INPUTparam.txt";
	private static final String servletFolder ="/optMage_1/";
	//private static final String inputTargetFileName = "INPUTtarg.txt";
	private static final String genomeFileName = "genome.fasta";   
    private static final String oligoFile = "OUToligos.txt";
    private static final String mascpcrFile = "MASCPCR.txt";
    private static final String diversificationFile = "diversification.txt";



	public Utils(){
		super();
	}
	
	/**
	 * 
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set response type and create an output printer
		response.setContentType("text");
		PrintWriter out = response.getWriter();

		// Get Servlet Directory

		//System.out.println("GET Received");
		
		/*System.out.println("unpacking map before run");
	    for (String key : map.keySet()){
	    	System.out.println("key: " + key);
	    	System.out.println("value: " + map.get(key).toString());
	    }*/

		// Load and validate parameters
		Map<String,String[]> parameters = load(request);

		//System.out.println("Start Array = " + parameters.get("start").length);

		//String user_id = parameters.get(MageEditor.USERID)[0];
		//String directory = getDirectory(servletFolder)+"/copy"+user_id+"/" ;
		
		//debugging
		//ArrayList<String> res = new ArrayList<String>();
		//res.addAll(parameters.keySet());
		//for (String[] value : parameters.values()){
		//	ArrayList<String> arr = new ArrayList<String>();
		//	for (String v : value){
		//		arr.add(v);
		//	}
		//	res.addAll(arr);
		//}
		
		
		try{
			System.out.println("Validating headers");
			if (validateheaders(parameters)){
				//System.out.println("headers validated. Getting requesttype");
				String type = parameters.get("requesttype")[0];
				//System.out.println("requesttype = " + type);
				if (type.equals("mascpcr")){
					String user_id = parameters.get("userID")[0];
					String directory = getDirectory(servletFolder) + "/copy" + user_id + "/";
					String file = directory + mascpcrFile;
					String res = "MASCPCR primers could not be loaded. Please try running the MERLIN process again.";
					try{
						System.out.println("attempting to load " + file);
						res = TextFile.read(file);
					}
					catch (IOException e){System.out.print(e.getMessage());}
					
					this.result = res;
				}
				else if (type.equals("dsdna")){
					String user_id = parameters.get("userID")[0];
					String directory = getDirectory(servletFolder) + "/copy" + user_id + "/";
					int left = Integer.valueOf(parameters.get("left")[0]);
					int right = Integer.valueOf(parameters.get("right")[0]);
					String seq = parameters.get("seq")[0];
					
					//load genome
					String res = "Genome could not be loaded. Please try running the MERLIN process again.";
					try{
						String genome = readFasta(directory+genomeFileName);
						List<String> resList = mage.Tools.OutputTools.getDSDNAPrimers(genome, seq, left, right);
						if(!resList.isEmpty()){
							res = "";
							for (String primer : resList){
								res = res + primer + "\t";
							}
						}						
					}
					catch (IOException e){}
					
					this.result = res.trim();
					
				}
				else if (type.equals("diversity")){
					//TODO handle diversity request
				}
				/*else if (type.equals("oligos")){
					//System.out.println("generating oligo response");
					//TODO return OUToligos file
					//this.map.put("OligioFile", TextFile.getLinesAsArray(directory+oligoFile ));
					//String[] arr = {"Example Oligos String"};
					//System.out.println("arr: " + arr.toString());
					//this.map.put("OligioFile", arr);
					//System.out.println("map: " + map.toString());
				    //System.out.println("Unpacking map after run:");
				    //for (String key : map.keySet()){
				    //	System.out.println("key: " + key);
				    //	System.out.println("value: " + map.get(key).toString());
				    //}
					//this.result = this.buildURLfromMap();
				    //this.result = "test oligo string";
					String user_id = parameters.get("userID")[0];
					String directory = getDirectory(servletFolder)+"/copy"+user_id+"/" ;
					String res = "Oligos could not be loaded. Please try running the MERLIN process again.";
					try{
						res = TextFile.read(directory+"MerlinOligos.txt");
					}
					catch (IOException e){}
					
					this.result = res;
				    //System.out.println("result: " + result);
				}*/
				
			}
			else{
				System.out.println("failed to validate parameters");
				this.map.remove(MageEditor.ERROR);
				this.map.put(MageEditor.ERROR, new String[]{ "Invalid Parameters"});
				this.result = this.buildURLfromMap();		
			}
		}
		catch (Exception EE){ 
			System.out.println("error encountered: processing Utils GET request");
			System.out.println(EE.getMessage());
			EE.printStackTrace();
			this.map.remove(MageEditor.ERROR);
			this.map.put(MageEditor.ERROR, new String[] {EE.toString()});
			this.result = this.buildURLfromMap();
		}
		finally{
			
			// Print to console and return the results
			//System.out.println(this.result);
			out.write(this.result);
			//out.write(res.toString());
			//out.write("this is a test");
			// Close the output Stream.
			out.close();
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
		if (!headers.contains("requesttype") || !headers.contains("userID")){
			return false;
		}
		else{
			type = parameters.get("requesttype")[0];
		}
		//...because you can't use switch statements with strings until Java 7
		if ( type.equals("mascpcr")){
			return (headers.contains("userID"));
		}
		else if (type.equals("diversity")){
			return (headers.contains("cycles"));
		}
		else if (type.equals("dsdna")){
			return (headers.contains("left") && headers.contains("right") && headers.contains("seq"));
		}
		else if (type.equals("oligos")){
			return (headers.contains("userID"));
		}
		else return false;
	}
	
	//as TextFile.read, except chopping off the top line which is metadata for FASTAs
	private String readFasta(String file) throws IOException{
		BufferedReader reader = new BufferedReader( new FileReader(file));
		String line  = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		line = reader.readLine(); //read the first line, do nothing with it
		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}
		reader.close();
		return stringBuilder.toString();
	}
	
}
