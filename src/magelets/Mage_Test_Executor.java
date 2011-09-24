package magelets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.servlet.GenericServlet;

public class Mage_Test_Executor {

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	/*Strings for file names*/
	final private String inputParameters= "INPUTparam.txt" ;
	final private String inputTargets= "INPUTtarg.txt" ;
	final private String inputFasta = "genome.fasta";
	final private String genPrimers= "optMAGEv0.9.pl";
	final private String directory = "/optMage/";
	private String parameters_File;
	private String target_File;
	private String fasta;
	private GenericServlet servlet;
	private Map<String, String[]> parameters ;
	public Mage_Test_Executor(GenericServlet servlet,Map<String, String[]> parameters) throws IOException{
		
		// Generates the Mage INPUT PARAMETERS String
		this.servlet = servlet;
		this.parameters = parameters;
		genINPUTPARAMETERS();
		//genINTPUTTARGETS();
		//genFASTA();
	}

	private void genINPUTPARAMETERS() throws IOException{
		String headerStr = Mage_Test.read(this.servlet.getServletContext().getRealPath("/optMage/optMage_Validate.txt")) ;
		String[] headers = headerStr.split("\n");
		String print_head= "";
		String print_param= "";
		for( String header: headers){
			print_head += header+"	";
			print_param += parameters.get(header)[0]+"	";
		}
		// Remove the last tabs
		print_head = print_head.substring(0, print_head.length()-1);
		print_param = print_param.substring(0, print_param.length()-1);
		this.parameters_File = print_head+"\n"+print_param;

	}
	
	private void genINTPUTTARGETS(){
		String print_header = "Gene Name	Sense	Replicore	LP	RP	Mutation	Sequence\n";
		
		String start = parameters.get("start")[0];
		String end = parameters.get("end")[0];
		String mutation = parameters.get("mutation")[0];
		String mutationSequence = parameters.get("mutatedSequence")[0];
		
		String print_parameters = "pps	-	2	"+start+"\t"+end+"\t"+mutation+"\t"+mutationSequence+"\n";
		System.out.println(print_parameters);
		
		this.target_File = print_header + print_parameters; 
	}
	
	private void genFASTA(){
		this.fasta = ">\n"+parameters.get("dnaSequence")[0]+"\n";
	}
	
	private void printFile(String directory, String fileName, String text) throws IOException{
		//Write to file
	    BufferedWriter out = new BufferedWriter(new FileWriter(this.servlet.getServletContext().getRealPath(directory+fileName)));
	    out.write(text);
	    out.close();
	}
	
	public String execute() throws IOException, InterruptedException{
		//Write PARAMETER, TARGETS AND FASTA TO FILE
	    printFile(this.directory, this.inputParameters, this.parameters_File);
	    //printFile(this.directory, this.inputTargets, this.target_File);
	    //printFile(this.directory, this.inputFasta, this.fasta);
		
	    // Create a process builder to execute perl scripts
	    ProcessBuilder pb = new ProcessBuilder("perl",this.genPrimers);
	    ProcessBuilder pb2 = new ProcessBuilder("open",this.servlet.getServletContext().getRealPath("/optMage/"));
	    pb.directory(new File(this.servlet.getServletContext().getRealPath("/optMage/")));
	    
	    //String [] executionParams = {"perl",genPrimers};
	    //String [] environment= null;
	    Process p = pb.start();
	    p.waitFor();
	    pb2.start();
	    System.out.print("Mage Calculation Completed  ... ");
	   
	    String result = Mage_Test.read(servlet.getServletContext().getRealPath("/optMage/OUToligos.txt"));
	    System.out.println(result);
	    
	    // Delete the file
	    File f = new File(servlet.getServletContext().getRealPath("/optMage/OUToligos.txt"));
	    boolean success = f.delete();
	    if (!success) {throw new IllegalArgumentException("Delete: deletion failed"); }
	    else {System.out.println("Request Completed");}
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
