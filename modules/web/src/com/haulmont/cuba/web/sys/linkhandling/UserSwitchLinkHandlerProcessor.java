package com.haulmont.cuba.web.sys.linkhandling;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.DialogAction;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserSubstitution;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.actions.ChangeSubstUserAction;
import com.haulmont.cuba.web.actions.DoNotChangeSubstUserAction;
import com.vaadin.server.Page;
import com.vaadin.ui.JavaScript;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@org.springframework.stereotype.Component(UserSwitchLinkHandlerProcessor.NAME)
@Order(1000)
public class UserSwitchLinkHandlerProcessor implements LinkHandlerProcessor {
    public static final String NAME = "cuba_UserSwitchLinkHandlerProcessor";

    private final Logger log = LoggerFactory.getLogger(UserSwitchLinkHandlerProcessor.class);

    @Resource(name = ScreensLinkHandlerProcessor.NAME)
    protected LinkHandlerProcessor screenHandler;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected DataService dataService;

    @Inject
    protected Messages messages;

    @Override
    public boolean canHandle(Map<String, String> requestParams, String action) {
        if (!screenHandler.canHandle(requestParams, action)) {
            return false;
        }

        UUID userId = getUUID(requestParams.get("user"));
        return userId != null;
    }

    @Override
    public void handle(Map<String, String> requestParams, String action, App app) {
        UUID userId = getUUID(requestParams.get("user"));
        assert userId != null;

        UserSession userSession = App.getInstance().getConnection().getSession();
        if (userSession == null) {
            log.warn("No user session");
            return;
        }

        if (!userSession.getCurrentOrSubstitutedUser().getId().equals(userId)) {
            substituteUserAndOpenWindow(requestParams, userId, action, app);
        } else {
            screenHandler.handle(requestParams, action, app);
        }
    }

    protected void substituteUserAndOpenWindow(Map<String, String> requestParams, UUID userId, String action, App app) {
        UserSession userSession = app.getConnection().getSession();
        assert userSession != null;

        final User substitutedUser = loadUser(userId, userSession.getUser());
        if (substitutedUser != null) {
            Map<String, String> currentRequestParams = new HashMap<>(requestParams);

            app.getWindowManager().showOptionDialog(
                    messages.getMainMessage("toSubstitutedUser.title"),
                    getDialogMessage(substitutedUser),
                    Frame.MessageType.CONFIRMATION_HTML,
                    new Action[]{
                            new ChangeSubstUserAction(substitutedUser) {
                                @Override
                                public void doAfterChangeUser() {
                                    super.doAfterChangeUser();
                                    screenHandler.handle(currentRequestParams, action, app);
                                }

                                @Override
                                public void doRevert() {
                                    super.doRevert();

                                    JavaScript js = Page.getCurrent().getJavaScript();
                                    js.execute("window.close();");
                                }

                                @Override
                                public String getCaption() {
                                    return messages.getMainMessage("action.switch");
                                }
                            },
                            new DoNotChangeSubstUserAction() {
                                @Override
                                public void actionPerform(Component component) {
                                    super.actionPerform(component);

                                    JavaScript js = Page.getCurrent().getJavaScript();
                                    js.execute("window.close();");
                                }

                                @Override
                                public String getCaption() {
                                    return messages.getMainMessage("action.cancel");
                                }
                            }
                    });
        } else {
            User user = loadUser(userId);
            app.getWindowManager().showOptionDialog(
                    messages.getMainMessage("warning.title"),
                    getWarningMessage(user),
                    Frame.MessageType.WARNING_HTML,
                    new Action[]{
                            new DialogAction(DialogAction.Type.OK).withHandler(event -> {
                                JavaScript js = Page.getCurrent().getJavaScript();
                                js.execute("window.close();");
                            })
                    });
        }
    }

    protected UUID getUUID(String id) {
        if (StringUtils.isBlank(id))
            return null;

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            uuid = null;
        }
        return uuid;
    }

    protected User loadUser(UUID userId, User user) {
        if (user.getId().equals(userId))
            return user;
        LoadContext loadContext = new LoadContext(UserSubstitution.class);
        LoadContext.Query query = new LoadContext.Query("select su from sec$UserSubstitution us join us.user u " +
                "join us.substitutedUser su where u.id = :id and su.id = :userId and " +
                "(us.endDate is null or us.endDate >= :currentDate) and (us.startDate is null or us.startDate <= :currentDate)");
        query.setParameter("id", user);
        query.setParameter("userId", userId);
        query.setParameter("currentDate", timeSource.currentTimestamp());
        loadContext.setQuery(query);
        List<User> users = dataService.loadList(loadContext);
        return users.isEmpty() ? null : users.get(0);
    }

    protected User loadUser(UUID userId) {
        LoadContext<User> loadContext = new LoadContext<>(User.class);
        LoadContext.Query query = new LoadContext.Query("select u from sec$User u where u.id = :userId");
        query.setParameter("userId", userId);
        loadContext.setQuery(query);
        List<User> users = dataService.loadList(loadContext);
        return users.isEmpty() ? null : users.get(0);
    }

    protected String getDialogMessage(User user) {
        return messages.formatMainMessage(
                "toSubstitutedUser.msg",
                StringUtils.isBlank(user.getName()) ? user.getLogin() : user.getName()
        );
    }

    protected String getWarningMessage(User user) {
        if (user == null)
            return messages.getMainMessage("warning.userNotFound");
        return messages.formatMainMessage(
                "warning.msg",
                StringUtils.isBlank(user.getName()) ? user.getLogin() : user.getName()
        );
    }
}
