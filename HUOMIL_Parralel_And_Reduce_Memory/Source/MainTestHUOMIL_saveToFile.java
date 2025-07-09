import java.io.IOException;


public class MainTestHUOMIL_saveToFile {

	public static void main(String [] arg) throws IOException{
		
		String input = "mushroom.txt";
		String output = "output.txt";
		float alpha = 0.03f;
		float beta = 0.3f;
		AlgoHUOMIL algo = new AlgoHUOMIL();
		algo.runAlgorithm(input, output, alpha, beta);
		algo.printStats();
	}
}
