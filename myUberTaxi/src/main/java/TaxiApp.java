import org.zeromq.ZMQ;

public class TaxiApp implements Runnable {
    private int id;
    private int[] position;
    private int speed;
    private int maxServices;
    private ZMQ.Context context;
    private ZMQ.Socket publisher;
    
    private static final int PUERTO_POSICIONES = 5556;

    public TaxiApp(int id, int[] startPosition, int speed, int maxServices, String serverIP) {
        this.id = id;
        this.position = startPosition;
        this.speed = speed;
        this.maxServices = maxServices;
        this.context = ZMQ.context(1);
        this.publisher = context.socket(ZMQ.PUB);
        this.publisher.connect("tcp://" + serverIP + ":" + PUERTO_POSICIONES);
    }

    @Override
    public void run() {
        int servicesDone = 0;
        while (servicesDone < maxServices) {
            try {
                // Simular movimiento cada 30 minutos del sistema (30 segundos reales)
                Thread.sleep(30000); 
                
                move();
                
                // Publicar la nueva posición
                String message = String.format("Taxi %d en posición (%d, %d)", id, position[0], position[1]);
                publisher.send(message);
                System.out.println(message);

                // Lógica para recibir asignación de servicio...

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Taxi " + id + " ha completado sus servicios del día.");
        publisher.close();
        context.close();
    }

    private void move() {
        // Lógica para mover el taxi en la cuadrícula según su velocidad
        if (speed > 0) {
            // Movimiento horizontal o vertical
            position[0] += (Math.random() > 0.5 ? 1 : -1); // Movimiento aleatorio en X
            position[1] += (Math.random() > 0.5 ? 1 : -1); // Movimiento aleatorio en Y
        }
    }
}
