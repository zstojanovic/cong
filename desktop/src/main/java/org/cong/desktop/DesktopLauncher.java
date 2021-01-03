package org.cong.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;
import org.cong.client.Game;

public class DesktopLauncher {

	public static void main(String[] args) {
		CommonWebSockets.initiate();

		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		configuration.setTitle("Cong");
		configuration.setWindowedMode(1280, 720);
		configuration.setBackBufferConfig(8, 8, 8, 8, 16, 0, 3);

		new Lwjgl3Application(new Game(), configuration);
	}
}
