package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplenishmentOrderServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ReplenishmentOrderServlet.class.getName());

    private final ReplenishmentOrderService service = new ReplenishmentOrderService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws IOException {
        try {
            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            String action = req.getParameter("action");
            if ("add".equals(action)) {
                service.add(
                        longParam(req, "stockId"),
                        longParam(req, "supplierId"),
                        doubleParam(req, "orderedQuantity")
                );
            } else if ("update".equals(action)) {
                service.update(
                        longParam(req, "id"),
                        longParam(req, "stockId"),
                        longParam(req, "supplierId"),
                        doubleParam(req, "orderedQuantity")
                );
            } else if ("transition".equals(action)) {
                service.changeState(longParam(req, "id"), stateParam(req));
            } else if ("delete".equals(action)) {
                service.delete(longParam(req, "id"));
            } else {
                throw new IllegalArgumentException("Невідома операція");
            }

            writeJson(resp, service.getAll());
        } catch (Exception e) {
            writeError(resp, e);
        }
    }

    private void writeJson(HttpServletResponse resp,
                           Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getWriter(), data);
    }

    // Предметні помилки та помилки даних -> 400, непередбачені помилки сервера -> 500
    private void writeError(HttpServletResponse resp,
                            Exception e) throws IOException {
        int status;
        String message;
        if (e instanceof IllegalArgumentException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            message = e.getMessage() == null ? "Некоректний запит" : e.getMessage();
        } else if (e instanceof SQLIntegrityConstraintViolationException) {
            status = HttpServletResponse.SC_BAD_REQUEST;
            message = "Операцію неможливо виконати: порушено обмеження цілісності даних "
                    + "(дублікат значення або існують пов'язані записи)";
        } else {
            LOG.log(Level.SEVERE, "Внутрішня помилка сервера", e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            message = "Внутрішня помилка сервера";
        }
        resp.setStatus(status);
        writeJson(resp, Map.of("error", message));
    }

    private long longParam(HttpServletRequest req, String name) {
        try {
            return Long.parseLong(req.getParameter(name).trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Некоректне значення параметра " + name);
        }
    }

    private double doubleParam(HttpServletRequest req, String name) {
        try {
            return Double.parseDouble(req.getParameter(name).trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Некоректне значення параметра " + name);
        }
    }

    private OrderState stateParam(HttpServletRequest req) {
        String value = req.getParameter("state");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Не задано стан замовлення");
        }
        try {
            return OrderState.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан замовлення");
        }
    }
}
