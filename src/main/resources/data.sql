-- Tarifas iniciales por tipo de vehiculo
INSERT INTO tarifas (tipo_vehiculo, valor_minuto, tope_dia, minutos_gracia) VALUES ('CARRO', 120, 25000, 5);
INSERT INTO tarifas (tipo_vehiculo, valor_minuto, tope_dia, minutos_gracia) VALUES ('MOTO', 70, 15000, 5);
INSERT INTO tarifas (tipo_vehiculo, valor_minuto, tope_dia, minutos_gracia) VALUES ('BICICLETA', 30, 6000, 10);

-- Cupos de carro: C-01 .. C-10
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-01', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-02', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-03', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-04', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-05', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-06', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-07', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-08', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-09', 'CARRO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('C-10', 'CARRO', false, 0);

-- Cupos de moto: M-01 .. M-08
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-01', 'MOTO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-02', 'MOTO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-03', 'MOTO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-04', 'MOTO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-05', 'MOTO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-06', 'MOTO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-07', 'MOTO', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('M-08', 'MOTO', false, 0);

-- Cupos de bicicleta: B-01 .. B-05
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('B-01', 'BICICLETA', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('B-02', 'BICICLETA', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('B-03', 'BICICLETA', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('B-04', 'BICICLETA', false, 0);
INSERT INTO cupos (codigo, tipo_vehiculo, ocupado, version) VALUES ('B-05', 'BICICLETA', false, 0);
