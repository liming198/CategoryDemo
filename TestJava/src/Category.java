import java.util.ArrayList;


public class Category {

	private String name= "";
	private int price = 0;
	
	private Category parent;
	private ArrayList<Category> childs = new ArrayList<Category>();
	
	public Category(){
		
	}
	
    public Category(String name){
		this.name = name;
	}
    
    public Category(String name,int price){
		this.name = name;
		this.price = price;
	}
    
    public Category(String name,int price,Category parent){
		this.name = name;
		this.price = price;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public Category getParent() {
		return parent;
	}
	public void setParent(Category parent) {
		this.parent = parent;
	}
	public ArrayList<Category> getChilds() {
		return childs;
	}
	public void setChilds(ArrayList<Category> childs) {
		this.childs = childs;
	}
	
	
	
}
