<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head><title>Vendor Management System — Log In</title></head>
<body>
<h1>Vendor Management System — Log In</h1>
<c:if test="${not empty sessionScope.flashMessage}">
    <p style="color:#1B7A3D;"><c:out value="${sessionScope.flashMessage}"/></p>
    <c:remove var="flashMessage" scope="session"/>
</c:if>
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
<p><a href="<c:url value='/register.jsp'/>">Apply as a vendor</a></p>
</body>
</html>
