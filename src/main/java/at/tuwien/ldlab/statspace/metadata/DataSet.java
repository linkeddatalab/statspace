package at.tuwien.ldlab.statspace.metadata;

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
	
	public DataSet(DataSet ds){
		sVariable=ds.getVariable();
		sUri=ds.getUri();
		sSubject=ds.getSubject();	
		sFeature=ds.getFeature();
		sAccessURL=ds.getAccessURL();	
		sLabel=ds.getLabel();
		sVariableLabel=ds.getVariableLabel();
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
	
	public String getCode(String s){
		if(s==null || s.isEmpty())
			return "";
		int i = s.length()-1;
		while(i>0 && s.charAt(i)!='/') i--;
		if(i>0) return s.substring(i+1);
		else
			return s;
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
	public String getSubjectForDisplay(){
		String s = sSubject;
		if(s!=null && !s.isEmpty()){
			int i = s.length()-1;
			while(i>0 && s.charAt(i)!='/')
				i--;
			if(i>0)
				s=s.substring(i+1);
		}
		
		if(s==null || s.isEmpty())
			return "";
		else{
			if(s.length()<40)
				return s;
			else
				return s.substring(0, 40)+"...";
		}	
	}
	public String getFeature(){return sFeature;}
	public String getAccessURL(){return sAccessURL;}	
	public String getLabel(){return sLabel;}
	public String getLabelForDisplay(){
		if(sLabel.length()<40)
			return sLabel;
		else
			return sLabel.substring(0, 40) + "...";
	}
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
