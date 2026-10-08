ALTER TABLE pedidos ADD COLUMN tipo_servicio VARCHAR(30) NOT NULL DEFAULT 'MESA';

CREATE TABLE pagos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    medio VARCHAR(30) NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    cambio DECIMAL(12,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pago_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_pago_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
);

CREATE TABLE facturas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    pago_id BIGINT NOT NULL,
    numero VARCHAR(40) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    reportada_dian BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL,
    reported_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_factura_pedido UNIQUE (pedido_id),
    CONSTRAINT uk_factura_pago UNIQUE (pago_id),
    CONSTRAINT uk_factura_numero UNIQUE (numero),
    CONSTRAINT fk_factura_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    CONSTRAINT fk_factura_pago FOREIGN KEY (pago_id) REFERENCES pagos(id)
);

CREATE TABLE clientes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(40) NOT NULL,
    direccion VARCHAR(250) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_cliente_telefono (telefono)
);

CREATE TABLE domicilios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    direccion VARCHAR(250) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    repartidor VARCHAR(120) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    delivered_at TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_domicilio_pedido UNIQUE (pedido_id),
    CONSTRAINT fk_domicilio_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    CONSTRAINT fk_domicilio_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE ingredientes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    unidad VARCHAR(30) NOT NULL,
    stock_actual DECIMAL(12,3) NOT NULL,
    stock_minimo DECIMAL(12,3) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ingrediente_nombre UNIQUE (nombre)
);

CREATE TABLE recetas (
    producto_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    cantidad DECIMAL(12,3) NOT NULL,
    PRIMARY KEY (producto_id, ingrediente_id),
    CONSTRAINT fk_receta_producto FOREIGN KEY (producto_id) REFERENCES productos(id),
    CONSTRAINT fk_receta_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id)
);

CREATE TABLE mesas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    numero INT NOT NULL,
    capacidad INT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    referencia VARCHAR(150) NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_mesa_numero UNIQUE (numero)
);

INSERT INTO mesas(numero,capacidad,estado,updated_at) VALUES
(1,4,'DISPONIBLE',CURRENT_TIMESTAMP),(2,4,'DISPONIBLE',CURRENT_TIMESTAMP),(3,4,'DISPONIBLE',CURRENT_TIMESTAMP),
(4,4,'DISPONIBLE',CURRENT_TIMESTAMP),(5,4,'DISPONIBLE',CURRENT_TIMESTAMP),(6,4,'DISPONIBLE',CURRENT_TIMESTAMP),
(7,4,'DISPONIBLE',CURRENT_TIMESTAMP),(8,4,'DISPONIBLE',CURRENT_TIMESTAMP),(9,6,'DISPONIBLE',CURRENT_TIMESTAMP),
(10,6,'DISPONIBLE',CURRENT_TIMESTAMP),(11,6,'DISPONIBLE',CURRENT_TIMESTAMP),(12,6,'DISPONIBLE',CURRENT_TIMESTAMP);

INSERT INTO ingredientes(nombre,unidad,stock_actual,stock_minimo,updated_at) VALUES
('Masa de maíz','g',20000,3000,CURRENT_TIMESTAMP),
('Queso','g',8000,1200,CURRENT_TIMESTAMP),
('Carne','g',6000,1000,CURRENT_TIMESTAMP),
('Pollo','g',6000,1000,CURRENT_TIMESTAMP),
('Bebidas','unidad',120,20,CURRENT_TIMESTAMP);
