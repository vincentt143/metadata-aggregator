package au.org.intersect.sydma.webapp.util;

import javax.servlet.http.HttpServletRequest;

public interface UrlHelper {

	public abstract String getCurrentBaseUrl(HttpServletRequest request);

}