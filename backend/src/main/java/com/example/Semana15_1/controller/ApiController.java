package com.example.Semana15_1.controller;

import com.example.Semana15_1.model.Mascota;
import com.example.Semana15_1.repository.MascotaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final MascotaRepository mascotas;
    public ApiController(MascotaRepository mascotas) { this.mascotas = mascotas; }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) { return Map.of("username", principal.getName()); }

    @GetMapping("/mascotas")
    public List<Mascota> listar() { return mascotas.findAll(org.springframework.data.domain.Sort.by("id")); }

    @GetMapping("/mascotas/{id}")
    public Mascota obtener(@PathVariable Long id) {
        return mascotas.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
    }

    @PostMapping("/mascotas")
    @ResponseStatus(HttpStatus.CREATED)
    public Mascota crear(@RequestBody Mascota mascota) {
        mascota.setId(null);
        validar(mascota);
        return guardar(mascota);
    }

    @PutMapping("/mascotas/{id}")
    public Mascota editar(@PathVariable Long id, @RequestBody Mascota mascota) {
        obtener(id);
        mascota.setId(id);
        validar(mascota);
        return guardar(mascota);
    }

    @DeleteMapping("/mascotas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { mascotas.delete(obtener(id)); }

    private Mascota guardar(Mascota mascota) {
        try { return mascotas.saveAndFlush(mascota); }
        catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El código de mascota ya existe");
        }
    }

    private void validar(Mascota m) {
        if (m.getCodigo() == null || m.getCodigo().isBlank() || m.getCodigo().length() > 40
            || m.getNombre() == null || m.getNombre().isBlank() || m.getNombre().length() > 80
            || m.getEspecie() == null || !List.of("PERRO", "GATO", "CONEJO", "OTRO").contains(m.getEspecie())
            || m.getSexo() == null || !List.of("MACHO", "HEMBRA").contains(m.getSexo())
            || m.getTamanio() == null || !List.of("PEQUEÑO", "MEDIANO", "GRANDE").contains(m.getTamanio())
            || m.getEstado() == null || !List.of("DISPONIBLE", "EN_PROCESO", "ADOPTADA").contains(m.getEstado())
            || m.getEdadMeses() < 0 || m.getEdadMeses() > 600
            || (m.getRaza() != null && m.getRaza().length() > 100)
            || (m.getDescripcion() != null && m.getDescripcion().length() > 1000)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Revisa los datos de la mascota");
        }
        m.setCodigo(m.getCodigo().trim().toUpperCase(java.util.Locale.ROOT));
        m.setNombre(m.getNombre().trim());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Map<String, String>> error(ResponseStatusException exception) {
        return org.springframework.http.ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("message", exception.getReason()));
    }
}
