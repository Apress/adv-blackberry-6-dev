import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.script.ScriptEngine;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import org.w3c.dom.Document;

public class BrowserScreen extends MainScreen
{
    private LabelField status;
    private StatusUpdater updater;
    private String url;
    private BrowserField field;

    private BrowserFieldListener listener = new BrowserFieldListener()
    {
        public void documentAborted(BrowserField browserField, Document document)
        {
            updater.sendDelayedMessage("Aborted " + document.getDocumentURI());
        }

        public void documentCreated(BrowserField browserField,
                ScriptEngine scriptEngine, Document document)
        {
            updater.sendDelayedMessage("Created document "
                    + document.getDocumentURI() + " with engine "
                    + scriptEngine);
        }

        public void documentError(BrowserField browserField, Document document)
        {
            updater.sendDelayedMessage("Error in " + document.getDocumentURI());
        }

        public void documentLoaded(BrowserField browserField, Document document)
        {
            updater.sendDelayedMessage("Loaded " + document.getDocumentURI());
        }

        public void documentUnloading(BrowserField browserField,
                Document document)
        {
            updater
                    .sendDelayedMessage("Unloading "
                            + document.getDocumentURI());
        }

        public void downloadProgress(BrowserField browserField,
                ContentReadEvent event)
        {
            updater.sendDelayedMessage("Downloaded " + event.getItemsRead()
                    + " of " + event.getItemsToRead());
        }

    };

    public BrowserScreen()
    {
        status = new LabelField("Loading...");
        add(status);
        updater = new StatusUpdater(status);
        url = "http://www.bing.com";
        field = new BrowserField();
        add(new LabelField("Your search starts here."));
        add(field);
        field.addListener(listener);
        add(new LabelField("Don't forget to tip the service!"));
    }

    protected void onUiEngineAttached(boolean attached)
    {
        if (attached)
        {
            try
            {
                field.requestContent(url);
            }
            catch (Exception e)
            {
                updater.sendDelayedMessage("Failed to request URL " + url
                        + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}

