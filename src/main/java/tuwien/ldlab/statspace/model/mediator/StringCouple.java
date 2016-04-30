package tuwien.ldlab.statspace.model.mediator;

public class StringCouple {
	private String sFirst;
	private String sSecond;
	
	public StringCouple(){
		sFirst="";	
		sSecond="";
	}
	
	public StringCouple(String s1, String s2){
		sFirst  = s1;		
		sSecond = s2;
	}
	
	public void setFirstString(String s1){sFirst=s1;}
	public void setSecondString(String s2){sSecond=s2;}
	
	public String getFirstString(){return sFirst;}
	public String getSecondString(){return sSecond;}
	
	
	public void display(){
		System.out.println(sFirst+"\t"+sSecond);
	}
}
