package at.tuwien.ldlab.statspace.metadata;

public class StringTriple {
	private String s1;
	private String s2;
	private String s3;
	
	public StringTriple(){
		s1="";
		s2="";
		s3="";	
	}
	
	public StringTriple(String str1, String str2, String str3){
		s1=str1;
		s2=str2;
		s3=str3;	
	}
	
	public void setFirstString(String s){s1=s;}
	public void setSecondString(String s){s2=s;}
	public void setThirdString(String s){s3=s;}	
	
	public String getFirstString(){return s1;}
	public String getSecondString(){return s2;}
	public String getThirdString(){return s3;}	
	
	public void display(){
		System.out.println(s1+"\t"+s2+"\t"+s3);
	}
}
