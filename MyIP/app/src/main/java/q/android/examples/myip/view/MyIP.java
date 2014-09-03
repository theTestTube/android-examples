package q.android.examples.myip.view;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

import q.android.examples.myip.Network;
import q.android.examples.myip.R;
import q.android.examples.myip.model.Client;
import q.android.examples.myip.service.Service;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Agus on 26/08/2014.
 */
public class MyIP
{
  private static String TAG = MyIP.class.getSimpleName();

  final private Network network = new Network();
  final private CompositeSubscription subscriptions = new CompositeSubscription();

  private Context context = null;
  private Service service = null;
  private Unknown unknown = null;

  // Subjects exposing observables to UI
  protected Subject<String, String> address = null;
  protected Subject<Boolean, Boolean> nated = null;

  public void bind(Context c, Service s)
  {
    context = c;
    context.registerReceiver(network, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    service = s;

    // Unknown requires context to be initialised
    unknown = new Unknown();

    // Subject definitions with initial values
    address = BehaviorSubject.create(unknown.getIp());
    nated = BehaviorSubject.create(false);

    // String subject subscription to address string values
    subscriptions.add(new Address().getValue().subscribe(address));

    // Boolean subject subscription to each address emitted, checking if nated
    subscriptions.add(address.flatMap(new Func1<String, Observable<Boolean>>()
    {
      @Override
      public Observable<Boolean> call(String s)
      {
        return Observable.just(!unknown.contentEquals(s) && !Network.hasIPAddress(s));
      }
    }).subscribe(nated));
  }

  public void unbind()
  {
    context.unregisterReceiver(network);
    subscriptions.clear();
    unknown = null;
    context = null;
    service = null;
  }


  public Observable<String> getIPAddress()
  {
    return address;
  }

  public Observable<Boolean> getNated()
  {
    return nated;
  }


  public class Unknown extends Client
  {
    public Unknown()
    {
      setIp(context.getString(R.string.unknown_address_text));
    }

    public boolean contentEquals(String s)
    {
      return s.contentEquals(context.getString(R.string.unknown_address_text));
    }
  }

  // Subject definition for address specific observable
  public class Address
  {
    private Observable<String> address;

    private Observable<String> getValue()
    {
      if (service == null)
        throw new RuntimeException("can't get address for null service");

      // Observable address string from service
      // Errors and schedulers are managed at this level
      if (address == null)
        address = service.fetchClient()
          .subscribeOn(Schedulers.io())
          .onErrorReturn(new Func1<Throwable, Client>()
          {
            @Override
            public Client call(Throwable throwable)
            {
              Log.e(TAG, throwable.getMessage());
              return unknown;
            }
          })
          .map(new Func1<Client, String>()
          {
            @Override
            public String call(Client info)
            {
              return info.getIp();
            }
          });

          /* TODO Implement an ArrayList<Service> services to do round-robin based failback
            .onErrorResumeNext(new Func1<Throwable, Observable<Client>>() {
            @Override
            public Observable<Client> call(Throwable throwable)
            {
              return failbackService.fetchClient();

              // TODO Limit failback to one round, might error affect any service
              if (services.hasNext())
                return ((Service) services.next()).fetchClient();

            }
          })
          */

      // Emit a client string from service if connected or from an unknown client otherwise
      // Error support or any special scheduler is not required
      return network.isConnected()
        .flatMap(new Func1<Boolean, Observable<String>>()
        {
          @Override
          public Observable<String> call(Boolean connected)
          {
            Log.d(TAG, "Connectivity changed to " + connected);
            return (connected ? address : Observable.just(unknown.getIp()));
          }
        });
    }
  }
}
