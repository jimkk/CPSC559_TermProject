import java.io.*;

public class IOUtilities {

	
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
	

	public static String rebuildString(String [] parts, int start, int end){
		StringBuffer buffer = new StringBuffer();
		for(int i = start; i < end; i++){
			if(i != start){buffer.append(" ");}
			buffer.append(parts[i]);
		}
		return buffer.toString();
	}

}
