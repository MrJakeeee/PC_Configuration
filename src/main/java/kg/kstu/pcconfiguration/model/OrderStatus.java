package kg.kstu.pcconfiguration.model;

public enum OrderStatus {
    NEW("Новый"),
    PAID("Оплачен"),
    ASSEMBLING("Сборка"),
    READY_FOR_DELIVERY("Готов к доставке"),
    IN_DELIVERY("В доставке"),
    COMPLETED("Завершён"),
    CANCELLED("Отменён");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
