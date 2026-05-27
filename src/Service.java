class Service {
    String name;
    int timeMinutes;
    int price;
    int category;  // 1-базовая, 2-обычная, 3-сложная, 4-нестандартная

    Service(String name, int timeMinutes, int price, int category) {
        this.name = name;
        this.timeMinutes = timeMinutes;
        this.price = price;
        this.category = category;
    }

    String getCategoryName() {
        if (category == 1) {
            return "базовая";
        } else if (category == 2) {
            return "обычная";
        } else if (category == 3) {
            return "сложная";
        } else if (category == 4) {
            return "нестандартная";
        } else {
            return "неизвестная";
        }
    }

    void printInfo() {
        System.out.print("  - ");
        System.out.print(name);
        System.out.print(": ");
        System.out.print(timeMinutes);
        System.out.print(" мин, ");
        System.out.print(price);
        System.out.print(" руб, категория: ");
        System.out.println(getCategoryName());
    }
}
