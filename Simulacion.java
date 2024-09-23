import kareltherobot.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.Semaphore;

public class Simulacion implements Directions {
    private static final Semaphore semaforoParada1 = new Semaphore(8);  // Parada 1: máximo 8 robots
    private static final Semaphore semaforoParada2 = new Semaphore(9);  // Parada 2: máximo 9 robots
    private static final Semaphore semaforoParada3 = new Semaphore(1);  // Parada 3: máximo 1 robot
    private static final Semaphore semaforoParada4 = new Semaphore(1);  // Parada 4: máximo 4 robots


    private static final Lock salidaLock = new ReentrantLock();  // Lock para la salida de los robots
    private static final List<String> posicionesOcupadas = Collections.synchronizedList(new ArrayList<>());
    private static final Lock posicionLock = new ReentrantLock();
    private static final Condition posicionDisponible = posicionLock.newCondition();

    public static void main(String[] args) {


        // Configuración inicial del mundo
        World.readWorld("PracticaOperativos.kwld");
        World.setVisible(true);
        World.showSpeedControl(true, true); //Needed to make them start

// Crear 8 robots en el parqueadero en diferentes posiciones
        RobotOp[] robots = new RobotOp[8];  // Definimos un array de tamaño 8 para los 8 robots

// Primeros 6 robots en las posiciones desde (2, 12) hasta (7, 12)
        for (int i = 0; i < 6; i++) {
            robots[i] = new RobotOp(i + 2, 12, East, 0);  // Posiciones en el parqueadero mirando al este
            String posicionInicial = robots[i].getPosition();
            posicionesOcupadas.add(posicionInicial);
        }

// Los dos robots adicionales en las posiciones (4, 18) y (5, 18) mirando hacia el oeste
        robots[6] = new RobotOp(4, 18, West, 0);  // Robot en (4, 18) mirando hacia el oeste
        String posicionInicial6 = robots[6].getPosition();
        posicionesOcupadas.add(posicionInicial6);

        robots[7] = new RobotOp(5, 18, West, 0);  // Robot en (5, 18) mirando hacia el oeste
        String posicionInicial7 = robots[7].getPosition();
        posicionesOcupadas.add(posicionInicial7);


        // Simular la salida uno por uno
        for (RobotOp robot : robots) {
            new Thread(() -> {
                moverRobotFueraDelParqueadero(robot);
                robot.setFueraDelParqueadero(true);
                while (hayBeepersEnPosicion(robot, 8, 19)) {
                    moverRobotACalle8(robot);
                    asignarRutaAleatoria(robot);
                    Regreso(robot);
                }
            }).start();
        }
    }

    public static void esperarHastaQuePosicionEsteDisponible(String posicion) throws InterruptedException {
        posicionLock.lock();
        try {
            while (posicionesOcupadas.contains(posicion)) {
                posicionDisponible.await();  // Esperar hasta que la posición esté disponible
            }
            posicionesOcupadas.add(posicion);  // Marcar la posición como ocupada
        } finally {
            posicionLock.unlock();
        }
    }


    public static void notificarPosicionDisponible(String posicion) {
        posicionLock.lock();
        try {
            if (posicionesOcupadas.contains(posicion)) {
                posicionesOcupadas.remove(posicion);  // Liberar la posición
                posicionDisponible.signalAll();  // Notificar a todos los hilos que la posición está disponible
            }
        } finally {
            posicionLock.unlock();
        }
    }

    public static boolean hayBeepersEnPosicion(RobotOp robot, int street, int avenue) {
        moverRobotAPosicion(robot, street, avenue);
        return robot.nextToABeeper();
    }

    public static void moverRobotAAvenida17(RobotOp robot) {
        // Mover el robot hacia la avenida 17
        while (robot.getAvenue() < 17) {
            avanzarSiPosicionLibre(robot);
        }
    }

    public static void Regreso(RobotOp robot) {
        if (robot.getStreet() == 17 || robot.getAvenue() == 6) {
            Camino3(robot);
        } else if (robot.getStreet() == 11 || robot.getAvenue() == 8) {
            Camino2(robot);
            Camino3(robot);
        } else if (robot.getStreet() == 7 || robot.getAvenue() == 9) {
            retorno3(robot);
        } else if (robot.getStreet() == 19 || robot.getAvenue() == 18) {
            retorno4(robot);
            Camino3(robot);
        }
    }

    public static void retorno1(RobotOp robot) {
        moverRobotAPosicion(robot, 15, 6);
        robot.turnLeft();
        moverRobotAPosicion(robot, 18, 6);
        robot.turnRight();
    }

    public static void retorno2(RobotOp robot) {
        moverRobotAPosicion(robot, 12, 9);
        robot.turnLeft();
        moverRobotAPosicion(robot, 12, 7);
        robot.turnLeft();
        moverRobotAPosicion(robot, 10, 7);
        robot.turnRight();
    }

    public static void retorno3(RobotOp robot) {
        moverRobotAPosicion(robot, 8, 8);
        robot.turnLeft();
        robot.move();
        robot.move();
        robot.turnLeft();
    }

    public static void retorno4(RobotOp robot) {
        if (robot.facingWest()) {
            robot.turnLeft();
            robot.turnLeft();
        }
        moverRobotAPosicion(robot, 11, 15);
        robot.turnRight();
        avanzarSiPosicionLibre(robot);
        robot.turnRight();
        semaforoParada4.release();
    }


    public static void asignarRutaAleatoria(RobotOp robot) {
        Random random = new Random();
        int rutaSeleccionada = random.nextInt(4) + 1;  // Generar un número entre 1 y 4
        switch (rutaSeleccionada) {
            case 1:
                RutaAParada1(robot);
                break;
            case 2:
                RutaAParada2(robot);
                break;
            case 3:
                RutaAParada3(robot);
                break;
            case 4:
                RutaAParada4(robot);
                break;
        }
    }

    public static void Camino1(RobotOp robot) {
        moverRobotAPosicion(robot, 9, 6);
        robot.turnRight();
    }

    public static void Camino2(RobotOp robot) {
        moverRobotAPosicion(robot, 9, 10);
        robot.turnRight();
    }

    public static void Camino3(RobotOp robot) {
        moverRobotAPosicion(robot, 10, 10);
        robot.turnRight();
    }

    public static void RutaAParada1(RobotOp robot) {
        // Usar el método que recorre la parada
        try {

            moverRobotAPosicion(robot, 18, 6);  // Moverse a la posición inicial
            semaforoParada1.acquire();
            robot.turnRight();
            robot.move();
            robot.move();
            robot.turnRight();
            robot.move();
            robot.turnRight();
            robot.move();
            robot.turnLeft();
            robot.move();
            robot.putBeeper();
            retorno1(robot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforoParada1.release();
        }
    }

    public static void RutaAParada2(RobotOp robot) {
        try {

            moverRobotAPosicion(robot, 10, 7);
            semaforoParada2.acquire();
            robot.turnRight();
            robot.move();
            robot.move();
            robot.turnRight();
            robot.move();
            robot.turnRight();
            robot.move();
            robot.turnLeft();
            robot.move();
            robot.putBeeper();
            retorno2(robot);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforoParada2.release();
        }


    }

    public static void RutaAParada3(RobotOp robot) {
        try {

            Camino1(robot);
            moverRobotAPosicion(robot, 6, 7);
            semaforoParada3.acquire();
            robot.move();
            robot.turnLeft();
            robot.move();
            robot.move();
            robot.turnRight();
            robot.move();
            robot.turnRight();
            robot.move();
            robot.putBeeper();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }finally {
            semaforoParada3.release();
        }
    }

    public static void RutaAParada4(RobotOp robot) {
        try {

            moverRobotAPosicion(robot, 10, 16);
            semaforoParada4.acquire();
            robot.move();
            robot.turnRight();
            robot.move();
            robot.turnLeft();
            moverRobotAPosicion(robot, 15, 15);
            robot.turnRight();
            moverRobotAPosicion(robot, 16, 12);
            robot.turnRight();
            moverRobotAPosicion(robot, 19, 18);
            robot.putBeeper();
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void moverRobotAPosicion(RobotOp robot, int targetStreet, int targetAvenue) {
        while (robot.getStreet() != targetStreet || robot.getAvenue() != targetAvenue) {
            avanzarSiPosicionLibre(robot);
        }
        System.out.println("Robot llegó a la posición (" + targetStreet + ", " + targetAvenue + ")");
    }


    public static void moverRobotACalle8(RobotOp robot) {
        moverRobotAPosicion(robot, 8, 19);
        robot.pickBeeper();
    }

    // Método para mover el robot hacia la calle 3 desde la avenida 17
    public static void moverRobotACalle3(RobotOp robot) {
        // Si el robot está en una calle superior a la 3, moverse hacia el norte
        if (robot.getStreet() > 3) {
            if (robot.facingWest()) {
                robot.turnLeft();
            } else {
                robot.turnRight();
            }
            while (robot.getStreet() > 3) {
                avanzarSiPosicionLibre(robot);
            }
            robot.turnLeft();
        }
        // Si el robot está en una calle inferior a la 3, moverse hacia el sur
        else if (robot.getStreet() < 3) {
            robot.turnLeft();
            avanzarSiPosicionLibre(robot);  // Moverse solo si la posición está libre
            robot.turnRight();
            avanzarSiPosicionLibre(robot);
        }
    }

    // Método para mover el robot hacia la puerta en (3, 18)
    public static void moverRobotFueraDelParqueadero(RobotOp robot) {
        try {
            salidaLock.lock();
            if (robot.getPosition().equals("(4, 18)") || robot.getPosition().equals("(5, 18)")) {
                robot.move();
                robot.turnLeft();
                moverRobotAAvenida17(robot);
                robot.turnLeft();
                moverRobotACalle3(robot);
            }
            moverRobotAAvenida17(robot);
            moverRobotACalle3(robot);
        } finally {
            salidaLock.unlock();
        }
    }

    public static void avanzarSiPosicionLibre(RobotOp robot) {
        String proximaPosicion = calcularProximaPosicion(robot);

        try {
            // Verificar si el robot puede avanzar físicamente
            if (robot.frontIsClear()) {
                esperarHastaQuePosicionEsteDisponible(proximaPosicion);  // Esperar hasta que la posición esté libre
                robot.move();
            } else {
                robot.turnLeft();  // Intentar girar si no puede avanzar
                proximaPosicion = calcularProximaPosicion(robot);
                esperarHastaQuePosicionEsteDisponible(proximaPosicion);  // Esperar hasta que la posición esté libre
                notificarPosicionDisponible(proximaPosicion);  // No pudo avanzar, liberar la próxima posición
                if (!robot.frontIsClear()) {
                    robot.turnLeft();  // Si tampoco puede avanzar después de girar, girar dos veces
                    robot.turnLeft();
                    proximaPosicion = calcularProximaPosicion(robot);
                    esperarHastaQuePosicionEsteDisponible(proximaPosicion);  // Esperar hasta que la posición esté libre
                    notificarPosicionDisponible(proximaPosicion);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restaurar el estado de interrupción si ocurre una excepción
        }
    }


    public static String calcularProximaPosicion(RobotOp robot) {
        int nextStreet = robot.getStreet();
        int nextAvenue = robot.getAvenue();

        if (robot.facingNorth()) {
            nextStreet++;
        } else if (robot.facingSouth()) {
            nextStreet--;
        } else if (robot.facingEast()) {
            nextAvenue++;
        } else if (robot.facingWest()) {
            nextAvenue--;
        }

        return "(" + nextStreet + ", " + nextAvenue + ")";
    }

}

// Clase que extiende a Robot y nos permite obtener la calle y avenida del robot
class RobotOp extends Robot {

    private static List<RobotOp> allRobots = new ArrayList<>();
    private boolean fueraDelParqueadero = false;

    public RobotOp(int street, int avenue, Direction direction, int beeps) {
        super(street, avenue, direction, beeps);
        allRobots.add(this);
    }


    public void setFueraDelParqueadero(boolean value) {
        this.fueraDelParqueadero = value;
    }

    public int getStreet() {
        String robotInfo = this.toString();
        int streetIndex = robotInfo.indexOf("street: ");
        int streetEndIndex = robotInfo.indexOf(")", streetIndex);
        return Integer.parseInt(robotInfo.substring(streetIndex + 8, streetEndIndex));
    }

    @Override
    public void move() {
        // Obtener la posición actual antes de moverse
        String posicionActual = getPosition();

        // Llamar al método move() original para mover al robot
        super.move();

        // Notificar que la posición anterior está libre
        Simulacion.notificarPosicionDisponible(posicionActual);
    }


    public int getAvenue() {
        String robotInfo = this.toString();
        int avenueIndex = robotInfo.indexOf("avenue: ");
        int avenueEndIndex = robotInfo.indexOf(")", avenueIndex);
        return Integer.parseInt(robotInfo.substring(avenueIndex + 8, avenueEndIndex));
    }

    public String getPosition() {
        return "(" + getStreet() + ", " + getAvenue() + ")";
    }


    // Método adicional para girar a la derecha
    public void turnRight() {
        turnLeft();
        turnLeft();
        turnLeft();
    }
}