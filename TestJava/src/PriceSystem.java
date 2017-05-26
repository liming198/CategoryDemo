import java.util.HashMap;
import java.util.Map;


public class PriceSystem {
	
	private Map<String,Category> maps = new HashMap<String,Category>();
	
	private static PriceSystem instance = new PriceSystem();
	
	private PriceSystem(){
		 initSystem();
	}
	
	public void initSystem(){

		Category root = new Category("Root",0);
		maps.put("Root", root);
		
		Category electronics = new Category("Electronics",1,root);
		Category sports= new Category("Sports",1,root);
		maps.put("Electronics", electronics);
		maps.put("Sports", sports);
		
		root.getChilds().add(electronics);
		root.getChilds().add(sports);
			
		Category phone = new Category("Phone",1,electronics);
		Category mobile= new Category("Mobile",10,phone);
		Category landPhone= new Category("Land Phone",10,phone);
		maps.put("Phone", phone);
		maps.put("Mobile", mobile);
		maps.put("Land Phone", landPhone);
		
		phone.getChilds().add(mobile);
		phone.getChilds().add(landPhone);
		
		Category game = new Category("Game",1,electronics);
		Category playstation= new Category("Playstation",10,game);
		Category xbox= new Category("Xbox",10,game);
		maps.put("Game", game);
		maps.put("Playstation", playstation);
		maps.put("Xbox", xbox);
		game.getChilds().add(playstation);
		game.getChilds().add(xbox);

		Category computer = new Category("Computer",1,electronics);
		Category laptop = new Category("Laptop",10,computer);
		Category desktop= new Category("Desktop",10,computer);
		maps.put("Computer", computer);
		maps.put("Laptop", laptop);
		maps.put("Desktop", desktop);
		computer.getChilds().add(laptop);
		computer.getChilds().add(desktop);
		
		Category audio = new Category("Audio",1,electronics);
		Category speaker = new Category("Speaker",11,audio);
		Category mP3= new Category("MP3",10,audio);
		Category test0= new Category("Test0",0,audio);
		maps.put("Audio", audio);
		maps.put("Speaker", speaker);
		maps.put("MP3", mP3);
		maps.put("Test0", test0);
		audio.getChilds().add(speaker);
		audio.getChilds().add(mP3);
		audio.getChilds().add(test0);
		
		electronics.getChilds().add(game);
		electronics.getChilds().add(computer);
		electronics.getChilds().add(phone);
		electronics.getChilds().add(audio);
		
		Category fitness = new Category("Fitness",1,sports);
		Category outdoor = new Category("Outdoor",1,sports);
		maps.put("Fitness", fitness);
		maps.put("Outdoor", outdoor);
		
		sports.getChilds().add(fitness);
		sports.getChilds().add(outdoor);
	
	}
	
	
	public static PriceSystem getInstance(){
		return instance;
	}
	
	
	public int getPrice(String categoryName){
		int price = 0;
		Category c = maps.get(categoryName);
		while(c != null){
				if(c.getPrice() != 0){
					price = c.getPrice();
					break;
				}else{
					c = c.getParent();
				}
         }
		 return price;
	  }
	
	public static void main(String[] args) {
		PriceSystem ps = PriceSystem.getInstance();
		System.out.println(ps.getPrice("Fitness"));
		System.out.println(ps.getPrice("Speaker"));
		System.out.println(ps.getPrice("Test0"));
	}

}
