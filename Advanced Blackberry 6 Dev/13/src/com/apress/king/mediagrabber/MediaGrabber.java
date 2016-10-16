package com.apress.king.mediagrabber;

import java.io.InputStream;

import javax.microedition.content.ActionNameMap;
import javax.microedition.content.ContentHandler;
import javax.microedition.content.ContentHandlerServer;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.content.RequestListener;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.pim.Contact;

import net.rim.blackberry.api.homescreen.ShortcutEventListener;
import net.rim.blackberry.api.menuitem.*;
import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.blackberry.api.push.PushApplication;
import net.rim.blackberry.api.push.PushApplicationDescriptor;
import net.rim.blackberry.api.push.PushApplicationRegistry;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.http.PushInputStream;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

public class MediaGrabber extends UiApplication implements RequestListener,
        MediaGrabberResource, PushApplication, ShortcutEventListener
{
    private Invocation pending;
    private ContentHandlerServer server;

    static ResourceBundle r = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);

    private static final String CHAPI_ID = "com.apress.king.mediagrabber";
    private static final String[] MIME_TYPES = new String[]
    { "image/png", "image/jpeg", "audio/amr-wb", "audio/amr", "audio/pcm",
            "audio/mpeg" };
    private static final String[] SUFFIXES = new String[]
    { ".png", ".jpg", ".jpeg", ".amr", ".pcm", ".mp3" };

    public MediaGrabber()
    {
        String className = MediaGrabber.class.getName();
        try
        {
            verifyRegistration();
            server = Registry.getServer(className);
            pending = server.getRequest(false);
            server.setListener(this);
        }
        catch (Exception e)
        {
            System.err.println("Error checking CHAPI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean checkPermissions()
    {
        ApplicationPermissionsManager manager = ApplicationPermissionsManager
                .getInstance();
        ApplicationPermissions current = manager.getApplicationPermissions();

        int email = ApplicationPermissions.PERMISSION_EMAIL;
        int interProcess = ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION;
        int file = ApplicationPermissions.PERMISSION_FILE_API;
        int media = ApplicationPermissions.PERMISSION_MEDIA;
        int pim = ApplicationPermissions.PERMISSION_ORGANIZER_DATA;
        int screenCapture = ApplicationPermissions.PERMISSION_RECORDING;
        int allow = ApplicationPermissions.VALUE_ALLOW;

        if (current.getPermission(email) != allow
                || current.getPermission(interProcess) != allow
                || current.getPermission(file) != allow
                || current.getPermission(media) != allow
                || current.getPermission(pim) != allow
                || current.getPermission(screenCapture) != allow)
        {
            ApplicationPermissions updated = new ApplicationPermissions();
            updated.addPermission(email);
            updated.addPermission(interProcess);
            updated.addPermission(file);
            updated.addPermission(media);
            updated.addPermission(pim);
            updated.addPermission(screenCapture);
            return manager.invokePermissionsRequest(updated);
        }
        return true;
    }

public static void main(String[] args)
{
    MediaGrabber grabber = new MediaGrabber();
    if (args != null && args.length > 0 && args[0].equals("launch"))
    {
        if (grabber.checkPermissions())
        {
            grabber.pushScreen(new ChoicesScreen());
            grabber.enterEventDispatcher();
        }
    }
    if (args != null && args.length > 0 && args[0].equals("push"))
    {
        grabber.enterEventDispatcher();
    }
    else if (grabber.pending != null)
    {
        // Started via CHAPI. Show our UI.
        grabber.processRequest();
        grabber.requestForeground();
        grabber.enterEventDispatcher();
    }
    else
    {
        // Startup execution.
        try
        {
            RuntimeStore store = RuntimeStore.getRuntimeStore();
            long menuItemID = 0x65fad834642a5345L;
            if (store.get(menuItemID) == null)
            {
                CheckContactMenuItem item = new CheckContactMenuItem();
                ApplicationMenuItemRepository repo = 
                    ApplicationMenuItemRepository.getInstance();
                repo.addMenuItem(ApplicationMenuItemRepository.
                        MENUITEM_ADDRESSBOOK_LIST, item);
                store.put(menuItemID, item);
                ApplicationDescriptor descriptor = ApplicationDescriptor
                        .currentApplicationDescriptor();
                ApplicationDescriptor pushDescriptor = new ApplicationDescriptor(
                        descriptor, new String[] { "push" });
                PushApplicationDescriptor registeredDescriptor = new 
                        PushApplicationDescriptor(4242, pushDescriptor);
                PushApplicationRegistry
                        .registerApplication(registeredDescriptor);
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
}

    private void verifyRegistration()
    {
        String className = MediaGrabber.class.getName();
        Registry registry = Registry.getRegistry(className);
        ContentHandler registered = registry.forID(CHAPI_ID, true);
        if (registered != null)
        {
            return;
        }
        // Wasn't registered before, so do it now.
        String[] actions = new String[]
        { ContentHandler.ACTION_SEND };
        String[] actionNames = new String[]
        { "Send Encrypted Via MediaGrabber" };
        ActionNameMap[] maps = new ActionNameMap[]
        { new ActionNameMap(actions, actionNames, "en") };
        try
        {
            registry.register(className, MIME_TYPES, SUFFIXES, actions, maps,
                    CHAPI_ID, null);
        }
        catch (Exception e)
        {
            System.err.println("Could not register for " + CHAPI_ID + ": "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processRequest()
    {
        FileConnection file = null;
        InputStream is = null;
        try
        {
            String filename = null;
            String type = null;
            synchronized (this)
            {
                filename = pending.getURL();
                type = pending.getType();
            }
            if (filename != null && type != null)
            {
                file = (FileConnection) Connector.open(filename);
                is = file.openInputStream();
                byte[] data = new byte[is.available()];
                is.read(data);
                SendingScreen sending = new SendingScreen(type, filename
                        .substring(filename.lastIndexOf('/') + 1), r
                        .getString(I18N_CHAPI_EMAIL_TEXT), data, true);
                pushScreen(sending);
            }
            else
            {
                pushScreen(new ChoicesScreen());
            }
            server.finish(pending, Invocation.OK);
        }
        catch (Exception e)
        {
            System.out.println("Could not send file: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (file != null)
                    file.close();
                if (is != null)
                    is.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public synchronized void invocationRequestNotify(
            ContentHandlerServer handler)
    {
        pending = handler.getRequest(false);
        if (pending != null)
        {
            processRequest();
        }
    }

    private static class CheckContactMenuItem extends ApplicationMenuItem
    {
        private Registry registry;

        public CheckContactMenuItem()
        {
            super(0);
            registry = Registry.getRegistry(getClass().getName());
        }

        public Object run(Object context)
        {
            if (context == null || !(context instanceof Contact))
                return null;
            try
            {
                Contact contact = (Contact) context;
                if (contact.countValues(BlackBerryContact.USER4) > 0)
                {
                    // We've sent them media before.
                    Dialog.inform(r.getString(I18N_CONTACT_SHARED_MEDIA));
                }
                else
                {
                    // Give a chance to select some media.
                    int choice = Dialog.ask(Dialog.D_YES_NO, r
                            .getString(I18N_CONTACT_VERIFY_SHARED));
                    if (choice == Dialog.YES)
                    {
                        Invocation request = new Invocation();
                        request.setID(CHAPI_ID);
                        registry.invoke(request);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public String toString()
        {
            return r.getString(I18N_CONTACT_VERIFY_SHARED);
        }
    }

    public void onMessage(PushInputStream inputStream, StreamConnection conn)
    {
        try
        {
            String mimeType = inputStream.readUTF();
            PlayingScreen screen = new PlayingScreen(inputStream, mimeType,
                    "This just in!");
            pushScreen(screen);
            requestForeground();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void onStatusChange(PushApplicationStatus status)
    {
        System.out.println("New status " + status.getStatus() + ": "
                + status.getReason() + ": " + status.getError());
    }

    public void editShortcut(String shortcutID)
    {
    }

    public void launchShortcut(String shortcutID)
    {
        System.out.println("shortcutID");
    }

    public void shortcutDeleted(String shortcutID)
    {
    }
}
