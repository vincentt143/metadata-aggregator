package usyd.security.taglib;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import usyd.security.ServletWASMAuth;
import usyd.security.WASMAuth;

public class WASMAuthenticateTag extends TagSupport
{
	public WASMAuthenticateTag () {
		super();
	}

	public void setTimeout(String timeout) {
		this.timeout = Integer.parseInt(timeout);
	}

	public void setEnforce(boolean enforce) {
		this.enforce = enforce;
	}

	public int doStartTag() throws JspException {
		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {

		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();

		WASMAuth auth = null;
		try {
			auth = ServletWASMAuth.getAuth(request, response, enforce);

			if (auth != null) {
				if (request.getParameter("wasmIkey") != null) {
					redirectToSelf(request, response);
					return SKIP_PAGE;
				}
				
				// save auth in page context
				pageContext.setAttribute("wasmauth", auth);
				pageContext.getRequest().setAttribute("wasmauth", auth);
				pageContext.getRequest().setAttribute("wasmUser", auth.get("loginName"));
			}

			if ((auth == null) && enforce) {
				reset();
				return SKIP_PAGE;
			}
			reset();
			return EVAL_PAGE;
		}
		catch (IOException e) { throw new JspException(e); }
	}

	private void redirectToSelf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		StringBuffer requestBuffer = req.getRequestURL();
		Map paramMap = req.getParameterMap();
		boolean firstParam = true;

		for (Iterator it = paramMap.keySet().iterator(); it.hasNext(); ) {
			String key = (String)it.next();
			if (!key.equals("wasmIkey")) {
				String[] values = (String[])paramMap.get(key);
				for (int i = 0; i < values.length; i++) {
					if (firstParam) {
						requestBuffer.append("?");
						firstParam = false;
					} else {
						requestBuffer.append("&");
					}
					requestBuffer.append(key + "=" + values[i]);
				}
			}
		}
	
		resp.sendRedirect(requestBuffer.toString());

	}

	private void reset() {
		timeout = 235;
		enforce = true;
	}

	private int timeout = 235;
	private boolean enforce = true;
}
