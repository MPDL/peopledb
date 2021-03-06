<%@page import="org.apache.commons.lang3.tuple.ImmutableTriple"%>
<%@page import="org.apache.commons.lang3.tuple.Triple"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="java.util.LinkedList"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ include file="../WEB-INF/include/db.jsp" %>
<%
	// TODO: convert to JSTL, model is in batch edit page
	// gets a list of all available properties
	int widgets;
	if (request.getParameter("more_criteria") == null) {
		widgets = 1;
	}
	else {
		widgets = Integer.parseInt(request.getParameter("more_criteria"));
	}

	Statement propStatement = connection.createStatement();
	ResultSet propertySet = propStatement.executeQuery("SELECT property.*, property_group.name AS group_name FROM property, property_group WHERE property_group = property_group_id ORDER BY (property_group.name != 'Basic Data'), property_group.name, property.property_id ASC");
	
	Map<String, List<Triple<String, String, String>>> propertyMap = new LinkedHashMap<>();
	while (propertySet.next()) {
		String groupName = propertySet.getString("group_name");
		if (!propertyMap.containsKey(groupName)) {
			propertyMap.put(groupName, new LinkedList<Triple<String, String, String>>());
		}
		String dbName = propertySet.getString("db_name");
		String name = propertySet.getString("name");
		String type = propertySet.getString("type");
		propertyMap.get(groupName).add(new ImmutableTriple<String, String, String>(dbName, type, name));
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Advanced Search</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/font-awesome.min.css" rel="stylesheet">
<link href="css/boxAligner.css" rel="stylesheet">
<script>
function changeInputType(sel, id) {
	var inputBox = document.getElementById('value' + id);
	var valueSelect = document.getElementById('booleanSelect' + id);
	if (sel.value.split('§')[1] == 'boolean') {
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
		if (sel.value.split('§')[1] == 'decimal') {
			inputBox.type = 'number';
			inputBox.step='0.0001';
		}
		else {
			inputBox.type = sel.value.split('§')[1]
		}
	}
}
function disableIllegalInputs(compareSel, matchSelID) {
	var matchSel = document.getElementById('queryMatch' + matchSelID);
	if (compareSel.value === 'biggerOrEqual' || compareSel.value === 'smallerOrEqual') {
		matchSel.disabled = true;
	}
	else {
		matchSel.disabled = false;
	}
}
function disablePartialMatch(typeSel, matchSelID) {
	var matchSel = document.getElementById('queryMatch' + matchSelID);
	if (typeSel.value.split('§')[1] == 'character_varying') {
		matchSel[1].disabled = false;
		matchSel[1].style.background = "rgba(255, 255, 255, 0.3)";
	}
	else {
		matchSel.selectedIndex = 0; 
		matchSel[1].disabled = true;
		matchSel[1].style.background = "rgba(100, 100, 100, 0.3)";
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
<body onload="return loadTypeInputs(<%= widgets %>);">
	<div class="vertical-center" align="center">
		<%@ include file="../WEB-INF/include/header.jsp" %>
		<div class="container">
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
		<h2>Advanced Search</h2>
		<div class="jumbotron">
		<form method="get" action="AdvancedQueryServlet">
		<div class="form-group">
		<table>
		<% for (int widget = 1; widget <= widgets; widget++) { %>
			<tr name="widget">
			<td>
				<select class="form-control form-control-inline" name="property<%= + widget %>" onchange="changeInputType(this,<%= widget %>); disablePartialMatch(this,<%= widget %>)">
				<% 
				for (Map.Entry<String, List<Triple<String, String, String>>> entry : propertyMap.entrySet()) { %>
						<optgroup label="<%= entry.getKey() %>">
						<% for (Triple<String, String, String> colNames : entry.getValue()) { %>
								<option name="option<%= widget %>" value="<%= colNames.getLeft() + "§" + colNames.getMiddle() %>"
								<% if (request.getParameter("property" + widget) != null && request.getParameter("property" + widget).equals(colNames.getLeft()+ "§" + colNames.getMiddle())) { %>
								selected 
								<% } %>
								><%= colNames.getRight() %></option>
						<% } %>
						</optgroup>
				<% 	}
				%>
			</select>
			</td>
			<td>
			<select class="form-control form-control-inline" name="lexicographic<%= +widget %>" onchange="disableIllegalInputs(this,<%= widget %>)">
				<option value="is" 
					<% if (request.getParameter("lexicographic" + widget) != null && request.getParameter("lexicographic" + widget).equals("is")) { %> 
						selected 
					<% } %>  >is</option>
				<option value="isnot" 
					<% if (request.getParameter("lexicographic" + widget) != null && request.getParameter("lexicographic" + widget).equals("isnot")) { %> 
						selected 
					<% } %>  >is not</option>
				<option value="biggerOrEqual" 
					<% if (request.getParameter("lexicographic" + widget) != null && request.getParameter("lexicographic" + widget).equals("biggerOrEqual")) { %> 
						selected 
					<% } %>  >>=</option>
				<option value="smallerOrEqual" 
					<% if (request.getParameter("lexicographic" + widget) != null && request.getParameter("lexicographic" + widget).equals("smallerOrEqual")) { %> 
						selected 
					<% } %>  ><=</option>
			</select>
			</td>
			<%-- Choose if exact or partial match --%>
			<td>
				<select class="form-control form-control-inline" id="queryMatch<%= +widget %>" name="queryMatch<%= +widget %>">
					<option value="matchExact"
					<% if (request.getParameter("queryMatch" + widget) != null && request.getParameter("queryMatch" + widget).equals("matchExact")) { %> 
						selected 
					<% } %>
					>exactly</option>
					<option value="matchLike"
					<% if (request.getParameter("queryMatch" + widget) != null && request.getParameter("queryMatch" + widget).equals("matchLike")) { %> 
						selected 
					<% } %>
					>like</option>
				</select>
			</td>
			<td>
			<%-- Dynamic input element for the search value --%>
			<c:set var = "previousBooleanValue" scope = "session" value = "<%= request.getParameter(\"booleanSelect\" + widget)%>"/>
			<% if (widgets == 1) { %>
				<input required class="form-control form-control-inline" type="text" id="value<%=+widget %>" name="value<%=+widget %>" value="<%= request.getParameter("value" + widget) != null ? request.getParameter("value" + widget) : "" %>"/>
				<select required style="display: none;" class="form-control form-control-inline" id="booleanSelect<%= +widget %>" name="booleanSelect<%= +widget %>">
					<option value="TRUE">true</option>
					<option value="FALSE">false</option>
					<option value="NULL">unknown</option>
				</select>
			<% } 
				else {%>
			<input class="form-control form-control-inline" type="text" id="value<%=+widget %>" name="value<%=+widget %>" value="<%= request.getParameter("value" + widget) != null ? request.getParameter("value" + widget) : "" %>"/>
			<select style="display: none;" class="form-control form-control-inline" id="booleanSelect<%= +widget %>" name="booleanSelect<%= +widget %>">
				<c:choose>
					<c:when test="${previousBooleanValue eq 'TRUE'}">
						<option value="TRUE" selected>true</option>
						<option value="FALSE">false</option>
						<option value="NULL">unknown</option>
					</c:when>
					<c:when test="${previousBooleanValue eq 'FALSE'}">
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
			<% } %>
			</td>
			</tr>
			<% } %>
			</table>
			<div class="saveGroup">
				<input type="checkbox" name="show_deleted" value="true" checked/> Show deleted
			</div>
			<div class="saveGroup"><button type="submit" name="advanced_search" value="Search" class="btn btn-primary">Search</button></div>
		</form>
		<button type="submit" name="more_criteria" value=<%= ++widgets %> class="btn  btn-default btn-sm saveGroup" formmethod="get" formaction="advancedSearch.jsp"><i class="fa fa-plus icon-center"></i>Add criteria</button>
		</div>
		</div>
		<%@ include file="../WEB-INF/include/footer.jsp" %>
	</div>
	</div>
	</div>
</body>
</html>