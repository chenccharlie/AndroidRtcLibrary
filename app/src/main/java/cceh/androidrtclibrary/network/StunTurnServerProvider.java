package cceh.androidrtclibrary.network;

import org.webrtc.PeerConnection;

import java.util.List;

/**
 * Interface for provider class which provides a list of STUN and TURN servers to be used in rtc.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/6/17.
 */
public interface StunTurnServerProvider {

  /**
   * The callback to be called when {@link StunTurnServerProvider#fetchServers(Callback)}
   * has results.
   */
  interface Callback {
    void onServersFetched(List<PeerConnection.IceServer> servers);
    void onServerFetchFails(NetworkException e);
  }

  /** Call this to fetch STUN and TURN servers from the provider, when completed,
   * use the {@link Callback} to pass results back to caller. */
  void fetchServers(Callback callback);
}
