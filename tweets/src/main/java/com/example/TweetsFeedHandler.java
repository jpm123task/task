package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TweetsFeedHandler implements RequestStreamHandler {

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
            if (event.get("pathParameters") != null) {
                Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

                JSONObject pathParams = (JSONObject) event.get("pathParameters");
                String usernameVal = (String) pathParams.get("username");

                Map<String, Object> expressionAttributeValues = new HashMap<String, Object>();
                expressionAttributeValues.put(":usernameVal", usernameVal);

                ItemCollection<ScanOutcome> items = table.scan("username <> :usernameVal", null,
                        expressionAttributeValues);


                List<Object> result = new ArrayList<Object>();
                Iterator<Item> iterator = items.iterator();
                while (iterator.hasNext()) {
                    result.add(iterator.next().get("tweets"));
                }

                responseBody.put("My feed", result);
                responseJson.put("statusCode", 200);
                responseJson.put("body", responseBody.toString());
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
