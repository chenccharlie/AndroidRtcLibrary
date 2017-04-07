package cceh.androidrtclibrary.network;

import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.webrtc.PeerConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link StunTurnServerProvider} which fetches server info from www.xirsys.com .
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/6/17.
 */
public class XirSysStunTurnServerProvider implements StunTurnServerProvider {
  private static final String TAG = "XirSysServerProvider";

  private static final String XIRSYS_URL = "https://service.xirsys.com/ice";
  private static final String DOMAIN = "domain";
  private static final String APPLICATION = "application";
  private static final String ROOM = "room";
  private static final String IDENTITY = "ident";
  private static final String SECRET = "secret";
  private static final String SECURE = "secure";

  private final String domain;
  private final String application;
  private final String room;
  private final String identity;
  private final String secret;
  private final String secure;

  @Nullable private List<PeerConnection.IceServer> iceServers;

  public XirSysStunTurnServerProvider(
      String domain,
      String application,
      String room,
      String identity,
      String secret,
      String secure) {
    this.domain = domain;
    this.application = application;
    this.room = room;
    this.identity = identity;
    this.secret = secret;
    this.secure = secure;

    this.iceServers = null;
  }

  @Override
  public void fetchServers(Callback callback) {
    List<PeerConnection.IceServer> servers = new ArrayList<PeerConnection.IceServer>();

    HttpClient httpClient = new DefaultHttpClient();
    HttpPost request = new HttpPost(XIRSYS_URL);
    List<NameValuePair> data = new ArrayList<NameValuePair>();
    data.add(new BasicNameValuePair(DOMAIN, domain));
    data.add(new BasicNameValuePair(APPLICATION, application));
    data.add(new BasicNameValuePair(ROOM, room));
    data.add(new BasicNameValuePair(IDENTITY, identity));
    data.add(new BasicNameValuePair(SECRET, secret));
    data.add(new BasicNameValuePair(SECURE, secure));

    //Encoding POST data
    try {
      request.setEntity(new UrlEncodedFormEntity(data));
      HttpResponse response = httpClient.execute(request);
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          response.getEntity().getContent(), "UTF-8"));

      StringBuilder builder = new StringBuilder();
      for (String line=null; (line = reader.readLine()) != null;) {
        builder.append(line).append("\n");
      }
      JSONTokener tokener = new JSONTokener(builder.toString());
      JSONObject json = new JSONObject(tokener);
      if (json.isNull("e")) {
        JSONArray iceServers = json.getJSONObject("d").getJSONArray("iceServers");
        for (int i = 0; i < iceServers.length(); i++) {
          JSONObject srv = iceServers.getJSONObject(i);
          PeerConnection.IceServer is;
          if (srv.has("username")) {
            is = new PeerConnection.IceServer(
                srv.getString("url"),
                srv.getString("username"),
                srv.getString("credential"));
          } else {
            is = new PeerConnection.IceServer(srv.getString("url"));
          }
          servers.add(is);
        }
      }
    } catch (IOException | JSONException e) {
      callback.onServerFetchFails(new NetworkException(e));
    }

    Log.i(TAG, "Servers: " + servers.toString());
    this.iceServers = servers;
    callback.onServersFetched();
  }

  @Override
  public List<PeerConnection.IceServer> getServers() throws NetworkException {
    if (iceServers == null) {
      throw new NetworkException("Servers are not fetched yet.");
    } else {
      return iceServers;
    }
  }
}
