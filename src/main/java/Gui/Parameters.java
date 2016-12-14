package Gui;

/**
 * Created by kostya on 13.12.2016.
 */
class Parameters {
    private final String variableParameter;
    private final int min;
    private final int max;
    private final int step;
    private final int numOfRequests;
    private final int numOfElements;
    private final int numOfClients;
    private final int delta;
    private final int serverType;

    Parameters(String variableParameter, int min, int max, int step, int numOfRequests, int numOfElements, int numOfClients, int delta, int serverType) {
        this.variableParameter = variableParameter;
        this.min = min;
        this.max = max;
        this.step = step;
        this.numOfRequests = numOfRequests;
        this.numOfElements = numOfElements;
        this.numOfClients = numOfClients;
        this.delta = delta;
        this.serverType = serverType;
    }

    public String getVariableParameter() {
        return variableParameter;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public int getNumOfRequests() {
        return numOfRequests;
    }

    public int getNumOfClients() {
        return numOfClients;
    }

    public int getDelta() {
        return delta;
    }

    public int getServerType() {
        return serverType;
    }

    public int getNumOfElements() {
        return numOfElements;
    }

    @Override
    public String toString() {
        return String.format(
                "Изменяемый параметр: %s\n" +
                        "Min: %d\n" +
                        "Max: %d\n" +
                        "Step: %d\n" +
                        "Количество запросов: %d\n" +
                        "Количество элементов: %d\n" +
                        "Количество клиентов: %d\n" +
                        "Пауза между запросами: %d\n",
                variableParameter, min, max, step, numOfRequests,numOfElements, numOfClients, delta);
    }
}
