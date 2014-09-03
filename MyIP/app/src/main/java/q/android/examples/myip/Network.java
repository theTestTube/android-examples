package q.android.examples.myip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

/**
 * Created by Agus on 04/08/2014.
 */
public class Network extends BroadcastReceiver
{
  private static String TAG = Network.class.getSimpleName();
  Subject<Boolean, Boolean> connectivity = BehaviorSubject.create();

  // TODO Evaluate Observable<Intent> fromLocalBroadcast(Context context, IntentFilter filter)
  // https://github.com/Netflix/RxJava/blob/master/rxjava-contrib/rxjava-android/src/main/java/rx
  // /android/observables/AndroidObservable.java

  /**
   * @param context to get <code>CONNECTIVITY_SERVICE</code> from
   * @return <code>boolean</code> to state if device is connected to any network
   */
  public static boolean isConnected(Context context)
  {
    ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context
      .CONNECTIVITY_SERVICE);

    if (manager != null)
    {
      NetworkInfo active = manager.getActiveNetworkInfo();
      NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

      if (mobile != null)
        Log.d(TAG, "Mobile Network Type : " + mobile.getTypeName());

      if (active != null)
      {
        Log.d(TAG, "Active Network Type : " + active.getTypeName());
        return active.getState() == NetworkInfo.State.CONNECTED;
      }
    }

    return false;
  }

  /**
   * @param address String representation for an IPv4 or IPv6 address
   * @return boolean value indicating if specified IP <code>address</code> is set for any interface
   */
  public static boolean hasIPAddress(String address)
  {
    try
    {
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface intf : interfaces)
      {
        List<InetAddress> addresses = Collections.list(intf.getInetAddresses());
        for (InetAddress addr : addresses)
        {
          if (addr.getHostAddress().toUpperCase().equals(address.toUpperCase()))
            return true;
        }
      }
    } catch (Exception ex)
    {
      Log.e(TAG, ex.getMessage());
    } // eat exceptions

    return false;
  }

  /**
   * @return <code>Observable</code> emitting <code>Boolean</code> for connectivity status
   */
  public Observable<Boolean> isConnected()
  {
    return connectivity.distinctUntilChanged();
  }

  @Override
  public void onReceive(Context context, Intent intent)
  {
    connectivity.onNext(isConnected(context));
  }
}
