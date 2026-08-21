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
    <p><label>Password: <html:password property="password"/></label></p>
    <p><html:submit value="Log In"/></p>
</html:form>
</body>
</html>
