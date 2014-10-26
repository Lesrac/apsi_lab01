package ch.fhnw.apsi.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import ch.fhnw.apsi.server.CookieUtil;
import ch.fhnw.apsi.server.Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CheckLoginHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		if ("GET".equals(t.getRequestMethod())) {
			URI uri = t.getRequestURI();
			String uriS = java.net.URLDecoder.decode(uri.toString(), "UTF-8");
			String paramS = uriS.substring(uriS.indexOf("?") + 1);
			String[] parameters = paramS.split("&");
			Headers hReq = t.getRequestHeaders();
			String userAgent = hReq.getFirst("User-Agent");
			String username = "";
			String password = "";
			for (String s : parameters) {
				if (s.startsWith("user")) {
					username = s.substring(5);
				} else if (s.startsWith("password")) {
					password = s.substring(9);
				}
			}
			Headers h = t.getResponseHeaders();
			if (checkUser(username, password)
					&& !Server.checkCookieExistForUser(username, password, userAgent)) {
				h.add("Location", "/loggedIn");
				String cookieName = "SESSION_COOKIE";
				h.add("Set-Cookie", CookieUtil.createCookieHeader(cookieName, username,
						password, userAgent, true));
			} else {
				h.add("Location", "/index");
			}
			t.sendResponseHeaders(302, 0); // response.getBytes().length
			OutputStream os = t.getResponseBody();
			os.close();
		} else {
			t.getResponseHeaders();
			t.sendResponseHeaders(404, 0);
			OutputStream os = t.getResponseBody();
			os.close();
		}
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
