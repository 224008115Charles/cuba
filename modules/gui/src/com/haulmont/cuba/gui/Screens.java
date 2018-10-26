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
 *
 */

package com.haulmont.cuba.gui;

import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.screen.FrameOwner;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.ScreenOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * JavaDoc
 */
public interface Screens {

    String NAME = "cuba_Screens";

    default <T extends Screen> T create(Class<T> screenClass, LaunchMode launchMode) {
        return create(screenClass, launchMode, FrameOwner.NO_OPTIONS);
    }

    default Screen create(WindowInfo windowInfo, LaunchMode launchMode) {
        return create(windowInfo, launchMode, FrameOwner.NO_OPTIONS);
    }

    /**
     * JavaDoc
     */
    <T extends Screen> T create(Class<T> screenClass, LaunchMode launchMode, ScreenOptions options);

    /**
     * JavaDoc
     */
    Screen create(WindowInfo windowInfo, LaunchMode launchMode, ScreenOptions options);

    /**
     * JavaDoc
     */
    void show(Screen screen);

    /**
     * Removes screen from UI and releases all the resources of screen.
     *
     * @param screen screen
     */
    void remove(Screen screen);

    /**
     * Removes all child screens (screens of work area and dialog screens) from the root screen and releases their resources.
     */
    void removeAll();

    /**
     * Check if there are screens that have unsaved changes.
     *
     * @return true if there are screens with unsaved changes
     */
    boolean hasUnsavedChanges();

    /**
     * @return living UI state object that provides information about opened screens
     */
    UiState getUiState();

    /**
     * Marker interface for screen launch modes.
     *
     * @see com.haulmont.cuba.gui.screen.OpenMode
     */
    interface LaunchMode {
    }

    interface WindowStack {
        /**
         * @return screens of the container in descending order, first element is active screen
         */
        Collection<Screen> getBreadcrumbs();
    }

    /**
     * UI state object. Provides information about opened screens, does not store state.
     * <br>
     * Each method obtains current info from UI components tree.
     */
    interface UiState {
        /**
         * @return the root screen of UI
         * @throws IllegalStateException in case there is no root screen in UI
         */
        @Nonnull
        Screen getRootScreen();

        /**
         * @return the root screen or null
         */
        @Nullable
        Screen getRootScreenOrNull();

        /**
         * @return all opened screens excluding the root screen
         * @throws IllegalStateException if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getOpenedScreens();

        /**
         * @return all opened screens excluding the root screen
         * @throws IllegalStateException if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getOpenedWorkAreaScreens();

        /**
         * @return top screens from work area tabs and all dialog windows
         * @throws IllegalStateException if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getActiveScreens();

        /**
         * @return top screens from work area tabs
         * @throws IllegalStateException if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getActiveWorkAreaScreens();

        /**
         * @return all dialog screens
         */
        Collection<Screen> getDialogScreens();

        /**
         * @return screens of the currently opened tab of work area in descending order, first element is active screen
         * @throws IllegalStateException if there is no root screen or root screen does not have {@link AppWorkArea}
         */
        Collection<Screen> getCurrentBreadcrumbs();

        /**
         * @return tab containers or single window container with access to breadcrumbs
         */
        Collection<WindowStack> getWorkAreaStacks();
    }
}