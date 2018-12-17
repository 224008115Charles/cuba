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

package com.haulmont.cuba.gui.components.factories;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.RuntimePropsDatasource;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.InstanceContainer;
import org.dom4j.Element;

import javax.annotation.Nullable;

public abstract class AbstractFieldFactory implements FieldFactory {

    protected UiComponentsGenerator componentsGenerator = AppBeans.get(UiComponentsGenerator.NAME);

    @Override
    public Component createField(Datasource datasource, String property, Element xmlDescriptor) {
        MetaClass metaClass = resolveMetaClass(datasource);

        ComponentGenerationContext context = new ComponentGenerationContext(metaClass, property)
                .setDatasource(datasource)
                .setOptionsDatasource(getOptionsDatasource(datasource, property))
                .setXmlDescriptor(xmlDescriptor)
                .setComponentClass(Table.class);

        return componentsGenerator.generate(context);
    }

    @Override
    public Component createField(InstanceContainer container, String property, Element xmlDescriptor) {
        MetaClass metaClass = container.getEntityMetaClass();

        ComponentGenerationContext context = new ComponentGenerationContext(metaClass, property)
                .setContainer(container)
                .setOptionsContainer(getOptionsContainer(container, property))
                .setXmlDescriptor(xmlDescriptor)
                .setComponentClass(Table.class);

        return componentsGenerator.generate(context);
    }

    protected MetaClass resolveMetaClass(Datasource datasource) {
        return datasource instanceof RuntimePropsDatasource ?
                ((RuntimePropsDatasource) datasource).resolveCategorizedEntityClass() : datasource.getMetaClass();
    }

    @Deprecated
    @Nullable
    protected abstract CollectionDatasource getOptionsDatasource(Datasource datasource, String property);

    @Nullable
    protected abstract CollectionContainer getOptionsContainer(InstanceContainer container, String property);
}