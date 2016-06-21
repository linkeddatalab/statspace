package at.tuwien.ldlab.statspace.metadata;

import java.util.ArrayList;

import at.tuwien.ldlab.statspace.metadata.MetaData;

public class SparqlQuery {
	private ArrayList<StringTriple> triples;
	private ArrayList<StringCouple> filters;
	private ArrayList<StringCouple> prefixes;
	private String sSelect;
	private String sVarObs; //observation variable
	private String sLimit_Order;
	private boolean bError;

	public SparqlQuery(){	
		triples  = new ArrayList<StringTriple>();
		filters  = new ArrayList<StringCouple>();
		prefixes = new ArrayList<StringCouple>();
		sSelect  = "";
		sVarObs  = "";		
		sLimit_Order = "";
		bError   = false;
	}
	
	public SparqlQuery(String sQuery){
		int i, j, k, m;
		String sTriples, sPrefixes, sPrefix, sTriple, sFilter, sQueryInLowerCase, s1, s2;
		String s="", p="", o="";
		
		triples   = new ArrayList<StringTriple>();
		filters   = new ArrayList<StringCouple>();	
		prefixes  = new ArrayList<StringCouple>();
		sSelect   = "";
		sVarObs   = "";
		sFilter   = "";
		sLimit_Order = "";
		bError    = false;		
		
		sQueryInLowerCase = sQuery.toLowerCase();		
		
		//prefix
		j = sQueryInLowerCase.indexOf("select");
		if(j==-1){
			bError = true;
			return;
		}
		sPrefixes = sQuery.substring(0, j).trim();			
		while(sPrefixes.toLowerCase().startsWith("prefix")){
			for(k=0; k<sPrefixes.length(); k++)
				if(sPrefixes.charAt(k)=='>')
					break;
			if(k<sPrefixes.length()){
				sPrefix   = sPrefixes.substring(0, k).trim();
				sPrefixes = sPrefixes.substring(k+1).trim();
				i = sPrefix.indexOf(":");
				if(i>6){
					s1 = sPrefix.substring(6, i).trim();
					sPrefix = sPrefix.substring(i+1).trim();
					if((k=sPrefix.indexOf("<"))!=-1){
						s2 = sPrefix.substring(k+1).trim();
						prefixes.add(new StringCouple(s1, s2));
					}else
						bError=true;					
				}else
					bError=true;	
			}else
				bError=true;			
			
			if(bError)
				return;
		}
		
		//select
		i = sQueryInLowerCase.indexOf("where");
		if(i==-1){
			bError = true;
			return;
		}
		sSelect = sQuery.substring(j+6, i).trim();
		
		//where			
		for(;i<sQueryInLowerCase.length(); i++)
			if(sQueryInLowerCase.charAt(i)=='{')
				break;
		for(j=sQueryInLowerCase.length()-1; j>i; j--)
			if(sQueryInLowerCase.charAt(j)=='}')
				break;
		
		//check validity of query
		if(i>=j){
			bError = true;
			return;
		}else{
			//limit - order
			sLimit_Order = sQuery.substring(j+1).trim();			
				
			//detect triples
			sTriples = sQuery.substring(i+1, j);
			
			//detect filters
			i=-1;
			while((i=sTriples.toLowerCase().indexOf("filter", i+1))!=-1){
				k=i;
				for(;i<sTriples.length();i++)
					if(sTriples.charAt(i)=='(')
						break;
				for(j=i+1; j<sTriples.length(); j++)
					if(sTriples.charAt(j)==')')
						break;
				if(i>=j){
					bError= true;
					return;
				}else{
					sFilter = sTriples.substring(i+1, j).trim();			
					m = sFilter.indexOf("=");
					if(m!=-1){
						s1 = sFilter.substring(0, m).trim();
						s2 = sFilter.substring(m+1).trim();
						if(s2.startsWith("<") && s2.endsWith(">"))
							s2 = s2.substring(1, s2.length()-1);							
						filters.add(new StringCouple(s1, s2));						
					}
					
					//remove filter out of the triples
					if(k>0){
						if(j<sTriples.length()-1)
							sTriples = sTriples.substring(0, k) + sTriples.substring(j+1);
						else
							sTriples = sTriples.substring(0, k);
					}else{
						sTriples = sTriples.substring(j+1);
					}
					i=-1;
				}				
			}
			i=0;			
			sTriples = sTriples.trim();
			
			//character '.' can appear inside an URI
			while((i=sTriples.indexOf(".", i+1))!=-1){
				for(j=i-1; j>=0; j--)
					if(sTriples.charAt(j)==' '|| sTriples.charAt(j)=='>' || sTriples.charAt(j)=='<')
						break;
				
				if(sTriples.charAt(j)!='<'){				
					sTriple = sTriples.substring(0, i).trim();
					sTriples = sTriples.substring(i+1).trim();
					j = sTriple.indexOf(" ");
					if(j!=-1){
						s = sTriple.substring(0, j);
						if(s.startsWith("<") && s.endsWith(">"))
							s=s.substring(1, s.length()-1);
						sTriple = sTriple.substring(j+1).trim();
						j = sTriple.indexOf(" ");
						if(j!=-1){
							p = sTriple.substring(0, j);
							if(p.startsWith("<") && p.endsWith(">"))
								p=p.substring(1, p.length()-1);
							o = sTriple.substring(j+1).trim();
							if(o.startsWith("<") && o.endsWith(">"))
								o=o.substring(1, o.length()-1);
							if(o.indexOf(" ")==-1){
								triples.add(new StringTriple(s, p, o));
							}
							else
								bError=true;
						}else
							bError=true;
					}else 
						bError=true;
									
					if(bError)
						return;	
					
					//start new search
					i=0;
				}
			}
		}	
		removePrefix();	
//		display();
	}	

	public StringTriple getTriple(int index){return triples.get(index);}
	public StringCouple getFilter(int index){return filters.get(index);}	
	public String getSelect(){return sSelect;}
	public String getVarObservation(){return sVarObs;}
	public String getLimit_Order(){return sLimit_Order;}
	public boolean getErrorStatus(){return bError;}
	public void addTriple(String s, String p, String o){triples.add(new StringTriple(s, p, o));}
	
	public void setSelect(String s){sSelect = s;}
	public void setVarObservation(String s){sVarObs = s;}
	public void setLimitOrder(String s){sLimit_Order = s;}
	
	public void removePrefix(){
		int i, j, k;
		String p, o, prefix, temp;
		for(i=0; i<triples.size(); i++){
			p = triples.get(i).getSecondString();
			o = triples.get(i).getThirdString();
			if(p.contains(":") && !p.contains("<")){
				j = p.indexOf(":");
				prefix = p.substring(0, j).trim();
				for(k=0; k<prefixes.size(); k++)
					if(prefixes.get(k).getFirstString().equalsIgnoreCase(prefix))
						break;
				if(k<prefixes.size()){
					temp = prefixes.get(k).getSecondString()+p.substring(j+1);
					triples.get(i).setSecondString(temp);
				}
			}
			
			if(o.contains(":") && !o.contains("<")){
				j = o.indexOf(":");
				prefix = o.substring(0, j).trim();
				for(k=0; k<prefixes.size(); k++)
					if(prefixes.get(k).getFirstString().equalsIgnoreCase(prefix))
						break;
				if(k<prefixes.size()){
					temp = prefixes.get(k).getSecondString()+o.substring(j+1);
					triples.get(i).setThirdString(temp);
				}
			}
		}
	}	

	
	public void display(){
		int i;
		
		for(i=0; i<prefixes.size(); i++)
			prefixes.get(i).display();		
			
//		System.out.println(sSelect);
		
//		System.out.println("Where{");
		for(i=0; i<triples.size(); i++)
			triples.get(i).display();		
//		System.out.println("}");		
		
//		System.out.println(sLimit_Order);
		
//		for(i=0; i<filters.size(); i++)
//			System.out.println(filters.get(i).getFirstString() +":" + filters.get(i).getSecondString());
	}

	

	public MetaData createMetaData() {
		MetaData md = new MetaData();
		int i, j, k;		
		String s, p, o;
		
		//dataset
		for(i=0; i<triples.size(); i++){			
			p = triples.get(i).getSecondString();
			o = triples.get(i).getThirdString();
			
			if(p.contains("subject")){				
				md.getDataSet().setSubject(o);
			}else if(p.contains("dataSet")){
				sVarObs = triples.get(i).getFirstString();
				md.getDataSet().setVariable(o);
				for(j=0; j<filters.size(); j++){
					if(filters.get(j).getFirstString().equalsIgnoreCase(o))
						md.getDataSet().setUri(filters.get(j).getSecondString());		
				}
				
			}
		}
		
		//label of dataset
		if(md.getDataSet().getVariable()!=""){
			for(i=0; i<triples.size(); i++){
				s = triples.get(i).getFirstString();
				p = triples.get(i).getSecondString();
				o = triples.get(i).getThirdString();
				if(s.equalsIgnoreCase(md.getDataSet().getVariable()) && p.contains("label") && o.startsWith("?"))
					md.getDataSet().setVariableLabel(o);
			}
		}
		
		//component
		for(i=0; i<triples.size(); i++){
			p = triples.get(i).getSecondString();
			o = triples.get(i).getThirdString();
			if(p.contains("sdmx")){
				if(p.contains("dimension")){
					Component cp = new Component();
					cp.setUri(p);
					cp.setVariable(o);					
					cp.setType("Dimension");	
					for(j=0; j<filters.size(); j++){
						if(filters.get(j).getFirstString().equalsIgnoreCase(o))
							cp.setFilterValue(filters.get(j).getSecondString());		
					}					
					md.addComponent(cp);
				}
				else if(p.contains("attribute")){
					Component cp = new Component();
					cp.setUri(p);
					cp.setVariable(o);
					cp.setType("Attribute");	
					for(j=0; j<filters.size(); j++){
						if(filters.get(j).getFirstString().equalsIgnoreCase(o))
							cp.setFilterValue(filters.get(j).getSecondString());		
					}
					md.addComponent(cp);
				}					
				else if(p.contains("measure")){
					Component cp = new Component();
					cp.setUri(p);
					cp.setVariable(o);
					cp.setType("Measure");			
					md.addComponent(cp);
				}					
			}				
		}
		
		//label of component
		for(k=0; k<md.getNumberofComponent(); k++){
			for(i=0; i<triples.size(); i++){
				s = triples.get(i).getFirstString();
				p = triples.get(i).getSecondString();
				o = triples.get(i).getThirdString();
				if(s.equalsIgnoreCase(md.getComponent(k).getVariable()) && p.contains("label") && o.startsWith("?"))
					md.getComponent(k).setVariableLabel(o);				
			}
		}
		
//		md.display();
		return md;
	}	
}
