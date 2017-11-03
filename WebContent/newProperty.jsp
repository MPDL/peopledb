<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ include file="../WEB-INF/include/db.jsp" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

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
				<h2>Create New Property</h2>
				<div class="jumbotron">
				<form method="post" action="NewPropertyServlet">
					<table>
					<tr><td>
						<label>Property group</label></td>
					<td>
						<select class="form-control" name="property_group">
						<c:forEach var="propGroupSet" items="${propertyGroupSet.rows}">
							<option value="${propGroupSet.property_group_id}">${propGroupSet.name}</option>
						</c:forEach>
						</select></td></tr>
					<tr><td><label>Property name</label></td>
					<td><input class="form-control" type="text" name="property_name" required/></td></tr>
					<tr><td><label>Data type</label></td>
					<td>
					<select class="form-control" name="data_type">
						<option value="varchar">String</option>
						<option value="integer">Integer</option>
						<option value="decimal">Decimal</option>
						<option value="boolean">Boolean</option>
						<option value="email">Email</option>
						<option value="date">Date</option>
					</select></td></tr>
					<tr>
					<td><label>Required</label></td>
					<td>
						<label class="radio-inline"><input type="radio" id="prop_required" name="prop_required" value="TRUE"/>Yes</label>
						<label class="radio-inline"><input type="radio" id="prop_required" name="prop_required" value="FALSE" checked="checked"/>No</label>
					</td>
					</tr>
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