import com.google.gson.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class SocketDeserializer implements JsonDeserializer<Socket>{

	@Override
		public Socket deserialize(JsonElement json, Type typeOfSrc, JsonDeserializationContext context){
			String combined = json.getAsJsonObject().get("Socket").getAsString();
			String address = combined.split("/")[1];
			int port = Integer.parseInt(combined.split("/")[2]);
			try{
				return new Socket(address, port);
			} catch (Exception e){e.printStackTrace();}
			return null;
		}

}
