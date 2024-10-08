import org.zeromq.ZMQ;

public class Taxi {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PUERTO_POSICIONES = 5560;

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
            System.out.println("Taxi " + id + " conectado exitosamente al servidor para enviar posiciones y recibir asignaciones en el puerto " + PUERTO_POSICIONES);
        } catch (Exception e) {
            System.err.println("Error en la conexión del Taxi " + id + " con el servidor: " + e.getMessage());
        }

        System.out.println("Taxi " + id + " inició en posición: (" + posX + ", " + posY + ")");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.move();
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

            if (respuesta.contains("asignado")) {
                assignService();
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

            System.out.println("Taxi " + id + " regresa a la posición anterior: (" + posX + ", " + posY + ")");
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
            context.close();
            System.out.println("Taxi " + id + " ha cerrado las conexiones.");
        } catch (Exception e) {
            System.err.println("Error al cerrar las conexiones del Taxi " + id + ": " + e.getMessage());
        }
    }
}