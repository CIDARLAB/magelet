package test;

public class test {

	/**
	 * Test Function
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		// Create a test environment for MERLIN
		String merlin_test_dir =  "/Users/mockingbird/dropbox/research/magic/server/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/magelet/optMage_1";
		String parametersFileName = "INPUTparam.txt"; 
		String targetFileName = "INPUTtarg.txt";
		String genomeFileName = "genome.fasta";
		
		// Create new instance of merlin object
		mage.Core.Merlin merlin = new mage.Core.Merlin(merlin_test_dir, targetFileName, parametersFileName, genomeFileName);
		merlin.verbose(true);
		// Enable plotting
		mage.Core.Merlin.plot = true;
		
		// Enable switches
		mage.Switches.Blast.method = 2;
		
		// Optimize oligos
		merlin.optimize();
		
		merlin.compareToOptMage("OUToligos.txt");
		
	}

}
