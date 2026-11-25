package com.example.Semana15_1;

import com.example.Semana15_1.config.DatosIniciales;
import com.example.Semana15_1.repository.MascotaRepository;
import com.example.Semana15_1.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:seedtest;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.seed.admin-password=Prueba2026!"
})
class DatosInicialesTests {
    @Autowired DatosIniciales datos;
    @Autowired UsuarioRepository usuarios;
    @Autowired MascotaRepository mascotas;
    @Autowired PasswordEncoder encoder;
    @Autowired UserDetailsService userDetails;

    @Test
    void inicializaSinDuplicarYConservaCredenciales() {
        var admin = usuarios.findByUsername("admin").orElseThrow();
        String hashOriginal = admin.getPasswordHash();
        assertThat(encoder.matches("Prueba2026!", hashOriginal)).isTrue();
        assertThat(hashOriginal).isNotEqualTo("Prueba2026!");
        assertThat(userDetails.loadUserByUsername("admin").getAuthorities())
                .extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(mascotas.findAll()).hasSize(6)
                .allSatisfy(mascota -> assertThat(mascota.getEstado()).isEqualTo("DISPONIBLE"));

        datos.run();
        assertThat(usuarios.count()).isEqualTo(1);
        assertThat(mascotas.count()).isEqualTo(6);
        assertThat(usuarios.findByUsername("admin").orElseThrow().getPasswordHash()).isEqualTo(hashOriginal);
    }
}
