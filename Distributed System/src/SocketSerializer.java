import com.google.gson.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class SocketSerializer implements JsonSerializer<Socket>{

	@Override
	
	/**
	 * An custom serializer for Socket objects. The GSON library cannot handle
	 * circular references so custom serialization is required for our purposes.
	 * @param Socket
	 * @param typeOfSrc
	 * @param context
	 * @return JsonElement - the representation of the Socket
	 */
	public JsonElement serialize(Socket src, Type typeOfSrc, JsonSerializationContext context){
		JsonObject object = new JsonObject();
		String convertion = src.getInetAddress().toString() + "/" + src.getPort();
		object.addProperty("Socket", convertion);
		return object;
	}

}
