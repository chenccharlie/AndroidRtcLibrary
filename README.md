# AndroidRtcLibrary
An easy to use Android library wrapping WebRtc apis, with customizable signaling module and STUN TURN server provider.

# Instructions
More detailed instructions yet to come.

For now, just build and install the testapp and try it out!

The library core (RtcClient) takes a SignalingService and a StunTurnServerProvider.
You can provide your own implementation based on the interface documentations, or just use the existing
PubnubSignalingService and XirSysStunTurnServerProvider. Just remember to register your own account on pubnub.com and xirsys.com
and edit your keys and credentials in Constants.java .
