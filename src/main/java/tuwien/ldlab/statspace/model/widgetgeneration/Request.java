package tuwien.ldlab.statspace.model.widgetgeneration;


public class Request {
	int id;
	Endpoint endpoint;	
	String download;
	int error;
	boolean bMDPurpose=false; //true - metadata; false - widget generation
	
	public Request(){
		id = 0;
		endpoint = new Endpoint();
		error = -1;
		download = "";
		bMDPurpose=false;
	}
	
	public void setId(int n){id = n;}
	public void setEndpoint(Endpoint end){endpoint = end;}
	public void setError(int n){error = n;}
	public void setDownload(String sDownload){download = sDownload;}
	public void setMetaDataPurpose(boolean bPurpose){bMDPurpose = bPurpose;}
	
	public int getId(){return id;}
	public int getError(){return error;}
	public Endpoint getEndpoint(){return endpoint;}
	public String getDownload(){return download;}
	public boolean getMetaDataPurpose(){return bMDPurpose;}

}

