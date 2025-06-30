package work_java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Lesson7 {
    public static void main(String[] args) {
        
// Lesson7の練習問題1番です
        for (int i = 1; i <= 9; i++)
        {
            for (int j = 1; j <= 9; j++)
            {
                System.out.print(i+"×"+j+"="+i * j + " ");  
            }
            System.out.println(); 
        }
        
 //Lesson7の練習問題の2番です
      		
      	HashMap<String, HashMap<String, String>> employees = new HashMap<String, HashMap<String, String>>() {
        	{
      			put("中田", new HashMap<String, String>() {
      				{
      					put("age", "23");
      					put("pref", "東京");
      				}
           		});
            	put("山本", new HashMap<String, String>() {
                	{
	                    put("age", "19");
	                    put("pref", "京都");
                	}
           		});
           		put("佐藤", new HashMap<String, String>() {
               		{
	                    put("age", "30");
	                    put("pref", "大阪");
               		}
           		});
            	put("小林", new HashMap<String, String>() {
            		{
            			put("age", "22");
            			put("pref", "福岡");
                	}
            	});
       		 }
        };
        
        for (Map.Entry<String, HashMap<String, String>> entry : employees.entrySet())
        {
            String key = entry.getKey();
            String age = entry.getValue().get("age");
            String prefecture = entry.getValue().get("pref");
            
            System.out.println("名前は" + key + "、年齢は" + age + "、出身地は" + prefecture);
        }
        
      		
      		
      		
 //応用問題の１番です

    	ArrayList<Integer> numbers=new ArrayList<Integer>();

    	for(int i=1;i<=40;i++)
    	{
    		if(i%3==0){
    			numbers.add(i);
    		}
    		else
    		{
    			String str= String.valueOf(i);
    			if(str.contains("3"))
    			{
    					numbers.add(i);
    					//true
    			}
    		}			

    		System.out.println(numbers);
    	}
   
    
    
    
//応用問題の２番です
    	
        ArrayList<HashMap<String, String>> people = new ArrayList<>();

        for (String key : employees.keySet()) 
        {
            String age = employees.get(key).get("age");
            HashMap<String, String> person = new HashMap<>();
            person.put("name", key);
            person.put("age", age);
            people.add(person);
        }

       
        System.out.println(people);
    }
}

