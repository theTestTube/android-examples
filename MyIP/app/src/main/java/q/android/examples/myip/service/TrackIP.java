package q.android.examples.myip.service;

import q.android.examples.myip.model.Client;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.GET;
import rx.Observable;
import rx.functions.Func1;

public class TrackIP implements Service
{
    private static final String WEB_SERVICE_BASE_URL = "http://www.trackip.net";
    private final WebService service;

    public TrackIP() {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader("Accept", "application/json");
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(WEB_SERVICE_BASE_URL)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        service = restAdapter.create(WebService.class);
    }

    private interface WebService {
        @GET("/ip?json")
        Observable<Client> fetchClient();
    }

    @Override
    public Observable<Client> fetchClient() {
        return service.fetchClient();

        /*
                .flatMap(new Func1<Client,
                        Observable<? extends Client>>() {

                    // Error out if the request was not successful.
                    @Override
                    public Observable<? extends Client> call(
                            final Client data) {
                        return data.filterWebServiceErrors();
                    }

                });


    public Observable filterWebServiceErrors() {

        int httpCode = 200;

        if (httpCode == 200) {
            return Observable.from(this);
        } else {
            return Observable.error(
                    new HttpException("There was a problem fetching the weather data."));
        }
    }


        */
    }

}
