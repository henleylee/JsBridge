package com.liyunlong.jsbridge.browse;

/**
 * Class description
 *
 * @author YEZHENNAN220
 * @date 2016-07-08 16:24
 */
public interface JsHandler {

    void onHandler(String handlerName, String data, Callback callback);

}
