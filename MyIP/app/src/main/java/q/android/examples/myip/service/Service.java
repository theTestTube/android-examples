package q.android.examples.myip.service;

import q.android.examples.myip.model.Client;
import rx.Observable;

/**
 * Created by Agus on 26/08/2014.
 */
public interface Service
{
  public Observable<Client> fetchClient();
}
