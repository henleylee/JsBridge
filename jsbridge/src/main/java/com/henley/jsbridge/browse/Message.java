package com.henley.jsbridge.browse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * JsBridge消息对象
 *
 * @author Henley
 * @date 2017/7/10 11:16
 */
final class Message {

    private final static String DATA = "data";
    private final static String CALLBACK_ID = "callbackId";
    private final static String RESPONSE_ID = "responseId";
    private final static String HANDLER_NAME = "handlerName";
    private final static String RESPONSE_DATA = "responseData";

    private String data; //data of message
    private String callbackId; //callbackId
    private String responseId; //responseId
    private String handlerName; //name of handler
    private String responseData; //responseData

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    /**
     * 将Message对象转换为JSON字符串
     */
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(DATA, getData());
            jsonObject.put(CALLBACK_ID, getCallbackId());
            jsonObject.put(RESPONSE_ID, getResponseId());
            jsonObject.put(HANDLER_NAME, getHandlerName());
            jsonObject.put(RESPONSE_DATA, getResponseData());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Message toMessage(String messageJson) {
        Message message = new Message();
        try {
            JSONObject jsonObject = new JSONObject(messageJson);
            message.setData(jsonObject.has(DATA) ? jsonObject.getString(DATA) : null);
            message.setCallbackId(jsonObject.has(CALLBACK_ID) ? jsonObject.getString(CALLBACK_ID) : null);
            message.setResponseId(jsonObject.has(RESPONSE_ID) ? jsonObject.getString(RESPONSE_ID) : null);
            message.setHandlerName(jsonObject.has(HANDLER_NAME) ? jsonObject.getString(HANDLER_NAME) : null);
            message.setResponseData(jsonObject.has(RESPONSE_DATA) ? jsonObject.getString(RESPONSE_DATA) : null);
            return message;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 将JSON字符串转换为Message集合
     */
    public static List<Message> toArrayList(String jsonStr) {
        List<Message> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                Message message = new Message();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                message.setData(jsonObject.has(DATA) ? jsonObject.getString(DATA) : null);
                message.setCallbackId(jsonObject.has(CALLBACK_ID) ? jsonObject.getString(CALLBACK_ID) : null);
                message.setResponseId(jsonObject.has(RESPONSE_ID) ? jsonObject.getString(RESPONSE_ID) : null);
                message.setHandlerName(jsonObject.has(HANDLER_NAME) ? jsonObject.getString(HANDLER_NAME) : null);
                message.setResponseData(jsonObject.has(RESPONSE_DATA) ? jsonObject.getString(RESPONSE_DATA) : null);
                list.add(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }
}
