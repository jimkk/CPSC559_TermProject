import com.google.gson.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class SocketDeserializer implements JsonDeserializer<Socket>{

	@Override
	/**
	 * An custom deserializer for Socket objects. The GSON library cannot handle
	 * circular references so custom serialization is required for our purposes.
	 * @param JsonElement
	 * @param typeOfSrc
	 * @param context
	 * @return Socket - the recreated socket
	 */
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
