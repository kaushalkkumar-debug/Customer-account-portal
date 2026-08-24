<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head><title>Admin — Vendor Management System</title></head>
<body>
<h1>Procurement Admin</h1>
<p>
    <a href="<c:url value='/logout.do'/>">Log out</a> |
    <a href="<c:url value='/catalog.do'/>">Browse catalog &amp; raise a purchase order</a>
</p>

<c:if test="${not empty flashMessage}">
    <p style="color:#1B7A3D;"><c:out value="${flashMessage}"/></p>
</c:if>

<h2>Pending vendor applications</h2>
<c:choose>
    <c:when test="${empty pendingVendors}">
        <p>Nothing awaiting review.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="4" cellspacing="0">
            <tr><th>ID</th><th>Company</th><th>Category</th><th>Username</th><th></th></tr>
            <c:forEach var="v" items="${pendingVendors}">
                <tr>
                    <td><c:out value="${v.id}"/></td>
                    <td><c:out value="${v.companyName}"/></td>
                    <td><c:out value="${v.category}"/></td>
                    <td><c:out value="${v.username}"/></td>
                    <td>
                        <form action="<c:url value='/admin/setApprovalStatus.do'/>" method="post" style="display:inline">
                            <input type="hidden" name="accountId" value="${v.id}">
                            <input type="hidden" name="status" value="APPROVED">
                            <input type="submit" value="Approve">
                        </form>
                        <form action="<c:url value='/admin/setApprovalStatus.do'/>" method="post" style="display:inline">
                            <input type="hidden" name="accountId" value="${v.id}">
                            <input type="hidden" name="status" value="REJECTED">
                            <input type="submit" value="Reject">
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

<h2>Vendors</h2>
<c:choose>
    <c:when test="${empty vendors}">
        <p>No decided vendors yet.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="4" cellspacing="0">
            <tr><th>ID</th><th>Company</th><th>Category</th><th>Approval</th><th>Status</th><th></th><th>Record a payment</th></tr>
            <c:forEach var="v" items="${vendors}">
                <tr>
                    <td><c:out value="${v.id}"/></td>
                    <td><c:out value="${v.companyName}"/></td>
                    <td><c:out value="${v.category}"/></td>
                    <td><c:out value="${v.approvalStatus}"/></td>
                    <td><c:out value="${v.active ? 'Active' : 'Deactivated'}"/></td>
                    <td>
                        <form action="<c:url value='/admin/toggleActive.do'/>" method="post" style="display:inline">
                            <input type="hidden" name="accountId" value="${v.id}">
                            <input type="hidden" name="active" value="${!v.active}">
                            <input type="submit" value="${v.active ? 'Deactivate' : 'Reactivate'}">
                        </form>
                    </td>
                    <td>
                        <form action="<c:url value='/admin/recordPayment.do'/>" method="post" style="display:inline">
                            <input type="hidden" name="vendorId" value="${v.id}">
                            <input type="text" name="amount" placeholder="amount" size="8">
                            <input type="text" name="description" placeholder="description" size="14">
                            <input type="submit" value="Pay">
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>
