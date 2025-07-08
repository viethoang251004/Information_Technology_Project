


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class AlgoHUOMIL {

	/** the time at which the algorithm started */
	public long startTimestamp = 0;  
	
	/** the time at which the algorithm ended */
	public long endTimestamp = 0; 
	
	/** the number of high-utility itemsets generated */
	public int huiCount =0; 
	
	/** the number of generated candidates (join operations) */
	public int candidateCount =0;
	
	/** Map to remember the TWU of each item */
	Map<Integer, Integer> mapItemToSup;
	 
    BufferedWriter writer = null;  
	/** enable LA-prune strategy  */
	boolean ENABLE_LA_PRUNE = true;
	
	/** variable for debug mode */
	boolean DEBUG = false;
	

	int DataBaseSize = 0; // size of the database
	/** buffer for storing the current itemset that is mined when performing mining
	* the idea is to always reuse the same buffer to reduce memory usage. */
	final int BUFFERS_SIZE = 200;
	private int[] itemsetBuffer = null;


	/**
	 * This class represent an item and its utility in a transaction
	 * @author Philippe Fournier-Viger
	 */
	class Pair{
		int item = 0;
		int utility = 0;
		
		public String toString() {
			return "[" + item + "," + utility + "]";
		}
	}
	
	/**
	 * Default constructor
	 */
	public AlgoHUOMIL() {
		
	}

	/**
	 * Run the algorithm
	 * @param input the input file path
	 * @param output the output file path
	 * @param minUtility the minimum utility threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm(String input, String output, double alpha, double beta) throws IOException {
		// reset maximum
		MemoryLogger.getInstance().reset();
		
		// initialize the buffer for storing the current itemset
		itemsetBuffer = new int[BUFFERS_SIZE];

		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
	
		
        writer = new BufferedWriter(new FileWriter("output.txt"));
		//  We create a  map to store the support of each item
		mapItemToSup = new HashMap<Integer, Integer>();

		List<List<Pair>> revisedTransactions= new ArrayList<List<Pair>>();
		List<Integer> transactionUtilities = new ArrayList<Integer>();

		// We scan the database a first time to calculate the support of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the transaction according to the : separator
				String split[] = thisLine.split(":"); 
				// the first part is the list of items
				String items[] = split[0].split(" "); 

				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					
					
					// get the current sup of that item
					Integer sup = mapItemToSup.get(item);
					// update the sup of that item
					sup = (sup== null)? 
							1 : sup + 1;
					mapItemToSup.put(item, sup);
				}
				// increase the size of the database
				DataBaseSize++;
			}
		
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		

		List<GUOList> listOfUOLists = new ArrayList<GUOList>();


		Map<Integer, GUOList> mapItemToUOList = new HashMap<Integer, GUOList>();

		// For each item
		for(Integer item: mapItemToSup.keySet()){

			if(mapItemToSup.get(item) >= (DataBaseSize * alpha)) {

				GUOList uList = new GUOList(item);
				mapItemToUOList.put(item, uList);

				listOfUOLists.add(uList); 
				
			}
		}
		// SORT THE LIST OF HIGH SUP ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUOLists, new Comparator<GUOList>(){
			public int compare(GUOList o1, GUOList o2) {
				// compare the support of the items
				return compareItems(o1.item, o2.item);
			}
			} );
		
		// SECOND DATABASE PASS 
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));

			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				int transactionUtility = Integer.parseInt(split[1]);  
				
				 
				
				// Create a list to store items
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// if the item has enough utility
					if(mapItemToSup.get(pair.item) >= alpha * DataBaseSize) {
						// add it
						revisedTransaction.add(pair);
					}
				}
				
				// sort the transaction
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}});
				revisedTransactions.add(revisedTransaction);
				//System.out.println(revisedTransaction);
				transactionUtilities.add(transactionUtility);
			}




		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// Mine the database recursively
		// 1. Chuẩn bị totalOrder
		List<Integer> totalOrder = new ArrayList<>();
		for (Integer item : mapItemToSup.keySet()) {
			if (mapItemToSup.get(item) >= alpha * DataBaseSize) {
				totalOrder.add(item);
			}
		}
		totalOrder.sort(Comparator.comparingInt(mapItemToSup::get));
		//

		// 2. Gọi constructGUOIL
		// System.out.println(revisedTransactions);
		Map<Integer, GUOList> mapItemToGUOIL = constructGUOIL(revisedTransactions,  transactionUtilities, totalOrder);
		//System.out.println("======= GUO-IL (Global Utility Occupancy Indexed List) =======");

		// for (Map.Entry<Integer, GUOList> entry : mapItemToGUOIL.entrySet()) {
		// 	int item = entry.getKey();
		// 	GUOList guoList = entry.getValue();

		// 	System.out.println("Item: " + item);
		// 	System.out.printf("  sumUO: %.4f\n", guoList.sumUO);
		// 	System.out.printf("  sumRUO: %.4f\n", guoList.sumRUO);
		// 	System.out.println("  Entries:");

		// 	int index = 0;
		// 	for (Element e : guoList.elements) {
		// 		System.out.printf("    [%d] uo=%.4f, ruo=%.4f, nextItem=%d, nextIndex=%d\n",
		// 			index++, e.uo, e.ruo, e.nextItem, e.nextIndex);
		// 	}
		// 	System.out.println();
		// }
		// 3. Bắt đầu khai phá theo HUOMIL
		
		mine(itemsetBuffer,0, mapItemToGUOIL, (int)(alpha * DataBaseSize), beta, null,1);

		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		
		// record end time
		endTimestamp = System.currentTimeMillis();

		writer.close();
	}
	
	/**
	 * Method to compare items by their TWU
	 * @param item1 an item
	 * @param item2 another item
	 * @return 0 if the same item, >0 if item1 is larger than item2,  <0 otherwise
	 */
	private int compareItems(int item1, int item2) {

		
		int compare = mapItemToSup.get(item1) - mapItemToSup.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		return (compare == 0)? item1 - item2 :  compare;
	}
	
	
	public Map<Integer, GUOList> constructGUOIL(
    	List<List<Pair>> revisedTransactions,
    	List<Integer> transactionUtilities,
    	List<Integer> totalOrder
	) {
		Map<Integer, GUOList> mapItemToGUOIL = new HashMap<>();

		for (int t = 0; t < revisedTransactions.size(); t++) {
			List<Pair> transaction = revisedTransactions.get(t);
			double transactionUtility = transactionUtilities.get(t);

			double ruo = 0.0;
			int nextItem = -1;
			int nextIndex = -1;

			// Duyệt transaction theo thứ tự ngược (phải -> trái)
			for (int i = transaction.size() - 1; i >= 0; i--) {
				Pair pair = transaction.get(i);
				int item = pair.item;
				double uo = pair.utility / transactionUtility;

				// Tạo UOList nếu chưa có
				GUOList guoList = mapItemToGUOIL.computeIfAbsent(item, k -> new GUOList(item));

				// Thêm Element(uo, ruo, nextItem, nextIndex)
				Element element = new Element(uo, ruo, nextItem, nextIndex);
				guoList.addElement(element);

				// Cập nhật cho item tiếp theo
				nextItem = item;
				nextIndex = guoList.size() - 1;
				ruo += uo;
			}
		}

    	return mapItemToGUOIL;
	}

	/**
	 * Method to mine high utility occupancy patterns recursively using the indexed list structure.
	 * @param prefix the current prefix being extended
	 * @param mapItemToGUOList a map of items to their GUO-IL (Global Utility Occupancy Indexed List)
	 * @param minSup the minimum support threshold
	 * @param minUtilOcc the minimum utility occupancy threshold
	 * @throws IOException if an error occurs while writing to the output file
	 */
	public void mine(int[] prefix, int prefixLength, Map<Integer, GUOList> mapItemToGUOList,
					int minSup, double minUtilOcc, GUOList baseList, int deep) throws IOException {

		final Map<Element, Element> reversePrefixMap = (baseList!=null) ? buildReversePrefixMap(baseList, mapItemToGUOList) : null;
		if(deep == 1){
			ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			for (Map.Entry<Integer, GUOList> entry : mapItemToGUOList.entrySet()) {
				int item = entry.getKey();
				GUOList guoList = entry.getValue();
				int supP = guoList.size();
				executor.submit(() -> {
					try {
						
						GUOList baseListkp1 = null;
						Map<Integer, GUOList> mapItemToCUOkp1 = new HashMap<>();
						if(supP >= minSup){
							if(guoList.sumUO/supP >= minUtilOcc){

								writeOut(prefix, prefixLength,guoList.item, guoList.size(), guoList.sumUO/guoList.size());
							
							}
							double lubuoP = (guoList.sumUO + guoList.sumRUO)/supP;
							if(lubuoP >= minUtilOcc){
								if(guoList.computeUBUO(minSup)> minUtilOcc){
									prefix[prefixLength] = item;

									baseListkp1 = new GUOList(item);

									for(int i = 0; i < guoList.size() ;i++){
										Element e  = guoList.get(i);
										int nextIndex = e.nextIndex;
										int nextItem = e.nextItem;
										Element prevEntry= new Element(e.uo, e.ruo, -1, -1); // Khởi tạo prevEntry để theo dõi chuỗi liên kết


										if(nextItem!=-1 && nextIndex != -1)baseListkp1.addElement(prevEntry);
										while(nextIndex != -1 && nextIndex != -1){
											
											
											GUOList nextGuoList = mapItemToGUOList.get(nextItem);
											if (nextGuoList == null) break;

											if (nextIndex >= nextGuoList.size()) break;
												Element ce = nextGuoList.get(nextIndex);

											GUOList newCUO = mapItemToCUOkp1.get(nextItem);
											if (newCUO == null) {
												newCUO = new GUOList(nextItem);
												mapItemToCUOkp1.put(nextItem, newCUO);
											}

											double peUO = 0.0;
											Element prefixEntry = (reversePrefixMap!= null) ? reversePrefixMap.get(e) : null;
											peUO = (prefixEntry != null) ? prefixEntry.uo : 0.0;
											
											//System.out.println("peUO = " + peUO);

											double neUO = e.uo + ce.uo - peUO;
											double neRUO = ce.ruo;
											//System.out.println("eUO:" + e.uo + " ceUO:"+ ce.uo + " neUO:" + neUO);
											Element ne = new Element(neUO, neRUO, -1, -1);
											newCUO.addElement(ne);
											
											if (prevEntry != null) {
												prevEntry.nextItem = nextItem;
												prevEntry.nextIndex = newCUO.elements.size()-1;
											}
											//System.out.println(newCUO);
											prevEntry = ne;

											nextIndex = ce.nextIndex;
											nextItem = ce.nextItem;
											
										}
									}

								}
								
							}
						}
						if(!mapItemToCUOkp1.isEmpty()){
								mine(prefix, prefixLength+1, mapItemToCUOkp1, minSup, minUtilOcc, baseListkp1, deep + 1);
									
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else{
					mine(itemsetBuffer,prefixLength, mapItemToGUOList, minSup, minUtilOcc, baseList);

		}
	}

	public void mine(int[] prefix,int prefixLength, Map<Integer, GUOList> mapItemToGUOList, int minSup, double minUtilOcc,GUOList baseList) throws IOException {
		

		Map<Element, Element> reversePrefixMap = null;
		if(baseList!=null) reversePrefixMap = buildReversePrefixMap(baseList, mapItemToGUOList);
	

		// Duyệt qua từng mục trong mapItemToUOList để mở rộng tiền tố
		for (Map.Entry<Integer, GUOList> entry : mapItemToGUOList.entrySet()) {
			int item = entry.getKey();
			GUOList guoList = entry.getValue();
			int supP = guoList.size();
			GUOList baseListkp1 = null;
			Map<Integer, GUOList> mapItemToCUOkp1 = new HashMap<>();

			if(supP >= minSup){
				if(guoList.sumUO/supP >= minUtilOcc){
					writeOut(prefix, prefixLength,guoList.item, guoList.size(), guoList.sumUO/guoList.size());
				}

				double lubuoP = (guoList.sumUO + guoList.sumRUO)/supP;
				if(lubuoP >= minUtilOcc){
					if(guoList.computeUBUO(minSup)> minUtilOcc){
						prefix[prefixLength] = item;
						baseListkp1 = new GUOList(item);

						for(int i = 0; i < guoList.size() ;i++){
							Element e  = guoList.get(i);
							int nextIndex = e.nextIndex;
							int nextItem = e.nextItem;
							Element prevEntry= new Element(e.uo, e.ruo, -1, -1); 

							if(nextItem!=-1 && nextIndex != -1)baseListkp1.addElement(prevEntry);
							while(nextIndex != -1 && nextIndex != -1){
								
								GUOList nextGuoList = mapItemToGUOList.get(nextItem);
								if (nextGuoList == null) break;

								if (nextIndex >= nextGuoList.size()) break;
									Element ce = nextGuoList.get(nextIndex);

								GUOList newCUO = mapItemToCUOkp1.get(nextItem);
								if (newCUO == null) {
									newCUO = new GUOList(nextItem);
									mapItemToCUOkp1.put(nextItem, newCUO);
								}

								double peUO = 0.0;
								Element prefixEntry = (reversePrefixMap!= null) ? reversePrefixMap.get(e) : null;
								peUO = (prefixEntry != null) ? prefixEntry.uo : 0.0;

								double neUO = e.uo + ce.uo - peUO;
								double neRUO = ce.ruo;

								Element ne = new Element(neUO, neRUO, -1, -1);
								newCUO.addElement(ne);
								
								if (prevEntry != null) {
									prevEntry.nextItem = nextItem;
									prevEntry.nextIndex = newCUO.elements.size()-1;
								}

								prevEntry = ne;

								nextIndex = ce.nextIndex;
								nextItem = ce.nextItem;
								
							}
						}

					}
					
				}
			}
			if(!mapItemToCUOkp1.isEmpty()){
					mine(prefix, prefixLength+1, mapItemToCUOkp1, minSup, minUtilOcc, baseListkp1);
						
			}
		}



	}

	public static Map<Element, Element> buildReversePrefixMap(
		GUOList baseList,
		Map<Integer, GUOList> mapItemToGUOList
	) {
		Map<Element, Element> map = new HashMap<>();

		for (Element baseE : baseList.elements) {
			int nextItem = baseE.nextItem;
			int nextIndex = baseE.nextIndex;

			while (nextItem != -1 && nextIndex != -1) {
				GUOList guo = mapItemToGUOList.get(nextItem);
				if (guo == null || nextIndex >= guo.size()) break;

				Element current = guo.get(nextIndex);
				map.put(current, baseE);

				nextItem = current.nextItem;
				nextIndex = current.nextIndex;
			}
		}

		return map;
	}

	
	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, int sup, double uo) throws IOException {
		synchronized (writer) {
			huiCount++; // nếu biến này cũng dùng chung -> nên synchronized hoặc dùng AtomicInteger

			StringBuilder buffer = new StringBuilder();
			for (int i = 0; i < prefixLength; i++) {
				buffer.append(prefix[i]).append(' ');
			}
			buffer.append(item);
			buffer.append(" #SUP: ").append(sup);
			buffer.append(" #UO: ").append(uo);
			
			writer.write(buffer.toString());
			writer.newLine();
		}
	}

	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  HOUMIL ALGORITHM - STATS =============");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" High-utility itemsets count : " + huiCount); 
	}
}