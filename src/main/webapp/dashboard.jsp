<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head><title>Vendor Dashboard — Vendor Management System</title></head>
<body>
<h1>Vendor Dashboard</h1>
<p><a href="<c:url value='/logout.do'/>">Log out</a></p>

<c:if test="${not empty flashMessage}">
    <p style="color:#1B7A3D;"><c:out value="${flashMessage}"/></p>
</c:if>
<c:if test="${not empty flashError}">
    <p style="color:#a00;"><c:out value="${flashError}"/></p>
</c:if>

<h2>Business profile</h2>
<c:choose>
    <c:when test="${not empty account}">
        <p>Company: <c:out value="${account.companyName}"/></p>
        <p>Category: <c:out value="${account.category}"/></p>
        <p>Application status:
            <c:choose>
                <c:when test="${account.approvalStatus == 'PENDING'}">
                    <strong style="color:#a06a00;">PENDING REVIEW</strong>
                </c:when>
                <c:when test="${account.approvalStatus == 'APPROVED'}">
                    <strong style="color:#1B7A3D;">APPROVED</strong>
                </c:when>
                <c:otherwise>
                    <strong style="color:#a00;">REJECTED</strong>
                </c:otherwise>
            </c:choose>
        </p>
    </c:when>
    <c:otherwise><p>No account on file.</p></c:otherwise>
</c:choose>

<c:if test="${not empty account && account.approvalStatus == 'PENDING'}">
    <p style="color:#a06a00;">Your application is awaiting review by a
        procurement admin. You'll be able to list catalog items and
        invoice once it's approved — check back here for updates.</p>
</c:if>
<c:if test="${not empty account && account.approvalStatus == 'REJECTED'}">
    <p style="color:#a00;">Your application was not approved. Contact
        procurement if you believe this is in error.</p>
</c:if>

<h2>Contact person</h2>
<c:choose>
    <c:when test="${not empty profile}">
        <p>Name: <c:out value="${profile.contactName}"/></p>
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

<h2>Amount owed to you: <fmt:formatNumber value="${amountOwed}" type="currency" currencySymbol="£"/></h2>

<h2>Ledger history</h2>
<c:choose>
    <c:when test="${empty ledgerEntries}">
        <p>No invoices or payments yet.</p>
    </c:when>
    <c:otherwise>
        <table border="1" cellpadding="4" cellspacing="0">
            <tr><th>Date</th><th>Type</th><th>Description</th><th>Amount</th></tr>
            <c:forEach var="entry" items="${ledgerEntries}">
                <tr>
                    <td><c:out value="${entry.occurredAt}"/></td>
                    <td><c:out value="${entry.invoice ? 'Invoice' : 'Payment'}"/></td>
                    <td><c:out value="${entry.description}"/></td>
                    <td><fmt:formatNumber value="${entry.amount}" type="currency" currencySymbol="£"/></td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

<c:if test="${not empty account && account.approvalStatus == 'APPROVED'}">
    <h3>Submit an invoice</h3>
    <p>For a charge that isn't tied to a purchase order — freight, a
        correction, a one-off service. Most of what you're owed comes from
        procurement raising purchase orders against your catalog below.</p>
    <form action="<c:url value='/dashboard/submitInvoice.do'/>" method="post">
        <p><label>Amount: <input type="text" name="amount"></label></p>
        <p><label>Description: <input type="text" name="description"></label></p>
        <p><input type="submit" value="Submit invoice"></p>
    </form>

    <h2>My catalog</h2>
    <c:choose>
        <c:when test="${empty catalog}">
            <p>You haven't listed any items yet.</p>
        </c:when>
        <c:otherwise>
            <table border="1" cellpadding="4" cellspacing="0">
                <tr><th>Name</th><th>Category</th><th>Unit price</th><th>Status</th></tr>
                <c:forEach var="item" items="${catalog}">
                    <tr>
                        <td><c:out value="${item.name}"/></td>
                        <td><c:out value="${item.category}"/></td>
                        <td><fmt:formatNumber value="${item.unitPrice}" type="currency" currencySymbol="£"/></td>
                        <td><c:out value="${item.active ? 'Listed' : 'Delisted'}"/></td>
                    </tr>
                </c:forEach>
            </table>
        </c:otherwise>
    </c:choose>

    <h3>List a new catalog item</h3>
    <form action="<c:url value='/dashboard/addItem.do'/>" method="post">
        <p><label>Item name: <input type="text" name="name"></label></p>
        <p><label>Category:
            <select name="category">
                <option value="SOFTWARE">Software</option>
                <option value="HARDWARE">Hardware</option>
                <option value="LAPTOP">Laptop</option>
                <option value="IOT_DEVICE">IoT device</option>
                <option value="ACCESSORY">Accessory</option>
            </select>
        </label></p>
        <p><label>Unit price: <input type="text" name="unitPrice"></label></p>
        <p><label>Description: <input type="text" name="description"></label></p>
        <p><input type="submit" value="Add to catalog"></p>
    </form>
</c:if>

</body>
</html>
