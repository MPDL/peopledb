<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>The MPDL People</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/font-awesome.min.css" rel="stylesheet">
<link href="css/boxAligner.css" rel="stylesheet">
<script>
function changeInputType(sel, id) {
	var inputBox = document.getElementById('new_prop' + id);
	var valueSelect = document.getElementById('booleanSelect' + id);
	if (sel.value.split('$')[1] == 'boolean') {
		inputBox.required = false;
		inputBox.style.display = 'none';
		valueSelect.style.display = 'inline';
	}
	else {
		if (id == 1) {
			inputBox.required = true;
		}
		valueSelect.style.display = 'none';
		inputBox.style.display = 'inline';
		if (sel.value.split('$')[1] == 'decimal') {
			inputBox.type = 'number';
			inputBox.step='0.0001';
		}
		else {
			inputBox.type = sel.value.split('$')[1]
		}
	}
}
// prevents input type from switching to text after adding criteria
function loadTypeInputs(widgets) {
	for (var i = 1; i <= widgets; i++) {
		changeInputType(document.getElementsByName('property' + i)[0], i);
	}
	return;
}
</script>
</head>
<body onload="return loadTypeInputs(${widgets});">
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
	<h3>Batch update</h3>
	<div class="jumbotron">
	<form method="post" action="BatchEditResultServlet">
			<input name="current_query" type="hidden" value="${current_query}"/>
			<label for="toChoose">Select entries to edit:</label>
			<select class="form-control" name="toChoose" multiple>
				<c:forEach items="${result.rows}" var="currentRow">
				<option name="approved" value="${currentRow.person_id}" selected>
					${currentRow.basic_data_given_name} ${currentRow.basic_data_family_name}
				</option>
				</c:forEach>
			</select>
			<table>
			<c:forEach var = "widget" begin = "1" end = "${widgets}">
				<tr>
				<td>
				<%-- After adding edit criteria each select element remembers its selected value --%>
				<select class="form-control" name="property${widget}" id="property${widget}" onchange="changeInputType(this,${widget})">
					<c:forEach items="${map}" var="entry">
						<optgroup label="${entry.key}">
							<c:forEach items="${entry.value}" var="colNames">
								<c:set var = "currentWidget" scope = "session" value = "property${widget}"/>
								<c:set var = "valueCurrentWidget" scope = "session" value = "${param[currentWidget]}"/>
								<c:set var = "valueCurrentOption" scope = "session" value = "${colNames.left}$${colNames.middle}"/>
								<c:choose>
									<c:when test="${valueCurrentWidget eq valueCurrentOption}">	
										<option value="${valueCurrentOption}" selected>${colNames.right}</option>
									</c:when>
									<c:otherwise>
										<option value="${valueCurrentOption}">${colNames.right}</option>
									</c:otherwise>
									</c:choose>
							</c:forEach>
						</optgroup>
					</c:forEach>
				</select>
				</td>
				<td>
				
				<%-- After adding edit criteria each text box remembers its specified value --%>
				<c:set var = "previousValue" scope = "session" value = "new_prop${widget}"/>
				<c:set var = "previousBooleanValue" scope = "session" value = "booleanSelect${widget}"/>
				<c:choose>
					<c:when test="${widgets eq 1}">
						<input required class="form-control" type="text" id="new_prop${widget}" name="new_prop${widget}" id="new_prop${widget}" value="${param[previousValue]}" required/>
						<select required style="display: none;" class="form-control form-control-inline" id="booleanSelect${widget}" name="booleanSelect${widget}">
							<option value="TRUE">true</option>
							<option value="FALSE">false</option>
							<option value="NULL">unknown</option>
						</select>
					</c:when>
					<c:otherwise>
						<input class="form-control" type="text" id="new_prop${widget}" name="new_prop${widget}" id="new_prop${widget}" value="${param[previousValue]}"/>
						<select style="display: none;" class="form-control form-control-inline" id="booleanSelect${widget}" name="booleanSelect${widget}">
							<c:choose>
								<c:when test="${param[previousBooleanValue] eq 'TRUE'}">
									<option value="TRUE" selected>true</option>
									<option value="FALSE">false</option>
									<option value="NULL">unknown</option>
								</c:when>
								<c:when test="${param[previousBooleanValue] eq 'FALSE'}">
									<option value="TRUE">true</option>
									<option value="FALSE" selected>false</option>
									<option value="NULL">unknown</option>
								</c:when>
								<c:otherwise>
									<option value="TRUE">true</option>
									<option value="FALSE">false</option>
									<option value="NULL" selected>unknown</option>
								</c:otherwise>
							</c:choose>
						</select>
					</c:otherwise>
				</c:choose>
			</td>
			</tr>
			</c:forEach>
			</table>
			<button type="submit" name="batch_edit" value="Apply" class="btn btn-primary">Apply</button>
			<button type="submit" formmethod="post" formaction="BatchEditServlet" name="editSelected" value="${widgets + 1}" class="btn btn-default btn-sm"><i class="fa fa-plus"></i>Add criteria</button>
			</form>
			</div>
			</c:otherwise>
	</c:choose>
	</div>
	</div>
	<%@ include file="../WEB-INF/include/footer.jsp" %>
</body>
</html>