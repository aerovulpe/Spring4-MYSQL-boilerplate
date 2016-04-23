<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ include file="/WEB-INF/views/include.jsp" %>

<div class="container" style="margin-top: -0px; padding-right: 0px; padding-top: 20px;" >
		<div class="content" style="margin-left: 0px; "  >
			<div class="row">
			<div class="page-header">
				<h1>Disabled Users</h1>
			</div>
				<form:form method="post" action="deleteAccounts" commandName="accountsToDeleteModel">
					<fieldset>
						<table id="sortTable" class="zebra-striped">
							<thead>
								<tr>
									<th>Id</th>
									<th>First Name</th>
									<th>Last Name</th>
									<th>Email</th>
									<th></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="userItem" items="${disabledAccounts}">
									<tr>
										<td><a href="./updateUser/${userItem.username}/"><c:out value="${userItem.username}"/></a></td>
										<td><c:out value="${userItem.firstName}"/></td>
										<td><c:out value="${userItem.lastName}"/></td>
										<td><c:out value="${userItem.email}"/></td>
										<td style="text-align: center;">
											<form:checkbox path="deactivate" value="${userItem.username}"/>
										</td>
									<tr>
								</c:forEach>
							</tbody>
						</table>
						<div class="actions">
							<div class="row">
<!-- 								<div class="span11 offset3"> -->
<!-- 									<div class="pagination"> -->
<!-- 									  <ul> -->
<!-- 									    <li class="prev disabled"><a href="#">&larr; Previous</a></li> -->
<!-- 									    <li class="active"><a href="#">1</a></li> -->
<!-- 									    <li><a href="#">2</a></li> -->
<!-- 									    <li><a href="#">3</a></li> -->
<!-- 									    <li><a href="#">3</a></li> -->
<!-- 									    <li><a href="#">3</a></li> -->
<!-- 									    <li><a href="#">3</a></li> -->
<!-- 									    <li><a href="#">3</a></li> -->
<!-- 									    <li><a href="#">3</a></li> -->
<!-- 									    <li><a href="#">4</a></li> -->
<!-- 									    <li><a href="#">5</a></li> -->
<!-- 									    <li class="next"><a href="#">Next &rarr;</a></li> -->
<!-- 									  </ul> -->
<!-- 									</div>				 -->
<!-- 								</div> -->
									<input type="submit" class="btn primary" style="float: right;" value="Delete">
							</div>
						</div>
					</fieldset>
				</form:form>				
			</div>
			
		</div><!-- /content -->
</div> <!-- /container -->
	