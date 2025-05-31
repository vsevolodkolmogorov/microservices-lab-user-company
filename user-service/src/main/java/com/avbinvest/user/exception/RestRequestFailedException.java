package com.avbinvest.user.exception;

/**
 * Исключение, возникающее при неудачном REST-запросе.
 * Используется для сигнализации о проблемах при взаимодействии с внешними REST-сервисами.
 */
public class RestRequestFailedException extends RuntimeException {

    /**
     * Создает новое исключение с указанным сообщением.
     *
     * @param text сообщение исключения
     */
    public RestRequestFailedException(String text) {
        super(text);
    }
}
