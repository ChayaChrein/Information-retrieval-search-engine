//Chaya Chrein
/**
 * Mapping class stores a pair of (fileName, offset)
 * fileName- name of the file 
 * offset- number of words from the beginning of the document that a word was found
 * it implements serializable so that the hashmap can be serialized
 */
import java.io.Serializable;

public class Mapping implements Serializable{

	private static final long serialVersionUID = 1L;
	String fileName;
	int offset;
	
	//constructor
	public Mapping(String fn, int o) {
		fileName=fn;
		offset=o;
	}
	
	//converts mapping to String value
	public String toString() {
		return "[" + fileName + ": " + offset +"]";
	}
}
