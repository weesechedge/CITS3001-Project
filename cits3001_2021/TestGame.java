package cits3001_2021;

public class TestGame {

    public static void main(String[] args) {
        // System.out.println(System.getProperty("java.class.path"));
        Agent[] agents = new Agent[6];
        // agents[0] = Agent22717772.init();
        // agents[1] = Agent22717772.init();
        // agents[2] = Agent22717772.init();
        agents[0] = RandomAgent.init();
        agents[1] = RandomAgent.init();
        agents[2] = RandomAgent.init();
        agents[3] = RandomAgent.init();
        agents[4] = RandomAgent.init();
        agents[5] = RandomAgent.init();
        // Game game = new Game(players);
        // Game.tor(6, agents);
    }
}
