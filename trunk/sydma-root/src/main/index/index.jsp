<%
    String redirectURL = request.getScheme() + "://" + request.getServerName() + "/${context.path}";
    response.sendRedirect(redirectURL);
%>
