<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<!DOCTYPE html>
<html>
<head><title>Customer Account Portal — Log In</title></head>
<body>
<h1>Log In</h1>
<html:errors/>
<html:form action="/login">
    <p><label>Username: <html:text property="username"/></label></p>
    <!-- redisplay="false": Struts' html:password tag defaults redisplay
         to true, which echoes the just-submitted (and possibly wrong)
         password back into the value="" attribute on a failed login —
         visible in page source and browser autofill/history. Found
         live: submitting a wrong password showed it right back in the
         form. Standard practice for password fields in Struts 1.x. -->
    <p><label>Password: <html:password property="password" redisplay="false"/></label></p>
    <p><html:submit value="Log In"/></p>
</html:form>
</body>
</html>
