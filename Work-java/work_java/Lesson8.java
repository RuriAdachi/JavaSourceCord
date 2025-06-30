package work_java;

import java.util.ArrayList;
import java.util.Arrays;

//練習問題８番の１番目の問題です。
public class Lesson8
{
	
		public static double getAverage(int num1,int num2,int num3){
			int[]score1={num1,num2,num3};
			int sum=0;
				for (int i=0;i<score1.length;i++){
					sum+=score1[i];
				}
			double ave=(double)sum/score1.length;
			return ave;
		
		}
		

		public static void main(String[]args){
			double score2=getAverage(20,40,60);
			
		
			
			System.out.println("平均値は" + score2 + "です");
		
		}
//練習問題８番２番目の問題です
		
	   {
		String[] colors1 = {"BLUE", "RED", "GREEN"};
		String[] colors2 = {"yellow", "magenta", "cyan"};
		String[] colors3 = new String[colors1.length+colors2.length];
		
		System.arraycopy(colors1, 0, colors3, 0, colors1.length);
        System.arraycopy(colors2, 0, colors3, colors1.length, colors2.length);
        System.out.println(Arrays.toString(colors3));
		//配列を連結してcolosr3を作成
		
			for (int i = 0; i < colors3.length; i++) {
				colors3[i] = colors3[i].toLowerCase();
			}
			for (String str : colors3) {
				System.out.println(str);
			}
		
        //colors3の文字をすべて小文字に変換

        ArrayList<String>colors4= new ArrayList<String>(Arrays.asList(colors3));  
        colors4.add("black");
		
		System.out.println(colors4);
		
		//新たにcolors4という配列を作成し、そこにblackを追加
		
			if (colors4.contains("green")) {
				System.out.println("greenは配列に存在します。");
			} else {
				System.out.println("greenは配列に存在しません。");
			}
		//greenが存在するか確認
	   }
	
}	

