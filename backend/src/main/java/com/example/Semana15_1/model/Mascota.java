package com.example.Semana15_1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mascotas")
@Getter
@Setter
@NoArgsConstructor
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String especie;
    private String raza;
    private int edadMeses;
    private String sexo;
    private String tamanio;
    @Column(length = 1000)
    private String descripcion;
    private boolean vacunada;
    private boolean esterilizada;
    @Column(nullable = false)
    private String estado;

    public Mascota(String codigo, String nombre, String especie, String raza, int edadMeses,
                   String sexo, String tamanio, String descripcion, boolean vacunada, boolean esterilizada) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.edadMeses = edadMeses;
        this.sexo = sexo;
        this.tamanio = tamanio;
        this.descripcion = descripcion;
        this.vacunada = vacunada;
        this.esterilizada = esterilizada;
        this.estado = "DISPONIBLE";
    }
}
