import java.util.ArrayList;

class Barbershop {
    String name;
    ArrayList<Service> services;
    ArrayList<Barber> barbers;
    ArrayList<Client> allClients;
    ArrayList<Client> leftClients;
    ArrayList<Client> servedClients;
    int totalRevenue;
    int currentDay;
    int[] revenueByCategory;
    ArrayList<Client>[] barberQueues;
    boolean[] barberBusy;
    int[] barberTimeUntilFree;
    int currentTime;

    Barbershop(String name) {
        this.name = name;
        this.services = new ArrayList<>();
        this.barbers = new ArrayList<>();
        this.allClients = new ArrayList<>();
        this.leftClients = new ArrayList<>();
        this.servedClients = new ArrayList<>();
        this.totalRevenue = 0;
        this.currentDay = 0;
        this.revenueByCategory = new int[5];
        this.currentTime = 0;
        initServices();
        initBarbers();
    }

    void initServices() {
        services.add(new Service("Стрижка машинкой", 15, 500, 1));
        services.add(new Service("Детская стрижка", 20, 600, 1));
        services.add(new Service("Чёлка", 10, 300, 1));
        services.add(new Service("Коррекция бороды", 10, 400, 1));
        services.add(new Service("Мужская стрижка ножницами", 30, 1000, 2));
        services.add(new Service("Женская стрижка", 45, 1500, 2));
        services.add(new Service("Укладка", 25, 800, 2));
        services.add(new Service("Окрашивание корней", 40, 1200, 2));
        services.add(new Service("Стрижка с окрашиванием", 90, 3500, 3));
        services.add(new Service("Химическая завивка", 120, 4000, 3));
        services.add(new Service("Биозавивка", 100, 3800, 3));
        services.add(new Service("Креативное окрашивание", 60, 2500, 4));
        services.add(new Service("Свадебная причёска", 75, 3000, 4));
        services.add(new Service("Афрокосички", 180, 5000, 4));
    }

    void initBarbers() {
        barbers.add(new Barber("Анна", 1));
        barbers.add(new Barber("Дмитрий", 1));

        Barber b1 = new Barber("Елена", 2);
        b1.specialServices.add(230);
        b1.specialServices.add(225);
        barbers.add(b1);

        Barber b2 = new Barber("Сергей", 2);
        b2.specialServices.add(230);
        b2.specialServices.add(245);
        barbers.add(b2);

        Barber b3 = new Barber("Ольга", 2);
        b3.specialServices.add(245);
        b3.specialServices.add(240);
        barbers.add(b3);

        barbers.add(new Barber("Мария", 3));
        barbers.add(new Barber("Александр", 3));
    }

    void nextDay() {
        currentDay = currentDay + 1;
        int barberCount = barbers.size();
        int i = 0;
        while (i < barberCount) {
            barbers.get(i).nextDay();
            i = i + 1;
        }
    }

    void resetDay() {
        if (barberBusy != null) {
            int barberCount = barbers.size();
            int i = 0;
            while (i < barberCount) {
                barberBusy[i] = false;
                barberTimeUntilFree[i] = 0;
                if (barberQueues != null && barberQueues[i] != null) {
                    barberQueues[i].clear();
                }
                i = i + 1;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void initDaySimulation() {
        int n = barbers.size();
        barberQueues = new ArrayList[n];
        barberBusy = new boolean[n];
        barberTimeUntilFree = new int[n];
        int i = 0;
        while (i < n) {
            barberQueues[i] = new ArrayList<>();
            barberBusy[i] = false;
            barberTimeUntilFree[i] = 0;
            i = i + 1;
        }
    }

    boolean addClient(Client client, int maxQueueLength) {
        int bestBarberIndex = -1;
        int shortestQueue = Integer.MAX_VALUE;
        int barberCount = barbers.size();
        int i = 0;
        while (i < barberCount) {
            Barber b = barbers.get(i);
            if (!b.isWorkingToday()) {
                i = i + 1;
                continue;
            }
            if (!b.canPerform(client.wantedService)) {
                i = i + 1;
                continue;
            }
            if (client.requiredLevel > 0 && b.level < client.requiredLevel) {
                i = i + 1;
                continue;
            }
            if (client.preferredBarberId > 0 && b.id != client.preferredBarberId) {
                i = i + 1;
                continue;
            }
            int queueLen = barberQueues[i].size();
            if (barberBusy[i]) {
                queueLen = queueLen + 1;
            }
            if (queueLen >= maxQueueLength) {
                i = i + 1;
                continue;
            }
            if (queueLen < shortestQueue) {
                shortestQueue = queueLen;
                bestBarberIndex = i;
            }
            i = i + 1;
        }

        allClients.add(client);

        if (bestBarberIndex == -1) {
            leftClients.add(client);
            client.wasServed = false;
            return false;
        }

        if (!barberBusy[bestBarberIndex]) {
            startService(bestBarberIndex, client, client.arrivalTime);
        } else {
            barberQueues[bestBarberIndex].add(client);
        }
        return true;
    }

    void startService(int barberIndex, Client client, int serviceStartTime) {
        Barber barber = barbers.get(barberIndex);
        Service service = client.wantedService;

        client.serviceStartTime = serviceStartTime;
        client.waitTime = client.serviceStartTime - client.arrivalTime;
        if (client.waitTime < 0) {
            client.waitTime = 0;
        }
        client.wasServed = true;

        barber.waitTimes.add(client.waitTime);
        barber.totalRevenue = barber.totalRevenue + service.price;
        totalRevenue = totalRevenue + service.price;
        revenueByCategory[service.category] = revenueByCategory[service.category] + service.price;

        barberBusy[barberIndex] = true;
        barberTimeUntilFree[barberIndex] = service.timeMinutes;

        servedClients.add(client);
    }

    void updateMinute(int time) {
        this.currentTime = time;

        int barberCount = barbers.size();
        int i = 0;
        while (i < barberCount) {
            if (barberBusy[i]) {
                barberTimeUntilFree[i] = barberTimeUntilFree[i] - 1;
                if (barberTimeUntilFree[i] <= 0) {
                    barberBusy[i] = false;
                }
            }
            Barber barber = barbers.get(i);
            if (barber.isWorkingToday()) {
                if (!barberBusy[i]) {
                    barber.totalIdleTime = barber.totalIdleTime + 1;
                }
            }
            if (!barberBusy[i]) {
                if (!barberQueues[i].isEmpty()) {
                    Client nextClient = barberQueues[i].getFirst();
                    barberQueues[i].removeFirst();
                    startService(i, nextClient, this.currentTime);
                }
            }
            i = i + 1;
        }
    }

    void printStatistics(int totalMinutes) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("        СТАТИСТИКА ПАРИКМАХЕРСКОЙ \"" + name + "\"");
        System.out.println("=".repeat(60));
        System.out.println("\n--- ВЫРУЧКА ПАРИКМАХЕРОВ ---");
        int beginnerRevenue = 0;
        int experiencedRevenue = 0;
        int masterRevenue = 0;
        int beginnerIdle = 0;
        int experiencedIdle = 0;
        int masterIdle = 0;
        int beginnerCount = 0;
        int experiencedCount = 0;
        int masterCount = 0;

        int barberCount = barbers.size();
        int idx = 0;
        while (idx < barberCount) {
            Barber b = barbers.get(idx);
            b.printInfo();
            if (b.level == 1) {
                beginnerRevenue = beginnerRevenue + b.totalRevenue;
                beginnerIdle = beginnerIdle + b.totalIdleTime;
                beginnerCount = beginnerCount + 1;
            } else if (b.level == 2) {
                experiencedRevenue = experiencedRevenue + b.totalRevenue;
                experiencedIdle = experiencedIdle + b.totalIdleTime;
                experiencedCount = experiencedCount + 1;
            } else {
                masterRevenue = masterRevenue + b.totalRevenue;
                masterIdle = masterIdle + b.totalIdleTime;
                masterCount = masterCount + 1;
            }
            idx = idx + 1;
        }

        System.out.println("\n--- ВЫРУЧКА ПО УРОВНЯМ ---");
        System.out.println("Общая выручка: " + totalRevenue + " руб");
        System.out.println("Начинающие (" + beginnerCount + " чел): " + beginnerRevenue + " руб");
        System.out.println("Опытные (" + experiencedCount + " чел): " + experiencedRevenue + " руб");
        System.out.println("Мастера (" + masterCount + " чел): " + masterRevenue + " руб");

        System.out.println("\n--- ВРЕМЯ В ОЧЕРЕДИ ---");
        int totalWaitTime = 0;
        int servedCount = servedClients.size();
        int j = 0;
        while (j < servedCount) {
            totalWaitTime = totalWaitTime + servedClients.get(j).waitTime;
            j = j + 1;
        }

        double avgWait;
        if (servedClients.isEmpty()) {
            avgWait = 0;
        } else {
            avgWait = (double) totalWaitTime / servedClients.size();
        }

        System.out.print("Среднее время ожидания в очереди: ");
        System.out.print(avgWait);
        System.out.println(" мин");

        System.out.println("\n--- УШЕДШИЕ КЛИЕНТЫ ---");
        int regularLeft = 0;
        int randomLeft = 0;
        int leftCount = leftClients.size();
        int k = 0;
        while (k < leftCount) {
            if (leftClients.get(k).isRegular) {
                regularLeft = regularLeft + 1;
            } else {
                randomLeft = randomLeft + 1;
            }
            k = k + 1;
        }

        System.out.println("Всего обслужено: " + servedClients.size() + " клиентов");
        System.out.println("Всего ушло: " + leftClients.size() + " клиентов");
        System.out.println("  - постоянных: " + regularLeft);
        System.out.println("  - случайных: " + randomLeft);

        System.out.println("\n--- ВЫРУЧКА ПО ТИПАМ УСЛУГ ---");
        System.out.println("  базовая: " + revenueByCategory[1] + " руб");
        System.out.println("  обычная: " + revenueByCategory[2] + " руб");
        System.out.println("  сложная: " + revenueByCategory[3] + " руб");
        System.out.println("  нестандартная: " + revenueByCategory[4] + " руб");

        System.out.println("\n--- НЕСТАНДАРТНЫЕ УСЛУГИ ---");
        int nonStandardRevenue = 0;
        int standardRevenue = 0;
        int barbersTotal = barbers.size();
        int n = 0;
        while (n < barbersTotal) {
            if (barbers.get(n).canDoNonStandard) {
                nonStandardRevenue = nonStandardRevenue + barbers.get(n).totalRevenue;
            } else {
                standardRevenue = standardRevenue + barbers.get(n).totalRevenue;
            }
            n = n + 1;
        }

        System.out.println("Выручка парикмахеров, владеющих нестандартными услугами: " + nonStandardRevenue + " руб");
        System.out.println("Выручка парикмахеров, НЕ владеющих нестандартными: " + standardRevenue + " руб");
        System.out.println("Разница: " + (nonStandardRevenue - standardRevenue) + " руб");

        System.out.println("\n--- ПРОСТОЙ ПАРИКМАХЕРОВ ---");
        System.out.println("Всего минут в рабочем дне: " + totalMinutes);

        if (beginnerCount > 0) {
            double percent = (double) beginnerIdle / (beginnerCount * totalMinutes);
            percent = percent * 100;
            int rounded = (int) (percent * 10);
            int whole = rounded / 10;
            int fraction = rounded % 10;
            System.out.println("Начинающие: " + beginnerIdle + " мин (" + whole + "." + fraction + "%)");
        }

        if (experiencedCount > 0) {
            double percent = (double) experiencedIdle / (experiencedCount * totalMinutes);
            percent = percent * 100;
            int rounded = (int) (percent * 10);
            int whole = rounded / 10;
            int fraction = rounded % 10;
            System.out.println("Опытные: " + experiencedIdle + " мин (" + whole + "." + fraction + "%)");
        }

        if (masterCount > 0) {
            double percent = (double) masterIdle / (masterCount * totalMinutes);
            percent = percent * 100;
            int rounded = (int) (percent * 10);
            int whole = rounded / 10;
            int fraction = rounded % 10;
            System.out.println("Мастера: " + masterIdle + " мин (" + whole + "." + fraction + "%)");
        }

        System.out.println("\n" + "=".repeat(60));
    }
}