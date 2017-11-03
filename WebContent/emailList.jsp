<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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
<div class="col-xs-11 vertical-center" align="center">
<div class="container">
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
				<h2>Send email</h2>
				<div class="form-group jumbotron">
				<form method="post" action="EmailDispatcherServlet">
					<label for="recipients">Select recipients:</label>
					<p>
						<select class="form-control" name="recipients" multiple>
							<c:forEach items="${resultData.rows}" var="currentRow">
								<option name="approved" value="${currentRow.basic_data_email}" selected>
								${currentRow.basic_data_given_name} ${currentRow.basic_data_family_name}
							</option>
							</c:forEach>
						</select>
					</p>
					<p>
						<input class="form-control" type="text" size=40 maxlength=80 placeholder="Subject" name="subject" required></input>
					</p>
					<p>
						<textarea class="form-control" rows=6 cols=42 maxlength=600 placeholder="Message body" name="msgBody" required></textarea>
					</p>
					<button type="submit" class="btn btn-primary">Send</button>
				</form>
				</div>
			</c:otherwise>
		</c:choose>
		</div>
	<%@ include file="../WEB-INF/include/footer.jsp" %>
</div>
</body>
</html>