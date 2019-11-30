package io.github.starwishsama.namelessbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TestClass {
    public static void main(String[] args){
        int i = 114514;
        boolean bool = false;
        String[] s = {"你", "妈", "死", "了"};

        JsonObject object = new JsonObject();
        object.add("botUsers", new Gson().toJsonTree(s));
        object.addProperty("boolean", bool);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String s1 = "{\n" +
                "  \"botUsers\": [\n" +
                "    \"你\",\n" +
                "    \"妈\",\n" +
                "    \"死\",\n" +
                "    \"了\"\n" +
                "  ],\n" +
                "  \"boolean\": false\n" +
                "}";
        System.out.println("高雅输出: " + gson.toJson(object));
        System.out.println("获取到的数据: " + new JsonParser().parse(s1).getAsJsonObject().get("botUsers"));
    }
}
