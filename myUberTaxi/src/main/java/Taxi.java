import org.zeromq.ZMQ;

public class Taxi {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PUERTO_POSICIONES = 5556;

    private int id;
    private int gridN;
    private int gridM;
    private int posX;
    private int posY;
    private int speed;
    private int maxServices;
    private int completedServices;
    private ZMQ.Context context;
    private ZMQ.Socket publisher;

    public Taxi(int id, int gridN, int gridM, int posX, int posY, int speed, int maxServices) {
        this.id = id;
        this.gridN = gridN;
        this.gridM = gridM;
        this.speed = speed;
        this.maxServices = maxServices;
        this.completedServices = 0;

        if (posX < 0 || posX > gridN || posY < 0 || posY > gridM) {
            throw new IllegalArgumentException("Posición inicial fuera de los límites permitidos.");
        }
        this.posX = posX;
        this.posY = posY;

        // Iniciar el socket para ZeroMQ (Pub/Sub)
        this.context = ZMQ.context(1);
        this.publisher = context.socket(ZMQ.PUB);
        this.publisher.connect("tcp://" + SERVER_IP + ":" + PUERTO_POSICIONES); // El taxi publica en el servidor central

        System.out.println("Taxi " + id + " inició en posición: (" + posX + ", " + posY + ")");
    }

    public void move() {
        if (speed > 0 && completedServices < maxServices) {
            if (Math.random() < 0.5) {
                posX = (posX + speed / 2) % gridN;
            } else {
                posY = (posY + speed / 2) % gridM;
            }

            // Publicar nueva posición al servidor central
            String positionUpdate = "Taxi " + id + " nueva posición: (" + posX + ", " + posY + ")";
            publisher.send(positionUpdate.getBytes(ZMQ.CHARSET));
            System.out.println(positionUpdate);
        }
    }

    public void assignService() {
        if (completedServices < maxServices) {
            System.out.println("Taxi " + id + " asignado a un servicio.");
            completedServices++;

            // Simular tiempo del servicio (30 minutos o 30 segundos en la simulación)
            try {
                Thread.sleep(30000); // Simula el tiempo de 30 minutos de un servicio
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Taxi " + id + " regresa a la posición original: (" + posX + ", " + posY + ")");
        } else {
            System.out.println("Taxi " + id + " ha completado sus servicios diarios.");
        }
    }

    public boolean hasCompletedServices() {
        return completedServices >= maxServices;
    }

    public void close() {
        publisher.close();
        context.close();
    }
}