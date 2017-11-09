<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="../WEB-INF/include/db.jsp" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>The MPDL People</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/font-awesome.min.css" rel="stylesheet">
<link href="css/boxAligner.css" rel="stylesheet">
</head>
<body>
	<div class="vertical-center" align="center">
		<%@ include file="../WEB-INF/include/header.jsp" %>
		<div class="container">
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
				<h2>Person Details</h2>
					<div class="jumbotron">
					<form method="get" action="EditPersonServlet">
					<input type="hidden" name="person_id" value="${person_id}"/>
				<c:forEach items="${personData.rows}" var="personInfo">
					<h3>Basic Data</h3>
					<table>
					<c:set var = "groupName" scope = "session" value = "Basic Data"/>
					<c:forEach items="${propertySet.rows}" var="current">
						<c:if test="${current.group_name ne groupName}">
							<c:set var = "groupName" scope = "session" value = "${current.group_name}"/>
							<tr><td colspan=2 align="center"><h3>${groupName}</h3></td></tr>
						</c:if>
						<tr>
							<td><label for="${current.db_name}">${current.name}</label></td>
								<c:set var = "col" scope = "session" value = "${current.db_name}"/>
								<td>${personInfo[col]}</td>
					</c:forEach>
					</table>
					</c:forEach>
						<button type="submit" class="btn btn-primary"><i class="fa fa-pencil"></i>Edit</button>
					</form>
					</div>
		</c:otherwise>
		</c:choose>
		</div>
		<%@ include file="../WEB-INF/include/footer.jsp" %>
	</div>
</body>
</html>