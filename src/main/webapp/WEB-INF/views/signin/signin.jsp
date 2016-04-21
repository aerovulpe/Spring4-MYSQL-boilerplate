<%--
  Created by IntelliJ IDEA.
  User: Aaron
  Date: 22/03/2016
  Time: 8:02 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container" style="margin-top: -0px; padding-right: 0px; padding-top: 20px;">

    <a href="login/iba">Basic Auth Sign In</a>
    <a href="login/facebook">Facebook Sign In</a>
    <a href="login/admin/facebook">Facebook Sign In (Admin)</a>
    <a href="login/twitter">Twitter Sign In</a>
    <a href="login/oidc">Google OpenID Connect Sign In</a>

    <footer>
        <p>Vestibulum id ligula porta felis euismod semper.</p>
    </footer>
</div>
<!-- /container -->
profile : ${profile}