package ch.fhnw.apsi.server.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;

import ch.fhnw.apsi.server.Server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class IndexHandler implements HttpHandler {

	private HttpCookie cookie;

	@Override
	public void handle(HttpExchange t) throws IOException {
		if ("GET".equals(t.getRequestMethod())) {
			Headers hIn = t.getRequestHeaders();
			boolean autologin = false;
			// TODO
			if (hIn.containsKey("Cookie")) {
				autologin = Server.checkCookie("hansmueller@gmx.com", hIn);
			}
			Headers h = t.getResponseHeaders();
			Server.setDefaultHeaders(h);
			if (autologin) {
				if (cookie != null)
					h.add("Set-Cookie",
							cookie.getName() + "=\"" + cookie.getValue() + "\"; Version="
									+ cookie.getVersion() + "; Max-Age=" + cookie.getMaxAge()
									+ "; Path=" + cookie.getPath());
				else
					System.out.println("Cookie is null");
				h.add("Location", "/loggedIn");
				t.sendResponseHeaders(302, 0);
				OutputStream os = t.getResponseBody();
				os.close();
			} else {
				t.sendResponseHeaders(200, 0);
				OutputStream os = t.getResponseBody();
				byte[] buf = new byte[256];
				int len;
				File f = new File(Server.INDEXPAGE);
				try (FileInputStream fis = new FileInputStream(f)) {
					while ((len = fis.read(buf)) != -1) {
						os.write(buf, 0, len);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				os.close();
			}
		} else {
			t.getResponseHeaders();
			t.sendResponseHeaders(404, 0);
			OutputStream os = t.getResponseBody();
			os.close();
		}
	}

}
