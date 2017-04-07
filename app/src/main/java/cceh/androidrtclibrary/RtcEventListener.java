package cceh.androidrtclibrary;

import org.webrtc.MediaStream;

/**
 * Listener to be registered with {@link RtcClient}, to notify caller of {@link RtcClient}
 * the events from remote peers.
 *
 * Created by Charlie Chen (ccehshmily@gmail.com) on 4/7/17.
 */
public class RtcEventListener {
  public void onClientReady() {};
  public void onClientDied(Throwable e) {};
  public void onConnected(String peerId) {};
  public void onDisconnected(String peerId) {};
  public void onRemoteStreamAdded(String peerId, MediaStream mediaStream) {};
  public void onRemoteStreamRemoved(String peerId, MediaStream mediaStream) {};
}
