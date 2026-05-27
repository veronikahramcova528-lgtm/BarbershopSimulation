class Client {
    static int nextId = 1;
    int id;
    String name;
    boolean isRegular;
    Service wantedService;
    int requiredLevel;
    int preferredBarberId;
    int arrivalTime;
    int serviceStartTime;
    int waitTime;
    boolean wasServed;

    Client(String name, boolean isRegular, Service service, int requiredLevel, int preferredBarberId, int arrivalTime) {
        this.id = nextId;
        nextId = nextId + 1;
        this.name = name;
        this.isRegular = isRegular;
        this.wantedService = service;
        this.requiredLevel = requiredLevel;
        this.preferredBarberId = preferredBarberId;
        this.arrivalTime = arrivalTime;
        this.wasServed = false;
        this.waitTime = 0;
        this.serviceStartTime = 0;
    }
}