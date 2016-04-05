import java.io.*;

public class IOUtilities {


	/**
	 * Reads characters from an input stream until it reaches the end
	 * of the file or a newline character is hit
	 * @param in The input stream reader to read from
	 */	
	public static String read(InputStreamReader in){
		try{
			StringBuffer buffer = new StringBuffer();
			int c;
			while((c = in.read()) != -1){
				if(c == (int) '\n'){
					break;
				}
				buffer.append((char) c);
			}
			return buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Rebuilds a string array into a single string.
	 * @param parts The string array to rebuild
	 * @param start The start index (inclusive)
	 * @param end The end index (exclusive)
	 * @return String - The newly created string
	 */
	public static String rebuildString(String [] parts, int start, int end){
		StringBuffer buffer = new StringBuffer();
		for(int i = start; i < end; i++){
			if(i != start){buffer.append(" ");}
			buffer.append(parts[i]);
		}
		return buffer.toString();
	}

}
