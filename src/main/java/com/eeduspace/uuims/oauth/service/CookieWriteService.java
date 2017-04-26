package com.eeduspace.uuims.oauth.service;

import javax.servlet.http.HttpServletResponse;

/**用户登录时，循环书写从数据库中查到的产品网址
 * @author songwei
 * Date 2016-03-11
 */
public interface CookieWriteService {
	/**生成多个产品的cookie，相同的cookie
	 * @param loginCookie
	 * @param response
	 */
	public void writeCookies(String loginCookie,HttpServletResponse response);
}
