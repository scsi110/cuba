package com.haulmont.cuba.web.app.embedded.components;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.RemoteEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.Window;
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

    public void addRemoteLookup(PickerField lookupField, String appName, String entityName) {
        lookupField.addAction(new RemoteLookupAction(lookupField, appName, entityName));
    }

    public void addRemoteOpen(PickerField lookupField, String appName, String entityName) {
        lookupField.addAction(new RemoteOpenAction(lookupField, appName, entityName));
    }

    private class RemoteLookupAction extends PickerField.LookupAction {
        private final String appName;
        private final String entityName;

        RemoteLookupAction(PickerField pickerField, String appName, String entityName) {
            super(pickerField);
            this.appName = appName;
            this.entityName = entityName;
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
                    appName,
                    entityName,
                    this::constructLocalEntity,
                    openType.getOpenMode(),
                    screenParams
            );
        }

        private void constructLocalEntity(RemoteEntityInfo[] remotes) {
            Class localEntityClass = pickerField.getMetaClass().getJavaClass();
            RemoteEntity annotation = (RemoteEntity) localEntityClass.getAnnotation(RemoteEntity.class);

            List<BaseUuidEntity> entities = Arrays.stream(remotes)
                    .map(remoteEntityInfo -> {
                        BaseUuidEntity entity = (BaseUuidEntity) metadata.create(pickerField.getMetaClass());
                        entity.setId(remoteEntityInfo.getId());
                        entity = checkLocal(localEntityClass, entity);

                        if (!annotation.title().isEmpty()) {
                            entity.setValue(annotation.title(), remoteEntityInfo.getTitle());
                        }
                        dataManager.commit(entity);
                        return entity;
                    })
                    .collect(Collectors.toList());

            handleLookupWindowSelection(entities);
            pickerField.requestFocus();
        }

        @SuppressWarnings("unchecked")
        private BaseUuidEntity checkLocal(Class localEntityClass, BaseUuidEntity entity) {
            LoadContext context = LoadContext.create(localEntityClass).setId(entity.getId());
            BaseUuidEntity local = ((BaseUuidEntity) dataManager.load(context));
            if (local == null) {
                return entity;
            }
            return local;
        }
    }


    private class RemoteOpenAction extends PickerField.OpenAction {
        private final String appName;
        private final String entityName;

        RemoteOpenAction(PickerField pickerField, String appName, String entityName) {
            super(pickerField);
            this.appName = appName;
            this.entityName = entityName;
        }
    }
}
