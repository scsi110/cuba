package com.haulmont.cuba.web.app.embedded.components;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.annotation.RemoteEntity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.web.AppUI;
import com.haulmont.cuba.web.app.embedded.GuestAppWindowManager;
import com.haulmont.cuba.web.app.embedded.RemoteEntityInfo;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component(RemoteComponentsHelper.NAME)
public class RemoteComponentsHelper {
    public static final String NAME = "cuba_RemoteComponentsHelper";

    @Inject
    private Metadata metadata;

    @Inject
    private DataManager dataManager;

    public void addRemoteLookupAction(PickerField lookupField) {
        RemoteLookupAction action = new RemoteLookupAction(lookupField);
        action.setIcon("font-icon:DATABASE");
        action.setEnabled(AppUI.getCurrent().getAppId() != null);
        lookupField.addAction(action);
    }

    public void addRemoteOpenAction(PickerField lookupField) {
        RemoteOpenAction action = new RemoteOpenAction(lookupField);
        action.setIcon("font-icon:EYE");
        action.setEnabled(AppUI.getCurrent().getAppId() != null);
        lookupField.addAction(action);
    }

    private BaseUuidEntity createEntity(RemoteEntityInfo remoteEntityInfo, MetaClass metaClass, String titleProperty) {
        BaseUuidEntity entity = (BaseUuidEntity) metadata.create(metaClass);
        entity.setId(remoteEntityInfo.getId());
        entity = checkLocal(metaClass, entity);

        if (!titleProperty.isEmpty()) {
            entity.setValue(titleProperty, remoteEntityInfo.getTitle());
        }
        dataManager.commit(entity);
        return entity;
    }

    @SuppressWarnings("unchecked")
    private BaseUuidEntity checkLocal(MetaClass localEntityClass, BaseUuidEntity entity) {
        LoadContext context = new LoadContext(localEntityClass).setId(entity.getId());
        BaseUuidEntity local = ((BaseUuidEntity) dataManager.load(context));
        if (local == null) {
            return entity;
        }
        return local;
    }

    private class RemoteLookupAction extends PickerField.LookupAction {
        private final String titleProperty;
        private final MetaClass metaClass;
        private final Class javaClass;

        RemoteLookupAction(PickerField pickerField) {
            super(pickerField);
            this.metaClass = pickerField.getMetaClass();
            javaClass = metaClass.getJavaClass();
            RemoteEntity annotation = (RemoteEntity) javaClass.getAnnotation(RemoteEntity.class);

            this.titleProperty = annotation.titleProperty();
        }

        @Override
        public void actionPerform(Component component) {
            GuestAppWindowManager wm;
            Window window = ComponentsHelper.getWindow(pickerField);
            if (window == null) {
                LoggerFactory.getLogger(PickerField.class).warn("Please specify Frame for PickerField");

                wm = (GuestAppWindowManager) AppBeans.get(WindowManagerProvider.class).get();
            } else {
                wm = (GuestAppWindowManager) window.getWindowManager();
            }

            WindowManager.OpenType openType = getLookupScreenOpenType();

            Map<String, Object> screenParams = prepareScreenParams();

            wm.openRemoteLookup(
                    javaClass,
                    this::remoteEntitiesHandler,
                    openType.getOpenMode(),
                    screenParams
            );
        }

        private void remoteEntitiesHandler(RemoteEntityInfo[] remotes) {
            List<BaseUuidEntity> entities = Arrays.stream(remotes)
                    .map(remoteEntityInfo -> createEntity(remoteEntityInfo, metaClass, titleProperty))
                    .collect(Collectors.toList());

            handleLookupWindowSelection(entities);
            pickerField.requestFocus();
        }
    }

    private class RemoteOpenAction extends PickerField.OpenAction {
        private final String titleProperty;
        private final MetaClass metaClass;
        private final String remoteName;
        private final Class javaClass;

        RemoteOpenAction(PickerField pickerField) {
            super(pickerField);
            this.metaClass = pickerField.getMetaClass();
            javaClass = metaClass.getJavaClass();
            RemoteEntity annotation = (RemoteEntity) javaClass.getAnnotation(RemoteEntity.class);
            this.remoteName = annotation.remoteName();
            this.titleProperty = annotation.titleProperty();
        }

        @Override
        public void actionPerform(Component component) {
            boolean composition = pickerField.getMetaPropertyPath() != null
                    && pickerField.getMetaPropertyPath().getMetaProperty().getType() == MetaProperty.Type.COMPOSITION;

            if (composition) {
                throw new IllegalStateException("No composition allowed");
            }

            BaseUuidEntity entity = (BaseUuidEntity) getEntity();
            if (entity == null)
                return;

            if (entity instanceof SoftDelete) {
                throw new IllegalStateException("No soft delete allowed");
            }


            GuestAppWindowManager wm;
            Window window = ComponentsHelper.getWindow(pickerField);
            if (window == null) {
                throw new IllegalStateException("Please specify Frame for EntityLinkField");
            } else {
                wm = (GuestAppWindowManager) window.getWindowManager();
            }

            WindowManager.OpenType openType = getEditScreenOpenType();
            Map<String, Object> screenParams = prepareScreenParams();

            entity = window.getDsContext().getDataSupplier().reload(entity, View.MINIMAL);

            String item = remoteName + "-" + entity.getId();
            wm.openRemoteEditor(javaClass,
                    item,
                    this::remoteEntitiesHandler,
                    openType.getOpenMode(),
                    screenParams
            );
        }

        private void remoteEntitiesHandler(RemoteEntityInfo[] remotes) {
            List<BaseUuidEntity> entities = Arrays.stream(remotes)
                    .map(remoteEntityInfo -> createEntity(remoteEntityInfo, metaClass, titleProperty))
                    .collect(Collectors.toList());

            if (!entities.isEmpty()) {
                BaseUuidEntity entity = entities.get(0);
                afterCommitOpenedEntity(entity);
            }

            pickerField.requestFocus();
        }
    }
}
