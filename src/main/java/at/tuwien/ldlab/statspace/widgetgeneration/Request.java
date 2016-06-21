package at.tuwien.ldlab.statspace.widgetgeneration;


public class Request {
	Endpoint endpoint;	
	String download;
	int error;
	boolean bMDPurpose=false; //true - metadata; false - widget generation
	
	public Request(){
		endpoint = new Endpoint();
		error = -1;
		download = "";
		bMDPurpose=false;
	}
	
	public void setEndpoint(Endpoint end){endpoint = end;}
	public void setError(int n){error = n;}
	public void setDownload(String sDownload){download = sDownload;}
	public void setMetaDataPurpose(boolean bPurpose){bMDPurpose = bPurpose;}
	public int getError(){return error;}
	public Endpoint getEndpoint(){return endpoint;}
	public String getDownload(){return download;}
	public boolean getMetaDataPurpose(){return bMDPurpose;}
}

