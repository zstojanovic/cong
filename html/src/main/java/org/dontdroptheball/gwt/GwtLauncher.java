package org.dontdroptheball.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.github.czyzby.websocket.GwtWebSockets;
import org.dontdroptheball.client.Game;

public class GwtLauncher extends GwtApplication {

	@Override
	public GwtApplicationConfiguration getConfig() {
		return new GwtApplicationConfiguration(1280, 720);
	}

	@Override
	public ApplicationListener createApplicationListener () {
		GwtWebSockets.initiate();
		return new Game();
	}
}
