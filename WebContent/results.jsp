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
		var toChoose = document.getElementsByName(name);
		var toChooseLength = toChoose.length;
		for (var i = 0; i < toChooseLength; i++) {
			if (toChoose[i].checked == true) {
				return true;
			}
		}
		alert('No entries are selected.');
		return false;	
	}
	function selectAllMembers(source) {
		var toSelect = document.querySelectorAll('input[name="toChoose"]');
		var toSelectLength = toSelect.length;
		for (var i = 0; i < toSelectLength; i++) {
			toSelect[i].checked = source.checked;
		}
		return;
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
					<h2>Search Results</h2>
					<c:choose>
					<c:when test="${not empty resultData.rows}">
						<form method="get" action="QueryServlet">
						<div class="input-group result-options">
						<span class="input-group-btn">
						<table>
							<tr>
							<td class="col-lg-1"><label for="sort_criteria">Sort by: </label></td>
							<td class="col-lg-3"><select name="sort_criteria" class="dropdown form-control">
								<c:forEach items="${nameList}" var="colName" varStatus="status">
									<c:choose>
										<c:when test="${fn:contains(fn:substringAfter(message, 'ORDER BY'), dbNameList[status.index])}">
											<option value="${dbNameList[status.index]}" selected>${colName}</option>
										</c:when>
										<c:otherwise>
											<option value="${dbNameList[status.index]}">${colName}</option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</select></td>
							<td class="col-lg-2">
							<input name="current_query" type="hidden" value="${current_query}"/>
							<select name="sort_by" class="dropdown form-control">
								<c:choose>
									<c:when test="${fn:contains(message, 'DESC')}">
										<option value="ASC"><i class="fa fa-sort-asc"></i>ASC</option>
										<option value="DESC" selected><i class="fa fa-sort-desc"></i>DESC</option>
									</c:when>
									<c:otherwise>
										<option value="ASC" selected><i class="fa fa-sort-asc"></i>ASC</option>
										<option value="DESC"><i class="fa fa-sort-desc"></i>DESC</option>
									</c:otherwise>
								</c:choose>
							</select></td>
							<td class="col-lg-1"><button name="go_sort" class="btn btn-default" value="to_sort">Go</button></td>
							<td class="col-lg-3"><input class="form-control form-control-inline" type="text" name="query" placeholder="Search within results"/></td>
							<td class="col-lg-2"><button formmethod="get" formaction="QueryServlet" name="nested_search" value="nested" class="btn btn-default"><i class="fa fa-search"></i></button></td>
						</tr>
						</table>
						</span>
						</div>
						</form>
						</div>
						<div class="smallContainer">
						<form method="get" action="EmailListServlet">
					 	<table class="table-striped table-hover table-responsive" style="white-space: nowrap;"> 
					 	<thead> 
					 		<th class="text-center" scope="row">All<input type="checkbox" name="selectAll" onclick="selectAllMembers(this);" class="headerCheckbox"/></th>
					 		<c:forEach items="${nameList}" var="colHead" varStatus="status">
					 			<th class="text-center" scope="row">${colHead}<input type="checkbox" name="toExport" value="${dbNameList[status.index]}" class="headerCheckbox" checked/></th>
					 		</c:forEach>
					 	</thead> 
					 	<c:forEach items="${resultData.rows}" var="currentRow">
					 		<tr>
						 		<td class="check text-center">
						 			<input type="checkbox" name="toChoose" value="person${currentRow.person_id}"></input>
						 		</td>
					 			<c:forEach items="${dbNameList}" var="colName">
					 				<td>${currentRow[colName]}</td>
					 			</c:forEach>
					 			<td>
					 				<input name="current_query" type="hidden" value="${current_query}"/>
					 				<button class="btn btn-default btn-sm" type="submit" formmethod="get" formaction="EditPersonServlet" name="person_id" value="${currentRow.person_id}" class="btn btn-outline">Edit</button>
					 			</td>
					 		</tr>
					 	</c:forEach>
						</table>
						<div class="result-options">
							<button id="sendMail" type="submit" class="btn btn-primary" onclick="return assertNotAllBlank('toChoose');"><i class="fa fa-envelope fa-fw"></i>Email selected</button>
							<button name="editSelected" type="submit" class="btn btn-primary" formmethod="post" formaction="BatchEditServlet" value=1 onclick="return assertNotAllBlank('toChoose');"><i class="fa fa-pencil fa-fw"></i>Edit selected</button>
							<button name="export_csv" type="submit" class="btn btn-primary" formmethod="get" formaction="CSVServlet" value="${current_query}" onclick="return assertNotAllBlank('toChoose') && assertNotAllBlank('toExport');"><i class="fa fa-file fa-fw"></i>Export selected as CSV</button>
						</div>
						</form>
					</c:when>
					<c:otherwise>
						<p><strong>The search did not return any matches. Try refining your search criteria.</strong></p>
						<form method="get" action="/people/index.jsp" style="display: inline-block;">
							<button type=submit class="btn btn-primary">Simple search</button>
						</form>
						<form method="get" action="/people/advancedSearch.jsp" style="display: inline-block;">
							<button type=submit class="btn btn-primary">Advanced search</button>
						</form>
					</c:otherwise>
					</c:choose>
			</c:otherwise>
		</c:choose>
	</div>
	</div>
	<%@ include file="../WEB-INF/include/footer.jsp" %>
</body>
</html>