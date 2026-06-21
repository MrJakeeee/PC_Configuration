package kg.kstu.pcconfiguration.service;

import java.util.Map;

public class CheckoutValidationException extends IllegalArgumentException {
    private final Map<String, String> fieldErrors;

    public CheckoutValidationException(Map<String, String> fieldErrors) {
        super("Проверьте данные заказа");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
