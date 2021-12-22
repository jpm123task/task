package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.json.simple.JSONObject;

public class LogoutRequestHandler implements RequestStreamHandler{

    JSONObject responseJson = new JSONObject();
    JSONObject responseBody = new JSONObject();
    
        @Override
        public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
            responseBody.put("message", "Logged out successfully");
            responseJson.put("statusCode", 200);
            responseJson.put("body", responseBody.toString());

        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(responseJson.toString());
        writer.close();
         
        }
        
    
}
