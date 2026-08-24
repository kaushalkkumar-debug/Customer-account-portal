<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head><title>Catalog — Vendor Management System</title></head>
<body>
<h1>Catalog</h1>
<p><a href="<c:url value='/admin.do'/>">Back to admin</a> | <a href="<c:url value='/logout.do'/>">Log out</a></p>

<c:if test="${not empty flashMessage}">
    <p style="color:#1B7A3D;"><c:out value="${flashMessage}"/></p>
</c:if>

<p>Everything on offer from approved, active vendors — software licences,
hardware, laptops, IoT devices, accessories. Raising a purchase order here
posts a positive ledger entry against that vendor for quantity &times;
unit price, exactly as if they'd invoiced for it themselves.</p>

<c:choose>
    <c:when test="${empty items}">
        <p>Nothing purchasable right now — no approved vendor has listed an active item.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="4" cellspacing="0">
            <tr><th>Item</th><th>Category</th><th>Vendor</th><th>Unit price</th><th>Description</th><th>Purchase</th></tr>
            <c:forEach var="item" items="${items}">
                <tr>
                    <td><c:out value="${item.name}"/></td>
                    <td><c:out value="${item.category}"/></td>
                    <td><c:out value="${vendorNames[item.accountId]}"/></td>
                    <td><fmt:formatNumber value="${item.unitPrice}" type="currency" currencySymbol="£"/></td>
                    <td><c:out value="${item.description}"/></td>
                    <td>
                        <form action="<c:url value='/catalog/purchase.do'/>" method="post" style="display:inline">
                            <input type="hidden" name="itemId" value="${item.id}">
                            <input type="text" name="quantity" value="1" size="3">
                            <input type="submit" value="Raise PO">
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

</body>
</html>
