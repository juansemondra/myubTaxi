import java.util.Random;

public class TaxiApp {
    public static void main(String[] args) {

        Random random = new Random();

        int id = random.nextInt(1, 999999);
        int gridN = 10;
        int gridM = 10;
        int posX = random.nextInt(gridN);
        int posY = random.nextInt(gridM);
        int speed = 2;
        int maxServices = 3;

        Taxi taxi = new Taxi(id, gridN, gridM, posX, posY, speed, maxServices);

        Thread moveThread = new Thread(() -> {
            while (!taxi.hasCompletedServices()) {
                try {
                    Thread.sleep(5000);
                    taxi.move();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        //Thread listenThread = new Thread(taxi::listenForAssignments);

        moveThread.start();
        //listenThread.start();

        try {
            moveThread.join();
            //listenThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        taxi.close();
    }
}