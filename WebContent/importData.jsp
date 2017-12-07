<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Import data</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
<link href="css/font-awesome.min.css" rel="stylesheet">
<link href="css/boxAligner.css" rel="stylesheet">
</head>
<body>
<div class="vertical-center" align="center">
	<%@ include file="../WEB-INF/include/header.jsp" %>
	<div class="container">
	
	
	<h2>Import data</h2>
	<p>Import a file (currently supported: csv). An example file will be provided soon.</p>
	<form method="post" action="ImportDataServlet" enctype="multipart/form-data">
	<table>
	<tr>
		<td>
			<select name="format" class="form-control form-control-inline">
			<option value="csv">CSV</option>
			<option value="other">Other</option>
			</select>
		</td>
		<td>
			<input name="toUpload" type="file" enctype="multipart/form-data" class="form-control form-control-inline form-control-file" multiple required></input>
		</td>
		<td>
			<button type="submit" value="Upload" class="btn btn-primary">Upload</button>
		</td>
	</tr>
	</table>
	</form>
	
	</div>
</div>

</body>
</html>