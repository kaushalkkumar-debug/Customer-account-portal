<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head><title>Admin — Customer Account Portal</title></head>
<body>
<h1>Admin Panel</h1>
<p><a href="<c:url value='/logout.do'/>">Log out</a></p>

<c:if test="${not empty flashMessage}">
    <p style="color:#1B7A3D;"><c:out value="${flashMessage}"/></p>
</c:if>

<h2>Customer accounts</h2>
<c:choose>
    <c:when test="${empty accounts}">
        <p>No accounts on file.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="4" cellspacing="0">
            <tr><th>ID</th><th>Username</th><th>Role</th><th>Status</th><th></th></tr>
            <c:forEach var="a" items="${accounts}">
                <tr>
                    <td><c:out value="${a.id}"/></td>
                    <td><c:out value="${a.username}"/></td>
                    <td><c:out value="${a.role}"/></td>
                    <td><c:out value="${a.active ? 'Active' : 'Deactivated'}"/></td>
                    <td>
                        <form action="<c:url value='/admin/toggleActive.do'/>" method="post" style="display:inline">
                            <input type="hidden" name="accountId" value="${a.id}">
                            <input type="hidden" name="active" value="${!a.active}">
                            <input type="submit" value="${a.active ? 'Deactivate' : 'Reactivate'}">
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>
