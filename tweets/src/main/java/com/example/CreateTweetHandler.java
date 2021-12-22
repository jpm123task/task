package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CreateTweetHandler implements RequestStreamHandler{

    private static final String DYNAMODB_TABLE_NAME = "user";

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        JSONParser parser = new JSONParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        JSONObject responseJson = new JSONObject();
        JSONObject responseBody = new JSONObject();

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDb = new DynamoDB(client);

        try {
            Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
            JSONObject event = (JSONObject) parser.parse(reader);

            if (event.get("pathParameters") != null && event.get("body") != null) {
                JSONObject pathParams = (JSONObject) event.get("pathParameters");
                String username = (String) pathParams.get("username");

                Object body = event.get("body");
                Object newTweet =  ((HashMap) body).get("tweet");


                if (newTweet.toString().length() < 160) {
                    List<Object> tweetList = new ArrayList<Object>();
                    tweetList.add(newTweet);

                    Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
                    expressionAttributeValues.put(":tweet", tweetList);

                    Map<String, String> expressionAttributeNamesMap = new HashMap<String, String>();
                    expressionAttributeNamesMap.put("#t","tweets");

                    UpdateItemOutcome outcome = table.updateItem("username",
                    username,
                    "SET #t = list_append(#t, :tweet)",
                    expressionAttributeNamesMap,
                    expressionAttributeValues );

                    responseBody.put("message", "Tweet created successfully.");
                    responseJson.put("statusCode", 200);
                    responseJson.put("body", responseBody.toString());
                
                } else {
                    responseBody.put("message", "Posts cannot exceed 160 characters.");
                    responseJson.put("statusCode", 400);
                    responseJson.put("body", responseBody.toString());
                }
                
            }

        } catch (org.json.simple.parser.ParseException e) {
            responseJson.put("statusCode", 400);
            responseJson.put("error", e);
        }

        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
    }
    
}
