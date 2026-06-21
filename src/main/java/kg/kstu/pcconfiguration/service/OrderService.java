package kg.kstu.pcconfiguration.service;

import kg.kstu.pcconfiguration.model.AppUser;
import kg.kstu.pcconfiguration.model.CustomerOrder;
import kg.kstu.pcconfiguration.model.OrderStatus;
import kg.kstu.pcconfiguration.model.PaymentStatus;
import kg.kstu.pcconfiguration.model.PcBuild;
import kg.kstu.pcconfiguration.model.ReadyPc;
import kg.kstu.pcconfiguration.repository.CustomerOrderRepository;
import kg.kstu.pcconfiguration.repository.PcBuildRepository;
import kg.kstu.pcconfiguration.repository.ReadyPcRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final CustomerOrderRepository orders;
    private final PcBuildRepository builds;
    private final ReadyPcRepository readyPcs;

    public OrderService(CustomerOrderRepository orders, PcBuildRepository builds, ReadyPcRepository readyPcs) {
        this.orders = orders;
        this.builds = builds;
        this.readyPcs = readyPcs;
    }

    @Transactional
    public CustomerOrder createFromBuild(AppUser user, Long buildId, String firstName, String lastName, String phone,
                                         String address, String cardNumber, String cardExpiry, String cardCvv,
                                         String comment) {
        PcBuild build = builds.findById(buildId).orElseThrow();
        if (!build.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Можно оформить только свою сборку");
        }
        CustomerOrder order = baseOrder(user, firstName, lastName, phone, address, cardNumber, cardExpiry, cardCvv, comment);
        order.setBuild(build);
        order.setAmount(build.getTotalPrice());
        order.setItemSummary(build.getName());
        return orders.save(order);
    }

    @Transactional
    public CustomerOrder createFromReadyPc(AppUser user, Long readyPcId, String firstName, String lastName, String phone,
                                           String address, String cardNumber, String cardExpiry, String cardCvv,
                                           String comment) {
        ReadyPc readyPc = readyPcs.findById(readyPcId).orElseThrow();
        CustomerOrder order = baseOrder(user, firstName, lastName, phone, address, cardNumber, cardExpiry, cardCvv, comment);
        order.setReadyPc(readyPc);
        order.setAmount(readyPc.getPrice());
        return orders.save(order);
    }

    @Transactional
    public CustomerOrder updateDelivery(Long orderId, OrderStatus status, LocalDate dueDate, String comment) {
        CustomerOrder order = orders.findById(orderId).orElseThrow();
        order.setStatus(status);
        order.setDeliveryDueDate(dueDate);
        order.setDeliveryComment(comment);
        return orders.save(order);
    }

    CustomerOrder baseOrder(AppUser user, String firstName, String lastName, String phone, String address,
                            String cardNumber, String cardExpiry, String cardCvv, String comment) {
        Map<String, String> fieldErrors = validateCheckout(firstName, lastName, phone, address, cardNumber, cardExpiry, cardCvv);
        if (!fieldErrors.isEmpty()) {
            throw new CheckoutValidationException(fieldErrors);
        }

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setCustomerFirstName(firstName.trim());
        order.setCustomerLastName(lastName.trim());
        order.setCustomerPhone(phone.trim());
        order.setDeliveryAddress(address.trim());
        String digits = cardNumber.replaceAll("\\D", "");
        order.setCardLast4(digits.substring(digits.length() - 4));
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setDeliveryComment(comment);
        return order;
    }

    private Map<String, String> validateCheckout(String firstName, String lastName, String phone, String address,
                                                 String cardNumber, String cardExpiry, String cardCvv) {
        Map<String, String> errors = new LinkedHashMap<>();
        validateRequired(errors, "firstName", firstName, "Укажите имя");
        validateRequired(errors, "lastName", lastName, "Укажите фамилию");
        validateRequired(errors, "phone", phone, "Укажите телефон для доставки");
        validateRequired(errors, "address", address, "Укажите адрес доставки");
        validateCard(errors, cardNumber, cardExpiry, cardCvv);
        return errors;
    }

    private void validateRequired(Map<String, String> errors, String field, String value, String message) {
        if (value == null || value.isBlank()) {
            errors.put(field, message);
        }
    }

    private void validateCard(Map<String, String> errors, String cardNumber, String cardExpiry, String cardCvv) {
        String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        if (digits.length() != 16 || !passesLuhn(digits)) {
            errors.put("cardNumber", "Проверьте номер карты");
        }
        if (cardCvv == null || !cardCvv.matches("\\d{3,4}")) {
            errors.put("cardCvv", "Проверьте CVV код карты");
        }
        if (cardExpiry == null || !cardExpiry.matches("\\d{2}/\\d{2}")) {
            errors.put("cardExpiry", "Укажите срок карты в формате ММ/ГГ");
            return;
        }
        try {
            YearMonth expiry = YearMonth.parse(cardExpiry, DateTimeFormatter.ofPattern("MM/yy"));
            if (expiry.isBefore(YearMonth.now())) {
                errors.put("cardExpiry", "Срок действия карты истек");
            }
        } catch (DateTimeParseException ex) {
            errors.put("cardExpiry", "Укажите срок карты в формате ММ/ГГ");
        }
    }

    private boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int value = digits.charAt(i) - '0';
            if (doubleDigit) {
                value *= 2;
                if (value > 9) {
                    value -= 9;
                }
            }
            sum += value;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
