package org.cong.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.github.czyzby.websocket.GwtWebSockets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Panel;
import org.cong.client.Game;

public class GwtLauncher extends GwtApplication {

  @Override
  public GwtApplicationConfiguration getConfig() {
    var config = new GwtApplicationConfiguration(1280, 720);
    config.useAccelerometer = false;
    config.antialiasing = true;
    return config;
  }

  @Override
  public Preloader.PreloaderCallback getPreloaderCallback() {
    return createPreloaderPanel(GWT.getHostPageBaseURL() + "preloadlogo.png");
  }

  @Override
  protected void adjustMeterPanel(Panel meterPanel, Style meterStyle) {
    meterPanel.setStyleName("gdx-meter");
    meterPanel.addStyleName("nostripes");
    meterStyle.setProperty("backgroundColor", "#e3c615");
    meterStyle.setProperty("backgroundImage", "none");
  }

  @Override
  public ApplicationListener createApplicationListener () {
    GwtWebSockets.initiate();
    return new Game();
  }
}
