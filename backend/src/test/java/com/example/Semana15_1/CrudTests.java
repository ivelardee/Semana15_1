package com.example.Semana15_1;

import com.example.Semana15_1.repository.MascotaRepository;
import com.example.Semana15_1.config.DatosIniciales;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:crud;DB_CLOSE_DELAY=-1")
class CrudTests {
    @Autowired WebApplicationContext context;
    @Autowired MascotaRepository mascotas;
    @Autowired DatosIniciales datos;

    @Test
    void loginCrudValidacionesYLogout() throws Exception {
        var mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        mvc.perform(get("/api/mascotas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
        mvc.perform(post("/api/login").with(csrf()).param("username", "admin").param("password", "incorrecta"))
                .andExpect(status().isUnauthorized());
        var login = mvc.perform(post("/api/login").with(csrf()).param("username", "admin").param("password", "Prueba2026!"))
                .andExpect(status().isNoContent()).andReturn();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/me").session(session)).andExpect(jsonPath("$.username").value("admin"));
        String body = """
            {"codigo":"TEST-CRUD","nombre":"Prueba","especie":"GATO","raza":"Mestizo",
            "edadMeses":12,"sexo":"HEMBRA","tamanio":"PEQUEÑO","descripcion":"Prueba de adopción",
            "vacunada":true,"esterilizada":true,"estado":"DISPONIBLE"}
            """;
        mvc.perform(post("/api/mascotas").session(session).contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/mascotas").session(session).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.nombre").value("Prueba"));
        long id = mascotas.findAll().stream().filter(m -> m.getCodigo().equals("TEST-CRUD")).findFirst().orElseThrow().getId();
        mvc.perform(post("/api/mascotas").session(session).with(csrf()).contentType("application/json").content(body))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/mascotas/" + id).session(session).with(csrf()).contentType("application/json")
                .content(body.replace("DISPONIBLE", "ADOPTADA")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("ADOPTADA"));
        mvc.perform(get("/api/mascotas/" + id).session(session)).andExpect(status().isOk());
        mvc.perform(put("/api/mascotas/" + id).session(session).with(csrf()).contentType("application/json")
                .content(body.replace("\"edadMeses\":12", "\"edadMeses\":-1"))).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/mascotas/" + id).session(session).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/mascotas/" + id).session(session)).andExpect(status().isNotFound());
        var inicial = mascotas.findAll().getFirst();
        mascotas.delete(inicial);
        datos.run();
        assertThat(mascotas.existsById(inicial.getId())).isFalse();
        mvc.perform(post("/api/logout").session(session).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/mascotas")).andExpect(status().isUnauthorized());
    }
}
