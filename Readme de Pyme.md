# Módulo Pyme - Documentación Completa de Ventanas y Funcionalidades

Este documento detalla todas las pantallas del módulo Pyme, sus funciones principales y las mejoras clave implementadas para optimizar la gestión del negocio.

---

## 📊 1. Ventana de Finanzas (Gastos e Ingresos)
Esta pantalla centraliza el control económico de la empresa.

- **Resumen Visual:** Tarjetas superiores con el total de Ingresos (verde), Gastos (rojo) y el Balance Neto (azul) calculado en tiempo real.
- **Registro de Movimientos:** Botones rápidos para añadir nuevos ingresos o gastos mediante un diálogo emergente.
- **Sistema de Ordenación Inteligente (Mejorado):**
    - Al ordenar por **Cantidad**, el sistema ahora reconoce los gastos como valores negativos.
    - Esto permite que los gastos de mayor importe aparezcan primero en orden ascendente (al ser los valores más bajos), facilitando el control de las salidas de dinero más importantes.
- **Filtrado:** Buscador por concepto para localizar rápidamente cualquier movimiento pasado.

## 📦 2. Ventana de Control de Stock (Almacén)
Diseñada para una gestión de inventario ágil y sin errores.

- **Indicadores de Estado:** Resumen del total de ítems y una alerta destacada con el número de productos con "Stock Bajo".
- **Ficha de Producto Compacta (Rediseñada):**
    - **ID Automático:** Cada producto recibe un identificador único (ej: `001L`, `002U`) basado en un contador incremental y la inicial de su unidad de medida. Se genera solo al crear y se mantiene permanentemente.
    - **Información Unificada:** El ID y el Stock Mínimo aparecen en la misma línea que los controles de cantidad, optimizando el espacio visual.
    - **Alertas Visuales:** Los productos por debajo del stock mínimo se resaltan con un fondo claro y un icono de advertencia (⚠️).
- **Controles de Cantidad:** Botones `+` y `-` para ajustes rápidos de una unidad, o pulsación en el número para introducir una cantidad exacta manualmente.
- **Ordenación por Defecto:** La lista se muestra inicialmente por **Orden de Registro** (los últimos creados primero/últimos según preferencia), con opción de cambiar a orden alfabético o por cantidad.

## 👥 3. Ventana de Gestión de Empleados
Permite administrar el equipo humano de la empresa.

- **Listado de Personal:** Visualización de todos los empleados registrados con su cargo y detalles básicos.
- **Edición y Registro:** Capacidad para añadir nuevos miembros del equipo o modificar los datos de los existentes a través de un diálogo dedicado.
- **Borrado Seguro:** Opción para eliminar registros de personal que ya no formen parte de la plantilla.

## 📋 4. Ventana de Tareas
Herramienta de organización para el seguimiento de actividades pendientes.

- **Asignación de Labores:** Creación de tareas con descripción y estado.
- **Seguimiento:** Marcado de tareas como completadas o pendientes.
- **Priorización:** Permite mantener el flujo de trabajo bajo control delegando o registrando los pasos a seguir en la operación diaria.

## 🚛 5. Ventana de Proveedores
Gestión del contacto y relación con suministradores.

- **Base de Datos de Contacto:** Registro de proveedores con su nombre, categoría de productos que suministran y datos de contacto.
- **Localización Rápida:** Filtros para encontrar proveedores específicos según la necesidad del momento.

## 🔍 6. Sistema de Filtros Unificado
Todas las ventanas del módulo Pyme comparten una lógica de filtrado coherente:

- **Buscador Dinámico:** Filtra los resultados en tiempo real mientras escribes.
- **Botones de Ordenación:** Ubicados en tarjetas expandibles para no saturar la pantalla.
- **Desmarcado Inteligente:** Si pulsas de nuevo sobre el criterio de ordenación activo, el sistema "desmarca" el filtro y vuelve automáticamente al **Orden de Registro**, manteniendo la lista tal cual fue creada.

---
