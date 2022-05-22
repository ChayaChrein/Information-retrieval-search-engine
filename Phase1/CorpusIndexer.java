package projectPhase1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

public class CorpusIndexer {

	public static void main(String args[]) throws IOException {
		
		String sw_in = null; //flag -in
		String search = null; //-search
		String out_file = null; //-output
		String print = null; //-print
		String file_path = null;
		
		for (int a=0; a<args.length; a++) {
			if(args[a].equals("-in")) {
				sw_in=args[a+1]; //set the stopword input file
				a++;
			}
			else if(args[a].equals("-output")) {
				out_file=args[a+1]; //set the output file name
				a++;
			}
			else if(args[a].equals("-search")) {
				search=args[a+1]; //set the word or document name to be searched
				a++;
			}
			else if(args[a].equals("-print")) {
				print=args[a+1]; //set the word or document name to be printed	
				a++;
			}
			else if(args[a].equals("-path")) {
				file_path=args[a+1]; //set the word or document name to be printed	
				a++;
			}
		}
		
		HashMap<String,Integer> stopWords = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(sw_in));
		String line = br.readLine();
		int counter=0;
		
		//input all words from stopword file into a hashmap with the key and value being the word and number respectively
		while (line!=null) {
				stopWords.put(line, counter);					
				line = br.readLine();
				counter++;
			}
		br.close();
			
		//set the names of all files in the corpus
		File[] corpus = new File(file_path).listFiles();
	
		HashMap<String, Vector<Mapping>> index = new HashMap<String, Vector<Mapping>>();

		//iterate through all 200 files in the corpus
		for(int i1=0;i1<200;i1++) {
			br = new BufferedReader (new FileReader(corpus[i1].getAbsolutePath()));
			line = br.readLine();
			int wordCount = 1; //track the location of each word- how many words from the first word in the doc
			
			//iterate through each line by splitting it into an array of words and indexing each word
			while(line != null) {
				String[] words = line.split(" ");
				
				//iterate through each word on the line
				for (int i2=0; i2<words.length; i2++) {
					//do nothing if the word is a stopword
					if (stopWords.containsKey(words[i2])); 
					//if the word is already in the index add another Mapping (doc name and location) to the value)
					else if (index.containsKey(words[i2])) index.get(words[i2]).add(new Mapping(corpus[i1].getName(),wordCount));
					//if word isn't yet in index put it and add its location
					else {
						index.put(words[i2], new Vector<Mapping>());
						index.get(words[i2]).add(new Mapping(corpus[i1].getName(),wordCount));
					}
					wordCount++;
				}//for loop i2
				
				line = br.readLine();
			}//while loop
			
			br.close();
		}//for loop i1
		
		//execute search query
		if (search !=null) {
			if (search.contains(".html")) searchDoc(index, search, out_file);
			else searchWord(index,search,out_file,corpus);
		}
		
		//execute print query
		if (print !=null) {
			if (print.contains(".html")) printDoc(index, print, out_file);
			else printWord(index,print,out_file);
		}
		
	}//main
	
	//returns documents and number of appearneces of a specific word
	public static void searchWord(HashMap<String, Vector<Mapping>> hm, String word, String outFile, File[] f) throws IOException {
		@SuppressWarnings("unchecked")
		File file = new File(outFile);
		FileWriter fw = new FileWriter(file);
		
		Vector<Mapping> vector = (Vector<Mapping>) hm.get(word);
		int counter=0;
		
		//iterate through vector and for each occurence in a specific document, increment the array cell corresponding to it by 1
		for(int i=0;i<vector.size();i++) {
			String placeholder=vector.get(i).fileName;
			counter=1;
			fw.write(placeholder+": ");
				for (int j=i+1; j<vector.size() && placeholder.equals(vector.get(j).fileName);j++) {
					counter++;
					i++;
				}
				fw.write(counter+"\n");
		}
		fw.close();
	}//searchWord
	
	//returns all words found in the document specified and how many times
	public static void searchDoc(HashMap<String, Vector<Mapping>> hm,String doc, String outFile) throws IOException {
		File file = new File(outFile);
		FileWriter fw = new FileWriter(file);
		
		//iterate through each key/word in the hashmap and see if that document appears in it's list of values and count how many times
		hm.forEach((k,v)->{
		try {
			for(int i=0; i<v.size() ; i++) {
				if (v.get(i).fileName.equals(doc)) {
					fw.write(k+": "); 
					int count=0;
					while (i<v.size() && v.get(i).fileName.equals(doc)) {
						count++;
						i++;
					}
					fw.write(count+"\n");
				}
			}
		} catch (IOException e) {}});
		fw.close();
	}//searchDoc
	
	//prints all values associated with a give key/word
	public static void printWord(HashMap<String, Vector<Mapping>> hm,String word, String outFile) throws IOException {
		File file = new File(outFile);
		FileWriter fw = new FileWriter(file);
		
		Vector<Mapping> vector = (Vector<Mapping>) hm.get(word);
		int[] counter = new int[vector.size()];
		for(int i=0;i<vector.size();i++) {
			fw.write(vector.get(i)+"\n");
		}
		fw.close();	
	}
	
	//prints all information stored regarding a specific document
	public static void printDoc(HashMap<String, Vector<Mapping>> hm,String doc, String outFile) throws IOException {
		File file = new File(outFile);
		FileWriter fw = new FileWriter(file);
		
		hm.forEach((k,v)->{
		try {
			for(int i=0; i<v.size() ; i++) {
				if (v.get(i).fileName.equals(doc)) {
					fw.write(k+": "); 
					while (i<v.size() && v.get(i).fileName.equals(doc)) {
						fw.write(v.get(i)+", ");
						i++;
					}
					fw.write("\n\n");
				}
			}
		} catch (IOException e) {}});
		fw.close();
	}
}

