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

package com.haulmont.cuba.gui.screen;

import com.google.common.base.Strings;
import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.LookupComponent;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.actions.BaseAction;
import com.haulmont.cuba.gui.util.OperationResult;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StandardLookup<T extends Entity> extends Screen implements LookupScreen<T> {
    public static final String LOOKUP_ACTIONS_FRAGMENT_ID = "lookupActions";

    protected Consumer<Collection<T>> selectHandler;
    protected Predicate<ValidationContext<T>> selectValidator;

    public StandardLookup() {
        addInitListener(this::initActions);
        addBeforeShowListener(this::beforeShow);
    }

    protected void initActions(@SuppressWarnings("unused") InitEvent event) {
        Window window = getWindow();

        Configuration configuration = getBeanLocator().get(Configuration.NAME);
        Messages messages = getBeanLocator().get(Messages.NAME);

        String commitShortcut = configuration.getConfig(ClientConfig.class).getCommitShortcut();

        Action commitAction = new BaseAction(LOOKUP_SELECT_ACTION_ID)
                .withCaption(messages.getMainMessage("actions.Select"))
                .withPrimary(true)
                .withShortcut(commitShortcut)
                .withHandler(this::select);

        window.addAction(commitAction);

        Action closeAction = new BaseAction(LOOKUP_CANCEL_ACTION_ID)
                .withCaption(messages.getMainMessage("actions.Cancel"))
                .withHandler(this::cancel);

        window.addAction(closeAction);
    }

    private void beforeShow(@SuppressWarnings("unused") BeforeShowEvent beforeShowEvent) {
        loadData();
        setupLookupComponent();
    }

    protected void loadData() {
        getScreenData().loadAll();
    }

    protected void setupLookupComponent() {
        if (this.selectHandler != null) {
            getLookupComponent().setLookupSelectHandler(this::select);
        }
    }

    @Override
    public Consumer<Collection<T>> getSelectHandler() {
        return selectHandler;
    }

    @Override
    public void setSelectHandler(Consumer<Collection<T>> selectHandler) {
        this.selectHandler = selectHandler;

        Component lookupActionsFragment = getWindow().getComponent(LOOKUP_ACTIONS_FRAGMENT_ID);
        if (lookupActionsFragment != null) {
            lookupActionsFragment.setVisible(true);
        }
    }

    @Override
    public Predicate<ValidationContext<T>> getSelectValidator() {
        return selectValidator;
    }

    @Override
    public void setSelectValidator(Predicate<ValidationContext<T>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    @SuppressWarnings("unchecked")
    protected LookupComponent<T> getLookupComponent() {
        com.haulmont.cuba.gui.screen.LookupComponent annotation =
                getClass().getAnnotation(com.haulmont.cuba.gui.screen.LookupComponent.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            throw new IllegalStateException(
                    String.format("StandardLookup %s does not declare @LookupComponent", getClass())
            );
        }
        return (LookupComponent) getWindow().getComponentNN(annotation.value());
    }

    protected void select(@SuppressWarnings("unused") Action.ActionPerformedEvent event) {
        LookupComponent<T> lookupComponent = getLookupComponent();
        Collection<T> lookupSelectedItems = lookupComponent.getLookupSelectedItems();
        select(lookupSelectedItems);
    }

    protected void cancel(@SuppressWarnings("unused") Action.ActionPerformedEvent event) {
        close(WINDOW_DISCARD_AND_CLOSE_ACTION);
    }

    protected void select(Collection<T> items) {
        boolean valid = true;
        if (selectValidator != null) {
            valid = selectValidator.test(new ValidationContext<>(this, items));
        }

        if (valid) {
            OperationResult result = close(LOOKUP_SELECT_CLOSE_ACTION);
            if (selectHandler != null) {
                result.then(() -> selectHandler.accept(items));
            }
        }
    }
}