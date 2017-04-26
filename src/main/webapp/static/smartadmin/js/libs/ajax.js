/**
 * ajax工具类
 * @param url
 * @param data
 * @param callback
 */

function doGet(url, data, callback) {
    get(url, data, callback);
}

function doSnycGet(url, data, callback) {
    syncGet(url, data, callback);
}

function doSnycPost(url, data, callback) {
    syncPost(url, data, callback);
}
function doPostLogin(url, data, callback) {
    post(url, data, callback);
}
function doPost(url, data,callback,a) {
    postHeaderJson(url, data, callback,a);
}

function doPut(url, data, callback) {
    put(url, data, callback);
}

function doDelete(url, data, callback) {
    del(url, data, callback);
}

/**
 * get请求
 * @param url
 * @param data
 * @param callback
 */
function get(url, data, callback) {
    // 对请求数据进行编码
    var _data = null;
    /*if (data != null && data != "")
        _data = encodeURI($.toJSON(data));*/
    // 发送 Http:get 请求
    $.ajax({
        url : url,
        type : 'GET',
//        cache : false,
        //contentType: "application/json; charset=utf-8",
        data : data,
        success : function(json) {
            if(json=='<script type="text/javascript">window.location.href = "/login.jsp";</script>'){
                window.location.replace("/login.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/error400.jsp";</script>'){
                window.location.replace("/error400.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/errorNoOpen.jsp";</script>'){
                window.location.replace("/errorNoOpen.jsp");
            }else
            if(json.httpCode == 403 && json.code == "Forbidden.NoPermission"){
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="403" && json.code =="Forbidden"){
                console.log("code:403 , message:服务器拒绝请求，请检查帐号、口令等参数  ,  code:Frobidden");
                window.location.href = "../../../../login.jsp";
            }else if(json.httpCode=="400" && json.code =="Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="400" && json.code =="Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else{
                callback(json);
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(textStatus.error + "" + errorThrown);
            if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden.NoPermission"){
                showErrorMsg("","没有操作权限。");
            }else if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden"){
                window.location.href = "../../../../login.jsp";
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg("","未知的操作请求。");
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg("","请求参数无效");
            }else{
                console.log("服务器内部出错啦！");
                callback('{"code":"500"}');
            }
        }
   //     dataType : 'json'
    });
}
/**
 * 同步请求
 * @param url
 * @param data
 * @param callback
 */
function syncGet(url, data, callback) {
    // 对请求数据进行编码
    var _data = null;
    if (data != null && data != "")
        _data = encodeURI($.toJSON(data));
    // 发送 Http:get 请求
    $.ajax({
        url : url,
        type : 'GET',
//        cache : false,
        async:false,
  //      contentType: "application/json; charset=utf-8",
        data : _data,
        success : function(json) {
            if(json=='<script type="text/javascript">window.location.href = "/login.jsp";</script>'){
                window.location.replace("/login.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/error400.jsp";</script>'){
                window.location.replace("/error400.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/errorNoOpen.jsp";</script>'){
                window.location.replace("/errorNoOpen.jsp");
            }else
            if(json.httpCode == 403 && json.code == "Forbidden.NoPermission"){
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="403" && json.code =="Forbidden"){
                console.log("code:403 , message:服务器拒绝请求，请检查帐号、口令等参数  ,  code:Frobidden");
                window.location.href = "../../../../login.jsp";
            }else if(json.httpCode=="400" && json.code =="Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="400" && json.code =="Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else{
                callback(json);
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(textStatus.error + "" + errorThrown);
            if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden.NoPermission"){
                showErrorMsg("","没有操作权限。");
            }else if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden"){
                window.location.href = "../../../../login.jsp";
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg("","未知的操作请求。");
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg("","请求参数无效");
            }else{
                console.log("服务器内部出错啦！");
                callback('{"code":"500"}');
            }
        }
   //     dataType : 'json'
    });
}

/**
 * 同步请求（POST）
 * @param url
 * @param data
 * @param callback
 */
function syncPost(url, data, callback) {
    $.ajax({
        url : url,
        type : 'POST',
        async:false,
        data : data,
        success : function(json) {
            if(json=='<script type="text/javascript">window.location.href = "/login.jsp";</script>'){
                window.location.replace("/login.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/error400.jsp";</script>'){
                window.location.replace("/error400.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/errorNoOpen.jsp";</script>'){
                window.location.replace("/errorNoOpen.jsp");
            }else{
                callback(json);
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(textStatus.error + "" + errorThrown);
            if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden.NoPermission"){
                showErrorMsg("","没有操作权限。");
            }else if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden"){
                window.location.href = "../../../../login.jsp";
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg("","未知的操作请求。");
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg("","请求参数无效");
            }else{
                console.log("服务器内部出错啦！");
                callback('{"code":"500"}');
            }
        }
    });
}
/**
 * post请求，带Header json格式
 * @param url
 * @param data
 * @param key
 * @param val
 * @param callback
 * @returns
 */
function postHeaderJson(url,data,callback,a){
        if(a==undefined){
            a=true;
        }
    $.ajax({
        type : 'POST',
        url : url,
        data : data,
        success : function(json) {
            if(json=='<script type="text/javascript">window.location.href = "/login.jsp";</script>'){
                window.location.replace("/login.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/error400.jsp";</script>'){
                window.location.replace("/error400.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/errorNoOpen.jsp";</script>'){
                window.location.replace("/errorNoOpen.jsp");
            }else
            if(json.httpCode == 403 && json.code == "Forbidden.NoPermission"){
                console.log(json.message);
                failNotification("没有权限","请于管理人员联系",function(){});
            }else if(json.httpCode=="403" && json.code =="Forbidden"){
                failNotification(rpL(json.code),rpL(json.message),function(){});
                console.log("code:403 , message:服务器拒绝请求，请检查帐号、口令等参数  ,  code:Frobidden");
                window.location.href = "../../../../login.jsp";
            }else if(json.httpCode=="400"){
                if(a){
                    callback(json);
                    return;
                }
                failNotification(rpL(json.code),rpL(json.message),function(){});
                //callback(json);
            }else if(json.httpCode=="500" && json.code =="Service.Error"){
                failNotification(rpL(json.code),rpL(json.message),function(){});
                //callback(json);
            }else if(json.httpCode=="404"){
                failNotification(rpL(json.code),rpL(json.message),function(){});
            }else{
                callback(json);
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            //console.log(XMLHttpRequest.status)

            console.log(textStatus.error + "" + errorThrown);
            if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden.NoPermission"){
                console.log("没有操作权限。");
            }else if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden"){
                window.location.href = "../../../../login.jsp";
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                console.log("","未知的操作请求。");
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                console.log("","请求参数无效");
            }else{
                console.log("服务器内部出错啦！");
                callback('{"code":"500"}');
            }

        },
        async : a

    });
}

function post(url, data, callback) {
    // 发送 Http:post 请求
    $.ajax({
        type : 'POST',
        url : url,
        data : data,
        success : function(json) {
            if(json=='<script type="text/javascript">window.location.href = "/login.jsp";</script>'){
                window.location.replace("/login.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/error400.jsp";</script>'){
                window.location.replace("/error400.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/errorNoOpen.jsp";</script>'){
                window.location.replace("/errorNoOpen.jsp");
            }else
            if(json.httpCode == 403 && json.code == "Forbidden.NoPermission"){
                $(".login-note-error").html("没有相应权限").show();
            }else if(json.httpCode=="403" && json.code =="Forbidden"){
                console.log("code:403 , message:服务器拒绝请求，请检查帐号、口令等参数  ,  code:Frobidden");
                window.location.href = "../../../../login.jsp";
            }else if(json.httpCode=="400" && json.code =="Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                $(".login-note-error").html("服务器内部错误").show();
            }else{
                callback(json);
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(textStatus.error + "" + errorThrown);
            if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden.NoPermission"){
                showErrorMsg("","没有操作权限。");
            }else if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden"){
                window.location.href = "../../../../login.jsp";
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg("","未知的操作请求。");
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效,code:Parameter.Invalid");
                showErrorMsg("","请求参数无效");
            }else{
                console.log("服务器内部出错啦！");
                callback('{"code":"500"}');
            }
        }
    });
}

function put(url, data, callback) {
    // 发送 Http:put 请求
    $.ajax({
        url : url,
 //       contentType: "application/json; charset=utf-8",
        type : 'PUT',
//        cache : false,
//        dataType : 'json',
        data : data,
        success : function(json) {
            if(json=='<script type="text/javascript">window.location.href = "/login.jsp";</script>'){
                window.location.replace("/login.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/error400.jsp";</script>'){
                window.location.replace("/error400.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/errorNoOpen.jsp";</script>'){
                window.location.replace("/errorNoOpen.jsp");
            }else
            if(json.httpCode == 403 && json.code == "Forbidden.NoPermission"){
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="403" && json.code =="Forbidden"){
                console.log("code:403 , message:服务器拒绝请求，请检查帐号、口令等参数  ,  code:Frobidden");
                window.location.href = "../../../../login.jsp";
            }else if(json.httpCode=="400" && json.code =="Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="400" && json.code =="Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else{
                callback(json);
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(textStatus.error + "" + errorThrown);
            if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden.NoPermission"){
                showErrorMsg("","没有操作权限。");
            }else if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden"){
                window.location.href = "../../../../login.jsp";
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg("","未知的操作请求。");
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg("","请求参数无效");
            }else{
                console.log("服务器内部出错啦！");
                callback('{"code":"500"}');
            }
        }
    });
}

function del(url, data, callback) {
    // 对请求数据进行编码
    var _data = null;
    if (data != null && data != "")
        _data = encodeURI($.toJSON(data));
    // 发送 Http:delete 请求
    $.ajax({
        url : url,
  //      contentType: "application/json; charset=utf-8",
        type : 'DELETE',
//        cache : false,
 //       dataType : 'json',
        data : _data,
        success : function(json) {
            if(json=='<script type="text/javascript">window.location.href = "/login.jsp";</script>'){
                window.location.replace("/login.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/error400.jsp";</script>'){
                window.location.replace("/error400.jsp");
            }else if(json=='<script type="text/javascript">window.location.href = "/errorNoOpen.jsp";</script>'){
                window.location.replace("/errorNoOpen.jsp");
            }else
            if(json.httpCode == 403 && json.code == "Forbidden.NoPermission"){
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="403" && json.code =="Forbidden"){
                console.log("code:403 , message:服务器拒绝请求，请检查帐号、口令等参数  ,  httpCode:Frobidden");
                window.location.href = "../../../../login.jsp";
            }else if(json.httpCode=="400" && json.code =="Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else if(json.httpCode=="400" && json.code =="Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg(rpL(json.code),rpLRespond(json.message));
            }else{
                callback(json);
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(textStatus.error + "" + errorThrown);
            if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden.NoPermission"){
                showErrorMsg("","没有操作权限。");
            }else if(XMLHttpRequest.status == 403 && errorThrown == "Forbidden"){
                window.location.href = "../../../../login.jsp";
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Operation.UnKnown"){
                console.log("code:400 , message:未知的操作请求  ,  code:Operation.UnKnown");
                showErrorMsg("","未知的操作请求。");
            }else if(XMLHttpRequest.status == 400 && errorThrown == "Parameter.Invalid"){
                console.log("code:400 , message:请求参数无效  ,  code:Parameter.Invalid");
                showErrorMsg("","请求参数无效");
            }else{
                console.log("服务器内部出错啦！");
                callback('{"code":"500"}');
            }
        }
    });
}

/**
 * 功能:
 * 1)去除字符串前后所有空格, 需要设置第2个参数为: false
 * 2)去除字符串中所有空格(包括中间空格,需要设置第2个参数为: true)
 * @param str
 * @param is_global
 * @returns
 */
function trimByCondition(str, is_global) {
    var result;
    result = str.replace(/(^\s+)|(\s+$)/g,"");
    if(is_global)
        result = result.replace(/\s/g,"");
    return result;
}

//处理键盘事件 禁止后退键（Backspace）密码或单行、多行文本框除外 
function banBackSpace(e){
    //console.log(event.keyCode)
    var ev = e || window.event;//获取event对象   
    var obj = ev.target || ev.srcElement;//获取事件源     
    var t = obj.type || obj.getAttribute('type');//获取事件源类型     
    //获取作为判断条件的事件类型 
    var vReadOnly = obj.readOnly;
    var vDisabled = obj.disabled;
    //处理undefined值情况 
    vReadOnly = (vReadOnly == undefined) ? false : vReadOnly;
    vDisabled = (vDisabled == undefined) ? true : vDisabled;
    //当敲Backspace键时，事件源类型为密码或单行、多行文本的，  
    //并且readOnly属性为true或disabled属性为true的，则退格键失效  
    var flag1 = ev.keyCode == 8 && (t == "password" || t == "text" || t == "textarea")
        && (vReadOnly == true || vDisabled == true);
    //当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效    
    var flag2 = ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea";
    //判断    
    if (flag2 || flag1) return false;
}

function logout(){
    doPost("/action/userAccount/logout",{},function(objs){
        if(objs.httpCode=="200"){
            window.location.href = "/login.jsp";
        }
    });
}