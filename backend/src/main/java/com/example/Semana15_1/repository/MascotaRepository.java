package com.example.Semana15_1.repository;

import com.example.Semana15_1.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    boolean existsByCodigo(String codigo);
}
