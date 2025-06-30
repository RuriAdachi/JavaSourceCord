package work_java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;



class Lesson9
//1	問目
//.JSONファイルを読み込んで、各都道府県のidと名前（short）を表示させてください。
{
	public static void main(String[] args) throws IOException
	
	{
		Gson gson = new Gson();
		
		BufferedReader reader = new BufferedReader(new FileReader("src/work_java/sample.json"));
		//Filereaderでファイル読みこみ
		
		
		JsonObject sample = gson.fromJson(reader, JsonObject.class);
		//オブジェクト化
		
			for (Entry<String, JsonElement> entry : sample.entrySet()) 
			{
				JsonObject value = entry.getValue().getAsJsonObject(); // 値を取得
            
            
				String  id = value.get("id").getAsString();
				String shortname = value.get("short").getAsString();
            
				System.out.println("id:"+id+"は"+shortname);
            
            }
		
//2問目　　県ごとの市区町村の数を表示してください
		

			for (Entry<String, JsonElement> entry : sample.entrySet()) 
			{
				JsonObject value = entry.getValue().getAsJsonObject(); // 値を取得
            
            
				JsonArray  city = value.get("city").getAsJsonArray();
				String  id = value.get("id").getAsString();
				String shortname  = value.get("short").getAsString();
            	
				System.out.println("id:"+id+"の"+shortname+"は"+city.size());
			}
		
	}
}	
	


