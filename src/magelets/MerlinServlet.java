package magelets;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mage.Editor.PlotData;

/**
 * Servlet implementation class Merlin
 */
public final class MerlinServlet extends Magelet {

	private static final long serialVersionUID = 1L;

	private static final String validHeaders = "validate.txt";
	private static final String inputParameterFileName = "INPUTparam.txt";
	private static final String servletFolder ="/optMage_1/";
	private static final String inputTargetFileName = "INPUTtarg.txt";
	private static final String genomeFileName = "genome.fasta";   
    private static final String oligoFile = "OUToligos.txt";



	/**
	 * @see Magelet#Magelet()
	 */
	public MerlinServlet() {
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
				// ASSUME FILES ARE ALREADY GENERATED
				//				generate(directory, inputParameterFileName, MageEditor.parameterKey, parameters );
				//				generate(directory, inputTargetFileName, MageEditor.targetKey, parameters );
				//				generateFASTA(directory, genomeFileName,  parameters.get(MageEditor.dnaSequence)[0] );

				// Create a New Instance of Merlin
				mage.Core.Merlin merlin = new mage.Core.Merlin(directory, inputTargetFileName, inputParameterFileName, genomeFileName);
				merlin.verbose(true);
				// Enable Plotting
				mage.Core.Merlin.plot = true;
				
				// Enable Switches
				mage.Switches.Blast.method = 2;

				// Run the optimization
				merlin.optimize();
				
				// Run the optMage Comparison
				merlin.compareToOptMage(oligoFile);
				// Run the genbank generation
				List<String> gbList = merlin.generateGenbank();

				String[] genbanks = gbList.toArray( new String[gbList.size()]);
				
				List<String> nameList = merlin.generateNames();
				String[] names = nameList.toArray( new String[nameList.size()]);

				// Add the genbank array to the response
				map.put(MageEditor.GENBANK, genbanks);
				map.put(MageEditor.NAMES, names);
				
				// Generate the plot data
				List<PlotData> plotList= merlin.generatePlotData();
				addPlots(plotList);
				
			    // Something about renaming the genome file would go here.
			    TextFile.delete(directory+inputTargetFileName);
			    TextFile.delete(directory+inputParameterFileName);
			    TextFile.delete(directory+oligoFile);

			    this.result = this.buildURLfromMap();				
				System.out.println("Response Completed");
			}
			else { 
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
		finally{

			// Print to console and return the results
			out.write(this.result);
			System.out.println(this.result);

			// Close the output Stream.
			out.close();
		}
	}
	
	private void addPlots(List<PlotData> list)
	{
		String [] freeEnergy = new String[list.size()];
		String [] blastGenome = new String[list.size()];
		String [] blastOligo = new String[list.size()];
		String [] optMagePosition = new String[list.size()];
		String [] merlinPosition = new String[list.size()];
		
		
		// Populate arrays with the plot data values
		for ( int ii = 0; ii < list.size() ;ii++ )
		{
			freeEnergy[ii] = list.get(ii).getFreeEnergy();
			blastOligo[ii] = list.get(ii).getBlastOligo();
			blastGenome[ii] =list.get(ii).getBlastGenome();
			merlinPosition[ii] = list.get(ii).getMerlin();
			optMagePosition[ii] = list.get(ii).getOptMage();
		}
		
		// Add the string [] to the map
		map.put(MageEditor.FREE_ENERGY, freeEnergy);
		map.put(MageEditor.BLAST_GENOME, blastGenome);
		map.put(MageEditor.BLAST_OLIGO, blastOligo);
		map.put(MageEditor.MERLIN_POSITION, merlinPosition);
		map.put(MageEditor.OPTMAGE_POSITION, optMagePosition);
		
	}
}
