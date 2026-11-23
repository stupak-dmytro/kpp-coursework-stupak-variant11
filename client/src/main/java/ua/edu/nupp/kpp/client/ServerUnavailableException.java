package ua.edu.nupp.kpp.client;

// Сервер не відповідає (Tomcat вимкнено, хибна адреса, тайм-аут): це не помилка окремого запису
public class ServerUnavailableException extends RuntimeException {

    public ServerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
