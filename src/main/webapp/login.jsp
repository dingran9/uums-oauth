<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en-us">
<head>
    <meta charset="utf-8">
    <!--<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">-->

    <title> 登录 </title>
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Use the correct meta names below for your web application
         Ref: http://davidbcalhoun.com/2010/viewport-metatag-->

    <meta name="HandheldFriendly" content="True">
    <meta name="MobileOptimized" content="320">

    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">

    <!-- Basic Styles -->
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/bootstrap.css?version=4.2.6.2">
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/font-awesome.min.css?version=4.2.6.2">

    <!-- SmartAdmin Styles : Please note (smartadmin-production.css) was created using LESS variables -->
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/smartadmin-production.css?version=4.2.6.2">
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/smartadmin-skins.css?version=4.2.6.2">

    <!-- We recommend you use "your_style.css" to override SmartAdmin
         specific styles this will also ensure you retrain your customization with each SmartAdmin update.-->
    <%--<link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/your_style.css?version=4.2.6.2">--%>
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/login.css?version=4.2.6.2">

    <!-- Demo purpose only: goes with demo.js, you can delete this css when designing your own WebApp -->
    <link rel="stylesheet" type="text/css" media="screen" href="static/smartadmin/css/demo.css?version=4.2.6.2">

    <!-- FAVICONS -->
    <link rel="shortcut icon" href="static/smartadmin/img/favicon/favicon.ico" type="image/png">
    <link rel="icon" href="static/smartadmin/img/favicon/favicon.ico" type="image/png">

    <!-- GOOGLE FONT -->
    <!--<link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Open+Sans:400italic,700italic,300,400,700">-->
    <link rel="stylesheet" href="static/smartadmin/fonts/fonts.googleapis.com.css?version=4.2.6.2">

</head>
<body class="smart-style-2 cniaas" style="overflow-y:scroll;" id="login">
<!-- possible classes: minified, fixed-ribbon, fixed-header, fixed-width-->

<!-- HEADER -->
<%--<header id="header" class="text-center" style="height: 77px;">
    <div style="width: 0;"></div>
    <div class="container" style="float: none;">
        <div id="logo-group" style="margin-top: 13px;">

            <!-- PLACE YOUR LOGO HERE -->
            <span id="logo" style="margin-top: 7px;"> <img src="/static/smartadmin/img/logo_ezcloud.png" alt="SmartAdmin"> </span>
            <!-- END LOGO PLACEHOLDER -->

        </div>

        <!-- custom header navigation -->
        <div class="pull-right">
            <ul class="nav navbar-nav">
                <li><a class="navbar-li-a " href="/index.html">首页</a></li>
                <li><a class="navbar-li-a" href="/action/cniaas/products.html">产品</a></li>
                <li><a class="navbar-li-a" href="/action/cniaas/userCenter.html">用户中心</a></li>
                <li><a class="navbar-li-a" href="/action/cniaas/consoleIndex.html">控制台</a></li>

                <li class="notLogin"><a class="navbar-li-a active" href="../../login.jsp">登录</a></li>

                <li class="registerBtn" style="display: none;"><a class="navbar-li-a ezcloud-btn-success" href="../../register.html">注册</a></li>
                &lt;%&ndash;<li class="notLogin"><a class="navbar-li-a ezcloud-btn-success" href="../../register.html" >注册</a></li>&ndash;%&gt;
                <li id="loginSucc" style="display: none;width: auto;"></li>
                <li id="li-logout"><a class='navbar-li-a' onclick='logout()'>退出</a></li>
            </ul>
        </div>
    </div>
</header>--%>
<!-- END HEADER -->
<div class="row" style="background: url('static/smartadmin/img/login-bg.jpg') no-repeat  top center;">


    <!-- MAIN CONTENT -->

    <div id="content" class="container">

        <div class="row">
            <div class="col-xs-offset-1 col-xs-10 col-sm-6 col-md-5 col-lg-4 not-mobile-pull-right" >
                <div class="well no-padding"  style="">
                    <form  id="login-form" class="smart-form client-form" >
                        <header class="text-center">
                            用户登陆
                        </header>

                        <fieldset>
                            <section>
                                <label class="input"> <i class="icon-prepend fa fa-user"></i>
                                    <input type="text" name="user" id="user" placeholder="邮箱 / 手机号">
                                    <b class="tooltip tooltip-top-right"><i class="fa fa-user txt-color-teal"></i> 请输入用户名</b></label>
                            </section>


                            <section><input type="password" style="display: none;">
                                <label class="input"> <i class="icon-prepend fa fa-lock"></i>
                                    <input type="password" name="password" id="pass" placeholder="密码" autocomplete="false">
                                    <b class="tooltip tooltip-top-right"><i class="fa fa-lock txt-color-teal"></i> 请输入密码</b> </label>
                            </section>
                            <div class="row" style="margin-left: -7px;">
                                <section class="col col-12">
                                    <input type="text"  name="verifyCode-login" class="col-lg-5 col-md-6 col-sm-6 col-xs-6" style="font-size: 16px;height: 26px;border-radius: 5px;" />
                                    <span class="col-lg-6 col-md-6 col-sm-6 col-xs-6" style="height: 26px;margin-left: 12px;">
                                        <img id="verifyCode" class="verify-code-img"  style="height: 26px;width: 50%;" src="#"  />
                                        <span class="verify-code-text" style="width: 50%;line-height: 26px;margin-left: 10px;">
                                            <i class="fa fa-refresh" style="color: #fff;"></i><a style="font-size: 14px;color: #fff;" onclick="refreshVerifyCode()">换一张</a></span>
                                    </span>

                                </section>

                            </div>
                            <label class="login-note-error ezcloud-red" style="display: none;"></label>
                            <section style="margin-left: 15px;margin-top: 15px;margin-bottom: 10px;">
                                <label class="checkbox">
                                    <input type="checkbox" name="rmb_user" id="rmbUser" class="small" checked="checked">
                                    <i></i>记住登录帐号和密码</label>
                            </section>

                            <section>
                                <label class="input">
                                    <input type="button" name="submit" id="loginBtn" onclick="login()" value="登&nbsp;&nbsp;陆">
                                </label>
                            </section>
                            <div class="row" style="margin-top: -15px;margin-left: 10px;">
                                <section class="col-lg-12 form-group">
                                    <span style="padding-left: 13px;" class="forget-password"><a href="#">忘记密码？</a></span>
                                    <span class="registerBtn" style="display: none;">没有账号，<a href="#">注册</a>一个</span>
                                </section>
                            </div>


                        </fieldset>
                    </form>

                </div>

            </div>
        </div>
    </div>
</div>
<%--<div id="content" class="container" >
    <div id="login" style="margin-top: 2%;">
        <form id="login-form" class="smart-form ms-yahei" novalidate="novalidate">
            <fieldset class="col-lg-3  col-md-3 col-sm-4 col-lg-offset-4" style="margin-left: auto;margin-right: auto;float: none;">
                <div class="row">
                    <section class="col col-12 text-center">
                        <i class="fa fa-user fa-4x"></i>
                    </section>

                </div>
                <div id="user-not-login-div">
                    <div class="row">
                        <section class="col col-12">
                            <label class="input"> <i class="icon-append fa fa-user"></i>
                                <input type="text" name="user" id="user" placeholder="邮箱 / 手机号">
                            </label>
                        </section>

                    </div>

                    <div class="row">
                        <section class="col col-12">
                            <label class="input"> <i class="icon-append fa fa-lock"></i>
                                <input type="password" name="password" id="pass" placeholder="密码">
                            </label>

                        </section>

                    </div>
                    <div class="row">
                        <section class="col col-12">
                            <input type="text"  name="verifyCode-login" class="col-lg-5 col-md-6 col-sm-12 col-xs-12" style="height: 26px;">
                            <span class="col-lg-6 col-md-6 col-sm-12 col-xs-12" style="height: 26px;">
                                <img id="verifyCode" class="verify-code-img"  style="height: 26px;width: 50%;" src="/action/userAccount/verifyCode?type=1"  />
                                <span class="verify-code-text" style="width: 50%;line-height: 26px;margin-left: 10px;">
                                    <i class="fa fa-refresh"></i><a style="font-size: 14px;" onclick="refreshVerifyCode()">换一张</a></span>
                            </span>

                        </section>

                    </div>
                    <label class="login-note-error ezcloud-red" style="display: none;"></label>
                    <div class="row">
                        <section class="col col-12 form-group">
                            <label class="checkbox">
                                <input type="checkbox" name="rmb_user" id="rmbUser" class="small" checked="checked">
                                <i></i>记住登录帐号和密码</label>
                        </section>
                    </div>
                    <div class="row text-center">
                        <section class="col col-12 form-group">
                            <div style="width: 100%;">
                                <a id="loginBtn" onclick="login()" class="btn disabled txt-color-darken" style="padding: 5px;width: 100%;" >登录</a>
                            </div>
                        </section>
                    </div>
                    <div class="row">
                        <section class="col-lg-12 form-group">
                            <span style="padding-left: 13px;" class="forget-password"><a href="resetpassword-email.html">忘记密码？</a></span>
                            <span class="registerBtn" style="display: none;">没有账号，<a href="register.html">注册</a>一个</span>
                        </section>
                    </div>
                </div>
                <div id="user-login-div" style="display: none;text-align: center;vertical-align:middle;">
                    <div class="input-group1 input-group" style="font-family:'Microsoft YaHei';text-align: center;vertical-align:middle;">
                        <span> 您目前已登录账户 : <%=session.getAttribute("userAccountEmail")%></span>
                    </div>
                    <div class="input-group1 input-group input-group-sm center-block" style="padding-top: 50px;">
                        <a onclick="handleLogout()" class="btn btn-primary col-lg-12 col-md-12 col-sm-12 ">登&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;出</a>
                    </div>
                </div>
            </fieldset>
        </form>
    </div>
</div>--%>
<div class="footer1">
    <span class="text-center col-lg-12 col-md-12 col-sm-12 col-xs-12"  id="copyrightInfo">Copyright @ 2014 CNIaas.com</span>
</div>
<script src="static/smartadmin/js/libs/jquery-2.0.2.min.js?version=4.2.6.2"></script>
<script src="static/smartadmin/js/bootstrap/bootstrap.min.js?version=4.2.6.2"></script>
<%--<script src="static/smartadmin/js/notification/SmartNotification.min.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/plugin/jquery-validate/jquery.validate.min.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/plugin/jquery-validate/jquery.validate.additional-methods.js?version=4.2.6.2"></script>--%>
<script src="static/smartadmin/js/libs/ajax.js?version=4.2.6.2"></script>
<%--<script src="static/smartadmin/js/libs/tools.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/libs/md5.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/include.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/libs/i18n/dynamicLocale.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/libs/i18n/i18n_locale.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/libs/jquery.cookie.js?version=4.2.6.2"></script>--%>
<%--<script src="static/smartadmin/js/ezcloud/login.js?version=4.2.6.2"></script>--%>
<script type="text/javascript">
/*    loadHeader("login.jsp","登陆");
    function handleLogout(){
        doPost("/action/userAccount/logout",{},function(){
            window.location.replace("login.jsp");
        });
    }*/
</script>
</body>
</html>