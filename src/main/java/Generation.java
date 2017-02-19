import static java.util.Arrays.*;

/**
 * Genetic Algorithm
 * Generation.java
 * @author Allen Ng
 * @date October 30, 2007
 */
public class Generation
{
	Generation()
	{
		for(int i=0; i<GENERATION_SIZE;++i)
			population[i]= new Chromosome();
	}
	Generation(Generation prevGen)
	{
		prevGen.fitSort();
		for(int i=0; i<REPRODUCTION_THRESHOLD; i++)
		{
			population[i] = prevGen.population[i].mate(new Chromosome());
			for(int j=0; j<OFFSPRING_QUOTA; j++)
				population[i*3 + j + REPRODUCTION_THRESHOLD] = prevGen.population[i].mate(prevGen.population[i+1]);
		}
	}
	void fitSort()
	{
		sort(population);
	}

	/**
	 * precondition: <code>fitSort</code> has been called on <code>population</code>.
	 * @return String -the sequence of the top candidate of this Generation.
	 */
	String getTopSeq()
	{
		return population[0].readSeq();
	}
	/**
	 * precondition: <code>fitSort</code> has been called on <code>population</code>.
	 * @return double -the <code>value</code> of the top candidate of this Generation.
	 */
	double getTopValue()
	{
		return population[0].getValue();
	}
	/**
	 * precondition: <code>fitSort</code> has been called on <code>population</code>.
	 * @return double -the processing <time> of the top candidate of this Generation.
	 */
	double getTopTime()
	{
		return population[0].getTime();
	}
	/**
	 * precondition: <code>fitSort</code> has been called on <code>population</code>.
	 * @return double -the fitness score of the top candidate of this Generation.
	 */
	double getTopFitness()
	{
		return population[0].getFitness();
	}
	double getTopCorrectness()
	{
		return population[0].getCorrectness();
	}
	
	public static void main(String[] args)
	{
		Generation prevGen=null, curGen = new Generation();
		for(int i=0;i<1000;++i)
		{
			prevGen = curGen;
			curGen = new Generation(prevGen);
		}
		System.out.println("Sequence: "+prevGen.getTopSeq());
		System.out.println("Value: "+prevGen.getTopValue());
		System.out.println("Time: "+prevGen.getTopTime());
		System.out.println("Fitness: "+prevGen.getTopFitness()+"\n");
	}
	
	private Chromosome[] population = new Chromosome[GENERATION_SIZE];
	public static final int REPRODUCTION_THRESHOLD = 25;
	public static final int OFFSPRING_QUOTA = 3;
	public static final int GENERATION_SIZE = REPRODUCTION_THRESHOLD*(OFFSPRING_QUOTA+1);
}
