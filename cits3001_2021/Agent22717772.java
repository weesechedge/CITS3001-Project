package cits3001_2021;
import java.util.*;

public class Agent22717772 implements Agent {

    private String name;
    private static int agentCount = 0;
    // Spy stores whether player is a spy or resistance
    private boolean spy;

    // Need to keep track of the players
    private int[] players;
    private int[] spies;
    private int numPlayers;
    private int playerIndex;

    // Each agent
    private double[] suspicion;
    private double[] expected_reward;

    // This Bayesian agent maintains the probabilities even if it is a spy, because it needs to gauge
    // what other Bayesian players are likely to think so it can act contrarily.
    // Boolean of whether it is a spy or not informs how to treat expected reward (positive reward to non-spies, negative to spies).
    // If it is a resistance bot, increase reward for all mission players - assess volatility of reward to observe if a player is a spy.
    public Agent22717772(String name) {
        this.name = name;
    }

    /**
     * returns an instance of this agent for testing. The progam should allocate the
     * agent's name, and can use a counter to ensure no two agents have the same
     * name.
     * 
     * @return an instance of the agent, with the given name.
     **/
    public static Agent22717772 init() {
        return new Agent22717772("Metagamer" + Integer.toString(agentCount++));
    }

    /**
     * gets the name of the agent
     * 
     * @return the agent's name.
     **/
    public String getName() {

        return name;
    }

    /**
     * initialises a new game. The agent should drop their current gameState and
     * reinitialise all their game variables.
     * 
     * @param numPlayers  the number of players in the game.
     * @param playerIndex the players index in the game.
     * @param spies,      the index of all the spies in the game, if this agent is a
     *                    spy (i.e. playerIndex is an element of spies)
     **/
    public void newGame(int numPlayers, int playerIndex, int[] spies) {
        // Check if this agent is a spy
        this.numPlayers = numPlayers;
        this.playerIndex = playerIndex;
        // Is an empty array if the player is the resistance
        this.spies = spies;
        for(int i = 0; i < spies.length; i++) {
            if(spies[i] == playerIndex) {
                spy = true;
                break;
            }
        }
        // Track new players, set default suspicion and reward
        players = new int[numPlayers];
        suspicion = new double[numPlayers];
        expected_reward = new double[numPlayers];
        for(int i = 0; i < numPlayers; i++) {
            players[i] = i;
            suspicion[i] = 0.0;
            expected_reward[i] = 0.0;
        }
    }

    /**
     * This method is called when the agent is required to lead (propose) a mission
     * 
     * @param teamSize      the number of agents to go on the mission
     * @param failsRequired the number of agent fails required for the mission to
     *                      fail
     * @return an array of player indexes, the proposed mission.
     **/
    public int[] proposeMission(int teamsize, int failsRequired) {
        // Write code to choose players
        
        // teamsize number of passthroughs to get only the least sus players. Gets the most sus if leader is a spy.
        // Should be updated to make decision based on reward, not using suspicion as a proxy for reward.
        int[] team = new int[teamsize];
        if(spy) {
            double[] temp_sus = Arrays.copyOf(suspicion, numPlayers);
            for(int i = 0; i < teamsize; i++) {
                int min_sus = 0;
                for(int j = 0; j < numPlayers; j++) {
                    if(temp_sus[j] > temp_sus[min_sus]) {
                        min_sus = j;
                    }
                }
                team[i] = min_sus;
                // So that it cannot be detected again as the least sus
                temp_sus[min_sus] = -1;
            }
            return team;
        }
        else {
            double[] temp_sus = Arrays.copyOf(suspicion, numPlayers);
            for(int i = 0; i < teamsize; i++) {
                int min_sus = 0;
                for(int j = 0; j < numPlayers; j++) {
                    if(temp_sus[j] < temp_sus[min_sus]) {
                        min_sus = j;
                    }
                }
                team[i] = min_sus;
                // So that it cannot be detected again as the least sus
                temp_sus[min_sus] = 2;
            }
            return team;
        }
    }

    /**
     * This method is called when an agent is required to vote on whether a mission
     * should proceed
     * 
     * @param mission the array of agent indexes who will be going on the mission.
     * @param leader  the index of the agent who proposed the mission.
     * @return true is this agent votes that the mission should go ahead, false
     *         otherwise.
     **/
    public boolean vote(int[] mission, int leader) {
        // Only allow mission if all players are below the average suspicion.
        double sum = 0.0;
        for(int i = 0; i < numPlayers; i++) {
            sum += suspicion[i];
        }
        double avg_sus = sum/numPlayers;


        for(int player:mission) {
            if(suspicion[player] > avg_sus) return false;
        }
        return true;
    }

    /**
     * The method is called on an agent to inform them of the outcome of a vote, and
     * which agent voted for or against the mission.
     * 
     * @param mission the array of agent indexes represent the mission team
     * @param leader  the agent index of the leader, who proposed the mission
     * @param votes   an array of booleans such that votes[i] is true if and only if
     *                agent i voted for the mission to go ahead.
     **/
    public void voteOutcome(int[] mission, int leader, boolean[] votes) {
        // Do stuff to update suspicion based on who voted for and against mission
        // Infer from votes an evidence double value, pass to doBayesian.
        // Calculate average suspicion of the team proposed, use this to determine
        // new suspicion of the leader and of the players that voted in favour of the team.
        double sum0 = 0.0;
        double sum1 = 0.0;
        for(int player:mission) {
            sum0 += suspicion[player];
        }
        for(int i = 0; i < numPlayers; i++) {
            sum1 += suspicion[i];
        }
        double team_sus = sum0/mission.length;
        double avg_sus = sum1/numPlayers;
        // Cannot infer reward changes from the vote alone
        for(int i = 0; i < numPlayers; i++) {
            double evidence = 0.0;
            // Voting in favour of a suspicious team increases your suspicion when the team sus is greater than avg sus
            if((votes[i] && team_sus > avg_sus) || !votes[i] && avg_sus < team_sus) {
                evidence = (suspicion[i] + 1.0)/2;   
            }
            doBayesian(i, evidence, 0);
        }
    }

    /**
     * This method is called on an agent who has a choice to betray (fail) the
     * mission
     * 
     * @param mission the array of agent indexes representing the mission team
     * @param leader  the agent who proposed the mission
     * @return true is the agent choses to betray (fail) the mission
     **/
    public boolean betray(int[] mission, int leader) {
        // Use current probabilities to decide on action

        // For now, it will always betray.
        // Needs to learn to be conservative, increasing reward while reducing suspicion.
        // Reward cannot be simply represented as 1.0, must be Bayesian and probabilistic of a win.

        return true;
    }

    /**
     * Informs all agents of the outcome of the mission, including the number of
     * agents who failed the mission.
     * 
     * @param mission        the array of agent indexes representing the mission
     *                       team
     * @param leader         the agent who proposed the mission
     * @param numFails       the number of agent's who failed the mission
     * @param missionSuccess true if and only if the mission succeeded.
     **/
    public void missionOutcome(int[] mission, int leader, int numFails, boolean missionSuccess) {
        if(!missionSuccess) {
            // If failure, the chance each player is a spy independent of their current suspicion rating
            // is merely 1.0 divided by the number of players.
            // May need a better heuristic later.
            double spy_chance = 1.0/mission.length;
            for(int player:mission) {
                // Metric = 0 indicates to update suspicion table.
                doBayesian(player, spy_chance, 0);
            }
        }
        else {
            // Assume 1.0 for now
            double reward = 1.0;
            for(int player:mission) {
                // Metric = 0 indicates to update suspicion table.
                doBayesian(player, reward, 1);
            }
        }
    }

    /**
     * Informs all agents of the game state at the end of the round
     * 
     * @param roundsComplete the number of rounds played so far
     * @param roundsLost     the number of rounds lost so far
     **/
    public void roundOutcome(int roundsComplete, int roundsLost) {
        // System.out.println("Completed: " + roundsComplete);
        // System.out.println("Lost: " + roundsLost);
    }

    /**
     * Informs all agents of the outcome of the game, including the identity of the
     * spies.
     * 
     * @param roundsLost the number of rounds the Resistance lost
     * @param spies      an array with the indexes of all the spies in the game.
     **/
    public void gameOutcome(int roundsLost, int[] spies) {
        // System.out.println("Spies were: " + Arrays.toString(spies));

    }

    // Generalised Bayesian rule calculator that can work for both suspicion and expected reward.
    // Must be able to handle voteOutcome and missionOutcome for calculating suspicion.
    public void doBayesian(int player, double evidence, int metric) {
        if(metric == 0) {
            // Probability of E and H
            // P(H|E) = P(E|H)*P(H)/P(E)
            // Updates the Bayesian probability
            suspicion[player] = likelihood(evidence, suspicion[player])*suspicion[player]/evidence; 
        }
        // else {
        //     expected_reward[player] = likelihood(evidence, expected_reward[player])*expected_reward[player]/evidence; 
        // }
    }

    // Method of calculating likelihood of observing evidence given a hypothesis about a player
    public double likelihood(double evidence, double hypothesis) {

        // Make sure 0.0 isn't the output in a default case, or suspicion/reward will never update.
        // Multiply by suspicion

        // Update later
        return evidence * hypothesis;
    }

    // Get status of whether a mission failed
    // Get the players who were in that mission
    // If mission succeeded, lower all players suspicion
    // If mission failed, increase suspicion (weighted by different variables in the game, this is your baseline heuristic)

}
