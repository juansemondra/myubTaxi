public class TaxiAppMain {
    public static void main(String[] args) {
        // Definir los parámetros del taxi
        int id = 1;
        int gridN = 10; // Tamaño de la cuadrícula N
        int gridM = 10; // Tamaño de la cuadrícula M
        int posX = 0; // Posición inicial en X
        int posY = 0; // Posición inicial en Y
        int speed = 2; // Velocidad del taxi (1, 2 o 4 km/h)
        int maxServices = 3; // Máximo de servicios por día

        // Crear una instancia del taxi
        Taxi taxi = new Taxi(id, gridN, gridM, posX, posY, speed, maxServices);

        // Simular el movimiento y asignaciones del taxi
        while (!taxi.hasCompletedServices()) {
            taxi.move();
            
            // Simular la asignación de un servicio
            taxi.assignService();
        }

        // Cerrar el socket y el contexto ZeroMQ cuando el taxi haya completado sus servicios
        taxi.close();
    }
}
