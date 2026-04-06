# QTengo — Aplicación de Gestión

## Descripción general

QTengo es una aplicación Android de gestión empresarial y familiar (ERP) desarrollada en **Kotlin** con **Jetpack Compose**. Permite gestionar diferentes perfiles (Familiar, Pyme y Restauración) desde una misma app con autenticación centralizada.

---

## Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material3 |
| Autenticación | Firebase Authentication |
| Base de datos | Firebase Firestore |
| Arquitectura | MVVM (ViewModel + StateFlow) |
| Red | Retrofit 2 + OkHttp |
| Procesador de anotaciones | KSP 2.1.0 |

---

## Estructura del proyecto

```
com.example.qtengo
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   ├── familiar/
│   │   ├── compra/                        ✅ Conectado a Firestore
│   │   │   ├── ShoppingListViewModel.kt
│   │   │   ├── ShoppingListScreen.kt
│   │   │   ├── ShoppingListDetailScreen.kt
│   │   │   ├── ShoppingListCard.kt
│   │   │   ├── ShoppingItemCard.kt
│   │   │   ├── NuevaListaDialog.kt
│   │   │   └── NuevoItemDialog.kt
│   │   ├── gastos/                        ✅ Conectado a Firestore
│   │   │   ├── GastosViewModel.kt
│   │   │   ├── GastosScreen.kt
│   │   │   ├── GastosResumenCard.kt
│   │   │   ├── GastosFijosSection.kt
│   │   │   ├── GastosPuntualesSection.kt
│   │   │   ├── EditarGastoDialog.kt
│   │   │   ├── GastoRecurrenteDialog.kt
│   │   │   └── AddGastoScreen.kt
│   │   ├── tareas/                        ✅ Conectado a Firestore
│   │   │   ├── TareasViewModel.kt
│   │   │   ├── TareasScreen.kt
│   │   │   ├── TareaCard.kt
│   │   │   ├── NuevaTareaDialog.kt
│   │   │   └── EditarTareaDialog.kt
│   │   ├── FamiliarHomeScreen.kt
│   │   ├── InventarioScreen.kt            ⏳ Conectado a Firestore (pendiente refactorizar)
│   │   ├── AddInventarioScreen.kt
│   │   └── InventarioViewModel.kt
│   ├── screens/
│   │   ├── PymeHomeScreen.kt              ⏳ Pendiente de desarrollar
│   │   ├── PymeFinanceScreen.kt
│   │   ├── RestauracionHomeScreen.kt      ⏳ Pendiente de desarrollar
│   │   ├── EmployeeScreen.kt
│   │   ├── SupplierScreen.kt
│   │   └── SplashScreen.kt
│   ├── products/
│   │   └── ProductScreen.kt
│   └── theme/
│       └── QtengoTheme.kt
└── MainActivity.kt
```

---

## Configuración Firebase

### Servicios usados
- **Authentication** — Registro e inicio de sesión con email/contraseña
- **Firestore** — Base de datos en la nube

### Estructura de Firestore

```
usuarios/
  {uid}/
    - nombre
    - apellido1
    - apellido2
    - email
    - perfil              → "Familiar" | "Pyme" | "Restauración"
    - presupuestoMensual  → Double (módulo gastos)

    listas/               → Lista de la compra
      {listaId}/
        - name
        - itemCount
        - date
        productos/
          {productoId}/
            - name
            - quantity
            - isChecked

    inventario/           → Inventario del hogar
      {itemId}/
        - nombre
        - cantidad
        - ubicacion
        - minStock
        - notas
        - fechaCaducidad (opcional)

    gastos/               → Control de gastos (puntuales)
      {gastoId}/
        - descripcion
        - cantidad
        - categoria
        - tipo            → "GASTO" | "INGRESO"
        - fecha
        - origen          → "manual" | "lista_compra"
        - listaId         (opcional)

    gastosRecurrentes/    → Gastos fijos mensuales
      {gastoId}/
        - descripcion
        - cantidad
        - categoria
        - fechaCobro

    tareas/               → Tareas y recordatorios
      {tareaId}/
        - titulo
        - descripcion
        - fecha
        - completada
        - prioridad       → "Alta" | "Media" | "Baja"
```

### Reglas de seguridad Firestore

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /usuarios/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

---

## Perfiles de usuario

| Perfil | Módulos disponibles |
|---|---|
| **Familiar** | Lista de la compra, Control de gastos, Inventario del hogar, Tareas y recordatorios |
| **Pyme** | Productos/Stock, Gastos e ingresos, Proveedores, Empleados, Agenda de tareas |
| **Restauración** | Stock de cocina, Proveedores |

---

## Estado actual del módulo Familiar

### ✅ Lista de la compra — COMPLETADO
- Crear y eliminar listas
- Añadir y eliminar productos
- Marcar/desmarcar productos como comprados
- Barra de progreso
- Banner de lista completada con registro de gasto vinculado
- Refactorizado en `ui/familiar/compra/`

### ✅ Control de gastos — COMPLETADO
- Gastos puntuales con categorías
- Gastos fijos mensuales con fecha de cobro
- Presupuesto mensual con barra de progreso (verde/naranja/rojo)
- Total estimado mensual (puntuales + fijos) reactivo
- Editar y eliminar gastos
- Vinculación con lista de la compra
- Refactorizado en `ui/familiar/gastos/`

### ✅ Inventario del hogar — CONECTADO (pendiente refactorizar)
- Añadir y eliminar artículos
- Campos: nombre, cantidad, ubicación, stock mínimo, notas, fecha caducidad (opcional)
- Aviso visual de stock bajo
- Conectado a Firestore
- **Pendiente:** refactorizar en `ui/familiar/inventario/` siguiendo el patrón de compra y gastos

### ✅ Tareas y recordatorios — COMPLETADO
- Añadir, editar y eliminar tareas
- Prioridades: Alta (rojo), Media (naranja), Baja (verde)
- Indicador visual de prioridad en cada tarjeta
- Separación entre tareas pendientes y completadas
- Resumen: pendientes, completadas, urgentes
- Conectado a Firestore
- Refactorizado en `ui/familiar/tareas/`

---

## Pendiente

### Módulo Familiar
- [ ] Refactorizar Inventario del hogar en `ui/familiar/inventario/`

### Módulo Pyme
- [ ] Conectar Productos/Stock a Firestore
- [ ] Conectar Gastos e ingresos a Firestore
- [ ] Conectar Proveedores a Firestore
- [ ] Conectar Empleados a Firestore
- [ ] Conectar Agenda de tareas a Firestore

### Módulo Restauración
- [ ] Conectar Stock de cocina a Firestore
- [ ] Conectar Proveedores a Firestore

---

## Buenas prácticas aplicadas

- Cada módulo tiene su propio package con archivos separados por responsabilidad
- Los ViewModels siguen el patrón de `ShoppingListViewModel` como referencia
- Los totales reactivos se calculan desde los `StateFlow` directamente en la UI
- Los datos de cada usuario están aislados en Firestore bajo su `uid`
- Se usa `addSnapshotListener` para actualizaciones en tiempo real

---

## Notas para retomar el trabajo

- Seguir el patrón de `ui/familiar/compra/` para nuevos módulos
- Para nuevas pantallas: crear archivo → añadir ViewModel → registrar en `MainActivity.kt` → añadir opción en HomeScreen
- Los comentarios en los métodos del ViewModel son obligatorios
- Usar `Icons.Default.*` de Material3 en lugar de emojis para los iconos
