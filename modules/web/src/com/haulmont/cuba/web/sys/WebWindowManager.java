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
package com.haulmont.cuba.web.sys;

import com.haulmont.bali.events.EventHub;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.Screen;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerUtils;
import com.haulmont.cuba.gui.components.DialogWindow;
import com.haulmont.cuba.gui.components.RootWindow;
import com.haulmont.cuba.gui.components.TabWindow;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.sys.WindowImplementation;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.screen.*;
import com.haulmont.cuba.gui.sys.ScreenDependencyInjector;
import com.haulmont.cuba.gui.sys.ScreenUtils;
import com.haulmont.cuba.gui.sys.ScreenViewsLoader;
import com.haulmont.cuba.gui.xml.layout.*;
import com.haulmont.cuba.gui.xml.layout.loaders.ComponentLoaderContext;
import com.haulmont.cuba.security.entity.PermissionType;
import com.haulmont.cuba.web.AppUI;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Locale;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(WindowManager.NAME)
public class WebWindowManager implements WindowManager {
    @Inject
    protected WindowConfig windowConfig;
    @Inject
    protected Security security;
    @Inject
    protected ComponentsFactory componentsFactory;
    @Inject
    protected BeanLocator beanLocator;
    @Inject
    protected ScreenXmlLoader screenXmlLoader;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected ScreenViewsLoader screenViewsLoader;

    protected AppUI ui;

    public WebWindowManager(AppUI ui) {
        this.ui = ui;
    }

    @Override
    public <T extends Screen> T create(Class<T> requiredScreenClass, LaunchMode launchMode, ScreenOptions options) {
        WindowInfo windowInfo = getScreenInfo(requiredScreenClass);

        checkPermissions(launchMode, windowInfo);

        // todo perf4j stop watches for lifecycle

        @SuppressWarnings("unchecked")
        Class<T> resolvedScreenClass = (Class<T>) windowInfo.getScreenClass();

        Window window = createWindow(windowInfo, resolvedScreenClass, launchMode);

        T controller = createController(windowInfo, window, resolvedScreenClass, launchMode);

        loadScreenXml(windowInfo, window, controller, options);

        WindowManagerUtils.setWindow(controller, window);

        WindowImplementation windowImpl = (WindowImplementation) window;
        windowImpl.setController(controller);
        windowImpl.setLaunchMode(launchMode);

        // todo legacy datasource layer

        ScreenDependencyInjector dependencyInjector =
                beanLocator.getPrototype(ScreenDependencyInjector.NAME, controller, options);
        dependencyInjector.inject();

        InitEvent initEvent = new InitEvent(controller, options);
        WindowManagerUtils.fireEvent(controller, InitEvent.class, initEvent);

        AfterInitEvent afterInitEvent = new AfterInitEvent(controller, options);
        WindowManagerUtils.fireEvent(controller, AfterInitEvent.class, afterInitEvent);

        return controller;
    }

    protected <T extends Screen> void loadScreenXml(WindowInfo windowInfo, Window window, T controller,
                                                    ScreenOptions options) {
        String templatePath = windowInfo.getTemplate();

        if (StringUtils.isNotEmpty(templatePath)) {
            // todo support relative design path

            Element element = screenXmlLoader.load(templatePath, windowInfo.getId(),
                    Collections.emptyMap()); // todo support legacy params map

            // todo load XML markup if annotation present, or screen is legacy screen

            ComponentLoaderContext componentLoaderContext =
                    new ComponentLoaderContext(Collections.emptyMap()); // todo support legacy parameters map
            componentLoaderContext.setFullFrameId(windowInfo.getId());
            componentLoaderContext.setCurrentFrameId(windowInfo.getId());

            ComponentLoader windowLoader = createLayout(windowInfo, window, element, componentLoaderContext);

            screenViewsLoader.deployViews(element); // todo will be removed from new screens

            // todo load datasources here

            windowLoader.loadComponent();

            EventHub eventHub = WindowManagerUtils.getEventHub(controller);
            eventHub.subscribe(AfterInitEvent.class, event -> {
                componentLoaderContext.setFrame(window);
                componentLoaderContext.executePostInitTasks();
            });
        }
    }

    protected ComponentLoader createLayout(WindowInfo windowInfo, Window window, Element rootElement,
                                           ComponentLoader.Context context) {
        String descriptorPath = windowInfo.getTemplate();

        LayoutLoader layoutLoader = beanLocator.getPrototype(LayoutLoader.NAME, context);
        layoutLoader.setLocale(getLocale());

        // todo should we load messages depending on Class ?
        if (StringUtils.isNotEmpty(descriptorPath)) {
            if (descriptorPath.contains("/")) {
                descriptorPath = StringUtils.substring(descriptorPath, 0, descriptorPath.lastIndexOf("/"));
            }

            String path = descriptorPath.replaceAll("/", ".");
            int start = path.startsWith(".") ? 1 : 0;
            path = path.substring(start);

            layoutLoader.setMessagesPack(path);
        }
        //noinspection UnnecessaryLocalVariable
        ComponentLoader windowLoader = layoutLoader.createWindowContent(window, rootElement, windowInfo.getId());
        return windowLoader;
    }

    protected Locale getLocale() {
        return userSessionSource.getUserSession().getLocale();
    }

    @Override
    public void show(Screen screen) {
        checkMultiOpen(screen);

        // todo load and apply settings

        // todo UI security

        BeforeShowEvent beforeShowEvent = new BeforeShowEvent(screen);
        WindowManagerUtils.fireEvent(screen, BeforeShowEvent.class, beforeShowEvent);

        WindowImplementation windowImpl = (WindowImplementation) screen.getWindow();

        if (windowImpl.getLaunchMode() instanceof OpenMode) {
            OpenMode openMode = (OpenMode) windowImpl.getLaunchMode();

            switch (openMode) {
                case ROOT:
                    showRootWindow(screen);
                    break;

                case THIS_TAB:
                    showThisTabWindow(screen);
                    break;

                case NEW_WINDOW:
                case NEW_TAB:
                    showNewTabWindow(screen);
                    break;

                case DIALOG:
                    showDialogWindow(screen);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported OpenMode " + openMode);
            }
        }

        AfterShowEvent afterShowEvent = new AfterShowEvent(screen);
        WindowManagerUtils.fireEvent(screen, AfterShowEvent.class, afterShowEvent);
    }

    protected void showRootWindow(Screen screen) {
        // todo
        /*if (topLevelWindow instanceof AbstractMainWindow) {
            AbstractMainWindow mainWindow = (AbstractMainWindow) topLevelWindow;

            // bind system UI components to AbstractMainWindow
            ComponentsHelper.walkComponents(windowImpl, component -> {
                if (component instanceof AppWorkArea) {
                    mainWindow.setWorkArea((AppWorkArea) component);
                } else if (component instanceof UserIndicator) {
                    mainWindow.setUserIndicator((UserIndicator) component);
                } else if (component instanceof FoldersPane) {
                    mainWindow.setFoldersPane((FoldersPane) component);
                }

                return false;
            });
        }*/

        ui.setTopLevelWindow((RootWindow) screen.getWindow());

        // todo
        /*if (screen instanceof Window.HasWorkArea) {
            AppWorkArea workArea = ((Window.HasWorkArea) screen).getWorkArea();
            if (workArea != null) {
                workArea.addStateChangeListener(new AppWorkArea.StateChangeListener() {
                    @Override
                    public void stateChanged(AppWorkArea.State newState) {
                        if (newState == AppWorkArea.State.WINDOW_CONTAINER) {
                            initTabShortcuts();

                            // listener used only once
                            getConfiguredWorkArea(createWorkAreaContext(topLevelWindow)).removeStateChangeListener(this);
                        }
                    }
                });
            }
        }*/
    }

    protected void showThisTabWindow(Screen screen) {
        // todo
    }

    protected void showNewTabWindow(Screen screen) {
        // todo
    }

    protected void showDialogWindow(Screen screen) {
        // todo
    }

    @Override
    public void remove(Screen screen) {
        // todo remove event
    }

    @Override
    public void removeAll() {
        // todo implement
    }

    protected <T extends Screen> T createController(WindowInfo windowInfo, Window window,
                                                    Class<T> screenClass, LaunchMode launchMode) {
        Constructor<T> constructor;
        try {
            constructor = screenClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new DevelopmentException("No accessible constructor for screen class " + screenClass);
        }

        T controller;
        try {
            controller = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create instance of screen class " + screenClass);
        }

        return controller;
    }

    protected Window createWindow(WindowInfo windowInfo, Class<? extends Screen> screenClass, LaunchMode launchMode) {
        Window window;
        if (launchMode instanceof OpenMode) {
            OpenMode openMode = (OpenMode) launchMode;
            switch (openMode) {
                case ROOT:
                    window = componentsFactory.createComponent(RootWindow.NAME);
                    break;

                case THIS_TAB:
                case NEW_TAB:
                    window = componentsFactory.createComponent(TabWindow.NAME);
                    break;

                case DIALOG:
                    window = componentsFactory.createComponent(DialogWindow.NAME);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported launch mode");
            }
        } else {
            throw new UnsupportedOperationException("Unsupported launch mode");
        }

        return window;
    }

    protected void checkMultiOpen(Screen screen) {
        // todo check if already opened, replace buggy int hash code
    }

    protected void checkPermissions(LaunchMode launchMode, WindowInfo windowInfo) {
        // ROOT windows are always permitted
        if (launchMode != OpenMode.ROOT) {
            boolean permitted = security.isScreenPermitted(windowInfo.getId());
            if (!permitted) {
                throw new AccessDeniedException(PermissionType.SCREEN, windowInfo.getId());
            }
        }
    }

    protected WindowInfo getScreenInfo(Class<? extends Screen> screenClass) {
        ScreenController screenController = screenClass.getAnnotation(ScreenController.class);
        // todo legacy screens
        if (screenController == null) {
            throw new IllegalArgumentException("No @ScreenController annotation for class " + screenClass);
        }

        String screenId = ScreenUtils.getInferredScreenId(screenController, screenClass);

        return windowConfig.getWindowInfo(screenId);
    }
}