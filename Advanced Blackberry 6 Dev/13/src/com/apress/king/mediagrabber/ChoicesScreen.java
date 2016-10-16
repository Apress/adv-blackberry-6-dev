package com.apress.king.mediagrabber;

import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.MessageFormat;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.ButtonFieldFactory;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.picker.FilePicker;

public class ChoicesScreen extends MainScreen implements MediaGrabberResource
{

    private static ResourceBundleFamily r = ResourceBundle.getBundle(BUNDLE_ID,
            BUNDLE_NAME);

    private BasicEditField location = new BasicEditField(r
            .getString(I18N_PROMPT_LOCATION), "file:///SDCard/BlackBerry", 100,
            Field.FIELD_VCENTER | BasicEditField.FILTER_URL);
    private MenuItem audioItem = new MenuItem(r,
            MediaGrabber.I18N_CHOICE_RECORD_SOUND, 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_AUDIO);
        }
    };
    private MenuItem pictureItem = new MenuItem(r,
            MediaGrabber.I18N_CHOICE_TAKE_PICTURE, 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_PICTURE);
        }
    };
    private MenuItem videoItem = new MenuItem(r,
            MediaGrabber.I18N_CHOICE_RECORD_VIDEO, 0, 0)
    {
        public void run()
        {
            launchRecorder(RecordingScreen.RECORD_VIDEO);
        }
    };
    private MenuItem launchVideoItem = new MenuItem(r,
            MediaGrabber.I18N_CHOICE_PLAY_MEDIA, 0, 0)
    {
        public void run()
        {
            launchPlayer();
        }
    };
    private MenuItem sendLogItem = new MenuItem(r, MediaGrabber.I18N_SEND_LOG,
            0x10000, 0)
    {
        public void run()
        {
            String message = StatusUpdater.getLog();
            SendingScreen sending = new SendingScreen("text/plain", "log.txt",
                    "Log attached", message.getBytes(), false);
            UiApplication.getUiApplication().pushScreen(sending);
        }
    };
    private MenuItem updateItem = new MenuItem(r,
            MediaGrabber.I18N_UPGRADE_CHOICE, 0x20000, 0)
    {
        public void run()
        {
            String url = "http://www.example.com/MediaGrabber.jad";
            Browser.getDefaultSession().displayPage(url);
        }
    };

public ChoicesScreen()
{
    setTitle("MediaGrabber");
    add(new LabelField(r, MediaGrabber.I18N_INSTRUCTIONS));
    add(location);
    ButtonField button = ButtonFieldFactory.getInstance().create(
            new Command(new CommandHandler()
            {
                public void execute(ReadOnlyCommandMetadata metadata,
                        Object context)
                {
                    FilePicker picker = FilePicker.getInstance();
                    picker.setPath("file:///SDCard/BlackBerry");
                    picker.setTitle("Choose media");
                    String file = picker.show();
                    if (file != null)
                    {
                        location.setText(file);
                    }
                }
            }));
    button.setLabel("Choose media");
    add(button);
}

    public void close()
    {
        location.setDirty(false);
        super.close();
    }

    public void makeMenu(Menu menu, int instance)
    {
        if (instance == Menu.INSTANCE_DEFAULT)
        {
            String property = System.getProperty("supports.audio.capture");
            if (property != null && property.equals("true"))
            {
                menu.add(audioItem);
            }
            property = System.getProperty("video.snapshot.encodings");
            if (property != null && property.length() > 0)
            {
                menu.add(pictureItem);
            }
            property = System.getProperty("supports.video.capture");
            if (property != null && property.equals("true"))
            {
                menu.add(videoItem);
            }
            menu.add(launchVideoItem);
            menu.add(sendLogItem);
            menu.add(updateItem);
        }

        super.makeMenu(menu, instance);
    }

    private void launchRecorder(int type)
    {
        String directory = location.getText();
        RecordingScreen screen = new RecordingScreen(type, directory);
        UiApplication.getUiApplication().pushScreen(screen);
    }

    private void launchPlayer()
    {
        String url = location.getText();
        String pattern = r.getString(MediaGrabber.I18N_PLAY_MEDIA_TITLE);
        MessageFormat format = new MessageFormat("");
        format.setLocale(Locale.getDefaultForSystem());
        Object[] arguments = new String[]
        { url };
        format.applyPattern(pattern);
        String formatted = format.format(arguments);
        PlayingScreen screen = new PlayingScreen(url, formatted);
        UiApplication.getUiApplication().pushScreen(screen);
    }

    public boolean onSavePrompt()
    {
        return true;
    }

}
