package com.example.Semana15_1.config;

import com.example.Semana15_1.model.Mascota;
import com.example.Semana15_1.model.Usuario;
import com.example.Semana15_1.repository.MascotaRepository;
import com.example.Semana15_1.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DatosIniciales implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final MascotaRepository mascotas;
    private final PasswordEncoder encoder;
    private final String adminPassword;

    public DatosIniciales(UsuarioRepository usuarios, MascotaRepository mascotas, PasswordEncoder encoder,
                         @Value("${app.seed.admin-password}") String adminPassword) {
        this.usuarios = usuarios;
        this.mascotas = mascotas;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional("transactionManager")
    public void run(String... args) {
        boolean primeraCarga = !usuarios.existsByUsername("admin");
        if (primeraCarga) {
            usuarios.save(new Usuario("admin", "Administrador", "admin@gmail.com",
                    encoder.encode(adminPassword), "ADMIN"));
        }

        var iniciales = List.of(
            new Mascota("PET-001", "Luna", "PERRO", "Mestiza", 24, "HEMBRA", "MEDIANO",
                    "Cariñosa y tranquila. Disfruta los paseos y la compañía.", true, true),
            new Mascota("PET-002", "Max", "PERRO", "Mestizo", 36, "MACHO", "GRANDE",
                    "Activo y juguetón. Busca una familia que disfrute caminar.", true, true),
            new Mascota("PET-003", "Milo", "GATO", "Doméstico de pelo corto", 12, "MACHO", "PEQUEÑO",
                    "Curioso y sociable. Le encanta jugar con pelotas.", true, true),
            new Mascota("PET-004", "Nala", "GATO", "Doméstica de pelo corto", 18, "HEMBRA", "PEQUEÑO",
                    "Dulce y tranquila. Prefiere espacios relajados para descansar.", true, true),
            new Mascota("PET-005", "Toby", "PERRO", "Mestizo", 8, "MACHO", "PEQUEÑO",
                    "Cachorro enérgico y cariñoso, listo para aprender con su familia.", true, false),
            new Mascota("PET-006", "Coco", "CONEJO", "Mestizo", 10, "MACHO", "PEQUEÑO",
                    "Sociable y curioso. Necesita espacio seguro y cuidados para conejos.", false, false)
        );
        if (!primeraCarga) return;
        for (var mascota : iniciales) {
            if (!mascotas.existsByCodigo(mascota.getCodigo())) {
                mascotas.save(mascota);
            }
        }
    }
}
