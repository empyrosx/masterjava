package ru.javaops.masterjava.web;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static ru.javaops.masterjava.export.ThymeleafListener.engine;

/**
 * Created by empyros on 01.04.17.
 */
@WebServlet(urlPatterns = "/users")
public class UsersServlet extends HttpServlet {

    private static final int MAX_USERS_PER_PAGE = 20;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        List<User> users = loadUsers();
        webContext.setVariable("users", users);
        engine.process("users", webContext, resp.getWriter());
    }

    private List<User> loadUsers() {
        UserDao dao = DBIProvider.getDao(UserDao.class);
        return dao.getWithLimit(MAX_USERS_PER_PAGE);
    }
}
