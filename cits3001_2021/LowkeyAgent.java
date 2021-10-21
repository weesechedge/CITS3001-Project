package cits3001_2021;


public class LowkeyAgent implements Agent
{
	private String name;
	private boolean amSpy;
	private int[] fellowSpies;
	private int agentIndex;
	private int players;
	private int[] sus;
	private int timesLeader = 0;
	private int roundsLost = 0;
	private int roundsComplete = 0;
	private static int agentCount;
	
	public LowkeyAgent(String name)
	{
		this.name = name;
	}
	
	public static Agent init()
	{
		
		switch(agentCount++)
		{
			case 0: return new LowkeyAgent("Lowkey-the-1st");
			case 1: return new LowkeyAgent("Lowkey-the-2nd");
			case 2: return new LowkeyAgent("Lowkey-the-3rd");
			default: return new LowkeyAgent("Lowkey-the-"+agentCount+"th");
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public void newGame(int numPlayers, int playerIndex, int[] spies)
	{
		players = numPlayers;
		agentIndex = playerIndex;
		fellowSpies = spies;
		if(spies.length == 0) amSpy = false;
		else amSpy = true;
		sus = new int[players];
		for(int i = 0; i < players; i++)
		{
			sus[i] = 0;
		}
	}
	
	public int[] proposeMission(int teamSize, int failsRequired)
	{
		int[] missionTeam = new int[teamSize];
		int[] tempSus = sus.clone();
		
		if(amSpy)
		{
			boolean spyFound = false;
			
			for(int i = 0; i < players; i++)
			{
				if(spyFound) break;
				int min = minSusValue(tempSus);
				if(min == agentIndex) continue;
				for(int j = 0; j < fellowSpies.length; j++)
				{
					if(j == min)
					{
						missionTeam[0] = j;
						spyFound = true;
					}
				}
			}
			tempSus = sus.clone();
			tempSus[missionTeam[0]] = 999;
			tempSus[agentIndex] = 999;
			
			for(int i = 1; i < teamSize; i++)
			{
				missionTeam[i] = minSusValue(tempSus);
				tempSus[missionTeam[i]] = 999;
			}
			
			return missionTeam;
		}
		
		int min;
		
		for(int i = 0; i < teamSize; i++)
		{
			min = minSusValue(tempSus);
			missionTeam[i] = min;
			tempSus[min] = 999;
		}
		return missionTeam;
	}
	
	public boolean vote(int[] mission, int leader)
	{	
		int average = averageSusValues();
		
		int susCount = 0;
		
		if(amSpy)
		{
			for(int i = 0; i < mission.length; i++)
			{
				for(int j = 0; j < fellowSpies.length; j++)
				{
					if(mission[i] == fellowSpies[j]) return true;
				}
			}
			return false;
		}
		
		for(int i = 0; i < mission.length; i++)
		{
			if(sus[i] > average) susCount++;
		}
		if(sus[leader] > average) susCount++;
		
		if(susCount > ((mission.length + 1) / 2)) return false;
		return true;
	}
	
	public void voteOutcome(int[] mission, int leader, boolean[] votes)
	{
		for(int i = 0; i < players; i++)
		{
			if(votes[i] != votes[agentIndex]) sus[i]++;
		}
	}
	
	public boolean betray(int[] mission, int leader)
	{
		if(amSpy)
		{
			return true;
		}
		return false;
	}
	
	public void missionOutcome(int[] mission, int leader, int numFails, boolean missionSuccess)
	{
		if(!missionSuccess)
		{
			if(leader != agentIndex) sus[leader]++;
			if(amSpy && (leader == agentIndex)) sus[leader]++;
				
			for(int i = 0; i < mission.length; i++)
			{
				if(mission[i] != agentIndex) sus[mission[i]]++;
			}
		}
	}
	
	public void roundOutcome(int roundsComplete, int roundsLost)
	{
		this.roundsLost = roundsLost;
		this.roundsComplete = roundsComplete;
	}
	
	public void gameOutcome(int roundsLost, int[] spies)
	{
		
	}
	
	private int averageSusValues()
	{
		int average = 0;
		for(int i = 0; i < players; i++)
		{
			average += sus[i];
		}
		return average/players;
		
	}
	
	private int minSusValue(int[] susValues)
	{
		int minValue = 999;
		int minIndex = 0;
		
		for(int i = 0; i < players; i++)
		{
			if(susValues[i] < minValue)
			{
				minValue = susValues[i];
				minIndex = i;
			}
		}
		return minIndex;
	}
}