<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="../WEB-INF/include/db.jsp" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

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
<div align="center">
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
		<h2>Batch Update Results</h2>
	 	<table class="table-striped table-hover table-responsive" style="white-space: nowrap;"> 
	 	<thead>
	 		<c:forEach items="${nameList}" var="name" varStatus="status">
	 			<th class="text-center" scope="row">
		 			<div>${name}</div>
		 			<div style="font-size: 80%;">${groupList[status.index]}</div>
	 			</th>
	 		</c:forEach>
	 	</thead> 
		<c:forEach items="${result.rows}" var="currentRow">
	 		<tr>
	 		<c:forEach items="${dbNameList}" var="dbName">
	 			<td style="word-wrap: break-word;">${currentRow[dbName]}</td>
	 		</c:forEach>
	 		<td><form>
	 			<button type="submit" formmethod="get" formaction="EditPersonServlet" class="btn btn-default btn-sm" name="person_id" value="${currentRow.person_id}">Edit</button>
	 		</form></td>
	 		</tr>
	 	</c:forEach>
	</table>
	<form method="get" action="RefreshedQueryServlet">
	 		<input type="hidden" name="current_query" value="${current_query}">
	 		<button type="submit" class="btn btn-default btn-sm"><i class="fa fa-arrow-left" aria-hidden="true"></i>Back to search results</button>
	</form>
	</c:otherwise>
	</c:choose>
	</div>
	</div>
	<%@ include file="../WEB-INF/include/footer.jsp" %>
</body>
</html>