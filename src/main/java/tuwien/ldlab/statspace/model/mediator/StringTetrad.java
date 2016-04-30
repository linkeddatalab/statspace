package tuwien.ldlab.statspace.model.mediator;

public class StringTetrad {
	private String s1;
	private String s2;
	private String s3;
	private String s4;
	
	public StringTetrad(){
		s1="";
		s2="";
		s3="";	
		s4="";
	}
	
	public StringTetrad(String str1, String str2, String str3, String str4){
		s1=str1;
		s2=str2;
		s3=str3;	
		s4=str4;
	}
	
	public void setFirstString(String s){s1=s;}
	public void setSecondString(String s){s2=s;}
	public void setThirdString(String s){s3=s;}	
	public void setFourthString(String s){s4=s;}
	
	public String getFirstString(){return s1;}
	public String getSecondString(){return s2;}
	public String getThirdString(){return s3;}	
	public String getFourthString(){return s4;}	
	
	public void display(){
		System.out.println(s1+"\t"+s2+"\t"+s3+"\t"+s4);
	}
}
