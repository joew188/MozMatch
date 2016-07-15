<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>hello world</title>
</head>
<body>
<div class="container">
    <h1>${title}</h1>
    <p>
        <c:if test="${not empty msg}">
        Hello ${msg}
        </c:if>

        <c:if test="${empty msg}">
       Moz Matching System!
        </c:if>
    <p>
        <a class="btn btn-primary btn-lg" href="#" role="button">Learn more</a>
    </p>
</div>
</body>
</html>
