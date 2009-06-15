/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Dmitry Abramov
 * Created: 06.04.2009 10:39:36
 * $Id$
 */
package com.haulmont.cuba.web.gui.components;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.global.ViewHelper;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.HierarchicalDatasource;
import com.haulmont.cuba.gui.data.TreeTableDatasource;
import com.haulmont.cuba.web.gui.data.CollectionDsWrapper;
import com.haulmont.cuba.web.gui.data.HierarchicalDsWrapper;
import com.haulmont.cuba.web.gui.data.ItemWrapper;
import com.haulmont.cuba.web.gui.data.PropertyWrapper;
import com.haulmont.cuba.web.toolkit.data.TreeTableContainer;
import com.haulmont.cuba.web.toolkit.ui.TableSupport;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TreeTable
    extends
        AbstractTable<com.haulmont.cuba.web.toolkit.ui.TreeTable>
    implements
        com.haulmont.cuba.gui.components.TreeTable, Component.Wrapper
{
    protected String hierarchyProperty;

    public TreeTable() {
        component = new com.haulmont.cuba.web.toolkit.ui.TreeTable();
        initComponent(component);
    }

    public String getHierarchyProperty() {
        return hierarchyProperty;
    }

    public void setDatasource(HierarchicalDatasource datasource)
    {
        setDatasource((CollectionDatasource)datasource);
    }

    protected CollectionDsWrapper createContainerDatasource(CollectionDatasource datasource, Collection<MetaPropertyPath> columns) {
        return new TreeTableDsWrapper((TreeTableDatasource) datasource);
    }

    protected void setVisibleColumns(List<MetaPropertyPath> columnsOrder) {
        component.setVisibleColumns(columnsOrder.toArray());
    }

    protected void setColumnHeader(MetaPropertyPath propertyPath, String caption) {
        component.setColumnHeader(propertyPath, caption);
    }

    public void setDatasource(CollectionDatasource datasource) {
        super.setDatasource(datasource);
        this.hierarchyProperty = ((HierarchicalDatasource) datasource).getHierarchyPropertyName();

        // if showProperty is null, the Tree will use itemId.toString
        MetaProperty metaProperty = hierarchyProperty == null ? null : datasource.getMetaClass().getProperty(hierarchyProperty);
        component.setItemCaptionPropertyId(metaProperty);
    }

    public void setStyleProvider(StyleProvider styleProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void addGeneratedColumn(Object id, Object generator) {
        component.addGeneratedColumn(id, (TableSupport.ColumnGenerator) generator);
    }

    public void setEditable(boolean editable) {
        super.setEditable(editable);
        component.setEditable(editable);
    }

    @Override
    protected void initComponent(com.haulmont.cuba.web.toolkit.ui.TreeTable component) {
        super.initComponent(component);
        component.setSelectable(true);
    }

    protected class TreeTableDsWrapper
            extends HierarchicalDsWrapper
            implements TreeTableContainer
    {
        public TreeTableDsWrapper(TreeTableDatasource datasource) {
            super(datasource);
        }

        @Override
        protected void createProperties(View view, MetaClass metaClass) {
            if (columns.isEmpty()) {
                super.createProperties(view, metaClass);
            } else {
                for (Map.Entry<MetaPropertyPath, Column> entry : columns.entrySet()) {
                    final MetaPropertyPath propertyPath = entry.getKey();
                    if (view == null || ViewHelper.contains(view, propertyPath)) {
                        properties.add(propertyPath);
                    }
                }
            }
        }

        @Override
        protected ItemWrapper createItemWrapper(Object item) {
            return new ItemWrapper(item, properties) {
                @Override
                protected PropertyWrapper createPropertyWrapper(Object item, MetaPropertyPath propertyPath) {
                    final PropertyWrapper wrapper = new TablePropertyWrapper(item, propertyPath);

                    return wrapper;
                }
            };
        }

        public boolean isCaption(Object itemId) {
            return ((TreeTableDatasource<Entity, Object>) datasource).isCaption(itemId);
        }

        public String getCaption(Object itemId) {
            return ((TreeTableDatasource<Entity, Object>) datasource).getCaption(itemId);
        }

        public boolean setCaption(Object itemId, String caption) {
            return false;
        }

        public int getLevel(Object itemId) {
            return -1;
        }
    }
}
