package usyd.security.taglib;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

public class WASMAuthenticateTagInfo extends TagExtraInfo {
	public VariableInfo[] getVariableInfo(TagData data)
	{
		// Cretate a new script variable in page context.
		return new VariableInfo[] { 
			new VariableInfo("wasmauth", "usyd.security.WASMAuth", true, VariableInfo.AT_END),
		};
	}
}
