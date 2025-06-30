package work_java;
import java.io.IOException;

public class Lesson5 {

		public static void main(String[]args) throws IOException
		{
			//Lesson5の１番です

			
			int num = (int)(Math.random()*10)+1;
			
				if((num%2)==0)
					System.out.println(num+"は偶数です。");
				else
					System.out.println(num+"は奇数です。");
			
			//Lesson5の２番です
			
			int res = (int)(Math.random()*100)+1;
			
			System.out.println("テストの点数は"+res+"です。");
			
				if(res >=0 && res <=49)
				{
					System.out.println("不可");
				}
			
				else if(res >=50 && res<=69)
				{
					System.out.println("可");
				}
			
				else if(res >=70 && res<=79)
				{
					System.out.println("良");
				}
			
				else if(res >=80 && res<=99)
				{
					System.out.println("優");
				}	
			
				else if(res <= 100)
				{
					System.out.println("満点");
				}	
			
				else 
				{
					System.out.println("1～100までの点数を入力してください。");
				}
		
			
			//Lesson5の３番です
			
			int res1 = (int)(Math.random()*100)+1;
			int res2 = (int)(Math.random()*100)+1;
			
			System.out.println("res1は"+res1+","+"res2は"+res2+"です。");
			
				if(res1 >=60 && res2 >=60){
					System.out.println("合格");}
			
				else if(res1+res2>=130){
					System.out.println("合格");}
			
				else if( (res1+res2>=100) && (res1>=90 || res2>=90)){
					System.out.println("合格");}
				
				else {
					System.out.println("不合格");}
		}

}
