package kg.kstu.pcconfiguration.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    private ComponentItem component;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReadyPc readyPc;

    private int quantity = 1;

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public ComponentItem getComponent() {
        return component;
    }

    public void setComponent(ComponentItem component) {
        this.component = component;
    }

    public ReadyPc getReadyPc() {
        return readyPc;
    }

    public void setReadyPc(ReadyPc readyPc) {
        this.readyPc = readyPc;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public String getName() {
        return component != null ? component.getName() : readyPc.getName();
    }

    public String getTypeLabel() {
        return component != null ? component.getCategory().getName() : "Готовый ПК";
    }

    public java.math.BigDecimal getPrice() {
        return component != null ? component.getPrice() : readyPc.getPrice();
    }

    public java.math.BigDecimal getTotalPrice() {
        return getPrice().multiply(java.math.BigDecimal.valueOf(quantity));
    }
}
