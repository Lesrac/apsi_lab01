package ch.fhnw.apsi.server.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import ch.fhnw.apsi.server.PermanentCookie;
import ch.fhnw.apsi.server.Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoggedInHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		URI uri = t.getRequestURI();
		System.out.println(uri);
		Headers hIn = t.getRequestHeaders();
		if (Server.checkCookie(hIn)) {
			loginSuccess(t);
		} else {
			loginFailed(t);
		}
	}

	private void loginSuccess(HttpExchange t) throws IOException {
		Headers h = t.getResponseHeaders();
		Server.setDefaultHeaders(h);

		t.sendResponseHeaders(200, 0);
		OutputStream os = t.getResponseBody();
		StringBuffer sb = new StringBuffer();
		for (PermanentCookie c : Server.getCookies()) {
			sb.append("CookieStore: Name=" + c.getName() + ", Value=" + c.getValue()
					+ ", MaxAge=" + c.getMaxAge() + ", Path=" + c.getPath()
					+ ", Version=" + c.getVersion());
			sb.append("<br>");
		}
		sb.append("<form action=\"" + "seemore" + "\" method=\"GET>\">");
		sb.append("<input type=submit value=\"Next Page\">");
		sb.append("</form>");
		os.write(sb.toString().getBytes());
		os.close();
	}

	private void loginFailed(HttpExchange t) throws IOException {
		Headers h = t.getResponseHeaders();
		h.add("Location", "/index");
		t.sendResponseHeaders(302, 0); // response.getBytes().length
		OutputStream os = t.getResponseBody();
		os.close();
	}

}
