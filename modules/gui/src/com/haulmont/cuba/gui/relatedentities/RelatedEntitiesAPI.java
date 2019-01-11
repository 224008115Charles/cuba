/*
 * Copyright (c) 2008-2017 Haulmont.
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

package com.haulmont.cuba.gui.relatedentities;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.screen.FrameOwner;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.Screen;

import java.util.Collection;
import java.util.Map;

public interface RelatedEntitiesAPI {

    String NAME = "cuba_RelatedEntities";

    /**
     * Shows found related entities in default browse screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param metaClass        metaClass of single entity from <code>selectedEntities</code>
     * @param metaProperty     chosen field to find related entities. Can be obtained from <code>metaClass</code>
     */
    void openRelatedScreen(Collection<? extends Entity> selectedEntities, MetaClass metaClass, MetaProperty metaProperty);

    /**
     * Shows found related entities in chosen screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param metaClass        metaClass of single entity from <code>selectedEntities</code>
     * @param metaProperty     chosen field to find related entities. Can be obtained from <code>metaClass</code>
     * @param descriptor       descriptor contains screen id or screen class, {@link OpenMode}, frame owner and
     *                         generated filter caption
     */
    void openRelatedScreen(Collection<? extends Entity> selectedEntities, MetaClass metaClass, MetaProperty metaProperty,
                           RelatedScreenDescriptor descriptor);

    /**
     * Shows found related entities in default browse screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param clazz            class of single entity from <code>selectedEntities</code>
     * @param property         chosen field to find related entities
     */
    <T extends Entity> void openRelatedScreen(Collection<T> selectedEntities, Class<T> clazz, String property);

    /**
     * Shows found related entities in chosen screen.
     *
     * @param selectedEntities set of entities which represents one side of relation
     * @param clazz            class of single entity from <code>selectedEntities</code>
     * @param property         chosen field to find related entities
     * @param descriptor       descriptor contains screen id or screen class, {@link OpenMode}, frame owner and
     *                         generated filter caption
     */
    <T extends Entity> void openRelatedScreen(Collection<T> selectedEntities, Class<T> clazz, String property,
                           RelatedScreenDescriptor descriptor);

    class RelatedScreenDescriptor {

        protected String screenId;
        protected WindowManager.OpenType openType = WindowManager.OpenType.THIS_TAB;
        protected String filterCaption;
        protected Map<String, Object> screenParams;

        protected Class screenClass;
        protected OpenMode openMode = OpenMode.THIS_TAB;
        protected FrameOwner origin;

        /**
         * @param screenId screen id
         * @param openType open type
         * @deprecated Use {@link #RelatedScreenDescriptor(String, OpenMode, FrameOwner)} instead.
         */
        @Deprecated
        public RelatedScreenDescriptor(String screenId, WindowManager.OpenType openType) {
            this(screenId, openType.getOpenMode(), null);
            this.openType = openType;
        }

        /**
         * @param screenId screen id
         * @deprecated Use {@link #RelatedScreenDescriptor(String, FrameOwner)} instead.
         */
        @Deprecated
        public RelatedScreenDescriptor(String screenId) {
            this(screenId, (FrameOwner) null);
        }

        /**
         * @param screenId screen id
         * @param frameOwner screen owner from which you open related screen
         */
        public RelatedScreenDescriptor(String screenId, FrameOwner frameOwner) {
            this(screenId, OpenMode.THIS_TAB, frameOwner);
        }

        /**
         * @param screenId id of opening screen
         * @param openMode open mode
         * @param origin screen owner from which you open related screen
         */
        public RelatedScreenDescriptor(String screenId, OpenMode openMode, FrameOwner origin) {
            this.screenId = screenId;
            this.openMode = openMode;
            this.origin = origin;
        }

        /**
         * @param screenClass controller class
         * @param openMode open model
         * @param origin screen owner from which you open related screen
         * @param <S> class of controller that extends Screen
         */
        public <S extends Screen> RelatedScreenDescriptor(Class<S> screenClass, OpenMode openMode, FrameOwner origin) {
            this.screenClass = screenClass;
            this.openMode = openMode;
            this.origin = origin;
        }

        /**
         * @param screenClass controller class
         * @param origin screen owner from which you open related screen
         * @param <S> class of controller that extends Screen
         */
        public <S extends Screen> RelatedScreenDescriptor(Class<S> screenClass, FrameOwner origin) {
            this(screenClass, OpenMode.THIS_TAB, origin);
        }

        public RelatedScreenDescriptor() {
        }

        public String getScreenId() {
            return screenId;
        }

        /**
         * @return open type of screen
         * @deprecated Use {@link #getOpenMode()} instead.
         */
        @Deprecated
        public WindowManager.OpenType getOpenType() {
            return openType;
        }

        public String getFilterCaption() {
            return filterCaption;
        }

        public Map<String, Object> getScreenParams() {
            return screenParams;
        }

        public void setScreenId(String screenId) {
            this.screenId = screenId;
        }

        /**
         * Sets open type to screen.
         *
         * @param openType open type
         * @deprecated Use {@link #setOpenMode(OpenMode)} instead.
         */
        @Deprecated
        public void setOpenType(WindowManager.OpenType openType) {
            setOpenMode(openType.getOpenMode());

            this.openType = openType;
        }

        /**
         * @return open mode of screen
         */
        public OpenMode getOpenMode() {
            return openMode;
        }

        /**
         * Sets open mode to screen.
         *
         * @param openMode open  mode
         */
        public void setOpenMode(OpenMode openMode) {
            this.openMode = openMode;
        }

        /**
         * @param <S> class of controller that extends Screen
         * @return screen class
         */
        public <S extends Screen> Class<S> getScreenClass() {
            //noinspection unchecked
            return screenClass;
        }

        /**
         * Sets screen class.
         *
         * @param screenClass screen class
         * @param <S>         class of controller that extends Screen
         */
        public <S extends Screen> void setScreenClass(Class<S> screenClass) {
            this.screenClass = screenClass;
        }

        /**
         * @return screen owner from which you open related screen
         */
        public FrameOwner getOrigin() {
            return origin;
        }

        /**
         * Sets screen owner from which you open related screen
         *
         * @param origin origin
         */
        public void setOrigin(FrameOwner origin) {
            this.origin = origin;
        }

        public void setFilterCaption(String filterCaption) {
            this.filterCaption = filterCaption;
        }

        public void setScreenParams(Map<String, Object> screenParams) {
            this.screenParams = screenParams;
        }
    }
}