package com.infernostats;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BrewingDataCollection {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson GSON = new Gson();
    private static final int MAX_LENGTH = 10000;

    @Inject
    private OkHttpClient okHttpClient;

    private List<Object> data = new ArrayList<>();

    public int size()
    {
        return data.size();
    }

    public void storeEvent(Object event)
    {
        if (this.size() > MAX_LENGTH)
        {
            return;
        }

        synchronized (this)
        {
            data.add(event);
        }
    }

    protected void submitToAPI(String server)
    {
        List<Object> temp;
        synchronized (this)
        {
            if (data.isEmpty())
            {
                return;
            }
            temp = data;
            data = new ArrayList<>();
        }

        Request r = new Request.Builder()
                .url(server)
                .post(RequestBody.create(JSON, GSON.toJson(temp)))
                .build();

        okHttpClient.newCall(r).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                log.debug("Error sending crowdsourcing data", e);
            }

            @Override
            public void onResponse(Call call, Response response)
            {
                log.debug("Successfully sent crowdsourcing data");
                response.close();
            }
        });
    }
}