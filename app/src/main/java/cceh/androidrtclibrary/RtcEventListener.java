package cceh.androidrtclibrary;

import org.webrtc.MediaStream;

/**
 * Listener to be registered with {@link RtcClient}, to notify caller of {@link RtcClient}
 * the events from remote peers.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/7/17.
 */
public interface RtcEventListener {
  void onClientReady();
  void onClientDied(Throwable e);
  void onConnected(String peerId);
  void onDisconnected(String peerId);
  void onRemoteStreamAdded(String peerId, MediaStream mediaStream);
  void onRemoteStreamRemoved(String peerId, MediaStream mediaStream);
}
