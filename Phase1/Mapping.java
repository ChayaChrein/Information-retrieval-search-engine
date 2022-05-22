package projectPhase1;
/* Class Mapping stores a filename together with an offset value
 * filename is type string and offset is type int
 * it has a constuctor and toString method
 * */

public class Mapping {
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
