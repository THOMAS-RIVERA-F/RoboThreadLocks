# KarelJ: Simulación Concurrente de Robots

¡Bienvenido a **KareJ**, un simulador de robots que utiliza hilos, concurrencia y bloqueos para gestionar el tráfico de robots en un entorno complejo! 🚗🤖

## Descripción del Proyecto

Este proyecto simula el movimiento de varios robots a través de una ciudad con varias "paradas" en las cuales se regulan los flujos de tráfico mediante semáforos y bloqueos (locks). Utilizamos hilos para mover los robots de manera simultánea, y mecanismos de concurrencia como semáforos, locks y condiciones para evitar colisiones y gestionar rutas aleatorias.

La simulación se desarrolla en un entorno de "parqueadero" inicial, desde donde los robots deben moverse, completar diferentes rutas y recoger beepers (objetos) en paradas definidas.

### Características Principales:
- **Manejo de concurrencia:** Uso de semáforos y locks para controlar el acceso de múltiples robots a las mismas posiciones.
- **Asignación aleatoria de rutas:** Los robots siguen rutas aleatorias que los llevan a distintas paradas.
- **Simulación de tráfico:** Los robots deben esperar su turno para moverse, asegurándose de que no ocupen la misma posición simultáneamente.
- **Regreso y reinicio automático:** Los robots completan sus rutas y regresan al punto de inicio para repetir el ciclo.
  
## Dependencias

Este proyecto se basa en la librería **Karel the Robot**, un marco simple que simula robots en un ambiente 2D. Para ejecutar el proyecto, asegúrate de tener instalada la librería y cualquier otra dependencia que necesite.

```bash
javac -cp .:kareltherobot.jar Simulacion.java
java -cp .:kareltherobot.jar Simulacion
