import java.util.Scanner;

void main() {
    var scanner = new Scanner(System.in);

    System.out.println("=".repeat(60));
    System.out.println("     ПАРИКМАХЕРСКАЯ - ПРОГРАММА СИМУЛЯЦИИ");
    System.out.println("=".repeat(60));

    var shop = new Barbershop("Стильные стрижки");

    System.out.println("\nДОСТУПНЫЕ УСЛУГИ:");
    for (Service s : shop.services) {
        s.printInfo();
    }

    System.out.println("\nНАШИ ПАРИКМАХЕРЫ:");
    for (Barber b : shop.barbers) {
        String levelName;
        if (b.level == 1) {
            levelName = "начинающий";
        } else if (b.level == 2) {
            levelName = "опытный";
        } else {
            levelName = "мастер";
        }

        String nonStandard;
        if (b.canDoNonStandard) {
            nonStandard = "да";
        } else {
            nonStandard = "нет";
        }

        System.out.print("  - ");
        System.out.print(b.name);
        System.out.print(", уровень: ");
        System.out.print(levelName);
        System.out.print(", нестандартные услуги: ");
        System.out.println(nonStandard);
    }

    System.out.println("\n" + "=".repeat(60));
    System.out.println("НАСТРОЙКИ СИМУЛЯЦИИ");
    System.out.println("=".repeat(60));

    System.out.print("Длительность рабочего дня (минут, 300-600): ");
    int dayDuration = scanner.nextInt();
    if (dayDuration < 60) {
        dayDuration = 480;
    }
    if (dayDuration > 720) {
        dayDuration = 480;
    }

    System.out.print("Количество дней симуляции (1-30): ");
    int days = scanner.nextInt();
    if (days < 1) {
        days = 5;
    }
    if (days > 30) {
        days = 30;
    }

    System.out.print("Максимальная длина очереди к парикмахеру (2-10): ");
    int maxQueue = scanner.nextInt();
    if (maxQueue < 2) {
        maxQueue = 5;
    }
    if (maxQueue > 10) {
        maxQueue = 10;
    }

    System.out.println("\n" + "=".repeat(60));
    System.out.println("ЗАПУСК СИМУЛЯЦИИ...");
    System.out.println("=".repeat(60));

    var sim = new Simulation(shop, dayDuration, days);
    sim.run(maxQueue);

    System.out.println("\nОткрытие окна статистики...");
    new Statistics(shop, dayDuration);

    scanner.close();
}
