<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head><title>Apply as a Vendor — Vendor Management System</title></head>
<body>
<h1>Apply as a Vendor</h1>
<p>Submitting this sends your application to procurement for review — you
won't be able to list catalog items or invoice until an admin approves it.
You can log in right away to check your application's status.</p>
<html:errors/>
<html:form action="/register">
    <p><label>Company name: <html:text property="companyName"/></label></p>
    <p><label>Category:
        <html:select property="category">
            <html:option value="">-- choose one --</html:option>
            <html:option value="SUPPLIES">Supplies</html:option>
            <html:option value="SERVICES">Services</html:option>
            <html:option value="MAINTENANCE">Maintenance</html:option>
            <html:option value="LOGISTICS">Logistics</html:option>
            <html:option value="PROFESSIONAL_SERVICES">Professional services</html:option>
        </html:select>
    </label></p>
    <p><label>Contact name: <html:text property="contactName"/></label></p>
    <p><label>Contact email: <html:text property="email"/></label></p>
    <p><label>Username: <html:text property="username"/></label></p>
    <p><label>Password: <html:password property="password"/></label></p>
    <p><html:submit value="Submit Application"/></p>
</html:form>
<p><a href="<c:url value='/login.jsp'/>">Back to log in</a></p>
</body>
</html>
