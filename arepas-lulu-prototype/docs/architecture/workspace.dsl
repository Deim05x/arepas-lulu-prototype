workspace "Arepas Lulú" "Prototipo F-01 y F-02" {
    model {
        administrador = person "Administrador / Cajero" "Mantiene catálogo y precios."
        mesero = person "Mesero" "Registra pedidos digitales por mesa."

        system = softwareSystem "Arepas Lulú" "Sistema de gestión del restaurante" {
            web = container "Web App" "SPA para catálogo y pedidos" "React 19.3 + Vite 8.3"
            api = container "API" "Reglas de negocio y API REST" "Spring Boot 4.1.1 / Java 21" {
                productController = component "ProductoController" "Expone F-01 por HTTP" "Spring MVC"
                productService = component "ProductoService" "Casos de uso del catálogo" "Service Layer"
                orderController = component "PedidoController" "Expone F-02 por HTTP" "Spring MVC"
                orderService = component "PedidoService" "Casos de uso del pedido" "Service Layer"
                orderFactory = component "PedidoFactory" "Construye pedidos consistentes" "Factory"
                pricing = component "PricingStrategy" "Calcula subtotales" "Strategy"
                persistence = component "Repositories" "Acceso a persistencia" "Spring Data JPA"
            }
            database = container "Base de datos" "Catálogo, pedidos e ítems" "MariaDB 12.3.3" "Database"
        }

        administrador -> web "Administra catálogo" "HTTPS"
        mesero -> web "Registra pedidos" "HTTPS"
        web -> api "Consume API" "REST/JSON"
        api -> database "Lee y escribe" "JPA/JDBC"

        productController -> productService "Invoca"
        productService -> persistence "Usa"
        orderController -> orderService "Invoca"
        orderService -> orderFactory "Construye pedido"
        orderFactory -> pricing "Calcula subtotal"
        orderService -> persistence "Usa"
    }

    views {
        systemContext system "Context" {
            include *
            autolayout lr
        }
        container system "Containers" {
            include *
            autolayout lr
        }
        component api "ApiComponents" {
            include *
            autolayout lr
        }
        styles {
            element "Person" { shape person }
            element "Database" { shape cylinder }
        }
    }
}
