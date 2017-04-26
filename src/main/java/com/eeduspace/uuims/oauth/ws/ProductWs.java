package com.eeduspace.uuims.oauth.ws;

import com.eeduspace.uuims.oauth.ActionName;
import com.eeduspace.uuims.oauth.LogActionEnum;
import com.eeduspace.uuims.oauth.convert.AuthConverter;
import com.eeduspace.uuims.oauth.model.ProductModel;
import com.eeduspace.uuims.oauth.persist.enumeration.RoleEnum;
import com.eeduspace.uuims.oauth.persist.po.ManagerPo;
import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.persist.po.UserPo;
import com.eeduspace.uuims.oauth.response.BaseResponse;
import com.eeduspace.uuims.oauth.response.ProductResponse;
import com.eeduspace.uuims.oauth.service.*;
import com.eeduspace.uuims.rescode.ParamName;
import com.eeduspace.uuims.rescode.ResourceName;
import com.eeduspace.uuims.rescode.ResponseCode;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: dingran
 * Date: 2015/11/9
 * Description:产品管理
 */
@Component
@Path(value = "/product")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@CrossOriginResourceSharing(allowAllOrigins = true)
public class ProductWs extends BaseWs{

    private final Logger logger = LoggerFactory.getLogger(ProductWs.class);

    private Gson gson = new Gson();

    @Inject
    private AuthConverter authConverter;
    @Inject
    private ProductService productService;
    @Inject
    private AclService aclService;
    @Inject
    private ManagerLogService managerLogService;
    @Inject
    private ManagerService managerService;
    @Inject
    private UserService userService;
    @Override
    public Response dispatch(String action,String token, String requestBody, HttpServletRequest httpServletRequest, ManagerPo managerPo, UserPo userPo) {
        ProductModel productModel = gson.fromJson(requestBody, ProductModel.class);

        switch (ActionName.toEnum(action)) {
            case LIST:
                return list(productModel, managerPo);
            case CREATE:
                return create(productModel, managerPo);
            case DESCRIBE:
                return describe(productModel, managerPo);
            case UPDATE:
                return update(productModel, managerPo);
            case DELETE:
                return delete(productModel, managerPo);
            default:
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.UNKNOWN_OPERATION.toString(), ResourceName.PRODUCT.toString()))).build();
        }
    }


    /**
     * 产品列表
     * @param managerPo
     * @return
     */
    private Response list(ProductModel productModel, ManagerPo managerPo) {
        try {
            //TODO 只有系统管理员 有权限管理
/*            if(!aclService.isSystem(managerPo)){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }*/
            BaseResponse baseResponse = new BaseResponse(requestId);
            ProductResponse productResponse = new ProductResponse();
            List<ProductModel> productModels=new ArrayList<>();
            if(managerPo.getRolePo().getType() == RoleEnum.Type.System){
                productModels= authConverter.fromProductPos(productService.findAll());
            }else if(managerPo.getRolePo().getType() == RoleEnum.Type.Product){
                productModels.add(authConverter.fromProductPo(managerPo.getProductPo()));
            }
            productResponse.setProductModels(productModels);
            productResponse.setCount(productModels.size());
            baseResponse.setResult(productResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.LIST.toString(), LogActionEnum.PRODUCT.toString(),true,managerPo.getProductPo(),null,productModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},listProduct Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.PRODUCT.toString()))).build();
        }
    }

    /**
     * 产品详情
     * @param managerPo
     * @return
     */
    private Response describe(ProductModel productModel, ManagerPo managerPo) {
    	
        if(productModel.getProductId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            ProductPo productPo=productService.findByUuid(productModel.getProductId());
            if(productPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.PRODUCT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
            }
            BaseResponse baseResponse = new BaseResponse(requestId);
            ProductResponse productResponse = new ProductResponse();
            productResponse.setProductModel(authConverter.fromProductPo(productPo));
            baseResponse.setResult(productResponse);
            managerLogService.create(managerPo,managerPo.getUuid(), LogActionEnum.DESCRIBE.toString(), LogActionEnum.PRODUCT.toString(),true,managerPo.getProductPo(),null,productModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},describeProduct Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.PRODUCT.toString()))).build();
        }
    }
    /**
     * 产品新增
     * @param managerPo
     * @return
     */
    private Response create(ProductModel productModel, ManagerPo managerPo) {

        if(productModel.getName()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
        }
/*        if(productModel.getType()==null){
            logger.error("requestId："+"requestId,"+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.TYPE.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.TYPE.toString()))).build();
        }*/
        if(productModel.getIsManyEquipmentLogin()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.IS_MANY_EQUIPMENT_LOGIN.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.IS_MANY_EQUIPMENT_LOGIN.toString()))).build();
        }
        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            ProductPo po= productService.findByType(productModel.getType());
            if(po!=null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_DUPLICATE.toString()+"."+ParamName.TYPE.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_DUPLICATE.toString(),ParamName.TYPE.toString()))).build();
            }
            ProductPo productPo=new ProductPo();
            productPo.setName(productModel.getName());
            productPo.setType(productModel.getType());
            productPo.setIsManyEquipmentLogin(productModel.getIsManyEquipmentLogin());
            if(StringUtils.isNotBlank(productModel.getDescription())){
                productPo.setDescription(productModel.getDescription());
            }
            productPo.setDomain(productModel.getDomain());
            productPo=productService.create(productPo);
            BaseResponse baseResponse = new BaseResponse(requestId);
            ProductResponse productResponse = new ProductResponse();
            productResponse.setProductModel(authConverter.fromProductPo(productPo));
            baseResponse.setResult(productResponse);
            managerLogService.create(managerPo,productPo.getUuid(), LogActionEnum.CREATE.toString(), LogActionEnum.PRODUCT.toString(),true,managerPo.getProductPo(),null,productModel.getEquipmentType(),requestId);
            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},describeProduct Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.PRODUCT.toString()))).build();
        }
    }
    /**
     * 产品更新
     * @param managerPo
     * @return
     */
    private Response update(ProductModel productModel, ManagerPo managerPo) {

        if(productModel.getProductId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            ProductPo productPo=productService.findByUuid(productModel.getProductId());
            if(productPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.PRODUCT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
            }
            if(StringUtils.isNotBlank(productModel.getDescription())){
                productPo.setDescription(productModel.getDescription());
            }
            if(StringUtils.isNotBlank(productModel.getName())){
                productPo.setName(productModel.getName());
            }
            if(productModel.getIsManyEquipmentLogin()!=null){
                productPo.setIsManyEquipmentLogin(productModel.getIsManyEquipmentLogin());
            }
            if(StringUtils.isNotBlank(productModel.getDomain())){
                productPo.setDomain(productModel.getDomain());
            }
            productPo= productService.save(productPo);
            BaseResponse baseResponse = new BaseResponse(requestId);
            ProductResponse productResponse = new ProductResponse();
            productResponse.setProductModel(authConverter.fromProductPo(productPo));
            baseResponse.setResult(productResponse);

            managerLogService.create(managerPo,productPo.getUuid(), LogActionEnum.UPDATE.toString(), LogActionEnum.PRODUCT.toString(),true,managerPo.getProductPo(),null,productModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},updateProduct Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.PRODUCT.toString()))).build();
        }
    }

    /**
     * 产品删除
     * @param managerPo
     * @return
     */
    private Response delete(ProductModel productModel, ManagerPo managerPo) {


        if(productModel.getProductId()==null){
            logger.error("requestId："+requestId+","+ResponseCode.PARAMETER_MISS.toString()+"."+ParamName.PRODUCT_ID.toString());
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.PARAMETER_MISS.toString(), ParamName.PRODUCT_ID.toString()))).build();
        }

        try {
            //TODO 只有系统管理员 有权限管理
            if(!aclService.isSystem(managerPo)){
                logger.error("requestId："+requestId+","+ResponseCode.FORBIDDEN_NOPERMISSION.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.FORBIDDEN_NOPERMISSION.toString()))).build();
            }
            ProductPo productPo=productService.findByUuid(productModel.getProductId());
            if(productPo==null){
                logger.error("requestId："+requestId+","+ResponseCode.RESOURCE_NOTFOUND.toString()+"."+ResourceName.PRODUCT.toString());
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_NOTFOUND.toString(), ResourceName.PRODUCT.toString()))).build();
            }
            List<ManagerPo> list= managerService.findByProductId(productPo.getId());
            if(list!=null && list.size()>0){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_INUSE.toString(), ResourceName.PRODUCT.toString()))).build();
            }
            List<UserPo> userPos= userService.findByProductId(productPo.getId());
            if(userPos!=null && userPos.size()>0){
                return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.RESOURCE_INUSE.toString(), ResourceName.PRODUCT.toString()))).build();
            }
            productService.delete(productPo.getId());
            BaseResponse baseResponse = new BaseResponse(requestId);

            managerLogService.create(managerPo,productPo.getUuid(), LogActionEnum.DELETE.toString(), LogActionEnum.PRODUCT.toString(),true,managerPo.getProductPo(),null,productModel.getEquipmentType(),requestId);

            return Response.ok(gson.toJson(baseResponse)).build();
        } catch (Exception e) {
            logger.error("requestId：{},deleteProduct Exception：", requestId, e);
            return Response.ok(gson.toJson(BaseResponse.setResponse(new BaseResponse(requestId), ResponseCode.SERVICE_ERROR.toString(), ResourceName.PRODUCT.toString()))).build();
        }
    }
}
