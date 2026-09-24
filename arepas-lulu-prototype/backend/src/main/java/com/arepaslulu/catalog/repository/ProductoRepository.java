package com.arepaslulu.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arepaslulu.catalog.domain.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findAllByOrderByNombreAsc();
    List<Producto> findByActivoTrueAndDisponibleTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
