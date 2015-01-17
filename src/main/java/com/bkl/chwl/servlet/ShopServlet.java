package com.bkl.chwl.servlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bkl.chwl.MainConfig;
import com.bkl.chwl.entity.Shop;
import com.bkl.chwl.entity.Tradeorder;
import com.bkl.chwl.entity.User;
import com.bkl.chwl.service.OrderService;
import com.bkl.chwl.service.ShopService;
import com.bkl.chwl.service.impl.OrderServiceImpl;
import com.bkl.chwl.service.impl.ShopServiceImpl;
import com.bkl.chwl.utils.ApiCommon;
import com.bkl.chwl.utils.StringUtil;
import com.bkl.chwl.utils.UserUtil;
import com.bkl.chwl.vo.SellLog;
import com.bkl.chwl.vo.SellLogEntity;
import com.km.common.servlet.CommonServlet;
import com.km.common.utils.ServletUtil;
import com.km.common.utils.TimeUtil;
import com.km.common.vo.Page;
import com.km.common.vo.PageReply;
import com.km.common.vo.RetCode;

public class ShopServlet extends CommonServlet {
	public void addShop(HttpServletRequest request,HttpServletResponse response) throws Exception{
		Shop shop=ServletUtil.readObjectServletQuery(request, Shop.class);
		User u=UserUtil.getCurrentUser(request);
		if(u.getVertify()==u.VERTIFY_FALSE){
			ServletUtil.writeCommonReply(null, RetCode.USER_VERTIFY_FALSE, response);
			return;
		}
		ShopService shopServ=new ShopServiceImpl();
		if(shop.getId()==0){
		shop.setCtime(TimeUtil.getUnixTime());
		//初始化店铺的统计数据 like collect shop_sellnum
		shop.setShop_like(0);
		shop.setShop_collect(0);
		shop.setShop_sellnum(0);
		}else{
			Shop tempShop=shopServ.get(shop.getId());
			shop.setCtime(tempShop.getCtime());
			shop.setShop_collect(tempShop.getShop_collect());
			shop.setShop_sellnum(tempShop.getShop_sellnum());
		}
		long id=shopServ.save(shop);
		shop.setId(id);
		ServletUtil.writeOkCommonReply(shop, response);
	}
	public void getShopPageHTML(HttpServletRequest request,HttpServletResponse response) throws Exception{
		ShopService shopServ=new ShopServiceImpl();
		int local=MainConfig.defaultProvinceId();
		int local2=0;
		int local3=0;
		int type=0;
		int type2=0;
		int sort=0;
		if(request.getParameter("local")!=null) local=Integer.valueOf(request.getParameter("local"));
		if(request.getParameter("local2")!=null) local2=Integer.valueOf(request.getParameter("local2"));
		if(request.getParameter("local3")!=null) local3=Integer.valueOf(request.getParameter("local3"));
		if(request.getParameter("type")!=null) type=Integer.valueOf(request.getParameter("type"));
		if(request.getParameter("type2")!=null) type2=Integer.valueOf(request.getParameter("type2"));
		if(request.getParameter("sort")!=null) sort=Integer.valueOf(request.getParameter("sort"));
		
		Map searchMap=ServletUtil.getSearchMap(request);
		Page page=ServletUtil.getPage(request);
		PageReply<Shop> shops=shopServ.getListPage(local, local2, local3, type, type2, sort, searchMap, page);
		if(request.getParameter("pagenum")!=null){
			if(Integer.parseInt(request.getParameter("pagenum"))>shops.getTotalpage()){
				ServletUtil.writeOkCommonReply("", response);
				return;
			}
		}
		String result="";
		if(shops.getPagedatas() == null || shops.getPagedatas().length == 0) { 
			result="";
		}else{
			for (int i = 0; i < shops.getPagedatas().length; i++) {
				Shop s = shops.getPagedatas()[i];
				String images[]=s.getImage().split("@");
				result+="<div class='container no-bottom list_style' onclick=\"javascript:location.href='shop_detail.jsp?id="+s.getId()+"'\"><div class='recent-post'><div class='dealcard-img'><img src='"+images[1]+"'></div><div class='dealcard-block-right'><div class='title'><strong>"+s.getTitle()+"</strong></div><div class='detail'>地址："+s.getShop_loc()+"</div><div class='pricepanel'><span class='strong-color'>立省</span><strong>"+StringUtil.payBackDoubleToRate(s.getCoinRate())+"</strong><span class='line-right'></span></div></div></div></div><br><div class='decoration'></div>";
			}
		}
		ServletUtil.writeOkCommonReply(result, response);
	}
	//TODO 查看商家订单
	public void getOrderListHTML(HttpServletRequest request,HttpServletResponse response) throws Exception{
		User u = UserUtil.getCurrentUser(request);
		Page page=ServletUtil.getPage(request);
		SellLogEntity sle=ApiCommon.getSellLog(u.getId(), page);
		List<SellLog> sellLogs=sle.getList();
		
		OrderService orderServ=new OrderServiceImpl();
		Map<String,Tradeorder> ordersMap=orderServ.getListBySeller(u.getId());
		
		String res="";
		for(SellLog sellLog:sellLogs){
			Tradeorder order=ordersMap.get(sellLog.getId());
			if(order==null) continue;
			res+="<div class='tableList downborder list-lg'><div class='detail'>"+sellLog.getBuyerName()+"消费"+order.getPrice()+"元<br>订单金币量："+sellLog.getCoin()+"<br>订单返回金币量："+sellLog.getRcoin()+"<br>"+TimeUtil.fromUnixTime(sellLog.getOrderTime())+"</div><div class='status'>"+order.getStatusString()+"</div></div>";
		}
		Map map=new HashMap<String, Object>();
		map.put("resStr", res);
		map.put("hasNext", sle.isHasNext());
		ServletUtil.writeOkCommonReply(map, response);
	}
	
	public void addLike(HttpServletRequest request,HttpServletResponse response) throws Exception{
		User u=UserUtil.getCurrentUser(request);
		if(u==null){
			ServletUtil.writeCommonReply(null, RetCode.SESSION_TIMEOUT, response);
			return;
		}
		long uid=u.getId();
		long sid=Long.valueOf(request.getParameter("sid"));
		ShopService shopServ=new ShopServiceImpl();
		shopServ.addLike(uid, sid);
		ServletUtil.writeOkCommonReply(null, response);
	}
	public void addCollect(HttpServletRequest request,HttpServletResponse response) throws Exception{
		User u=UserUtil.getCurrentUser(request);
		if(u==null){
			ServletUtil.writeCommonReply(null, RetCode.SESSION_TIMEOUT, response);
			return;
		}
		long uid=u.getId();
		long sid=Long.valueOf(request.getParameter("sid"));
		ShopService shopServ=new ShopServiceImpl();
		shopServ.addCollection(uid, sid);
		ServletUtil.writeOkCommonReply(null, response);
	}
	public void getCollectShop(HttpServletRequest request,HttpServletResponse response) throws Exception{
		User u=UserUtil.getCurrentUser(request);
		if(u==null){
			ServletUtil.writeCommonReply(null, RetCode.SESSION_TIMEOUT, response);
			return;
		}
		long uid=u.getId();
		ShopService shopServ=new ShopServiceImpl();
		ServletUtil.writeOkCommonReply(shopServ.getCollectList(uid),response);
	}
	//二维码扫码搜索过滤
	public void getShopForConfirm(HttpServletRequest request,HttpServletResponse response) throws Exception{
		ShopService shopServ=new ShopServiceImpl();
		int local=0;
		int local2=0;
		int local3=0;
		int type=0;
		int type2=0;
		int sort=0;
		if(request.getParameter("local")!=null) local=Integer.valueOf(request.getParameter("local"));
		if(request.getParameter("local2")!=null) local2=Integer.valueOf(request.getParameter("local2"));
		if(request.getParameter("local3")!=null) local3=Integer.valueOf(request.getParameter("local3"));
		if(request.getParameter("type")!=null) type=Integer.valueOf(request.getParameter("type"));
		if(request.getParameter("type2")!=null) type2=Integer.valueOf(request.getParameter("type2"));
		if(request.getParameter("sort")!=null) sort=Integer.valueOf(request.getParameter("sort"));
		
		Map searchMap=ServletUtil.getSearchMap(request);
		Page page=ServletUtil.getPage(request);
		PageReply<Shop> shops=shopServ.getListPage(local, local2, local3, type, type2, sort, searchMap, page);
		if(request.getParameter("pagenum")!=null){
			if(Integer.parseInt(request.getParameter("pagenum"))>shops.getTotalpage()){
				ServletUtil.writeOkCommonReply("", response);
				return;
			}
		}
		String result="";
		if(shops.getPagedatas() == null || shops.getPagedatas().length == 0) { 
			result="<div class=\"tableList downborder\"><div class=\"detail\" style=\"padding-left:1rem\">对不起，没有搜索到结果</div></div>";
		}else{
			for (int i = 0; i < shops.getPagedatas().length; i++) {
				Shop s = shops.getPagedatas()[i];
				result+="<a href=\"confirm_order.jsp?result="+s.getId()+"\"><div class=\"tableList downborder\"><div class=\"detail\" style=\"padding:.5rem 1rem .5rem 1rem !important\">"+s.getTitle()+"<br><span style=\"font-size:1rem;\">地址："+s.getShop_loc()+"</span></div><div class=\"status\" style=\"text-align: center\"><i class=\"iconfont\" style=\"color:#666\">&#x3435;</i></div></div></a>";
			}
		}
		ServletUtil.writeOkCommonReply(result, response);
	}
}
