<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../WEB-INF/include/db.jsp" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/font-awesome.min.css" rel="stylesheet">
<link href="css/boxAligner.css" rel="stylesheet">
<title>SQL Interface</title>
</head>
<body>
	<div class="vertical-center" align="center">
		<%@ include file="../WEB-INF/include/header.jsp" %>
			<c:choose>
				<c:when test="${not empty message}">
					<p class="alert alert-success"><strong>${message}</strong></p>
				</c:when>
			</c:choose>
			<c:choose>
				<c:when test="${not empty error}">
					<p class="alert alert-danger"><strong>${error}</strong></p>
				</c:when>
				<c:otherwise>
					<div class="container">
						<h2>SQL Interface</h2>
						<div class="jumbotron">
						Enter a SQL query in PostgreSQL syntax:
						<form method="post" action="CustomQueryServlet">
							<p><textarea class="form-control" id="sqlQuery" name="query" rows=6 cols=42 maxlength=400 required></textarea></p>
							<p><button type="submit" name="custom_query" class="btn btn-primary" value="Send query">Send query</button></p>
							<%@ include file="../WEB-INF/include/footer.jsp" %>
						</form>
						</div>
					</div>
				</c:otherwise>
			</c:choose>
	</div>
</body>
</html>