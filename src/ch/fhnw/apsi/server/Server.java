package ch.fhnw.apsi.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fhnw.apsi.server.handler.CheckLoginHandler;
import ch.fhnw.apsi.server.handler.IndexHandler;
import ch.fhnw.apsi.server.handler.LoggedInHandler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
	public static final String INDEXPAGE = "web/index.html";
	public static final String LOGGEDINPAGE = "web/loggedIn.html";

	private static Map<String, String> userpw_map = new HashMap<>();
	private static List<PermanentCookie> permanentCookies = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		initUserPwMap();
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/test", new MyHandler());
		server.createContext("/index", new IndexHandler());
		server.createContext("/checkLogin", new CheckLoginHandler());
		server.createContext("/loggedIn", new LoggedInHandler());
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	private static void initUserPwMap() {
		userpw_map.put("hansmueller@gmx.com", "qwertz12");
		userpw_map.put("bernd@asp.ch", "asdfgh21");
		userpw_map.put("sepp@dot.de", "yxcvbn12");
		userpw_map.put("olli@swisscom.ch", "qwertzui");
		userpw_map.put("andrea@bluewin.ch", "qwer");
	}

	public static List<PermanentCookie> getCookies() {
		return permanentCookies;
	}

	public static boolean checkCookie(Headers headers) {
		for (String s : headers.get("Cookie")) {
			String[] name_value = s.split("=");
			for (int i = 0; i < name_value.length; i += 2) {
				// if(name_value.length > i+2 && name_value[i+2].startsWith("SESSION"))
			}
			System.out.println("Check: " + s);
			if (checkCookie(name_value[0], name_value[1].replace("\"", ""))) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkCookie(String name, String value) {
		int i = 1;
		for (PermanentCookie cookie : permanentCookies) {
			System.out.println("Existing Cookie: " + cookie.getName() + ", "
					+ cookie.getValue() + " " + i++);
			if (cookie.getName().equals(name) && cookie.getValue().equals(value)
					&& !cookie.hasExpired()) {
				return true;
			}
		}
		return false;
	}

	public static PermanentCookie getCookie(int id, String name) {
		for (PermanentCookie cookie : permanentCookies) {
			System.out.println("GetCookie: " + cookie.getName());
			if (name.equals(cookie.getName())) {
				return cookie;
			}
		}
		return null;
	}

	public static void addCookie(String id, String name, String value, int maxAge) {
		PermanentCookie cookie = new PermanentCookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		permanentCookies.add(cookie);
	}

	public static void removeCookie(int id, String name) {
		PermanentCookie cookie = getCookie(id, name);
		permanentCookies.remove(cookie);
	}

	public static void setDefaultHeaders(Headers h) {
		h.add("Content-Type", "text/html;charset=utf-8");
	}

	static class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			String response = "Hello World";
			Headers h = t.getResponseHeaders();
			setDefaultHeaders(h);
			t.sendResponseHeaders(200, 0); // response.getBytes().length
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public static Map<String, String> getUserpw_map() {
		return userpw_map;
	}

}
