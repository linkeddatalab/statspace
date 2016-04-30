package tuwien.ldlab.statspace.model.widgetgeneration;

import java.util.ArrayList;

public class RequestList {
	ArrayList<Request> arrRequest;
	public RequestList(){
		arrRequest = new ArrayList<Request>();
	}
	public void addRequest(Request newRequest){
		arrRequest.add(newRequest);
	}
	public Request getRequestById(int id){
		int n = arrRequest.size();
		for(int i=0; i<n; i++)
			if(arrRequest.get(i).getId()==id)
				return arrRequest.get(i);
		return new Request();
	}
	
	public Request getRequest(int index){
		return arrRequest.get(index);
	}
	
	public int getIndexRequest(int id){
		int n = arrRequest.size();
		for(int i=0; i<n; i++)
			if(arrRequest.get(i).getId()==id)
				return i;
		return -1;
	}
	
}
