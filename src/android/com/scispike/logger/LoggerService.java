package com.scispike.logger;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;

import com.amchealth.auth.AccountService;
import com.amchealth.auth.AccountService.Server;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LoggerService extends IntentService {
  public LoggerService() {
    super(LoggerService.class.getSimpleName());
    setIntentRedelivery(true);
  }
  private static final String TAG = LoggerService.class.getSimpleName();
  private int logTime;
  private String context;
  private Process process;
  private BufferedOutputStream output;
  private Socket s;


  void close(Closeable c) {
    try {
      c.close();
    } catch (Exception e) {
      Log.e(TAG, Log.getStackTraceString(e));
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    this.logTime = intent.getExtras().getInt("logTime", 0);
    this.context = intent.getExtras().getString("context","");
    try {
      process = Runtime.getRuntime().exec(new String[] { "logcat", "-v", "threadtime", "-T", "0" });
      InputStream input = process.getInputStream();
      AccountService accountService = AccountService.get(this);
      URI uri = URI.create(accountService.getServerAddress(Server.Default));
      int port = uri.getPort();
      if (port == -1) {
        if (uri.getScheme().compareToIgnoreCase("HTTPS") == 0) {
          port = 443;
        } else {
          port = 80;
        }
      }
      s = new Socket(InetAddress.getByName(uri.getHost()), port);
      //explictly use default
      output = new BufferedOutputStream(s.getOutputStream(), 1024 * 8);
      Log.d(TAG, "searching for: " + context);
      output.write('$');
      // Since length is a single byte then enforce max size 0xff
      String shortName = context.length() > 0xff ? context.substring(0, 0xff) : context;
      output.write(shortName.length());
      output.write(shortName.getBytes());
      long time = System.currentTimeMillis();
      while (System.currentTimeMillis() - time < logTime) {
        output.write(input.read());
      }
      stopSelf();
    } catch (Exception e) {
      Log.e(TAG, Log.getStackTraceString(e));
    } finally {
      process.destroy();
      close(output);
      close(s);
    }
  }

  @Override
  public void onDestroy() {
    process.destroy();
    close(output);
    close(s);
  }
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}