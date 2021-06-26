package org.sitebay.android.networking;

import com.android.volley.RequestQueue;
import com.sitebay.rest.RestClient;

public interface RestClientFactoryAbstract {
    RestClient make(RequestQueue queue);
    RestClient make(RequestQueue queue, RestClient.REST_CLIENT_VERSIONS version);
}
