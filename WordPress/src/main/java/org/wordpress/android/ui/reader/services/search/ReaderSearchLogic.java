package org.sitebay.android.ui.reader.services.search;

import com.android.volley.VolleyError;
import com.sitebay.rest.RestRequest;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.sitebay.android.WordPress;
import org.sitebay.android.datasets.ReaderPostTable;
import org.sitebay.android.models.ReaderPostList;
import org.sitebay.android.ui.reader.ReaderConstants;
import org.sitebay.android.ui.reader.ReaderEvents;
import org.sitebay.android.ui.reader.services.ServiceCompletionListener;
import org.sitebay.android.util.AppLog;
import org.sitebay.android.util.UrlUtils;

import static org.sitebay.android.ui.reader.utils.ReaderUtils.getTagForSearchQuery;

public class ReaderSearchLogic {
    private ServiceCompletionListener mCompletionListener;
    private Object mListenerCompanion;

    public ReaderSearchLogic(ServiceCompletionListener listener) {
        mCompletionListener = listener;
    }

    public void startSearch(final String query, final int offset, Object companion) {
        mListenerCompanion = companion;
        String path = "read/search?q="
                      + UrlUtils.urlEncode(query)
                      + "&number=" + ReaderConstants.READER_MAX_SEARCH_RESULTS_TO_REQUEST
                      + "&offset=" + offset
                      + "&meta=site,likes";

        RestRequest.Listener listener = new RestRequest.Listener() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    handleSearchResponse(query, offset, jsonObject);
                } else {
                    EventBus.getDefault().post(new ReaderEvents.SearchPostsEnded(query, offset, false));
                }
            }
        };
        RestRequest.ErrorListener errorListener = new RestRequest.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                AppLog.e(AppLog.T.READER, volleyError);
                EventBus.getDefault().post(new ReaderEvents.SearchPostsEnded(query, offset, false));
                mCompletionListener.onCompleted(mListenerCompanion);
            }
        };

        AppLog.d(AppLog.T.READER, "reader search service > starting search for " + query);
        EventBus.getDefault().post(new ReaderEvents.SearchPostsStarted(query, offset));
        WordPress.getRestClientUtilsV1_2().get(path, null, null, listener, errorListener);
    }

    private void handleSearchResponse(final String query, final int offset, final JSONObject jsonObject) {
        new Thread() {
            @Override
            public void run() {
                ReaderPostList serverPosts = ReaderPostList.fromJson(jsonObject);
                ReaderPostTable.addOrUpdatePosts(getTagForSearchQuery(query), serverPosts);
                EventBus.getDefault().post(new ReaderEvents.SearchPostsEnded(query, offset, true));
                mCompletionListener.onCompleted(mListenerCompanion);
            }
        }.start();
    }
}
