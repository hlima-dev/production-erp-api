package com.hlima.erp.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de integração de ponta a ponta do fluxo mais crítico do projeto:
 * baixa de estoque de insumos ao iniciar uma ordem de produção, e entrada
 * do produto acabado ao concluí-la. Sobe um Postgres real via
 * Testcontainers (Flyway roda as migrations normalmente) e bate nos
 * endpoints HTTP reais, incluindo autenticação.
 *
 * IMPORTANTE: precisa de Docker disponível pra rodar (`mvn verify`) — não
 * roda no `mvn test`, só no `mvn verify` (ver exclude do maven-surefire-plugin
 * e execução do maven-failsafe-plugin no pom.xml).
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ProductionOrderFlowIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ordemDeProducaoBaixaInsumoEDaEntradaDoProdutoAcabado() throws Exception {
        String token = login();

        String materiaPrimaId = createProduct(token, "IT-MP001", "Farinha de trigo", "KG", "MATERIA_PRIMA", null);
        String produtoAcabadoId = createProduct(token, "IT-PA001", "Pão francês", "UN", "PRODUTO_ACABADO", "1.50");

        // 0.05kg de farinha por unidade de pão.
        mockMvc.perform(put("/products/" + produtoAcabadoId + "/bom")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notes":"Receita teste","items":[{"ingredientId":"%s","quantityPerUnit":0.05}]}
                                """.formatted(materiaPrimaId)))
                .andExpect(status().isOk());

        String warehouseId = firstWarehouseId(token);

        // Entrada de 10kg de farinha — suficiente pra 100 pães (5kg necessários).
        mockMvc.perform(post("/inventory/movements")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","warehouseId":"%s","type":"ENTRADA","quantity":10}
                                """.formatted(materiaPrimaId, warehouseId)))
                .andExpect(status().isCreated());

        String opJson = mockMvc.perform(post("/production-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","warehouseId":"%s","plannedQuantity":100}
                                """.formatted(produtoAcabadoId, warehouseId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String opId = objectMapper.readTree(opJson).get("id").asText();

        mockMvc.perform(post("/production-orders/" + opId + "/start")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("EM_PRODUCAO"));

        assertThat(stockQuantity(token, "IT-MP001")).isEqualByComparingTo("5.000000"); // 10 - (0.05 x 100)
        assertThat(stockQuantityOrNull(token, "IT-PA001")).isNull(); // ainda não concluiu

        mockMvc.perform(post("/production-orders/" + opId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("CONCLUIDA"));

        assertThat(stockQuantity(token, "IT-PA001")).isEqualByComparingTo("100.000000");
    }

    private String login() throws Exception {
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@erp.com.br","password":"Admin@123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private String createProduct(String token, String code, String name, String unit, String type, String price) throws Exception {
        String priceField = price != null ? "\"price\":" + price : "\"price\":null";
        String response = mockMvc.perform(post("/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"%s","unit":"%s","type":"%s","category":"Teste",%s}
                                """.formatted(code, name, unit, type, priceField)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    private String firstWarehouseId(String token) throws Exception {
        String response = mockMvc.perform(get("/warehouses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get(0).get("id").asText();
    }

    private java.math.BigDecimal stockQuantity(String token, String productCode) throws Exception {
        java.math.BigDecimal value = stockQuantityOrNull(token, productCode);
        assertThat(value).as("saldo de " + productCode).isNotNull();
        return value;
    }

    private java.math.BigDecimal stockQuantityOrNull(String token, String productCode) throws Exception {
        String response = mockMvc.perform(get("/inventory/stock").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode item : objectMapper.readTree(response)) {
            if (item.get("productCode").asText().equals(productCode)) {
                return new java.math.BigDecimal(item.get("quantity").asText());
            }
        }
        return null;
    }
}
