package com.eeduspace.uuims.oauth.service.impl;

import com.eeduspace.uuims.oauth.persist.po.ProductPo;
import com.eeduspace.uuims.oauth.service.CookieWriteService;
import com.eeduspace.uuims.oauth.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**cookie写入实现类
 * @author songwei
 *	Date 2016-0311
 */
@Service
public class CookieWriteServiceImpl implements CookieWriteService {

	private final Logger logger = LoggerFactory.getLogger(CookieWriteService.class);

	private String url = "eedcspace.uuims.cookie";
	
	@Inject
	private ProductService productService;
	
	@Value("${oauth.cookie.expires}")
    private String cookieExpires;
	
	@Override
	public void writeCookies(String loginCookie, HttpServletResponse response) {
		List<ProductPo> products =  productService.findAll();
		for (ProductPo pro : products) {
			Cookie cookie = new Cookie(url, loginCookie);
			cookie.setMaxAge(Integer.parseInt(cookieExpires));
			cookie.setPath("/");
			cookie.setDomain(pro.getDomain().toString());
			response.addCookie(cookie);
//			logger.debug("cookie:"+cookie.getName()+"---->----->"+cookie.getDomain());
		}
	}

}
