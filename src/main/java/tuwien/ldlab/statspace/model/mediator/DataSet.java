package tuwien.ldlab.statspace.model.mediator;

public class DataSet {
	private String sVariable;
	private String sUri;
	private String sSubject;
	private String sFeature;
	private String sAccessURL;	
	private String sLabel;
	private String sVariableLabel;
	
	public DataSet(){
		sVariable="";
		sUri="";
		sSubject="";
		sFeature="";
		sAccessURL="";	
		sLabel="";
		sVariableLabel="";
	}
	
	public DataSet(String uri, String label, String subject, String method, String access, String varDs, String varDsLabel){
		sVariable=varDs;
		sUri=uri;
		sSubject=subject;
		sFeature=method;
		sAccessURL=access;	
		sLabel=label;
		sVariableLabel=varDsLabel;
	}
	
	public void setVariable(String s){sVariable=s;}
	public void setUri(String s){sUri=s;}
	public void setSubject(String s){sSubject=s;}
	public void setFeature(String s){sFeature=s;}
	public void setAccessURL(String s){sAccessURL=s;}	
	public void setLabel(String s){sLabel=s;}
	public void setVariableLabel(String s){sVariableLabel=s;}
	
	public String getVariable(){return sVariable;}
	public String getUri(){return sUri;}
	public String getSubject(){return sSubject;}
	public String getFeature(){return sFeature;}
	public String getAccessURL(){return sAccessURL;}	
	public String getLabel(){return sLabel;}
	public String getVariableLabel(){return sVariableLabel;}
	
	public void display(){
		System.out.println("Variable: "+sVariable);
		System.out.println("Uri: "+sUri);
		System.out.println("Subject: "+sSubject);
		System.out.println("Feature: "+sFeature);
		System.out.println("AccessURL: "+sAccessURL);	
		System.out.println("Label: "+sLabel);
		System.out.println("VariableLabel: "+sVariableLabel);
	}
	
}
