package com.eeduspace.uuims.oauth.util;


import java.util.ArrayList;
import java.util.List;


public class PageUtil {

	private int totalCount;  	//总行数
	private int totalPage;	//总页数
	private int currentPage;	//当前页数
	private int lineSize;	//每页行数

	public PageUtil(){

	}

	public PageUtil(int currentPage, int lineSize, int totalCount, int totalPage) {
		super();
		this.currentPage = currentPage;
		this.lineSize = lineSize;
		this.totalCount = totalCount;
		this.totalPage = totalPage;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		if(currentPage < 1){
			this.currentPage = 1;
		}else if(currentPage > this.totalPage){
			this.currentPage = this.totalPage;
		}else{
			this.currentPage = currentPage;
		}
	}

	public int getLineSize() {
		return lineSize;
	}

	public void setLineSize(int lineSize) {
		this.lineSize = lineSize;
	}

	//设置总页数
	public void setTotalPage(){
		//如果总行数整除每页的行数
		if(this.getTotalCount()%this.getLineSize()==0){
			this.totalPage=this.getTotalCount()/this.getLineSize();
		}else{
			this.totalPage=this.getTotalCount()/this.getLineSize()+1;
		}
	}


	public List getPage(List list){
		List list1=new ArrayList();
		//找出分页后，当前页数的后一叶的最后一行是总共行数的第几行-----当前页数减一，乘以每页几行
		int start=(this.getCurrentPage()-1)*this.getLineSize();

		//找出分页后，最后一页的第一行数据是总共行数的第几行
		int end=this.getCurrentPage()*this.getLineSize();
		int end1=(this.getCurrentPage()-1)*this.getLineSize()+this.getTotalCount()%this.getLineSize();

		//如果说，当前页数等于总页数  并且 总行数除以每页几行不能整除
		if(this.getCurrentPage()==this.getTotalPage() && this.getTotalCount()%this.getLineSize()!=0){
			for (int i = start; i < end1; i++){
				list1.add(list.get(i));
			}
		}else{
			for (int i = start; i <end; i++) {
				list1.add(list.get(i));
			}
		}

		return list1;
	}

}

