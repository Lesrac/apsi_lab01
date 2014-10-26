package ch.fhnw.apsi.server;

import java.io.File;
import java.util.Base64;

public class CookieUtil {

	public static String createCookieHeader(String cookieName, String username,
			String password, String userAgent, boolean addCookieToStorage) {
		String cookie = "";
		String secureUsername = null;
		String secureUserAgent = null;
		String securePassword = null;
		try {
			secureUsername = AESCrypter.encrypt(username, new File("key/user.txt"));
			secureUserAgent = AESCrypter
					.encrypt(userAgent, new File("key/agent.txt"));
			securePassword = AESCrypter.encrypt(password, new File("key/key.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		byte[] encoded = Base64.getEncoder().encode(
				(secureUsername + secureUserAgent + securePassword).getBytes());
		String encodedStr = new String(encoded);
		if (addCookieToStorage) {
			Server.addCookie(username, cookieName, encodedStr, 1800);
			PermanentCookie pCookie = Server.getCookie(username, cookieName);
			cookie = cookieName + "=\"" + pCookie.getValue() + "\"; Version="
					+ pCookie.getVersion() + "; Max-Age=" + pCookie.getMaxAge()
					+ "; HttpOnly; secure";
		} else {
			cookie = encodedStr;
		}
		return cookie;
	}

}
