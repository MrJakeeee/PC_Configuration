package kg.kstu.pcconfiguration;

import kg.kstu.pcconfiguration.repository.ComponentItemRepository;
import kg.kstu.pcconfiguration.repository.ReadyPcRepository;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pc_config_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class WebPagesSmokeTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ComponentItemRepository components;

    @Autowired
    private ReadyPcRepository readyPcs;

    @Test
    void publicPagesRender() throws Exception {
        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/configurator")).andExpect(status().isOk());
        mvc.perform(get("/catalog")).andExpect(status().isOk());
        mvc.perform(get("/ready-pcs")).andExpect(status().isOk());
    }

    @Test
    void everyConfiguratorPartTypeHasSevenItems() {
        Map<String, Long> counts = components.findByActiveTrueOrderByCategoryNameAscNameAsc().stream()
                .collect(Collectors.groupingBy(item -> item.getPartType(), Collectors.counting()));
        for (String type : new String[]{"CASE", "MOTHERBOARD", "CPU", "COOLING", "RAM", "GPU", "M2", "SATA", "PSU"}) {
            org.assertj.core.api.Assertions.assertThat(counts.get(type)).as(type).isGreaterThanOrEqualTo(7);
        }
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void buildAndReadyPcFormsSubmit() throws Exception {
        mvc.perform(post("/builds")
                        .with(csrf())
                        .param("name", "Smoke build")
                        .param("componentIds",
                                id("Zalman i3 Neo ATX"),
                                id("MSI B650M Gaming WiFi"),
                                id("AMD Ryzen 5 7600"),
                                id("Kingston Fury Beast 32GB DDR5"),
                                id("DeepCool PK650D 650W")))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/orders/ready-pc")
                        .with(csrf())
                        .param("readyPcId", readyPcs.findByName("Office Start").orElseThrow().getId().toString())
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "+996700000000")
                        .param("address", "Test address")
                        .param("cardNumber", "4111111111111111")
                        .param("cardExpiry", "12/30")
                        .param("cardCvv", "123")
                        .param("comment", "test"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/checkout/ready-pc/" + readyPcs.findByName("Office Start").orElseThrow().getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void invalidCheckoutCardStaysOnFormWithFieldError() throws Exception {
        mvc.perform(post("/orders/ready-pc")
                        .with(csrf())
                        .param("readyPcId", readyPcs.findByName("Office Start").orElseThrow().getId().toString())
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phone", "+996700000000")
                        .param("address", "Test address")
                        .param("cardNumber", "4111111111111112")
                        .param("cardExpiry", "12/30")
                        .param("cardCvv", "123")
                        .param("comment", "test"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("field-invalid")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("cardNumber")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void cartAndFavoritesFlowWorks() throws Exception {
        String componentId = id("AMD Ryzen 5 7600");
        String readyPcId = readyPcs.findByName("Office Start").orElseThrow().getId().toString();

        mvc.perform(post("/cart/component")
                        .with(csrf())
                        .param("componentId", componentId))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/cart/ready-pc")
                        .with(csrf())
                        .param("readyPcId", readyPcId))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/cart")).andExpect(status().isOk());

        mvc.perform(post("/favorites/component")
                        .with(csrf())
                        .param("componentId", componentId))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/favorites/ready-pc")
                        .with(csrf())
                        .param("readyPcId", readyPcId))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/favorites")).andExpect(status().isOk());
    }

    private String id(String name) {
        return components.findByName(name).orElseThrow().getId().toString();
    }
}
