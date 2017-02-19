import java.applet.Applet;

/**
 * Genetic Algorithm
 * MasterControl.java
 * @author Allen Ng
 * @date November 7, 2007
 */
public class MasterControl extends Applet implements Runnable
{
	/**
	 * complier candy
	 */
	private static final long serialVersionUID = 3267830028765278802L;

	public void init()
	{
		prevGen=curGen = new Generation();
		// fork evolution thread
		Runnable r = new MasterControl();
		evolution = new Thread(r);
		evolution.start();
	}
	public void start()
	{
		// unsuspend evolution thread
	}
	public void stop()
	{
		// suspend evolution thread
	}
	public void destroy()
	{
		// kill evolution thread
		evolution.interrupt();
	}

	public void run()
	{
		while(!Thread.interrupted())
		{
			synchronized(MasterControl.prevGen)
			{
				prevGen = curGen;
				curGen = new Generation(prevGen);
				controlGen = new Generation();
				controlGen.fitSort();
				++iGenCount;
			}
		}
	}
	
	public boolean topIsUpdated()
	{
		return dGAHighestFit*REPORT_RESOLUTION<prevGen.getTopFitness();
	}
	public boolean controlIsUpdated()
	{
		if(controlGen==null)
			return false;
		return dRandHighestFit<controlGen.getTopFitness();
	}
	public String getTopReport()
	{
		String report;
		synchronized(MasterControl.prevGen)
		{
			report = genReport(prevGen);
		}
		return "Top candidate from inherited generation #"+iGenCount+":"+report;
	}
	public String getControlReport()
	{
		String report;
		synchronized(MasterControl.prevGen)
		{
			report = genReport(controlGen);
		}
		return "Top candidate from control generation #"+iGenCount+":"+report;
	}
	private String genReport(Generation gen)
	{
		StringBuilder sbOutput = new StringBuilder();
		sbOutput.append("\nSequence: "+gen.getTopSeq());
		sbOutput.append("\nValue: "+new Double(gen.getTopValue()).toString());
		sbOutput.append("\nTime: "+new Double(gen.getTopTime()).toString());
		sbOutput.append("\nFitness: "+new Double(gen.getTopFitness()).toString());
		sbOutput.append("\nCorrectness: "+new Double(gen.getTopCorrectness()).toString());
		return sbOutput.toString();
	}
	
	private boolean updateTop()
	{
		if(topIsUpdated())
		{
			System.out.println(getTopReport());
			System.out.println();
			dGAHighestFit=prevGen.getTopFitness();
			return true;
		}
		return false;
	}
	private boolean updateControl()
	{
		if(controlIsUpdated())
		{
			System.out.println(getControlReport());
			System.out.println();
			dRandHighestFit=controlGen.getTopFitness();
			return true;
		}
		return false;
	}
	private void era(int iEraLength)
	{
		int iLastGen = 0;
		while(iGenCount<iEraLength)
		{
			if(iLastGen==iGenCount)
				continue;
			synchronized(MasterControl.prevGen)
			{
				if(updateTop())
					iLastGen=iGenCount;
				//if(updateControl())
					//iLastGen=iGenCount;
			}
		}
		
		System.out.println(getTopReport());
		System.out.println();
		System.out.println(getControlReport());
	}
	private void outputMathematica(int iTableLength)
	{
		int iEntryCount = 0;
		while(iEntryCount<iTableLength && iGenCount<100000)
		{
			synchronized(MasterControl.prevGen)
			{
				if(controlIsUpdated())
				{
					System.out.print(iGenCount+" ");
					//System.out.print(prevGen.getTopFitness()+" ");
					//System.out.println(prevGen.getTopCorrectness());
					System.out.print(controlGen.getTopFitness()+" ");
					System.out.println(controlGen.getTopCorrectness());
					//dGAHighestFit=prevGen.getTopFitness();
					dRandHighestFit=controlGen.getTopFitness();
					++iEntryCount;
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		MasterControl MCP = new MasterControl();
		
		MCP.init();
		MCP.start();
		
		MCP.era(100000);
		//MCP.outputMathematica(100);
		
		MCP.stop();
		MCP.destroy();
	}

	private static Thread evolution;
	private static Generation prevGen, curGen, controlGen;
	private static int iGenCount;
	private static double dGAHighestFit;
	private static double dRandHighestFit;
	private static double REPORT_RESOLUTION = 10;
}
