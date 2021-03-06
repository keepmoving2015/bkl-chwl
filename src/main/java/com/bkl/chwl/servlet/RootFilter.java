package com.bkl.chwl.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.bkl.chwl.entity.User;
import com.bkl.chwl.service.UserService;
import com.bkl.chwl.service.impl.UserServiceImpl;
import com.km.common.utils.CookieUtil;

public class RootFilter implements Filter {
	private String sessionName = "";
	private String[] urls = {};
	private String[] userUrls={};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String page_url = req.getServletPath();
//		String username = (String) req.getSession(true).getAttribute(sessionName);
		String username = (String)CookieUtil.getCookie(sessionName, req);
		boolean isFilter = false;
		boolean isUserFilter = false;
		for (String url : urls) {
			if (url.equals(page_url)) {
				isFilter = true;
				break;
			}
		}
		for (String url : userUrls) {
			if (url.equals(page_url)) {
				isUserFilter = true;
				break;
			}
		}
		if (!isFilter) {
			filterChain.doFilter(request, response);
			return;
		}
		//未登录用户禁止进入用户中心
		if (username == null) {
			String forwardurl = ((HttpServletRequest)request).getServletPath();
			if(((HttpServletRequest)request).getQueryString()!=null)    
				forwardurl+="?"+((HttpServletRequest)request).getQueryString(); 
			res.sendRedirect("/login.jsp?forward=" + URLEncoder.encode(forwardurl));
			return;
		}
		String usernameSplit[]=username.split("\\*");
		int role=Integer.parseInt(usernameSplit[1]);
		if(isUserFilter&&role==User.ROLE_SHOPER){
			res.sendRedirect("/shop_index.jsp");
			return;
		}
//		User user = UserUtil.getCurrentUser((HttpServletRequest)request);
		//用户登录用户中心，但是没有填写用户资料，不允许进入其他页面。
//		if (StringUtils.isEmpty(user.getName()) && !"/userinfo.jsp".equals(page_url)) {
//			res.sendRedirect("/userinfo.jsp");
//			return;
//		}
		filterChain.doFilter(request, response);
		

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		sessionName = filterConfig.getInitParameter("session_name");
		String urlsString = filterConfig.getInitParameter("urls");
		String userUrlsString = filterConfig.getInitParameter("userUrls");
		if (urlsString != null) {
			urls = urlsString.split(",");
		}
		if (userUrlsString != null) {
			userUrls = userUrlsString.split(",");
		}
		if (StringUtils.isEmpty(sessionName)) {
			sessionName = "username";
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
