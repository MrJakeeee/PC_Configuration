package kg.kstu.pcconfiguration.model;

public enum PaymentStatus {
    WAITING("Ожидает оплаты"),
    PAID("Оплачено"),
    REFUNDED("Возврат");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
