import java.io.IOException;


public class MainTestHUOMIL_saveToFile {

	public static void main(String [] arg) throws IOException{
		
		String input = "retail.txt";
		String output = "output.txt";
		AlgoHUOMIL algo = new AlgoHUOMIL();
		algo.runAlgorithm(input, output, 0.0007, 0.3);
		algo.printStats();
	}
}