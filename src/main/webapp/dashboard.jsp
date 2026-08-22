<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head><title>My Account — Customer Account Portal</title></head>
<body>
<h1>My Account</h1>
<p><a href="<c:url value='/logout.do'/>">Log out</a></p>

<c:if test="${not empty flashMessage}">
    <p style="color:#1B7A3D;"><c:out value="${flashMessage}"/></p>
</c:if>
<c:if test="${not empty flashError}">
    <p style="color:#a00;"><c:out value="${flashError}"/></p>
</c:if>

<h2>Profile</h2>
<c:choose>
    <c:when test="${not empty profile}">
        <p>Name: <c:out value="${profile.fullName}"/></p>
        <p>Email: <c:out value="${profile.email}"/></p>
        <p>Phone: <c:out value="${profile.phone}"/></p>
        <p>Address: <c:out value="${profile.address}"/></p>
    </c:when>
    <c:otherwise><p>No profile on file.</p></c:otherwise>
</c:choose>

<h3>Update contact details</h3>
<form action="<c:url value='/dashboard/updateProfile.do'/>" method="post">
    <p><label>Phone: <input type="text" name="phone" value="${profile.phone}"></label></p>
    <p><label>Address: <input type="text" name="address" value="${profile.address}"></label></p>
    <p><input type="submit" value="Save"></p>
</form>

<h2>Balance: <fmt:formatNumber value="${balance}" type="currency" currencySymbol="£"/></h2>

<h2>Transaction history</h2>
<c:choose>
    <c:when test="${empty transactions}">
        <p>No transactions yet.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="4" cellspacing="0">
            <tr><th>Date</th><th>Description</th><th>Amount</th></tr>
            <c:forEach var="t" items="${transactions}">
                <tr>
                    <td><c:out value="${t.occurredAt}"/></td>
                    <td><c:out value="${t.description}"/></td>
                    <td><fmt:formatNumber value="${t.amount}" type="currency" currencySymbol="£"/></td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

<h3>Record a transaction</h3>
<form action="<c:url value='/dashboard/recordTransaction.do'/>" method="post">
    <p><label>Amount (negative for a debit, e.g. -25.00): <input type="text" name="amount"></label></p>
    <p><label>Description: <input type="text" name="description"></label></p>
    <p><input type="submit" value="Record"></p>
</form>

</body>
</html>
