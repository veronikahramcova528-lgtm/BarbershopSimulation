import java.util.Random;
import java.util.Scanner;

class Simulation {
    Barbershop shop;
    int currentTime;
    int dayDuration;
    int currentDay;
    int daysToSimulate;
    Random random;
    Scanner scanner;
    boolean paused;

    Simulation(Barbershop shop, int dayDuration, int daysToSimulate) {
        this.shop = shop;
        this.dayDuration = dayDuration;
        this.daysToSimulate = daysToSimulate;
        this.currentTime = 0;
        this.currentDay = 0;
        this.random = new Random();
        this.scanner = new Scanner(System.in);
        this.paused = false;
    }

    Client generateRandomClient(int time) {
        String[] names = {"Иван", "Петр", "Сидор", "Мария", "Ольга", "Анна", "Дмитрий",
                "Елена", "Алексей", "Татьяна", "Михаил", "Наталья", "Сергей", "Юлия"};

        int nameIndex = random.nextInt(names.length);
        String name = names[nameIndex];

        boolean isRegular = random.nextDouble() < 0.3;

        double serviceChance = random.nextDouble();
        Service service;

        if (serviceChance < 0.35) {
            service = shop.services.get(random.nextInt(4));
        } else if (serviceChance < 0.65) {
            service = shop.services.get(4 + random.nextInt(4));
        } else if (serviceChance < 0.85) {
            service = shop.services.get(8 + random.nextInt(3));
        } else {
            service = shop.services.get(11 + random.nextInt(3));
        }

        int requiredLevel = 0;
        if (random.nextDouble() < 0.2) {
            requiredLevel = 1 + random.nextInt(3);
        }

        int preferredBarberId = 0;
        if (isRegular && random.nextDouble() < 0.3) {
            int barberCount = shop.barbers.size();
            preferredBarberId = 1 + random.nextInt(barberCount);
        }

        return new Client(name, isRegular, service, requiredLevel, preferredBarberId, time);
    }

    void printMenu() {
        System.out.println("\n--- МЕНЮ (нажмите клавишу) ---");
        System.out.println("1 - Показать текущую статистику");
        System.out.println("2 - Показать статус парикмахеров");
        System.out.println("3 - Продолжить симуляцию");
        System.out.println("4 - Остановить и показать финальную статистику");
        System.out.print("Ваш выбор: ");
    }

    String getBarberStatus(int index, Barber barber) {
        if (!barber.isWorkingToday()) {
            return "ОТДЫХАЕТ";
        }
        if (shop.barberBusy != null && shop.barberBusy[index]) {
            return "ЗАНЯТ (" + shop.barberTimeUntilFree[index] + " мин)";
        }
        int queueSize = 0;
        if (shop.barberQueues != null) {
            queueSize = shop.barberQueues[index].size();
        }
        return "СВОБОДЕН, очередь: " + queueSize;
    }

    void showCurrentStats() {
        System.out.println("\n=== ТЕКУЩАЯ СТАТИСТИКА (день " + (currentDay + 1) + ", время " + currentTime + " мин) ===");
        System.out.println("Обслужено клиентов: " + shop.servedClients.size());
        System.out.println("Ушло клиентов: " + shop.leftClients.size());
        System.out.println("Текущая выручка: " + shop.totalRevenue + " руб");

        System.out.println("\nСтатус парикмахеров:");
        int barberCount = shop.barbers.size();
        int i = 0;
        while (i < barberCount) {
            Barber b = shop.barbers.get(i);
            String status = getBarberStatus(i, b);
            System.out.print("  ");
            System.out.print(b.name);
            System.out.print(": ");
            System.out.println(status);
            i = i + 1;
        }
    }

    void showBarberStatus() {
        System.out.println("\n=== ПАРИКМАХЕРЫ ===");
        int barberCount = shop.barbers.size();
        int i = 0;
        while (i < barberCount) {
            Barber b = shop.barbers.get(i);

            String levelName = switch (b.level) {
                case 1 -> "начинающий";
                case 2 -> "опытный";
                case 3 -> "мастер";
                default -> "неизвестно";
            };

            String workStatus;
            if (b.isWorkingToday()) {
                workStatus = "работает";
            } else {
                workStatus = "отдыхает";
            }

            System.out.print("  #");
            System.out.print(b.id);
            System.out.print(" ");
            System.out.print(b.name);
            System.out.print(" (");
            System.out.print(levelName);
            System.out.print(") - ");
            System.out.print(workStatus);
            System.out.print(", выручка: ");
            System.out.print(b.totalRevenue);
            System.out.println(" руб");

            i = i + 1;
        }
    }

    void run(int maxQueueLength) {
        System.out.println("=".repeat(60));
        System.out.println("         СИМУЛЯЦИЯ РАБОТЫ ПАРИКМАХЕРСКОЙ");
        System.out.println("=".repeat(60));
        System.out.println("Длительность рабочего дня: " + dayDuration + " минут");
        System.out.println("Количество дней симуляции: " + daysToSimulate);
        System.out.println("Максимальная длина очереди: " + maxQueueLength);
        System.out.println("\nДля паузы во время симуляции НАЖМИТЕ ENTER");
        System.out.println("=".repeat(60));

        int day = 0;
        while (day < daysToSimulate) {
            currentDay = day;
            currentTime = 0;

            // Запоминаем количество до начала дня
            int servedBeforeDay = shop.servedClients.size();
            int leftBeforeDay = shop.leftClients.size();

            System.out.println();
            System.out.print(">>> ДЕНЬ ");
            System.out.print(day + 1);
            System.out.println(" <<<");

            System.out.println("Работающие парикмахеры:");

            int barberCount = shop.barbers.size();
            int barberIndex = 0;
            while (barberIndex < barberCount) {
                Barber b = shop.barbers.get(barberIndex);
                if (b.isWorkingToday()) {
                    System.out.print("  - ");
                    System.out.print(b.name);
                    System.out.print(" (");

                    switch (b.level) {
                        case 1 -> System.out.print("начинающий");
                        case 2 -> System.out.print("опытный");
                        default -> System.out.print("мастер");
                    }

                    System.out.println(")");
                }
                barberIndex = barberIndex + 1;
            }

            shop.initDaySimulation();

            currentTime = 0;
            while (currentTime < dayDuration) {
                boolean isPauseTime = currentTime % 30 == 0 && currentTime > 0;

                if (isPauseTime) {
                    System.out.print("[");
                    System.out.print(currentTime);
                    System.out.print(" мин] Нажмите Enter для паузы...");

                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                        System.out.println("\n*** ПАУЗА ***");
                        boolean stop = false;
                        while (!stop) {
                            printMenu();
                            String choice = scanner.nextLine();

                            switch (choice) {
                                case "3" -> {
                                    stop = true;
                                    System.out.println("Продолжаем симуляцию...");
                                }
                                case "4" -> {
                                    shop.printStatistics(dayDuration);
                                    System.out.println("\nПрограмма завершена пользователем.");
                                    return;
                                }
                                case "1" -> showCurrentStats();
                                case "2" -> showBarberStatus();
                                default -> {}
                            }
                        }
                    }
                }

                double clientChance = random.nextDouble();
                if (clientChance < 0.22) {
                    Client client = generateRandomClient(currentTime);
                    boolean accepted = shop.addClient(client, maxQueueLength);

                    System.out.print("  [День ");
                    System.out.print(day + 1);
                    System.out.print(", ");
                    System.out.print(currentTime);
                    System.out.print(" мин] ");

                    if (accepted) {
                        System.out.print("Пришёл: ");
                        System.out.print(client.name);
                        System.out.print(" (");
                        System.out.print(client.wantedService.name);
                        System.out.println(")");
                    } else {
                        System.out.print("УШЁЛ: ");
                        System.out.print(client.name);
                        System.out.println(" (нет подходящего парикмахера или очередь переполнена)");
                    }
                }

                shop.updateMinute(currentTime);
                currentTime = currentTime + 1;
            }

            System.out.println();
            System.out.print("--- День ");
            System.out.print(day + 1);
            System.out.println(" завершён ---");

            // Вычисляем сколько обслужено и ушло именно за этот день
            int servedToday = shop.servedClients.size() - servedBeforeDay;
            int leftToday = shop.leftClients.size() - leftBeforeDay;

            System.out.print("Обслужено за день: ");
            System.out.println(servedToday);

            if (leftToday > 0) {
                System.out.print("Ушло за день: ");
                System.out.println(leftToday);
            }

            shop.nextDay();
            shop.resetDay();

            day = day + 1;
        }

        shop.printStatistics(dayDuration);
    }
}