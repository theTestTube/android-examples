package q.android.examples.myip;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import q.android.examples.myip.service.TrackIP;
import q.android.examples.myip.view.MyIP;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static rx.android.observables.AndroidObservable.bindActivity;

public class Activity extends ActionBarActivity
{
  TrackIP service = new TrackIP();
  MyIP myIP = new MyIP();

  private TextView ipText;
  private TextView natText;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_myip);
    ipText = (TextView) findViewById(R.id.ip_text);
    natText = (TextView) findViewById(R.id.nated_text);

    myIP.bind(this, service);

    subscribe(myIP.getIPAddress(), new Action1<String>()
    {
      @Override
      public void call(String address)
      {
        ipText.setText(address);
      }
    });

    subscribe(myIP.getNated(), new Action1<Boolean>()
    {
      @Override
      public void call(Boolean nated)
      {
        String result = nated ? getString(R.string.nated) : getString(R.string.not_nated);
        natText.setText(result);
      }
    });
  }

  @Override
  protected void onDestroy()
  {
    subscriptions.unsubscribe();
    myIP.unbind();
    super.onDestroy();
  }


  /**
   * Subscribes action to observable running <code>subscribe</code> in the UI thread.
   * No notifications will be forwarded to the activity in case it's scheduled to finish
   *
   * @param observable
   * @param action
   * @return a Subscription that should be unsubscribed by the activity
   */
  protected rx.Subscription subscribe(Observable observable,
                                      final Action1 action)
  {
    Observable bound = bindActivity(this, observable).observeOn(AndroidSchedulers.mainThread());
    rx.Subscription s = bound.subscribe(action);
    subscriptions.add(s);

    return s;
  }
}
