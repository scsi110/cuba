package com.haulmont.cuba.web.app.embedded;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.*;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.icons.Icons;
import com.haulmont.cuba.gui.settings.Settings;
import com.haulmont.cuba.web.app.embedded.window.GuestWindowHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GuestWindow implements Window, Component.Wrapper {
    private GuestWindowHolder holder;
    private HostAppWindowManager.GuestWindowHandlerImpl handler;

    private String caption;
    private String description;

    GuestWindow(GuestWindowHolder holder, HostAppWindowManager.GuestWindowHandlerImpl handler, String caption, String description) {
        this.holder = holder;
        this.handler = handler;
        this.caption = caption;
        this.description = description;
    }

    @Nullable
    @Override
    public Component getOwnComponent(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFocusComponent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFocusComponent(String componentId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addAction(Action action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WindowContext getContext() {
        return holder.getContext();
    }

    @Override
    public void setContext(FrameContext ctx) {
        holder.setContext(ctx);
    }

    @Override
    public void addListener(CloseListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListener(CloseListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCloseListener(CloseListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addCloseWithCommitListener(CloseWithCommitListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeCloseWithCommitListener(CloseWithCommitListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applySettings(Settings settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void saveSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Settings getSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean close(String actionId) {
        handler.closeWindow(actionId);
        return true;
    }

    @Override
    public boolean close(String actionId, boolean force) {
        handler.closeWindow(actionId);
        return true;
    }

    @Override
    public void closeAndRun(String actionId, Runnable runnable) {
        handler.closeWindow(actionId);
        runnable.run();
    }

    @Override
    public void addTimer(Timer timer) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Timer getTimer(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validate(List<Validatable> fields) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean validateAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WindowManager getWindowManager() {
        return holder.getWindowManager();
    }

    @Override
    public void setWindowManager(WindowManager windowManager) {
        holder.setWindowManager(windowManager);
    }

    @Override
    public DialogOptions getDialogOptions() {
        return holder.getDialogOptions();
    }

    @Override
    public ContentSwitchMode getContentSwitchMode() {
        return holder.getContentSwitchMode();
    }

    @Override
    public void setContentSwitchMode(ContentSwitchMode mode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBeforeCloseWithShortcutListener(BeforeCloseWithShortcutListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeBeforeCloseWithShortcutListener(BeforeCloseWithShortcutListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBeforeCloseWithCloseButtonListener(BeforeCloseWithCloseButtonListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeBeforeCloseWithCloseButtonListener(BeforeCloseWithCloseButtonListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DsContext getDsContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDsContext(DsContext dsContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(Component parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDebugId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDebugId(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEnabled(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isResponsive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResponsive(boolean responsive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVisible() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVisible(boolean visible) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVisibleItself() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEnabledItself() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void requestFocus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getHeight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeight(String height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getHeightUnits() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeightAuto() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeightFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getWidth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWidth(String width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getWidthUnits() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWidthAuto() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWidthFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSizeFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSizeAuto() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessagesPack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMessagesPack(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerComponent(Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unregisterComponent(Component component) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Component getRegisteredComponent(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Alignment getAlignment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAlignment(Alignment alignment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStyleName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStyleName(String styleName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addStyleName(String styleName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeStyleName(String styleName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <X> X unwrap(Class<X> internalComponentClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <X> X unwrapComposition(Class<X> internalCompositionClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Component childComponent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Component childComponent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Component getComponentNN(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate() throws ValidationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public DialogParams getDialogParams() {
        return holder.getDialogParams();
    }

    @Override
    public Window openWindow(String windowAlias, WindowManager.OpenType openType, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Window openWindow(String windowAlias, WindowManager.OpenType openType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor openEditor(Entity item, WindowManager.OpenType openType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor openEditor(Entity item, WindowManager.OpenType openType, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor openEditor(Entity item, WindowManager.OpenType openType, Map<String, Object> params, Datasource parentDs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor openEditor(String windowAlias, Entity item, WindowManager.OpenType openType, Map<String, Object> params, Datasource parentDs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor openEditor(String windowAlias, Entity item, WindowManager.OpenType openType, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor openEditor(String windowAlias, Entity item, WindowManager.OpenType openType, Datasource parentDs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Editor openEditor(String windowAlias, Entity item, WindowManager.OpenType openType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Component> getComponents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Component childComponent, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Component component) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Component getComponent(int index) {
        return null;
    }

    @Nonnull
    @Override
    public Component getComponentNN(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lookup openLookup(Class<? extends Entity> entityClass, Lookup.Handler handler, WindowManager.OpenType openType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lookup openLookup(Class<? extends Entity> entityClass, Lookup.Handler handler, WindowManager.OpenType openType, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lookup openLookup(String windowAlias, Lookup.Handler handler, WindowManager.OpenType openType, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lookup openLookup(String windowAlias, Lookup.Handler handler, WindowManager.OpenType openType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Frame openFrame(@Nullable Component parent, String windowAlias) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Frame openFrame(@Nullable Component parent, String windowAlias, Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showMessageDialog(String title, String message, MessageType messageType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showOptionDialog(String title, String message, MessageType messageType, Action[] actions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showOptionDialog(String title, String message, MessageType messageType, List<Action> actions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showNotification(String caption) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showNotification(String caption, NotificationType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showNotification(String caption, String description, NotificationType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showWebPage(String url, @Nullable Map<String, Object> params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Frame getFrame() {
        return null;
    }

    @Override
    public void setFrame(Frame frame) {

    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void addAction(Action action, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAction(@Nullable Action action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAction(@Nullable String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Action> getActions() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Action getAction(String id) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Action getActionNN(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getIcon() {
        return holder.getIcon();
    }

    @Override
    public void setIcon(String icon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIconFromSet(Icons.Icon icon) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getSpacing() {
        return false;
    }

    @Override
    public void setSpacing(boolean enabled) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMargin(boolean enable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMargin(boolean topEnable, boolean rightEnable, boolean bottomEnable, boolean leftEnable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MarginInfo getMargin() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMargin(MarginInfo marginInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void expand(Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void expand(Component component, String height, String width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetExpanded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isExpanded(Component component) {
        return false;
    }

    @Override
    public ExpandDirection getExpandDirection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getComponent() {
        return holder;
    }

    @Override
    public Object getComposition() {
        return holder;
    }
}