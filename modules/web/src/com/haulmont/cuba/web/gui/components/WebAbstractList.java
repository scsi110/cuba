/*
 * Copyright (c) 2008-2016 Haulmont.
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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.vaadin.v7.ui.AbstractSelect;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @param <T> type of underlying Vaadin component
 *
 * todo remove this class
 */
@Deprecated
public abstract class WebAbstractList<T extends AbstractSelect, E extends Entity>
    extends
        WebAbstractActionsHolderComponent<T>
    implements
        ListComponent<E> {

    @Deprecated
    protected CollectionDatasource datasource;

    @Override
    public boolean isMultiSelect() {
        return component.isMultiSelect();
    }

    public void setMultiSelect(boolean multiselect) {
        component.setMultiSelect(multiselect);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E getSingleSelected() {
        final Set selected = getSelectedItemIds();
        return selected == null || selected.isEmpty() ?
                null : (E) datasource.getItem(selected.iterator().next());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<E> getSelected() {
        Set<Object> itemIds = getSelectedItemIds();

        if (itemIds != null) {
            if (itemIds.size() == 1) {
                E item = (E) datasource.getItem(itemIds.iterator().next());
                return Collections.singleton(item);
            }

            Set res = new LinkedHashSet<>();
            for (Object id : itemIds) {
                Entity item = datasource.getItem(id);
                if (item != null)
                    res.add(item);
            }
            return res;
        } else {
            return Collections.emptySet();
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Set<Object> getSelectedItemIds() {
        Object value = component.getValue();
        if (value == null) {
            return null;
        } else if (value instanceof Set) {
            return (Set) value;
        } else if (value instanceof Collection) {
            return new LinkedHashSet((Collection) value);
        } else {
            return Collections.singleton(value);
        }
    }

    @Override
    public void setSelected(E item) {
        if (item == null) {
            component.setValue(null);
        } else {
            setSelected(Collections.singletonList(item));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setSelected(Collection<E> items) {
        if (items.isEmpty()) {
            setSelectedIds(Collections.emptyList());
        } else if (items.size() == 1) {
            E item = items.iterator().next();
            if (!datasource.containsItem(item.getId())) {
                throw new IllegalArgumentException("Datasource doesn't contain item to select: " + item);
            }
            setSelectedIds(Collections.singletonList(item.getId()));
        } else {
            Set itemIds = new HashSet();
            for (Entity item : items) {
                if (!datasource.containsItem(item.getId())) {
                    throw new IllegalArgumentException("Datasource doesn't contain item to select: " + item);
                }
                itemIds.add(item.getId());
            }
            setSelectedIds(itemIds);
        }
    }

    protected void setSelectedIds(Collection<Object> itemIds) {
        if (component.isMultiSelect()) {
            component.setValue(itemIds);
        } else {
            component.setValue(itemIds.size() > 0 ? itemIds.iterator().next() : null);
        }
    }

    @Override
    protected void attachAction(Action action) {
        if (action instanceof Action.HasTarget) {
            ((Action.HasTarget) action).setTarget(this);
        }

        super.attachAction(action);
    }
}