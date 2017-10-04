<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../WEB-INF/include/db.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>The MPDL People</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/font-awesome.min.css" rel="stylesheet">
<link href="css/boxAligner.css" rel="stylesheet">
</head>
<body>
	<div class="vertical-center" align="center">
		<%@ include file="../WEB-INF/include/header.jsp" %>
		<%
		if (messages.length() > 0)
		{
			%><p class="alert alert-success"><strong><%= messages.toString() %></strong></p><%
		}
		if (errors.length() > 0)
		{
			%><p class="alert alert-danger"><strong><%= errors.toString() %></strong></p><%
		}
		%>
		<div class="container">
		<h2>MPDL employees</h2>
		<div class="jumbotron form-group">
		<form method="get" action="QueryServlet">
			<input class="form-control form-control-inline" type="text" name="query" placeholder="Enter name" required/>
			<button type="submit" name="quick_search" value="Search" class="btn btn-primary"><i class="fa fa-search"></i>Search</button>
			<div class="checkbox">
			<label><input type="checkbox" name="show_deleted" value="true" checked=""/>Show deleted</label>
			</div>
		</form>
		</div>
		</div>
		<%@ include file="../WEB-INF/include/footer.jsp" %>
	</div>
</body>
</html>