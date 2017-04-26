package com.eeduspace.uuims.oauth.ws;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.eeduspace.uuims.oauth.model.UserModel;
import com.eeduspace.uuims.oauth.redis.RedisClientTemplate;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.service.ManagerService;
import com.eeduspace.uuims.oauth.service.UserService;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;

/**
 * Author: dingran
 * Date: 2015/10/28
 * Description:
 */
@Component
@Path(value = "/test")
public class TestWs  {
    @Inject
    private UserService userService;
    @Inject
    private ManagerService managerService;
    @Inject
    private RedisClientTemplate redisClientTemplate;
    private Gson gson=new Gson();
    @POST
    public Response processGet() {
        return dispatch();
    }
    @GET
    public Response processPost() {
        return dispatch();
    }
    public  Response dispatch(){
        userService.findAll();
        managerService.findAll();
        redisClientTemplate.get("test");
        UserModel userModel=new UserModel();
        userModel.setCityCode("cityCode");
        userModel.setAction("action");
        return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse("requestId"),ResponseCode.SUCCESS.toString()))).build();

    }
}
