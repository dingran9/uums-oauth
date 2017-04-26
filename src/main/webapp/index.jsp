<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en-us" xmlns:wb="http://open.weibo.com/wb">
<head>
    <meta charset="utf-8">
    <title>新国人用户中心</title>
    <meta property="qc:admins" content="142620104553655545301356375" />
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="HandheldFriendly" content="True">
    <meta name="MobileOptimized" content="320">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

    <!-- Basic Styles -->
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/bootstrap.css?version=4.2.6.2">
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/font-awesome.min.css?version=4.2.6.2">

    <!-- SmartAdmin Styles : Please note (smartadmin-production.css) was created using LESS variables -->
    <%--<link rel="stylesheet" type="text/css" media="screenmybg.png" href="static/smartadmin/css/smartadmin-production.css?version=4.2.6.2">--%>

    <!-- Demo purpose only: goes with demo.js, you can delete this css when designing your own WebApp -->
    <%--<link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/your_style.css?version=4.2.6.2">--%>
    <!-- FAVICONS -->
    <link rel="shortcut icon" href="static/smartadmin/img/favicon/favicon.ico" type="image/png">
    <link rel="icon" href="static/smartadmin/img/favicon/favicon.ico" type="image/png">
    <script src="http://tjs.sjs.sinajs.cn/open/api/js/wb.js?appkey=2090809038" type="text/javascript" charset="utf-8"></script>
    <!--[if IE 9]>
    <style>
        .error-text {
            color: #333 !important;
        }
    </style>
    <![endif]-->

</head>
<body>
<!-- MAIN PANEL -->
<div id="main" role="main" style="width: 100%;margin-left:0;">

<div style="text-align: center;height: 30px;font-size: 30px;margin-top: 20px;">新国人用户中心</div>
<div style="text-align: center;height: 30px;font-size: 30px;margin-top: 20px;" >
    <wb:login-button type="3,2" onlogin="login" onlogout="logout">登录按钮</wb:login-button>
    <wb:follow-button uid="2090809038" type="red_1" width="67" height="24" ></wb:follow-button>
</div>
<%--    <div id="wb_connect_btn" ></div>--%>
</div>
<!-- END MAIN PANEL -->


<!--================================================== -->

<!-- PACE LOADER - turn this on if you want ajax loading to show (caution: uses lots of memory on iDevices)-->
<script data-pace-options='{ "restartOnRequestAfter": true }' src="static/smartadmin/js/plugin/pace/pace.min.js?version=4.2.6.2"></script>

<!-- Link to Google CDN's jQuery + jQueryUI; fall back to local -->
<!--<script src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.2/jquery.min.js?version=4.2.6.2"></script>-->
<script>
    if (!window.jQuery) {
        document.write('<script src="static/smartadmin/js/libs/jquery-2.0.2.min.js?version=4.2.6.2"><\/script>');
    }
</script>

<script>
    if (!window.jQuery.ui) {
        document.write('<script src="static/smartadmin/js/libs/jquery-ui-1.10.3.min.js?version=4.2.6.2"><\/script>');
    }
</script>

<!-- JS TOUCH : include this plugin for mobile drag / drop touch events
<script src="js/plugin/jquery-touch/jquery.ui.touch-punch.min.js?version=4.2.6.2"></script> -->

<!-- BOOTSTRAP JS -->
<script src="static/smartadmin/js/bootstrap/bootstrap.min.js?version=4.2.6.2"></script>

<!--[if IE 7]>

<h1>您所使用的浏览器版本过低，为保证使用体验，请升级到最新版本。</h1>

<![endif]-->
<script>

    function login(o) {
        console.log(o.screen_name)
    }

    function logout() {
        console.log('logout');
    }
/*    WB2.anyWhere(function (W) {
        W.widget.connectButton({
            id: "wb_connect_btn",
            type: '3,2',
            callback: {
                login: function (o) { //登录后的回调函数
                    alert("login: " + o.screen_name)
                },
                logout: function () { //退出后的回调函数
                    alert('logout');
                }
            }
        });
    });*/
</script>
</body>

</html>