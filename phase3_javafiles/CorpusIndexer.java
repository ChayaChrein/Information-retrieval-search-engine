// Chaya Chrein
/**
 * CorpusIndexer indexes a corpus in a hashmap, storing every word and its mappings (where the word was found)
 * stemming could be applied if the user wishes when querying the corpus
 * 
 * command line arguments: -in "stopWord_file" -path "/.../.../Corpus" -stemming "on" or "off" -search "query" -snip "size"
 */

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CorpusIndexer implements Serializable{

	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String args[]) throws IOException {
		
		String sw_in = null; //flag -in stopwordFile
		String search[] = new String[20]; //-search query
		String searchFile = null; //-searchfile name
		String file_path = null; //-path
		boolean apply_stemming = false; //-stemming
		int snip_size = 5; //-snip
		String out_type = null; //-output: GUI both or TXT
		String out_file = "searchResults.txt"; //-outfile name can be overwritten
		boolean displayGUI = false; //set default display
		boolean displayTXT = true; //set default display
		
		for (int a=0; a<args.length; a++) {
			if(args[a].equals("-in")) {
				sw_in=args[a+1]; //set the stopword input file
				a++;
			}
			else if(args[a].equals("-search")) {
				search[0]=args[a+1]; //set the word or document name to be searched
				a++;
			}
			else if(args[a].equals("-searchfile")) {
				searchFile=args[a+1]; //set the word or document name to be searched
				a++;
			}
			else if(args[a].equals("-path")) {
				file_path=args[a+1]; //set the word or document name to be printed	
				a++;
			}
			else if(args[a].equals("-stemming")) {
				if (args[a+1].equals("on")) apply_stemming = true;
				a++;
			}
			else if(args[a].equals("-snip")) {
				snip_size=Integer.parseInt(args[a+1]);
				a++;
			}
			else if(args[a].equals("-output")) {
				out_type=args[a+1];
				a++;
				if (out_type.equals("GUI")) {
					displayGUI = true;
					displayTXT = false;
				}
				if (out_type.equals("both")) {
					displayGUI = true;
					displayTXT = true;
				}
			}
			else if(args[a].equals("-outfile")) {
				out_file=args[a+1];
				a++;
			}
		}
		
		//create stopWord HashMap
		HashMap<String,Integer> stopWords = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(sw_in));
		String line = br.readLine();
		int counter=0;
		
		//input all words from stopword file into a hashmap with the key and value being the word and number respectively
		while (line!=null) {
				stopWords.put(line.toLowerCase(), counter);					
				line = br.readLine();
				counter++;
			}
		br.close();
		
			
		//set the names of all files in the corpus
		File[] corpus = new File(file_path).listFiles();
		//create the HashMap for the indexed Corpus
		HashMap<String, Vector<Mapping>> index = new HashMap<>();
		
		//file for serialization
		boolean serialized = false;
		String filename = "seri.ser";
		//Deserialization - try to read in the index if it was already created. If not then make the index
		try {
			FileInputStream file = new FileInputStream(filename); 
			ObjectInputStream in = new ObjectInputStream(file);
			index = (HashMap<String, Vector<Mapping>>)in.readObject();
			in.close();
			file.close();
			serialized = true;
			System.out.println("SUCCESSFULLY DESIRIALIZED");
		}
		
		//if we couldn't deserialize, index the corpus
		catch(Exception e) {
			//iterate through all 200 files in the corpus
			for(int i1=0;i1<200;i1++) {
				br = new BufferedReader (new FileReader(corpus[i1].getAbsolutePath()));
				line = br.readLine().toLowerCase();
				int wordCount = 1; //track the location of each word- how many words from the first word in the doc
				
				//iterate through each line by splitting it into an array of words and indexing each word
				while(line != null) {
					String[] words = line.split(" ");
					
					//iterate through each word on the line
					for (int i2=0; i2<words.length; i2++) {
						//do nothing if the word is a stopword
						if (stopWords.containsKey(words[i2])); 
						//if the word is already in the index add another Mapping (doc name and location) to the value)
						else if (index.containsKey(words[i2])) {
							index.get(words[i2]).add(new Mapping(corpus[i1].getName(),wordCount));
							
							//stem the word and add it and/or its mapping to the index if stem is different than the original word
							PorterStemmer stemmer = new PorterStemmer();
							char[] temp = words[i2].toCharArray();
							for(int j=0; j<words[i2].length(); j++) stemmer.add(temp[j]);
							stemmer.stem();
							String stem = stemmer.toString();
							
							//add stem word to index only if stem is different from the original word
							if (stem.equals(words[i2]));
							else if (index.containsKey(stem)) index.get(stem).add(new Mapping(corpus[i1].getName(),wordCount));
							else {
								index.put(stem, new Vector<Mapping>());
								index.get(stem).add(new Mapping(corpus[i1].getName(),wordCount));
							}
							
						}
						//if word isn't yet in index put it and add its location
						else {
							index.put(words[i2], new Vector<Mapping>());
							index.get(words[i2]).add(new Mapping(corpus[i1].getName(),wordCount));
							
							PorterStemmer stemmer = new PorterStemmer();
							char[] temp = words[i2].toCharArray();
							for(int j=0; j<words[i2].length(); j++) stemmer.add(temp[j]);
							stemmer.stem();
							String stem = stemmer.toString();
							
							//add stem word to index only if stem is different from the original word
							if (stem.equals(words[i2]));
							else if (index.containsKey(stem)) index.get(stem).add(new Mapping(corpus[i1].getName(),wordCount));
							else {
								index.put(stem, new Vector<Mapping>());
								index.get(stem).add(new Mapping(corpus[i1].getName(),wordCount));
							}
						}
						wordCount++;
					}//for loop i2
					
					line = br.readLine();
				}//while loop
				
				br.close();
			}//for loop i1
		}//catch block
		
		//Serialization Code
		if (!serialized) {
			try {
				FileOutputStream file = new FileOutputStream(filename); 
				ObjectOutputStream out = new ObjectOutputStream(file);
				out.writeObject(index);
				out.close();
				file.close();
				System.out.println("SUCCESSFULLY SERIALIZED");
			}
			catch (Exception e) {
				System.out.println("COULD NOT SERIALIZE");
			}
		}//if !seralized
		
		//execute search query for all words in the query but not the stop words
		StringBuilder queryResponse = new StringBuilder();
		int queries=0;
		if (search[0] == null && searchFile == null) System.out.println("nothing to search for");
		if (searchFile !=null) {
			BufferedReader br2 = new BufferedReader(new FileReader(searchFile));
			String line2 = br2.readLine();	
			
			//read in each query
			while (line2!=null) {
					search[queries]=line2;
					line2 = br2.readLine();
					queries++;
				}
			br2.close();
			
		}
		

		//iterate through all the queries if multiple were submitted by  a word doc
		for (int q=0; q<queries; q++) {
			queryResponse.append("\n\n\n"+search[q]+"\n");
			String[] words = search[q].split(" ");
			//store firstWord searched for to return snippets
			String firstWord=null;
			for (int w=0; w<words.length; w++) {
				if (stopWords.containsKey(words[w]));
				else{firstWord=words[w];
				break;}
			}
			//store the vector of documents returned for each word searched
			ArrayList<Vector<String>> docsFound = new ArrayList<Vector<String>>();
			int swCount =0;
			for (int w=0; w<words.length; w++) {
				if (stopWords.containsKey(words[w])) swCount++;
				else {
					if (index.get(words[w])==null) {
						System.out.println("NO RESULTS FOUND");
						return;
					}
					if (!apply_stemming) docsFound.add(searchWord(index,words[w]));
					if (apply_stemming) docsFound.add(searchWordStemming(index,words[w]));	
				}
			}
	
			//check if all words in query were stopwords
			if (words.length==swCount) {
				System.out.println("NO RESULTS FOUND. ALL QUERY WORDS WERE STOP WORDS");
				return;
			}
		
		
		//Find the docs that are in common to all the words of the query and print them out
		Vector<String> docsToReturn = docsFound.get(0);
		if (words.length>1) docsToReturn = commonDocs(docsFound);
		for (int i=0; i<docsToReturn.size(); i++) {
			queryResponse.append(docsToReturn.get(i)+"\n");
			queryResponse.append("\'"+ findSnippet(index, docsToReturn.get(i), getFirstOccurrence(index, firstWord, docsToReturn.get(i)) ,snip_size)+"\'\n");
	
		}
		}
		
		//write the results to txtfile, gui, or both
		if (displayTXT) {
			FileOutputStream file = new FileOutputStream(out_file); 
			ObjectOutputStream out = new ObjectOutputStream(file);
			out.writeObject(queryResponse);
			System.out.println("WRITTEN");
			out.close();
			file.close();
		}
		if (displayGUI) {
			 JFrame frame = new JFrame("Search Results");
	         	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	         	frame.setSize(300,300);
	         	frame.setLayout(new GridLayout(1,1));
	         JTextArea ta = new JTextArea();
	         	ta.setEditable(false);
	         JScrollPane jScrollPane = new JScrollPane(ta);
				jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				frame.getContentPane().add(jScrollPane);	
			ta.setText(queryResponse.toString());
	        frame.setVisible(true);
		}
		
	}//main
	
	//find which documents every row of av have in common and return it in a vector
	public static Vector<String> commonDocs(ArrayList<Vector<String>> av){
		Vector<String> commons = new Vector<String>();
		boolean contained=false;
		for (int c=0; c<av.get(0).size(); c++) {
			for (int r=1; r<av.size(); r++) {
				if (av.get(0).get(c) != null)
					contained = av.get(r).contains(av.get(0).get(c));			
			}
			if (contained) commons.add(av.get(0).get(c));
		}		
		return commons;
	}
	
	//returns a vector of the documents in which a specific word is found
	public static Vector<String> searchWord(HashMap<String, Vector<Mapping>> hm, String word) throws IOException {
		@SuppressWarnings("unchecked")
		Vector<Mapping> vector = hm.get(word);
		Vector<String> docRecords = new Vector<String>();
		docRecords.add(vector.get(0).fileName);
		for(int i=1;i<vector.size();i++) {
			if (docRecords.get(docRecords.size()-1).equals(vector.get(i).fileName));
			else docRecords.add(vector.get(i).fileName);				
		}
		return docRecords;
	}//searchWord
	
	//returns a vector of the documents in which a specific word and its stem are found by using stem instead of word
	public static Vector<String> searchWordStemming(HashMap<String, Vector<Mapping>> hm, String word) throws IOException {
		@SuppressWarnings("unchecked")
		PorterStemmer stemmer = new PorterStemmer();
		char[] temp = word.toCharArray();
		for(int j=0; j<word.length(); j++) stemmer.add(temp[j]);
		stemmer.stem();
		String stem = stemmer.toString();
		
		Vector<Mapping> vector = hm.get(stem);
		Vector<String> docRecords = new Vector<String>();
		
		docRecords.add(vector.get(0).fileName);
		for(int i=1;i<vector.size();i++) {
			if (docRecords.get(docRecords.size()-1).equals(vector.get(i).fileName));
			else {docRecords.add(vector.get(i).fileName);	
			}
		}
		return docRecords;
	}
	
	//find the first occurrence of a given word in a given document - returns its offset number
	public static int getFirstOccurrence(HashMap<String, Vector<Mapping>> hm, String word, String doc) {
		Vector<Mapping> vector = hm.get(word);
		int c=0;
			while(!vector.get(c).fileName.equals(doc)) {				
				c++;
				if (c==vector.size()-1) return 0;
			}
			return vector.get(c).offset;
	}
	
	//returns the snippet we are looking for
	public static String findSnippet(HashMap<String, Vector<Mapping>> hm,String doc, int location, int snip) throws IOException {
		int lowestLocation = location-snip;
		int highestLocation = location+snip;
		String[] snippet = new String[snip*2+1];
		//iterate through the entire hashmap to find the words at offsets lowerstLocation-highestLocation and store each word in the snippet array
		hm.forEach((k,v)->{
			for(int i=0; i<v.size() ; i++) {
				if (v.get(i).fileName.equals(doc) && v.get(i).offset >=lowestLocation && v.get(i).offset<=highestLocation) {
					snippet[v.get(i).offset-lowestLocation]=k;
				}
			}
		});
		
		//turn the array into a string
	    StringBuilder sb = new StringBuilder();
	      for(int i = 0; i < snippet.length; i++) {
	         sb.append(snippet[i]);
	         sb.append(" ");
	      }
	      return sb.toString();
	}//findSnippet
	
}

