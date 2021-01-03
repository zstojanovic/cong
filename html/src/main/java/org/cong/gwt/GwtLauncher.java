package org.cong.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.github.czyzby.websocket.GwtWebSockets;
import org.cong.client.Game;

public class GwtLauncher extends GwtApplication {

	@Override
	public GwtApplicationConfiguration getConfig() {
		var config = new GwtApplicationConfiguration(1280, 720);
		config.antialiasing = true;
		return config;
	}

	@Override
	public ApplicationListener createApplicationListener () {
		GwtWebSockets.initiate();
		return new Game();
	}
}
