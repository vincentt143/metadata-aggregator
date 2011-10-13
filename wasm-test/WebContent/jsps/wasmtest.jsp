<%@ page language='java' %>
<%@ page import='java.util.HashMap' %>
<%@ page import='java.util.Iterator' %>
<%@ taglib uri='/tlds/security.tld' prefix='security' %>
<security:wasmauth enforce='true'/>
<html>
<head>
	<title>WASM 4 Test Page</title>
</head>
<body>
	<h1>WASM 4 Test Page</h1>
	<pre>
<% if (wasmauth == null) {
%>wasmauth is null<%
} else {
	HashMap attr = wasmauth.getAttributes();
	for (Iterator it = attr.keySet().iterator(); it.hasNext(); ) {
		Object key = it.next();
%><%= key.toString() %>: <%= attr.get(key).toString() %>
<%
	}
}
%>
	</pre>
</body>
</html>
