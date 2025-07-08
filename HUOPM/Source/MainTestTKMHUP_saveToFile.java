import java.io.IOException;


public class MainTestTKMHUP_saveToFile {

	public static void main(String [] arg) throws IOException{
		
		String input = "retail.txt";
		String output = "output.txt";

		AlgoHUOPM algo = new AlgoHUOPM();
		algo.runAlgorithm(input, output, 0.00007, 0.3);
		algo.printStats();
	}
}
