/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.sys;

import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.gui.Route;
import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.UrlRouting;
import com.haulmont.cuba.gui.components.DialogWindow;
import com.haulmont.cuba.gui.components.RootWindow;
import com.haulmont.cuba.gui.screen.EditorScreen;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.sys.RouteDefinition;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.WebConfig;
import com.haulmont.cuba.web.gui.UrlHandlingMode;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.cuba.gui.navigation.NavigationState;
import com.haulmont.cuba.web.sys.navigation.UrlIdSerializer;
import com.haulmont.cuba.web.sys.navigation.UrlTools;
import com.vaadin.server.Page;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.base.Strings.nullToEmpty;
import static com.haulmont.bali.util.Preconditions.checkNotNullArgument;
import static com.haulmont.cuba.gui.screen.UiControllerUtils.getScreenContext;

public class WebUrlRouting implements UrlRouting {

    protected static final int MAX_NESTING = 2;

    private static final Logger log = LoggerFactory.getLogger(WebUrlRouting.class);

    @Inject
    protected Events events;

    @Inject
    protected WebConfig webConfig;

    protected AppUI ui;

    public WebUrlRouting(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public void pushState(Screen screen, Map<String, String> urlParams) {
        if (!checkConditions(screen, urlParams)) {
            return;
        }

        updateState(screen, urlParams, true);
    }

    @Override
    public void replaceState(Screen screen, Map<String, String> urlParams) {
        if (!checkConditions(screen, urlParams)) {
            return;
        }

        updateState(screen, urlParams, false);
    }

    @Override
    public NavigationState getState() {
        if (UrlHandlingMode.URL_ROUTES != webConfig.getUrlHandlingMode()) {
            log.debug("UrlRouting is disabled for '{}' URL handling mode", webConfig.getUrlHandlingMode());
            return NavigationState.EMPTY;
        }

        if (UrlTools.headless()) {
            log.debug("Unable to resolve navigation state in headless mode");
            return NavigationState.EMPTY;
        }

        return UrlTools.parseState(Page.getCurrent().getLocation().getRawFragment());
    }

    protected void updateState(Screen screen, Map<String, String> urlParams, boolean pushState) {
        NavigationState currentState = getState();
        NavigationState newState = buildNavState(screen, urlParams);

        if (complexRouteNavigation(currentState, newState)) {
            // do not change URL until last sub route is handled
            return;
        }

        // do not push copy-pasted requested state to avoid double state pushing into browser history
        if (!pushState || externalNavigation(currentState, newState)) {
            UrlTools.replaceState(newState.asRoute());
        } else {
            UrlTools.pushState(newState.asRoute());
        }

        ((WebWindow) screen.getWindow()).setResolvedState(newState);

        if (pushState) {
            ui.getHistory().forward(newState);
        } else {
            ui.getHistory().replace(newState);
        }
    }

    protected NavigationState buildNavState(Screen screen, Map<String, String> urlParams) {
        NavigationState state;

        if (screen.getWindow() instanceof RootWindow) {
            state = new NavigationState(getRoute(screen), "", "", urlParams);
        } else {
            String rootRoute = getRoute(ui.getScreens().getOpenedScreens().getRootScreen());
            String stateMark = getStateMark(screen);

            String nestedRoute = buildNestedRoute(screen);
            Map<String, String> params = buildParams(screen, urlParams);

            state = new NavigationState(rootRoute, stateMark, nestedRoute, params);
        }

        return state;
    }

    protected String buildNestedRoute(Screen screen) {
        return screen.getWindow() instanceof DialogWindow
                ? buildDialogRoute(screen)
                : buildScreenRoute(screen);
    }

    protected String buildDialogRoute(Screen dialog) {
        RouteDefinition dialogRouteDefinition = getRouteDef(dialog);

        Iterator<Screen> currentTabScreens = ui.getScreens().getOpenedScreens().getCurrentBreadcrumbs().iterator();
        Screen currentScreen = currentTabScreens.hasNext()
                ? currentTabScreens.next()
                : null;
        String currentScreenRoute = currentScreen != null
                ? buildScreenRoute(currentScreen)
                : "";

        if (dialogRouteDefinition == null) {
            return currentScreenRoute;
        }
        String dialogRoute = dialogRouteDefinition.getPath();
        if (dialogRoute == null || dialogRoute.isEmpty()) {
            return currentScreenRoute;
        }

        String parentPrefix = dialogRouteDefinition.getParentPrefix();
        if (StringUtils.isNotEmpty(parentPrefix)
                && dialogRoute.startsWith(parentPrefix + '/')
                && currentScreenRoute.endsWith(parentPrefix)) {
            dialogRoute = dialogRoute.substring(parentPrefix.length() + 1);
        }

        return currentScreenRoute == null || currentScreenRoute.isEmpty()
                ? dialogRoute
                : currentScreenRoute + '/' + dialogRoute;
    }

    protected String buildScreenRoute(Screen screen) {
        List<Screen> screens = new ArrayList<>(ui.getScreens().getOpenedScreens().getCurrentBreadcrumbs());
        if (screens.isEmpty()
                || screens.get(0) != screen) {
            log.debug("Current breadcrumbs doesn't contain the given screen '{}'", screen.getId());
            return "";
        }

        Collections.reverse(screens);

        StringBuilder state = new StringBuilder();
        String prevSubRoute = null;

        for (int i = 0; i < screens.size() && i < MAX_NESTING; i++) {
            String subRoute = buildSubRoute(prevSubRoute, screens.get(i));

            if (StringUtils.isNotEmpty(state)
                    && StringUtils.isNotEmpty(subRoute)) {
                state.append('/');
            }
            state.append(subRoute);

            prevSubRoute = subRoute;
        }

        return state.toString();
    }

    protected String buildSubRoute(String prevSubRoute, Screen screen) {
        String screenRoute = getRoute(screen);

        String parentPrefix = getParentPrefix(screen);
        if (StringUtils.isEmpty(parentPrefix)) {
            return nullToEmpty(screenRoute);
        }

        if (Objects.equals(prevSubRoute, parentPrefix)) {
            return nullToEmpty(screenRoute.replace(parentPrefix + "/", ""));
        } else {
            return nullToEmpty(screenRoute);
        }
    }

    protected Map<String, String> buildParams(Screen screen, Map<String, String> urlParams) {
        String route = getRoute(screen);

        if (StringUtils.isEmpty(route)
                && (isEditor(screen) || MapUtils.isNotEmpty(urlParams))) {
            log.debug("There's no route for screen \"{}\". URL params will be ignored", screen.getId());
            return Collections.emptyMap();
        }

        if (omitParams(screen)) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new LinkedHashMap<>();

        if (isEditor(screen)) {
            Object entityId = ((EditorScreen) screen).getEditedEntity().getId();
            String base64Id = UrlIdSerializer.serializeId(entityId);
            if (base64Id != null && !"".equals(base64Id)) {
                params.put("id", base64Id);
            }
        }

        params.putAll(urlParams != null
                ? urlParams
                : Collections.emptyMap());

        return params;
    }

    protected String getParentPrefix(Screen screen) {
        String parentPrefix = null;

        Route routeAnnotation = screen.getClass().getAnnotation(Route.class);
        if (routeAnnotation != null) {
            parentPrefix = routeAnnotation.parentPrefix();
        } else {
            RouteDefinition routeDef = getScreenContext(screen)
                    .getWindowInfo()
                    .getRouteDefinition();
            if (routeDef != null) {
                parentPrefix = routeDef.getParentPrefix();
            }
        }

        return parentPrefix;
    }

    protected boolean omitParams(Screen screen) {
        Screens.LaunchMode launchMode = screen.getWindow().getContext().getLaunchMode();
        if (OpenMode.THIS_TAB != launchMode) {
            return false;
        }

        return ui.getScreens().getOpenedScreens().getCurrentBreadcrumbs().size() > MAX_NESTING;
    }

    protected boolean isEditor(Screen screen) {
        return screen instanceof EditorScreen;
    }

    protected String getRoute(Screen screen) {
        RouteDefinition routeDef = getRouteDef(screen);
        return routeDef == null ? null : routeDef.getPath();
    }

    protected RouteDefinition getRouteDef(Screen screen) {
        return screen == null
                ? null
                : getScreenContext(screen).getWindowInfo().getRouteDefinition();
    }

    protected String getStateMark(Screen screen) {
        WebWindow window = (WebWindow) screen.getWindow();
        return String.valueOf(window.getUrlStateMark());
    }

    protected boolean complexRouteNavigation(NavigationState currentState, NavigationState newState) {
        boolean complexRouteRequested = currentState != null
                && currentState.getNestedRoute() != null
                && currentState.getNestedRoute().contains("/");

        boolean notInHistory = currentState != null
                && StringUtils.isEmpty(currentState.getStateMark())
                || !ui.getHistory().has(currentState);

        boolean lastSubRoute = currentState != null
                && newState != null
                && currentState.getNestedRoute().endsWith(newState.getNestedRoute());

        return complexRouteRequested && notInHistory && !lastSubRoute;
    }

    protected boolean externalNavigation(NavigationState currentState, NavigationState newState) {
        if (currentState == null) {
            return false;
        }

        boolean notInHistory = !ui.getHistory().has(currentState);

        boolean sameRoot = Objects.equals(currentState.getRoot(), newState.getRoot());
        boolean sameNestedRoute = Objects.equals(currentState.getNestedRoute(), newState.getNestedRoute());
        boolean sameParams = Objects.equals(currentState.getParamsString(), newState.getParamsString());

        return notInHistory && sameRoot && sameNestedRoute && sameParams;
    }

    protected boolean checkConditions(Screen screen, Map<String, String> urlParams) {
        if (UrlHandlingMode.URL_ROUTES != webConfig.getUrlHandlingMode()) {
            log.debug("UrlRouting is disabled for '{}' URL handling mode", webConfig.getUrlHandlingMode());
            return false;
        }

        checkNotNullArgument(screen, "Screen cannot be null");
        checkNotNullArgument(urlParams, "Parameters cannot be null");

        if (notAttachedToUi(screen)) {
            log.info("Ignore changing of URL for not attached screen '{}'", screen.getId());
            return false;
        }

        return true;
    }

    protected boolean notAttachedToUi(Screen screen) {
        boolean notAttached;

        Screens.OpenedScreens openedScreens = ui.getScreens().getOpenedScreens();

        if (screen.getWindow() instanceof RootWindow) {
            Screen rootScreen = openedScreens.getRootScreenOrNull();
            notAttached = rootScreen == null || rootScreen != screen;
        } else if (screen.getWindow() instanceof DialogWindow) {
            notAttached = !openedScreens.getDialogScreens()
                    .contains(screen);
        } else {
            notAttached = !openedScreens.getActiveScreens()
                    .contains(screen);
        }

        return notAttached;
    }
}
