$ErrorActionPreference = 'Stop'

$Api = if ($env:API_URL) { $env:API_URL } else { 'http://localhost:8080/api' }

function Invoke-JsonRequest {
    param(
        [string]$Method,
        [string]$Uri,
        [object]$Body
    )

    $parameters = @{ Method = $Method; Uri = $Uri; ContentType = 'application/json' }
    if ($null -ne $Body) {
        $parameters.Body = $Body | ConvertTo-Json -Depth 10
    }
    Invoke-RestMethod @parameters
}

Write-Host '[1/6] Creando producto de prueba F-01...'
$product = Invoke-JsonRequest -Method Post -Uri "$Api/productos" -Body @{
    nombre = 'Arepa Todo Terreno Smoke PS'
    categoria = 'Arepas'
    descripcion = 'Producto para prueba de humo'
    precio = 18000
    disponible = $true
}
$productId = $product.id
Write-Host "Producto creado: $productId"

Write-Host '[2/6] Actualizando precio...'
Invoke-JsonRequest -Method Put -Uri "$Api/productos/$productId" -Body @{
    nombre = 'Arepa Todo Terreno Smoke PS'
    categoria = 'Arepas'
    descripcion = 'Producto para prueba de humo'
    precio = 19000
    disponible = $true
} | Out-Null

Write-Host '[3/6] Creando pedido de mesa F-02...'
$order = Invoke-JsonRequest -Method Post -Uri "$Api/pedidos" -Body @{
    mesaNumero = 4
    items = @(@{ productoId = $productId; cantidad = 2; observacion = 'Sin cebolla' })
}
if ($order.estado -ne 'ENVIADO_COCINA') { throw "Estado inesperado: $($order.estado)" }
Write-Host "Pedido creado: $($order.id) ($($order.estado))"

Write-Host '[4/6] Consultando pedido por ID...'
Invoke-RestMethod -Method Get -Uri "$Api/pedidos/$($order.id)" | Out-Null

Write-Host '[5/6] Consultando historial de mesa 4...'
$history = Invoke-RestMethod -Method Get -Uri "$Api/pedidos?mesa=4"
if (@($history).Count -lt 1) { throw 'El historial de la mesa no contiene el pedido creado.' }

Write-Host '[6/6] Marcando producto agotado y comprobando bloqueo...'
Invoke-JsonRequest -Method Patch -Uri "$Api/productos/$productId/disponibilidad" -Body @{ disponible = $false } | Out-Null
try {
    Invoke-JsonRequest -Method Post -Uri "$Api/pedidos" -Body @{
        mesaNumero = 5
        items = @(@{ productoId = $productId; cantidad = 1; observacion = $null })
    } | Out-Null
    throw 'Se esperaba HTTP 409 al pedir un producto no disponible.'
} catch {
    if ($_.Exception.Response.StatusCode.value__ -ne 409) { throw }
}

Write-Host 'OK: F-01 y F-02 superaron la prueba de humo.'