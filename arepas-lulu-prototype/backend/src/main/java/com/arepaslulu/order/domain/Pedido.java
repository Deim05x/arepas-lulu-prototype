package com.arepaslulu.order.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mesa_numero", nullable = false)
    private Integer mesaNumero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 30)
    private TipoServicio tipoServicio = TipoServicio.MESA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoPedido estado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PedidoItem> items = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Integer mesaNumero, TipoServicio tipoServicio, EstadoPedido estado) {
        this.mesaNumero = mesaNumero;
        this.tipoServicio = tipoServicio == null ? TipoServicio.MESA : tipoServicio;
        this.estado = estado;
    }

    public Pedido(Integer mesaNumero, EstadoPedido estado) {
        this(mesaNumero, TipoServicio.MESA, estado);
    }

    @PrePersist
    void onCreate() {
        if (tipoServicio == null) tipoServicio = TipoServicio.MESA;
        this.createdAt = Instant.now();
    }

    public void agregarItem(PedidoItem item) {
        item.asociarPedido(this);
        items.add(item);
        total = total.add(item.getSubtotal());
    }

    public void cambiarEstado(EstadoPedido nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public boolean esDeMesa() {
        return tipoServicio == TipoServicio.MESA && mesaNumero != null && mesaNumero > 0;
    }

    public Long getId() { return id; }
    public Integer getMesaNumero() { return mesaNumero; }
    public TipoServicio getTipoServicio() { return tipoServicio; }
    public EstadoPedido getEstado() { return estado; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public List<PedidoItem> getItems() { return Collections.unmodifiableList(items); }
}
