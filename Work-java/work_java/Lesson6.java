package work_java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lesson6 {
	public static void main(String[]args)
	{
		List<String> list = new ArrayList<>(Arrays.asList("orange","apple","grape"));
		list.add("lemon");
		
		System.out.println("lemon");
	
		
		

		Map<String, Object> map = new HashMap<>();
		
			
		map.put("name", "yoshiko");
		map.put("age", 30);
		map.put("job", "doctor");

		
		map.put("food", "curry");

		System.out.println(map.get("food"));
	
		
		
		
	
		List<Map<String, Object>> test = new ArrayList<>();
		

		Map<String, Object> person1 = new HashMap<>();
		person1.put("name", "taro");
		person1.put("age", 20);
		person1.put("from", "tokyo");
		test.add(person1);

		Map<String, Object> person2 = new HashMap<>();
		person2.put("name", "jiro");
		person2.put("age", 25);
		person2.put("from", "osaka");
		test.add(person2);

		Map<String, Object> person3 = new HashMap<>();
		person3.put("name", "saburo");
		person3.put("age", 30);
		person3.put("from", "aichi");
		test.add(person3);

		
		for(Map<String, Object> person : test) {
		    System.out.println(person);
		    
		}
	
	}	
	
}
