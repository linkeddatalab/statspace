package tuwien.ldlab.statspace.model.mediator;

public class DataSet {
	private String sVariable;
	private String sUri;
	private String sSubject;
	private String sMethod;
	private String sEndpoint;	
	private String sRML;
	private String sLabel;
	private String sVariableLabel;
	
	public DataSet(){
		sVariable="";
		sUri="";
		sSubject="";
		sMethod="";
		sEndpoint="";
		sRML="";
		sLabel="";
		sVariableLabel="";
	}
	
	public DataSet(String uri, String label, String subject, String method, String endpoint, String rml, String varDs, String varDsLabel){
		sVariable=varDs;
		sUri=uri;
		sSubject=subject;
		sMethod=method;
		sEndpoint=endpoint;
		sRML=rml;
		sLabel=label;
		sVariableLabel=varDsLabel;
	}
	
	public void setVariable(String s){sVariable=s;}
	public void setUri(String s){sUri=s;}
	public void setSubject(String s){sSubject=s;}
	public void setMethod(String s){sMethod=s;}
	public void setEndpoint(String s){sEndpoint=s;}
	public void setRML(String s){sRML=s;}
	public void setLabel(String s){sLabel=s;}
	public void setVariableLabel(String s){sVariableLabel=s;}
	
	public String getVariable(){return sVariable;}
	public String getUri(){return sUri;}
	public String getSubject(){return sSubject;}
	public String getMethod(){return sMethod;}
	public String getEndpoint(){return sEndpoint;}
	public String getRML(){return sRML;}
	public String getLabel(){return sLabel;}
	public String getVariableLabel(){return sVariableLabel;}
	
	public void display(){
		System.out.println("Variable: "+sVariable);
		System.out.println("Uri: "+sUri);
		System.out.println("Subject: "+sSubject);
		System.out.println("Method: "+sMethod);
		System.out.println("Endpoint: "+sEndpoint);
		System.out.println("RML: "+sRML);
		System.out.println("Label: "+sLabel);
		System.out.println("VariableLabel: "+sVariableLabel);
	}
	
}
