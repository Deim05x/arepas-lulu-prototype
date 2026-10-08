workspace "Arepas Lulú" "C4 actualizado para F-01 a F-08" {
  model {
    admin = person "Administrador / Cajero" "Gestiona catálogo, pagos, caja, inventario y mesas."
    mesero = person "Mesero" "Registra pedidos digitales por mesa."
    cocina = person "Cocina" "Gestiona la cola KDS y preparación."
    domiciliario = person "Domiciliario" "Gestiona despacho y entrega."

    sistema = softwareSystem "Arepas Lulú" "Sistema operativo integral del restaurante." {
      web = container "Web App" "SPA operativa F-01 a F-08" "React, Vite, Nginx"
      api = container "API" "Reglas de negocio y casos de uso" "Spring Boot, Java 21" {
        catalog = component "Catalog" "F-01: productos, precios y disponibilidad"
        order = component "Order" "F-02: pedido digital y agregado transaccional"
        kitchen = component "Kitchen" "F-03: cola de preparación"
        billing = component "Billing" "F-04: pago y factura"
        reporting = component "Reporting" "F-05: caja y estado DIAN"
        delivery = component "Delivery" "F-06: clientes y domicilios"
        inventory = component "Inventory" "F-07: ingredientes y recetas"
        tables = component "Table" "F-08: ocupación y reservas"
      }
      db = container "Base de datos" "Persistencia transaccional" "MariaDB"
    }

    admin -> web "Opera"
    mesero -> web "Registra pedidos"
    cocina -> web "Actualiza preparación"
    domiciliario -> web "Actualiza entregas"
    web -> api "REST/JSON; SSE catálogo"
    api -> db "JPA / JDBC"

    order -> catalog "Valida productos/precios"
    order -> inventory "Valida y consume receta"
    order -> tables "Ocupa mesa"
    kitchen -> order "Cambia estado"
    billing -> order "Valida y cierra pedido"
    billing -> tables "Libera mesa"
    reporting -> billing "Agrega pagos/facturas"
    delivery -> order "Crea pedido domicilio"
  }

  views {
    systemContext sistema "Context" { include *; autoLayout lr }
    container sistema "Containers" { include *; autoLayout lr }
    component api "ApiComponents" { include *; autoLayout lr }
    styles {
      element "Person" { shape person; background #8f2f1f; color #ffffff }
      element "Software System" { background #5d2f20; color #ffffff }
      element "Container" { background #c65d32; color #ffffff }
      element "Component" { background #e6a63b; color #2a211c }
    }
  }
}
