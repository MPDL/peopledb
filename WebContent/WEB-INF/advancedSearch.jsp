<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ include file="../WEB-INF/include/db.jsp" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Advanced Search</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/font-awesome.min.css" rel="stylesheet">
<link href="css/boxAligner.css" rel="stylesheet">
<script>
function changeInputType(sel, id) {
	var inputBox = document.getElementById('value' + id);
	if (sel.value.split('$')[1] == 'decimal') {
		inputBox.type = 'number';
		inputBox.step='0.0001';
	}
	else {
		inputBox.type = sel.value.split('$')[1]
	}
}
</script>
</head>
<body>
	<div class="vertical-center" align="center">
		<%@ include file="../WEB-INF/include/header.jsp" %>
		<div class="container">
		<c:choose>
			<c:when test="${not empty message}">
				<p class="alert alert-success">${message}</p>
			</c:when>
		</c:choose>
		<c:choose>
			<c:when test="${not empty error}">
				<p class="alert alert-danger">${error}</p>
			</c:when>
			<c:otherwise>
				<h2>Advanced Search</h2>
				<div class="jumbotron">
				<form method="get" action="QueryServlet">
				<div class="form-group">
				<table>
				<c:forEach var = "widget" begin = "1" end = "${widgets}">
					<c:set var = "current" scope = "session" value = "property${widget}"/>
				<tr>
				<td>
				<select class="form-control" name="property${widget}" id="property${widget}" onchange="changeInputType(this,${widget})">
					<c:forEach items="${map}" var="entry">
						<optgroup label="${entry.key}">
							<c:forEach items="${entry.value}" var="colNames">
								<c:set var="propertyAndType" value="${colNames.left}$${colNames.middle}"/>
								<c:choose>
									<c:when test="${current eq propertyAndType}">	
										<option value="${colNames.left}$${colNames.middle}" selected>${colNames.right}</option>
									</c:when>
									<c:otherwise>
										<option value="${colNames.left}$${colNames.middle}">${colNames.right}</option>
									</c:otherwise>
									</c:choose>
							</c:forEach>
						</optgroup>
					</c:forEach>
				</select>
				</td>
			<td>
				<c:set var="currentWidget" scope = "session" value="lexicographic${widget}"/>
				<select class="form-control form-control-inline" name="lexicographic${widget}">
					<c:choose>
						<c:when test="${currentWidget eq 'is'}">
							<option value="is" selected>is</option>
						</c:when>
						<c:otherwise>
							<option value="is">is</option>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${currentWidget eq 'isnot'}">
							<option value="isnot" selected>is</option>
						</c:when>
						<c:otherwise>
							<option value="isnot">is not</option>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${currentWidget eq 'biggerOrEqual'}">
							<option value="biggerOrEqual" selected>>=</option>
						</c:when>
						<c:otherwise>
							<option value="biggerOrEqual">>=</option>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${currentWidget eq 'smallerOrEqual'}">
							<option value="smallerOrEqual" selected><=</option>
						</c:when>
						<c:otherwise>
							<option value="smallerOrEqual"><=</option>
						</c:otherwise>
					</c:choose>
				</select>
				</td>
				<td>
					<c:set var="prevValue" value="value${widget}"/>
					<input class="form-control form-control-inline" type="text" id="value${widget}" name="value${widget}" value="${prevValue}"/>
					</td>
			</tr>
			</c:forEach>
			</table>
					<div class="saveGroup">
						<input type="checkbox" name="show_deleted" value="true" checked/> Show deleted
						<button type="submit" name="more_criteria" value="${widgets + 1}" class="btn btn-outline-primary" formmethod="get" formaction="AdvancedSearchServlet"><i class="fa fa-plus icon-center"></i></button>
					</div>
					<div class="saveGroup"><button type="submit" name="advanced_search" value="Search" class="btn btn-primary">Search</button></div>
				</form>
					</c:otherwise>
	</c:choose>
		</div>
		</div>
		<%@ include file="../WEB-INF/include/footer.jsp" %>
	</div>
	</div>
	</div>
</body>
</html>