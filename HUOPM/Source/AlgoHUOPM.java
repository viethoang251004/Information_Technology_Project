/* This file is copyright (c) 2008-2015 Philippe Fournier-Viger
* 
* This file is part of the SPMF DATA MINING SOFTWARE
* (http://www.philippe-fournier-viger.com/spmf).
* 
* SPMF is free software: you can redistribute it and/or modify it under the
* terms of the GNU General Public License as published by the Free Software
* Foundation, either version 3 of the License, or (at your option) any later
* version.
* 
* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* SPMF. If not, see <http://www.gnu.org/licenses/>.
*/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is an implementation of the "FHN" algorithm for High-Utility Itemsets 
 * Mining with negative profit values
 * as described in the conference paper : <br/><br/>
 * 
 * Fournier-Viger, P. (2014). FHN: Efficient Mining of High-Utility Itemsets with 
 * Negative Unit Profits. Proc. 10th International Conference on Advanced Data 
 * Mining and Applications (ADMA 2014), Springer LNCS 8933, pp. 16-29.
 * 
 * @see UtilityListFHN
 * @see ElementFHN
 * @author Philippe Fournier-Viger
 */
public class AlgoHUOPM {

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
	
	/** writer to write the output file  */
	BufferedWriter writer = null;  
	
	/** The eucs structure:  key: item   key: another item   value: twu */
	Map<Integer, FUtable> mapFMAP;  

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
	public AlgoHUOPM() {
		
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
		
		// Create the EUCP structure as described in the FHM and FHN papers
		mapFMAP =  new HashMap<Integer, FUtable>();
		
		// record the start time of the algorithm
		startTimestamp = System.currentTimeMillis();
	
		
		writer = new BufferedWriter(new FileWriter(output));

		//  We create a  map to store the TWU of each item
		mapItemToSup = new HashMap<Integer, Integer>();

		

		// We scan the database a first time to calculate the TWU of each item.
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
				DataBaseSize= DataBaseSize + 1; // increase the size of the database
				// split the transaction according to the : separator
				String split[] = thisLine.split(":"); 
				// the first part is the list of items
				String items[] = split[0].split(" "); 
				//===================== FHN ===========================
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");
				//===============================================
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);  
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					
					
					// get the current TWU of that item
					Integer sup = mapItemToSup.get(item);
					// update the twu of that item
					sup = (sup== null)? 
							1 : sup + 1;
					mapItemToSup.put(item, sup);
				}
				// increase the size of the database
				
			}
		
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		
		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS WITH TWU  >= MIN_UTILITY.
		List<UOList> listOfUOLists = new ArrayList<UOList>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, UOList> mapItemToUOList = new HashMap<Integer, UOList>();

		// For each item
		for(Integer item: mapItemToSup.keySet()){
			// if the item is promising  (TWU >= minutility)
			if(mapItemToSup.get(item) >= (DataBaseSize * alpha)) {
				// create an empty Utility List that we will fill later.
				UOList uList = new UOList(item);
				mapItemToUOList.put(item, uList);
				// add the item to the list of high TWU items
				listOfUOLists.add(uList); 
				
			}
		}
		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUOLists, new Comparator<UOList>(){
			public int compare(UOList o1, UOList o2) {
				// compare the TWU of the items
				return compareItems(o1.item, o2.item);
			}
			} );
		
		// SECOND DATABASE PASS TO CONSTRUCT THE UTILITY LISTS 
		// OF 1-ITEMSETS  HAVING TWU  >= minutil (promising items)
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			// variable to count the number of transaction
			int tid =0;
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
							
				// Copy the transaction into lists but 
				// without items with TWU < minutility
				
				int remainingUtility =0;
				 
				
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
						remainingUtility += pair.utility; // add its utility to the remaining utility
						//================================================
					}
				}
				
				// sort the transaction
				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}});

								
				// for each item left in the transaction
				for(int i = 0; i< revisedTransaction.size(); i++){
					Pair pair =  revisedTransaction.get(i);
					
					// subtract the utility of this item from the remaining utility
				
					remainingUtility = remainingUtility - pair.utility;
											
					
					// get the utility list of this item
					UOList UOListOfItem = mapItemToUOList.get(pair.item);
					
					Element element = new Element(tid, pair.utility*1.0/transactionUtility, remainingUtility*1.0/transactionUtility);
					UOListOfItem.addElement(element);
				}
				tid++; // increase tid number for next transaction
				
			}


			for(UOList ulist:listOfUOLists){
					FUtable mapFMAPItem = new FUtable(ulist.item, ulist.getSupport(), ulist.getAverageUO(), ulist.getAverageRUO());
					mapFMAP.put(ulist.item, mapFMAPItem);
					//ulist.print();
					// END OPTIMIZATION of FHM
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
		huopm(itemsetBuffer, 0, null, listOfUOLists, alpha, beta, DataBaseSize);
		
		// check the memory usage again and close the file.
		MemoryLogger.getInstance().checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
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
	
	/**
	 * This is the recursive method to find all high utility itemsets. It writes
	 * the itemsets to the output file.
	 * @param prefix  This is the current prefix. Initially, it is empty.
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param minUtility The minUtility threshold.
	 * @param prefixLength The current prefix length
	 * @throws IOException
	 */
	private void huopm(int [] prefix, 
			int prefixLength, UOList pUL, List<UOList> ULs, double alpha, double beta, int DataBaseSize)
			throws IOException {
		
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			UOList X = ULs.get(i);
			
			// If pX is a high utility itemset.
			// we save the itemset:  pX 
			if(X.getSupport() >= DataBaseSize*alpha){
				// save to file
				if(X.getAverageUO() >= beta) writeOut(prefix, prefixLength, X.item, X.getSupport(), X.getAverageUO());
			}
			
			// If the sum of the remaining utilities for pX
			// is higher than minUtility, we explore extensions of pX.
			// (this is the pruning condition)
			int minSup = (int) (DataBaseSize * alpha);
			if(X.getUpperBound(minSup) >= beta){
				// This list will contain the utility lists of pX extensions.
				List<UOList> exULs = new ArrayList<UOList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					UOList Y = ULs.get(j);
					
					// =========================== END OF NEW OPTIMIZATION
					
					// we construct the extension pXY 
					// and add it to the list of extensions of pX
					UOList temp = construct(pUL, X, Y, alpha, beta);
					
					if(temp != null && temp.getSupport() >= ((int)(alpha * DataBaseSize))){
						exULs.add(temp);
					}
				}
				// We create new prefix pX
				itemsetBuffer[prefixLength] = X.item;
				
				// We make a recursive call to discover all itemsets with the prefix pXY
				huopm(itemsetBuffer, prefixLength+1, X, exULs, alpha, beta, DataBaseSize); 
			}
		}
		MemoryLogger.getInstance().checkMemory();
	}
	
	/**
	 * This method constructs the utility list of pXY
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @param minUtility : the minimum utility threshold
	 * @return the utility list of pXY
	 */
	private UOList construct(UOList P, UOList px, UOList py, double alpha, double beta) {
		// create an empy utility list for pXY
		UOList pxyUL = new UOList(py.item);
		
		//== new optimization - LA-prune  == /
		// Initialize the sum of total utility
		int Sup = px.getSupport();
		// ================================================
		
		// for each element in the utility list of pX
		for(Element ex : px.elements){
			// do a binary search to find element ey in py with tid = ex.tid
			Element ey = findElementWithTID(py, ex.tid);
			if(ey == null){
				//== new optimization - LA-prune == /
				if(ENABLE_LA_PRUNE) {
					Sup -= 1;
					if(Sup < (int)(alpha * DataBaseSize)) {
						return null;
					}
				}
				// =============================================== /
				continue;
			}
			// if the prefix p is null
			if(P == null){
				// Create the new element
				Element eXY = new Element(ex.tid, ex.uo + ey.uo, ey.ruo);
				// add the new element to the utility list of pXY
				pxyUL.addElement(eXY);
				
			}else{
				// find the element in the utility list of p wih the same tid
				Element e = findElementWithTID(P, ex.tid);
				if(e != null){
					// Create new element
					Element eXY = new Element(ex.tid, ex.uo + ey.uo - e.uo,
								ey.ruo);
					// add the new element to the utility list of pXY
					pxyUL.addElement(eXY);
				}
			}	
		}
		// return the utility list of pXY.
		return pxyUL;
	}
	
	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element findElementWithTID(UOList ulist, int tid){
		List<Element> list = ulist.elements;
		
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;
       
        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);
            }
        }
		return null;
	}

	/**
	 * Method to write a high utility itemset to the output file.
	 * @param the prefix to be writent o the output file
	 * @param an item to be appended to the prefix
	 * @param utility the utility of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(int[] prefix, int prefixLength, int item, int sup, double uo) throws IOException {
		huiCount++; // increase the number of high utility itemsets found
		
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		// append the utility value
		buffer.append(" #SUP: ");
		buffer.append(sup);
		buffer.append(" #UO: ");
		buffer.append(uo);
		// write to file
		writer.write(buffer.toString());
		writer.newLine();
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 * @throws IOException 
	 */
	public void printStats() throws IOException {
		System.out.println("=============  HOUPM ALGORITHM - STATS =============");
		System.out.println(" Total time ~ "                  + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ "                      + MemoryLogger.getInstance().getMaxMemory() + " MB");
		System.out.println(" High-utility itemsets count : " + huiCount); 
	}
}