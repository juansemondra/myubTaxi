import org.zeromq.ZMQ;

public class Taxi {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PUERTO_POSICIONES = 5560;
    private static final int PUERTO_TAXI_PULL = 5561;

    private int id;
    private int gridN;
    private int gridM;
    private int posX;
    private int posY;
    private int speed;
    private int maxServices;
    private int completedServices;
    private boolean isBusy;
    private ZMQ.Context context;
    private ZMQ.Socket requester;
    private ZMQ.Socket puller;

    public Taxi(int id, int gridN, int gridM, int posX, int posY, int speed, int maxServices) {
        this.id = id;
        this.gridN = gridN;
        this.gridM = gridM;
        this.speed = speed;
        this.maxServices = maxServices;
        this.completedServices = 0;
        this.isBusy = false;

        if (posX < 0 || posX > gridN || posY < 0 || posY > gridM) {
            throw new IllegalArgumentException("Posición inicial fuera de los límites permitidos.");
        }
        this.posX = posX;
        this.posY = posY;

        this.context = ZMQ.context(1);

        try {
            this.requester = context.socket(ZMQ.REQ);
            this.requester.connect("tcp://" + SERVER_IP + ":" + PUERTO_POSICIONES);
            System.out.println("Taxi " + id + " conectado exitosamente al servidor para enviar posiciones en el puerto " + PUERTO_POSICIONES);
        } catch (Exception e) {
            System.err.println("Error en la conexión del Taxi " + id + " con el servidor: " + e.getMessage());
        }

        try {
            this.puller = context.socket(ZMQ.PULL);
            this.puller.connect("tcp://" + SERVER_IP + ":" + PUERTO_TAXI_PULL);
            System.out.println("Taxi " + id + " conectado exitosamente al servidor para recibir asignaciones en el puerto " + PUERTO_TAXI_PULL);
        } catch (Exception e) {
            System.err.println("Error en la conexión del Taxi " + id + " para recibir asignaciones: " + e.getMessage());
        }

        System.out.println("Taxi " + id + " inició en posición: (" + posX + ", " + posY + ")");

        new Thread(this::listenForAssignments).start();
        this.move();
    }

    private void listenForAssignments() {
        while (true) {
            try {
                String mensaje = puller.recvStr(0);
                System.out.println("Mensaje recibido en el taxi: " + mensaje);

                if (mensaje.contains("ha sido asignado")) {
                    System.out.println("Taxi " + id + " ha sido asignado a un servicio.");
                    assignService();
                }
            } catch (Exception e) {
                System.err.println("Error al recibir asignación para el Taxi " + id + ": " + e.getMessage());
            }
        }
    }

    public void move() {
        if (speed > 0 && completedServices < maxServices && !isBusy) {
            if (Math.random() < 0.5) {
                posX = (posX + speed / 2) % gridN;
            } else {
                posY = (posY + speed / 2) % gridM;
            }

            String positionUpdate = String.format("TaxiID:%d Pos:(%d,%d) Busy:%b", id, posX, posY, isBusy);
            System.out.println(positionUpdate);
            requester.send(positionUpdate.getBytes(ZMQ.CHARSET));

            String respuesta = requester.recvStr(0);
            System.out.println("Respuesta del servidor: " + respuesta);

            if (respuesta.contains("nada")) {
                System.out.println("El taxi continúa sin asignaciones. \n");
            }
        }
    }

    public void assignService() {
        if (completedServices < maxServices && !isBusy) {
            System.out.println("Taxi " + id + " asignado a un servicio.");
            isBusy = true;
            completedServices++;

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Servicio termina.");

            System.out.println("Taxi " + id + " regresa a la posición anterior: (" + posX + ", " + posY + ")\n");
            isBusy = false;

            this.move();
        } else if (isBusy) {
            System.out.println("Taxi " + id + " está ocupado y no puede aceptar más servicios.");
        } else {
            System.out.println("Taxi " + id + " ha completado sus servicios diarios.");
        }
    }

    public boolean hasCompletedServices() {
        return completedServices >= maxServices;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void close() {
        try {
            requester.close();
            puller.close();
            context.close();
            System.out.println("Taxi " + id + " ha cerrado las conexiones.");
        } catch (Exception e) {
            System.err.println("Error al cerrar las conexiones del Taxi " + id + ": " + e.getMessage());
        }
    }
}