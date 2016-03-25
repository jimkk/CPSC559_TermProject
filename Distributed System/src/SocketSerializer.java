import com.google.gson.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

public class SocketSerializer implements JsonSerializer<Socket>{

	@Override
	public JsonElement serialize(Socket src, Type typeOfSrc, JsonSerializationContext context){
		JsonObject object = new JsonObject();
		String convertion = src.getInetAddress().toString() + "/" + src.getPort();
		object.addProperty("Socket", convertion);
		return object;
	}

}
