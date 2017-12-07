<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<script>
	// blocks page redirect if nothing is chosen
	function assertNotAllBlank(name) {
		var approved = document.getElementsByName(name);
		var approvedLength = approved.length;
		for (var i = 0; i < approvedLength; i++) {
			if (approved[i].selected == true) {
				return true;
			}
		}
		alert('No entries are selected.');
		return false;	
	}
</script>
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
					<h2>Export CSV</h2>
						<div class="smallContainer">
						<form method="post" action="CSVServlet">
						<input type="hidden" value="${current_query}"/>
							<p>Review members:</p>
								<select class="form-control" name="recipients" multiple>
									<c:forEach items="${resultData.rows}" var="currentRow">
										<option name="approved" value="${currentRow.person_id}" selected>
											${currentRow.basic_data_given_name} ${currentRow.basic_data_family_name}
										</option>
									</c:forEach>
								</select>
							<p>Select properties to export:</p>
							<select name="toExport" class="form-control" multiple>
								<c:forEach items="${map}" var="entry">
									<optgroup label="${entry.key}">
										<c:forEach items="${entry.value}" var="colNames">
											<option name="exportOption" value="${colNames.left}">${colNames.right}</option>
										</c:forEach>
									</optgroup>
								</c:forEach>
							</select>
							<p>
								<button name="export_csv" type="submit" class="btn btn-primary" value="${current_query}" onclick="return assertNotAllBlank('approved') && assertNotAllBlank('exportOption');"><i class="fa fa-file fa-fw"></i>Export selected as CSV</button>
							</p>
							</form>
						</div>
				</c:otherwise>
	</c:choose>
	</div>
	<%@ include file="../WEB-INF/include/footer.jsp" %>
	</div>
</body>
</html>