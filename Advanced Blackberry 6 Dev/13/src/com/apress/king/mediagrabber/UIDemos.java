package com.apress.king.mediagrabber;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Enumeration;

import javax.microedition.io.HttpConnection;
import javax.microedition.pim.Contact;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.blackberry.api.homescreen.Location;
import net.rim.blackberry.api.homescreen.Shortcut;
import net.rim.blackberry.api.homescreen.ShortcutProvider;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.PhoneArguments;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.progressindicator.ActivityImageField;
import net.rim.device.api.ui.component.progressindicator.ActivityIndicatorView;
import net.rim.device.api.ui.component.progressindicator.ProgressBarField;
import net.rim.device.api.ui.component.progressindicator.ProgressIndicatorModel;
import net.rim.device.api.ui.component.progressindicator.ProgressIndicatorView;
import net.rim.device.api.ui.component.table.DataTemplate;
import net.rim.device.api.ui.component.table.SimpleList;
import net.rim.device.api.ui.component.table.TableController;
import net.rim.device.api.ui.component.table.TableModel;
import net.rim.device.api.ui.component.table.TableView;
import net.rim.device.api.ui.component.table.TemplateColumnProperties;
import net.rim.device.api.ui.component.table.TemplateRowProperties;
import net.rim.device.api.ui.container.AbsoluteFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.picker.DateTimePicker;
import net.rim.device.api.ui.picker.FilePicker;
import net.rim.device.api.ui.picker.HomeScreenLocationPicker;

public class UIDemos extends MainScreen
{

    private void showActivity()
    {
        /*
         * ActivityIndicatorModel model = new ActivityIndicatorModel();
         * ActivityIndicatorController controller = new
         * ActivityIndicatorController(); view.setController(controller);
         * view.setModel(model); controller.setModel(model);
         * controller.setView(view); model.setController(controller);
         */
        ActivityIndicatorView view = new ActivityIndicatorView(FIELD_HCENTER,
                new HorizontalFieldManager());
        Bitmap bitmap = Bitmap.getBitmapResource("waiting.png");
        view.createActivityImageField(bitmap, 12, 0);
        ActivityImageField animation = view.getAnimation();
        animation.setPadding(0, 5, 0, 0);
        view.setLabel("Please wait...");
        add(view);
    }

    private void showProgress()
    {
        ProgressIndicatorView progress = new ProgressIndicatorView(0);
        final ProgressIndicatorModel model = new ProgressIndicatorModel(0, 1, 0);
        progress.setModel(model);
        progress.createLabel("Downloading...", Field.FIELD_HCENTER);
        progress.createProgressBar(ProgressBarField.PROGRESS_TEXT_TRAILING
                | ProgressBarField.PERCENT);
        add(progress);
        (new Thread()
        {
            public void run()
            {
                HttpConnection conn = null;
                InputStream is = null;
                try
                {
                    conn = (HttpConnection) (new ConnectionFactory())
                            .getConnection(
                                    "http://www.gutenberg.org/cache/epub/4300/pg4300.txt")
                            .getConnection();
                    is = conn.openInputStream();
                    long total = conn.getLength();
                    model.setValueMax((int) (total));
                    long read = 0;
                    long totalRead = 0;
                    do
                    {
                        model.setValue((int) (totalRead));
                        read = is.skip(1024);
                        totalRead += read;
                    } while (read > 0);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if (is != null)
                            is.close();
                        if (conn != null)
                            conn.close();
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }).start();
    }

    public void overlapImages()
    {
        AbsoluteFieldManager absolute = new AbsoluteFieldManager();
        BitmapField lake = new BitmapField(Bitmap
                .getBitmapResource("CathedralLake.jpg"));
        BitmapField peak = new BitmapField(Bitmap
                .getBitmapResource("TuolumnePeak.jpg"));
        EditField title = new EditField();
        absolute.add(lake, 50, 50);
        absolute.add(peak, 220, 200);
        absolute.add(title, 200, 190);
        add(absolute);
    }

    public void createList()
    {
        VerticalFieldManager manager = new VerticalFieldManager();
        SimpleList list = new SimpleList(manager);
        list.add("Eggs");
        list.add("Flour");
        list.add("Sugar");
        list.add("Butter");
        add(new LabelField("Shopping List"));
        add(manager);
    }

    private class PhoneDataTemplate extends DataTemplate
    {
        public PhoneDataTemplate(TableView view)
        {
            super(view, 1, 3);
        }

        public Field[] getDataFields(int modelRowIndex)
        {
            Object[] data = (Object[]) ((TableModel) getView().getModel())
                    .getRow(modelRowIndex);
            Field[] fields = new Field[3];
            fields[0] = new LabelField(data[0], DrawStyle.ELLIPSIS);
            fields[1] = new LabelField(data[1], DrawStyle.ELLIPSIS);
            fields[2] = new LabelField(data[2], DrawStyle.ELLIPSIS);
            return fields;
        }

    }

    public void createTable() throws Exception
    {
        TableModel model = new TableModel();
        TableView view = new TableView(model);
        PhoneDataTemplate template = new PhoneDataTemplate(view);
        template.createRegion(new XYRect(0, 0, 1, 1));
        template.createRegion(new XYRect(1, 0, 1, 1));
        template.createRegion(new XYRect(2, 0, 1, 1));
        template.setColumnProperties(0, new TemplateColumnProperties(30,
                TemplateColumnProperties.PERCENTAGE_WIDTH));
        template.setColumnProperties(1, new TemplateColumnProperties(30,
                TemplateColumnProperties.PERCENTAGE_WIDTH));
        template.setColumnProperties(2, new TemplateColumnProperties(40,
                TemplateColumnProperties.PERCENTAGE_WIDTH));
        template.setRowProperties(0, new TemplateRowProperties(20));
        template.useFixedHeight(true);
        view.setDataTemplate(template);
        TableController controller = new TableController(model, view);
        controller.setFocusPolicy(TableController.ROW_FOCUS);
        view.setController(controller);
        controller.setCommand(new CallCommand(), null, view);
        add(new LabelField("Who you gonna call?"));
        add(view);

        PIM pim = PIM.getInstance();
        ContactList list = (ContactList) pim.openPIMList(PIM.CONTACT_LIST,
                PIM.READ_ONLY);
        Enumeration contacts = list.items();
        String[] rowHolder = new String[3];
        while (contacts.hasMoreElements())
        {
            try
            {
                Contact person = (Contact) contacts.nextElement();
                String[] names = person.getStringArray(Contact.NAME, 0);
                String number = person.getString(Contact.TEL, 0);
                rowHolder[0] = names[Contact.NAME_GIVEN];
                rowHolder[1] = names[Contact.NAME_FAMILY];
                rowHolder[2] = number;
                model
                        .addRow(new Object[]
                        { names[Contact.NAME_GIVEN],
                                names[Contact.NAME_FAMILY], number });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private class CallCommand extends CommandHandler
    {
        public void execute(ReadOnlyCommandMetadata metadata, Object context)
        {
            TableView view = (TableView) context;
            int focused = view.getRowNumberWithFocus();
            if (focused >= 0)
            {
                String number = (String) ((Object[]) ((TableModel) view
                        .getModel()).getRow(focused))[2];
                if (number != null && number.length() > 0)
                {
                    PhoneArguments args = new PhoneArguments(
                            PhoneArguments.ARG_CALL, number);
                    Invoke.invokeApplication(Invoke.APP_TYPE_PHONE, args);
                }
            }
        }
    }

    public void close()
    {
        super.close();
    }

    public void showFilePicker()
    {
        ButtonField button = new ButtonField();
        button.setRunnable(new Runnable()
        {

            public void run()
            {
                FilePicker picker = FilePicker.getInstance();
                picker.setFilter(".amr");
                picker.setPath("file:///SDCard/");
                String chosen = picker.show();
                if (chosen != null)
                {
                    // Act on the chosen file here.
                }
            }
        });
        button.setLabel("Press me");
        add(button);
    }

    public void showHomeScreenLocationPicker()
    {
        add(new LabelField("Where would you like to put your favorite song?"));
        final HomeScreenLocationPicker picker = HomeScreenLocationPicker
                .create();
        add(picker);
        ButtonField button = new ButtonField();
        button.setRunnable(new Runnable()
        {
            public void run()
            {
                Location location = picker.getLocation();
                Shortcut shortcut = ShortcutProvider
                        .createShortcut(
                                "Play Tunes",
                                "playMusic:///SDCard/BlackBerry/Music/WorldLeaderPretend.mp3",
                                1);
                shortcut.setIsFavourite(picker.getIsFavourite());
                HomeScreen.addShortcut(shortcut, location);
                close();
            }
        });
        button.setLabel("Create Shortcut");
        add(button);
    }

    public void showDateTime()
    {
        ButtonField button = new ButtonField();
        button.setRunnable(new Runnable()
        {

            public void run()
            {
                Calendar initial = Calendar.getInstance();
                initial.set(Calendar.YEAR, initial.get(Calendar.YEAR) + 1);
                Calendar max = Calendar.getInstance();
                max.set(Calendar.YEAR, max.get(Calendar.YEAR) + 5);
                DateTimePicker picker = DateTimePicker.createInstance(initial,
                        DateFormat.DATE_LONG, 0);
                picker.setMinimumDate(Calendar.getInstance());
                picker.setMaximumDate(max);
                if (picker.doModal())
                {
                    Calendar chosen = picker.getDateTime();
                }
            }
        });
        button.setLabel("Next Appointment");
        add(button);
    }

    public boolean onSavePrompt()
    {
        return true;
    }

    public UIDemos()
    {
        super(net.rim.device.api.ui.Manager.NO_VERTICAL_SCROLL);
        try
        {
            // showActivity();
            // showProgress();
            // overlapImages();
            // createList();
            createTable();
            // showFilePicker();
            // showHomeScreenLocationPicker();
            // showDateTime();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
