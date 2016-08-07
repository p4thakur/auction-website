<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html>
<head>
    <meta charset="utf-8">
    <title>Conversation</title>
    <jsp:useBean id="messagesLst" class="java.util.ArrayList" scope="request" />
    <jsp:useBean id="sendersLst" class="java.util.ArrayList" scope="request" />

    <link href="./css/skeleton.css" rel="stylesheet">
    <link href="./css/organism.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <c:if test="${not empty param.rid}">
    <!-- HEADER STUFF -->
    <div class="row">
        <div class="one column">
            <a href="#">
                <img class="u-max-full-width" src="./images/logo.png">
            </a>
        </div>
        <div class="offset-by-seven four columns">
            <ul class="nav u-full-width row">
                <li class="offset-by-one-third one-third column newMessage tooltip"><span class="tooltipFire">Messages</span>
                    <div class="tooltipText"><div class="tooltipMargin"></div>
                        <a href="/message.do?action=listInbox">Inbox</a>
                        <br/>
                        <a href="/message.do?action=listSent">Sent</a>
                    </div>
                </li>
                <li class="one-third column">
                    <a href="logout.do"><span class="delete">Logout</span></a>
                        <%--<form action="logout.do" method="post"> <input type="submit" value="Logout"/></form>--%>
                </li>
            </ul>
        </div>
    </div>
    <!-- end of header row -->

    <!-- MESSAGES -->
    <div class="row">
        <main class="u-full-width ">
            <div class="row u-full-width">
                <form action="message.do" method="POST">
                    <input type="hidden" name="rid" value=${rid} />
                    <input type="hidden" name="aid" value=${aid} />
                    <input type="text" name="message_text" required class="u-full-width">
                    <button class="button-primary" type="submit" name="action" value="addNewMessage">send</button>
                </form>
            </div>
            <c:if test="${not empty messagesLst}">
                <c:forEach var="message" items="${messagesLst}" varStatus="status">
                    <c:if test="${uid == sendersLst[status.index].userId}">
                        <p class="message message--you"><span class="message__composer">you:</span> <span class="message__text">${message.message}</span> <span class="message__time">${message.sendDate}</span></p>
                    </c:if>
                    <c:if test="${uid != sendersLst[status.index].userId}">
                        <p class="message message--other"><span class="message__composer">${sendersLst[status.index].firstname} ${sendersLst[status.index].lastname}:</span> <span class="message__text">${message.message}</span> <span class="message__time">${message.sendDate}</span></p>
                    </c:if>
                </c:forEach>
            </c:if>
            <hr />
        </main>
    </div>
    <!-- end of messages row -->
    </c:if>

</div>
</body>
</html>