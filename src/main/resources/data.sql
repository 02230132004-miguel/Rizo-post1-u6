-- Datos iniciales para pruebas del Sistema de Gestion de Pedidos

-- 1. Inventario de productos (101, 102, 103 con stock suficiente)
INSERT INTO inventario (producto_id, stock) VALUES (101, 100);
INSERT INTO inventario (producto_id, stock) VALUES (102, 100);
INSERT INTO inventario (producto_id, stock) VALUES (103, 100);

-- 2. Clientes
-- Cliente 1: VIP
INSERT INTO clientes (id, tipo_cliente, nit) VALUES (1, 'VIP', NULL);
-- Cliente 2: FRECUENTE (con historial de pedidos previos)
INSERT INTO clientes (id, tipo_cliente, nit) VALUES (2, 'FRECUENTE', NULL);
-- Cliente 3: MOROSO (con factura pendiente)
INSERT INTO clientes (id, tipo_cliente, nit) VALUES (3, 'ESTANDAR', NULL);
-- Cliente 4: ESTANDAR
INSERT INTO clientes (id, tipo_cliente, nit) VALUES (4, 'ESTANDAR', NULL);
-- Cliente 5: Cliente con NIT empresarial
INSERT INTO clientes (id, tipo_cliente, nit) VALUES (5, 'ESTANDAR', '900123456-7');

-- 3. Facturas
-- Cliente 3 tiene una factura sin pagar
INSERT INTO facturas (cliente_id, monto, pagada) VALUES (3, 250000.0, FALSE);
-- Cliente 1 tiene su factura al dia
INSERT INTO facturas (cliente_id, monto, pagada) VALUES (1, 100000.0, TRUE);

-- 4. Pedidos historicos para Cliente 2 (12 pedidos completados para verificar escala de descuento > 10 pedidos)
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
INSERT INTO pedidos (cliente_id, subtotal, descuento, impuesto, total, fecha, estado) VALUES (2, 50000.0, 0.0, 9500.0, 59500.0, CURRENT_TIMESTAMP, 'COMPLETADO');
