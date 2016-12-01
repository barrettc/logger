package com.scispike.logger;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import android.content.Intent;
import android.util.Log;

public class Logger extends CordovaPlugin {

  private final static String TAG = Logger.class.getSimpleName();

  // actions
  private static final String GET_LOGS = "getLogs";

  private CallbackContext getLogsCallback;

  @Override
  public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    Log.d(TAG, "action = " + action);

    if (action.equals(GET_LOGS)) {
      getLogsCallback = callbackContext;
      PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
      pluginResult.setKeepCallback(true);
      getLogsCallback.sendPluginResult(pluginResult);

      String context = args.getString(0);
      int logTime = args.getInt(1);

      getLogs(callbackContext, context, logTime);
      return true;

    } else {
      callbackContext.error("Invalid action");
      return false;
    }
  }

  private void getLogs(final CallbackContext callbackContext, final String context, final int logTime) {
    Intent service = new Intent(cordova.getActivity(), LoggerService.class);
    service.putExtra("logTime", logTime);
    service.putExtra("context", context);
    cordova.getActivity().startService(service);
    callbackContext.success();
  }
}
