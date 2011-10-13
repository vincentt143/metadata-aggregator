package usyd.security;

import java.io.IOException;

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;


public class ServletWASMAuth extends WASMAuth {

	private ServletWASMAuth(LinkedList lines) {
		super(lines);
	}

	public static WASMAuth getAuth(HttpServletRequest req, HttpServletResponse resp, boolean enforce) throws IOException {
		WASMAuth auth = null;

		String iKey = req.getParameter("wasmIkey");
		String sKey = null;

		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; ++i) {
				if (cookies[i].getName().equals("wasm_" + appRealm +  "_" + appID + "_skey")) {
					sKey = cookies[i].getValue();
					break;
				}
			}
		}

		if (iKey != null) {
			auth = getAuth(iKey, sKey, WASMAuth.IKEY_MODE);
			if (auth.get("status").equalsIgnoreCase("OK") && auth.get("sKey") != null) {
				Cookie sKeyCookie = new Cookie("wasm_" + appRealm +  "_" + appID + "_skey", auth.get("sKey"));
				sKeyCookie.setPath("/");
				resp.addCookie(sKeyCookie);
			}
			if (auth.get("status").equalsIgnoreCase("noSuchIkey")) {
				if (enforce) {
					redirectToLogin(req, resp);
				}
				return null;
			}
			return auth;
		} else if (sKey != null) {
			auth = getAuth(iKey, sKey, WASMAuth.SKEY_MODE);
			if (auth.get("status").equalsIgnoreCase("noSuchSkey")) {
				if (enforce) {
					redirectToLogin(req, resp);
				}
				return null;
			}
			return auth;
		}

		if (enforce) {
			redirectToLogin(req, resp);
		}
		return null;
	}

	private static void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		StringBuffer requestBuffer = req.getRequestURL();
		String queryString = req.getQueryString();
		if (queryString != null) {
			requestBuffer.append("?" + queryString);
		}
		resp.sendRedirect(loginURL + "?appID=" + appID + "&appRealm=" + appRealm + "&destURL=" + java.net.URLEncoder.encode(requestBuffer.toString(), "UTF-8"));
	}
	
	protected final static String loginURL = "https://wasm-test.ucc.usyd.edu.au/login.cgi";
}
