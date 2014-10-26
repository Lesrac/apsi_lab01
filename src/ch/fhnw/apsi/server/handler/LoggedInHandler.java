package ch.fhnw.apsi.server.handler;

import java.io.IOException;
import java.io.OutputStream;

import ch.fhnw.apsi.server.PermanentCookie;
import ch.fhnw.apsi.server.Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoggedInHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange t) throws IOException {
		if ("GET".equals(t.getRequestMethod())) {
			Headers hIn = t.getRequestHeaders();
			boolean loginSuccess = false;
			for(String s : Server.getUserpw_map().keySet()){
				if(Server.checkCookie(s, hIn)){
					loginSuccess = true;
				}
			}
			if (loginSuccess) {
				loginSuccess(t);
			} else {
				loginFailed(t);
			}
		} else {
			t.getResponseHeaders();
			t.sendResponseHeaders(404, 0);
			OutputStream os = t.getResponseBody();
			os.close();
		}
	}

	private void loginSuccess(HttpExchange t) throws IOException {
		Headers h = t.getResponseHeaders();
		Server.setDefaultHeaders(h);

		t.sendResponseHeaders(200, 0);
		OutputStream os = t.getResponseBody();
		StringBuffer sb = new StringBuffer();
		// TODO
		for (PermanentCookie c : Server.getCookies("hansmueller@gmx.com")) {
			sb.append("CookieStore: Name=" + c.getName() + ", Value=" + c.getValue()
					+ ", MaxAge=" + c.getMaxAge() + ", Path=" + c.getPath()
					+ ", Version=" + c.getVersion());
			sb.append("<br>");
		}
		// sb.append("<form action=\"" + "seemore" + "\" method=\"GET>\">");
		// sb.append("<input type=submit value=\"Next Page\">");
		// sb.append("</form>");
		os.write(sb.toString().getBytes());
		os.close();
	}

	private void loginFailed(HttpExchange t) throws IOException {
		Headers h = t.getResponseHeaders();
		h.add("Location", "/index");
		t.sendResponseHeaders(302, 0);
		OutputStream os = t.getResponseBody();
		os.close();
	}

}
