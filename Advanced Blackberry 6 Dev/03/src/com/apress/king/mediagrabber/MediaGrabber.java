package com.apress.king.mediagrabber;

import net.rim.device.api.ui.UiApplication;

public class MediaGrabber extends UiApplication 
{

	public static void main(String[] args) 
	{
		new MediaGrabber().enterEventDispatcher();
	}
	
	private MediaGrabber()
	{
	    String mixing = System.getProperty("supports.mixing");
		pushScreen(new ChoicesScreen());
	}

}
