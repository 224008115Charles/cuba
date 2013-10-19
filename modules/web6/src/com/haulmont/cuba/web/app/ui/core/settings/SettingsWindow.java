/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.cuba.web.app.ui.core.settings;

import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.AppWindow;
import com.haulmont.cuba.web.WebConfig;
import com.haulmont.cuba.web.app.UserSettingsTools;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public class SettingsWindow extends AbstractWindow {

    protected boolean changeThemeEnabled = false;
    protected OptionsGroup modeOptions;
    protected String msgTabbed;
    protected String msgSingle;

    @Inject
    protected UserSettingsTools userSettingsTools;

    @Inject
    protected Configuration configuration;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Button okBtn;

    @Inject
    protected Button cancelBtn;

    @Override
    public void init(Map<String, Object> params) {
        Boolean changeThemeEnabledParam = (Boolean) params.get("changeThemeEnabled");
        if (changeThemeEnabledParam != null) {
            changeThemeEnabled = changeThemeEnabledParam;
        }

        AppWindow.Mode mode = userSettingsTools.loadAppWindowMode();
        msgTabbed = getMessage("modeTabbed");
        msgSingle = getMessage("modeSingle");

        modeOptions = getComponent("mainWindowMode");
        modeOptions.setOptionsList(Arrays.asList(msgTabbed, msgSingle));
        if (mode == AppWindow.Mode.TABBED)
            modeOptions.setValue(msgTabbed);
        else
            modeOptions.setValue(msgSingle);

        final LookupField theme = getComponent("mainWindowTheme");

        WebConfig webConfig = configuration.getConfig(WebConfig.class);
        List<String> themesList = webConfig.getAvailableAppThemes();
        theme.setOptionsList(themesList);

        String userAppTheme = userSettingsTools.loadAppWindowTheme();
        theme.setValue(userAppTheme);

        theme.setEditable(changeThemeEnabled);

        Button changePasswBtn = getComponent("changePassw");
        final User user = userSessionSource.getUserSession().getUser();
        changePasswBtn.setAction(
                new AbstractAction("changePassw") {
                    @Override
                    public void actionPerform(Component component) {
                        openEditor("sec$User.changePassw", user, WindowManager.OpenType.DIALOG);
                    }
                }
        );
        if (!user.equals(userSessionSource.getUserSession().getCurrentOrSubstitutedUser())) {
            changePasswBtn.setEnabled(false);
        }

        okBtn.setAction(
                new AbstractAction("ok") {
                    @Override
                    public void actionPerform(Component component) {
                        if (changeThemeEnabled) {
                            String selectedTheme = theme.getValue();
                            userSettingsTools.saveAppWindowTheme(selectedTheme);
                            // set cookie
                            App.getInstance().setUserAppTheme(selectedTheme);
                        }
                        AppWindow.Mode m = modeOptions.getValue() == msgTabbed ? AppWindow.Mode.TABBED : AppWindow.Mode.SINGLE;
                        userSettingsTools.saveAppWindowMode(m);
                        showNotification(getMessage("modeChangeNotification"), IFrame.NotificationType.HUMANIZED);
                        close("ok");
                    }
                }
        );

        cancelBtn.setAction(
                new AbstractAction("cancel") {
                    @Override
                    public void actionPerform(Component component) {
                        close("cancel");
                    }
                }
        );
    }
}
