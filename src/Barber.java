import java.util.ArrayList;

class Barber {
    static int nextId = 1;
    int id;
    String name;
    int level;
    int workDay;
    int totalRevenue;
    int totalIdleTime;
    boolean canDoNonStandard;
    ArrayList<Integer> specialServices;
    ArrayList<Integer> waitTimes;

    Barber(String name, int level) {
        this.id = nextId;
        nextId = nextId + 1;
        this.name = name;
        this.level = level;
        this.workDay = 0;
        this.totalRevenue = 0;
        this.totalIdleTime = 0;
        this.canDoNonStandard = Math.random() < 0.5;
        this.specialServices = new ArrayList<>();
        this.waitTimes = new ArrayList<>();
    }

    boolean isWorkingToday() {
        return workDay == 0;
    }

    void nextDay() {
        workDay = workDay + 1;
        if (workDay > 3) {
            workDay = 0;
        }
    }

    boolean canPerform(Service service) {
        if (service.category == 4) {
            return canDoNonStandard;
        }

        if (service.category == 1) {
            return true;
        }

        if (service.category == 2) {
            if (level == 3) {
                return true;
            }
            if (level == 2) {
                return specialServices.contains(service.category * 100 + service.timeMinutes);
            }
            return false;
        }

        if (service.category == 3) {
            return level == 3;
        }

        return false;
    }

    void printInfo() {
        String levelName;
        if (level == 1) {
            levelName = "начинающий";
        } else if (level == 2) {
            levelName = "опытный";
        } else {
            levelName = "мастер";
        }

        String workStatus;
        if (isWorkingToday()) {
            workStatus = "работает";
        } else {
            workStatus = "отдыхает";
        }

        String nonStandardStatus;
        if (canDoNonStandard) {
            nonStandardStatus = "да";
        } else {
            nonStandardStatus = "нет";
        }

        System.out.print("Парикмахер #");
        System.out.print(id);
        System.out.print(" (");
        System.out.print(name);
        System.out.print("), ");
        System.out.print(levelName);
        System.out.print(", ");
        System.out.print(workStatus);
        System.out.print(", выручка: ");
        System.out.print(totalRevenue);
        System.out.print(" руб, простой: ");
        System.out.print(totalIdleTime);
        System.out.print(" мин, нестандартные: ");
        System.out.println(nonStandardStatus);
    }

    double getAverageWaitTime() {
        if (waitTimes.isEmpty()) {
            return 0;
        }

        int sum = 0;
        for (int t : waitTimes) {
            sum = sum + t;
        }

        return (double) sum / waitTimes.size();
    }
}