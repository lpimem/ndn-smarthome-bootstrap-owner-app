package edu.memphis.netlab.homesec.nacapp.query;

import com.google.gson.Gson;

/**
 * Description:
 * <p>
 * Author: lei
 */

public enum JsonDecoder {
  Instance;

  JsonDecoder() {
    mGson = new Gson();
  }

  public Gson getGson() {
    return mGson;
  }

  private final Gson mGson;
}
