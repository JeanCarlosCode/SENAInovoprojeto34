package application;

public class Main {
    public static void main(String[] args) {
        // Wrapper para contornar o erro de "JavaFX runtime components are missing"
        // Sem precisar configurar VM Arguments no Eclipse!
        App.main(args);
    }
}
