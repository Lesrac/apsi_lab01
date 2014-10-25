package ch.fhnw.apsi.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Map;

import ch.fhnw.apsi.server.Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CheckLoginHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		URI uri = t.getRequestURI();
		System.out.println(uri);
		String uriS = java.net.URLDecoder.decode(uri.toString(), "UTF-8");
		String paramS = uriS.substring(uriS.indexOf("?") + 1);
		String[] parameters = paramS.split("&");

		int id = 0;
		String username = "";
		String password = "";
		for (String s : parameters) {
			if (s.startsWith("id")) {
				id = Integer.valueOf(s.substring(3));
			} else if (s.startsWith("user")) {
				username = s.substring(5);
			} else if (s.startsWith("password")) {
				password = s.substring(9);
			}
		}
		System.out.println("Id: " + id);
		System.out.println("username: " + username);
		System.out.println("password: " + password);
		Headers h = t.getResponseHeaders();
		if (checkUser(username, password)) {
			h.add("Location", "/loggedIn");
			byte[] encoded = Base64.getEncoder().encode(
					(username + password).getBytes()); // userAgent +
			String encodedStr = new String(encoded);
			try {
				String cookieName = "SESSION_COOKIE";
				Server.addCookie(username, "/loggedIn", cookieName, encodedStr, 1800);
				HttpCookie cookie = Server.getCookie(id, cookieName);
				h.add(
						"Set-Cookie",
						cookieName + "=\"" + cookie.getValue() + "\"; Version="
								+ cookie.getVersion() + "; Max-Age=" + cookie.getMaxAge());
					//			+ "; Path=" + cookie.getPath());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			h.add("Location", "/index");
		}
		t.sendResponseHeaders(302, 0); // response.getBytes().length
		OutputStream os = t.getResponseBody();
		os.close();
	}

	private boolean checkUser(String username, String password) {
		Map<String, String> usermap = Server.getUserpw_map();
		if (usermap.containsKey(username)) {
			if (usermap.get(username).equals(password)) {
				return true;
			}
		}
		return false;
	}

}
