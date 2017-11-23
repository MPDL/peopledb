<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
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
				<h2>Create New Property Group</h2>
				<div class="jumbotron">
					<form method="post" action="NewGroupServlet">
					<table>
						<tr><td><label for="group_name">Property group name</label></td>
						<td><input class="form-control" type="text" name="group_name" maxlength=40 required/></td></tr>
						<tr><td></td></tr>
					</table>
					<div class="saveGroup">
						<button type="submit" class="btn btn-primary" name="save" value="Save"><i class="fa fa-floppy-o"></i>Save</button>
						<button type="reset" class="btn btn-secondary" name="reset" value="Reset"><i class="fa fa-trash-o"></i>Reset</button>
					</div>
					</form>
				</div>
			</c:otherwise>
		</c:choose>
		</div>
		<%@ include file="../WEB-INF/include/footer.jsp" %>
	</div>
</body>
</html>