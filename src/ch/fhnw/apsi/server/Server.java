package ch.fhnw.apsi.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import ch.fhnw.apsi.server.handler.CheckLoginHandler;
import ch.fhnw.apsi.server.handler.IndexHandler;
import ch.fhnw.apsi.server.handler.LoggedInHandler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public class Server {
	public static final String INDEXPAGE = "web/index.html";

	private static Map<String, String> userpw_map = new HashMap<>();
	private static Map<String, List<PermanentCookie>> permanentCookies = new HashMap<>();

	public static void main(String[] args) throws IOException {
		initUserPwMap();
		try {
			HttpsServer server = HttpsServer.create(new InetSocketAddress(8080), 0);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			char[] password = "simulator".toCharArray();
			KeyStore ks = KeyStore.getInstance("JKS");
			FileInputStream fis = new FileInputStream("key/identity.jks");
			ks.load(fis, password);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, password);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);

			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				public void configure(HttpsParameters params) {
					try {
						// initialise the SSL context
						SSLContext c = SSLContext.getDefault();
						SSLEngine engine = c.createSSLEngine();
						params.setNeedClientAuth(false);
						params.setCipherSuites(engine.getEnabledCipherSuites());
						params.setProtocols(engine.getEnabledProtocols());

						// get the default parameters
						SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
						params.setSSLParameters(defaultSSLParameters);
					} catch (Exception ex) {
						ex.printStackTrace();
						System.out.println("Failed to create HTTPS port");
					}
				}
			});

			server.createContext("/index", new IndexHandler());
			server.createContext("/checkLogin", new CheckLoginHandler());
			server.createContext("/loggedIn", new LoggedInHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void initUserPwMap() {
		userpw_map.put("hansmueller@gmx.com", "qwertz12");
		userpw_map.put("bernd@asp.ch", "asdfgh21");
		userpw_map.put("sepp@dot.de", "yxcvbn12");
		userpw_map.put("olli@swisscom.ch", "qwertzui");
		userpw_map.put("andrea@bluewin.ch", "qwer");
	}

	public static List<PermanentCookie> getCookies(String username) {
		return permanentCookies.get(username);
	}

	public static boolean checkCookie(String username, Headers headers) {
		List<String> headerList = headers.get("Cookie");
		if (headerList != null) {
			for (String s : headerList) {
				String[] name_value = s.split("COOKIE=\"");
				if (checkCookie(username, name_value[0],
						name_value[1].replace("\"", ""))) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean checkCookie(String username, String name, String value) {
		if ("SESSION_".equals(name)) {
			name = "SESSION_COOKIE";
		}
		if (permanentCookies.get(username) != null) {
			for (PermanentCookie cookie : permanentCookies.get(username)) {
				if (cookie.getName().equals(name) && cookie.getValue().equals(value)
						&& !cookie.hasExpired()) {
					return true;
				}
			}
		}
		return false;
	}

	public static PermanentCookie getCookie(String username, String name) {
		if (permanentCookies.get(username) != null)
			for (PermanentCookie cookie : permanentCookies.get(username)) {
				if (name.equals(cookie.getName())) {
					return cookie;
				}
			}
		return null;
	}

	public static void addCookie(String username, String name, String value,
			int maxAge) {
		PermanentCookie cookie = new PermanentCookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		if (permanentCookies.containsKey(username)) {
			List<PermanentCookie> cl = permanentCookies.get(username);
			cl.add(cookie);
			permanentCookies.put(username, cl);
		} else {
			List<PermanentCookie> cl = new ArrayList<>();
			cl.add(cookie);
			permanentCookies.put(username, cl);
		}
	}

	public static void removeCookie(String username, String name) {
		PermanentCookie cookie = getCookie(username, name);
		permanentCookies.remove(cookie);
	}

	public static void setDefaultHeaders(Headers h) {
		h.add("Content-Type", "text/html;charset=utf-8");
	}

	public static Map<String, String> getUserpw_map() {
		return userpw_map;
	}

	public static boolean checkCookieExistForUser(String username,
			String password, String userAgent) {
		List<PermanentCookie> cookies = getCookies(username);
		if (cookies != null) {
			for (PermanentCookie c : cookies) {
				if (!c.hasExpired()
						&& !c.getValue().equals(
								CookieUtil.createCookieHeader("COOKIE", username, password,
										userAgent, false))) {
					return true;
				}
			}
		}
		return false;
	}

}
