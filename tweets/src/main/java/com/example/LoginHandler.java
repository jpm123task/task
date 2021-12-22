package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LoginHandler implements RequestStreamHandler{

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
            JSONObject event = (JSONObject) parser.parse(reader);
            if (event.get("body") != null) {
                JSONObject body = (JSONObject) event.get("body");
                if (body.get("username") != null && body.get("password") != null) {
                    String usernameInput = (String) body.get("username");
                    String passwordInput = (String) body.get("password");
                    String password = (String) dynamoDb
                    .getTable(DYNAMODB_TABLE_NAME)
                    .getItem("username", usernameInput)
                    .get("password");
                    if (passwordInput.equals(password)) {
                        responseBody.put("message", "Logged in successfully.");
                        responseJson.put("statusCode", 200);

                    } else {
                        responseJson.put("statusCode", 401);
                        responseBody.put("message", "Incorrect password.");
                    

                    }
                    responseJson.put("body", responseBody.toString());
                }
        }
         }
            catch (org.json.simple.parser.ParseException e) {
                responseJson.put("statusCode", 400);
                responseJson.put("error", e);
        }
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();

    }

}
    
