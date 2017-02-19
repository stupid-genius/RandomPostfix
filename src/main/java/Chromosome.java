import java.util.Iterator;
import java.util.Stack;
import static java.lang.Character.*;
import static java.lang.Math.*;
import static java.util.Arrays.*;

/**
 * Genetic Algorithm
 * Chromosome.java
 * @author Allen Ng
 * @date October 30, 2007
 */
public class Chromosome implements Iterable<Character>, Iterator<Character>, Comparable<Chromosome>
{
	Chromosome()
	{
		StringBuilder sbSpontaneous = new StringBuilder();
		for(int i=0; i<CHROMOSOME_LENGTH;++i)
			sbSpontaneous.append(GENES.charAt((int) (random()*GENES.length())));
		sSequence = sbSpontaneous.toString();
		pfEvaluated = new PostfixExpression(this);
	}
	Chromosome(String sequence)
	{
		sSequence = sequence;
		pfEvaluated = new PostfixExpression(this);
	}
	
	/**
	 * returns shallow copy since all fields are final
	 * @throws CloneNotSupportedException 
	 */
	public Chromosome clone() throws CloneNotSupportedException
	{
		return (Chromosome)super.clone();
	}
	public Chromosome wildClone()
	{
		StringBuilder sbSister = new StringBuilder();
		for(char gene : this)
			if(random()<FREQ_MUTATION)
				sbSister.append(mutate());
			else
				sbSister.append(gene);
		return new Chromosome(sbSister.toString());
	}
	private char mutate()
	{
		return GENES.charAt((int) (GENES.length()*random()));
	}
	Chromosome mate(Chromosome partner)
	{
		StringBuilder sbOffspring = new StringBuilder();
		// gametes produced by meiosis
		Iterator<Character> p = partner.wildClone().iterator();
		Iterator<Character> m = this.wildClone().iterator();
		boolean bSwitch = true;

		while(p.hasNext())
		{
			char pGene = p.next();
			char mGene;
			if(m.hasNext())
			{
				mGene = m.next();
				if(random()<=CROSSOVER_RATE)
					bSwitch = !bSwitch;
				if(bSwitch)
					sbOffspring.append(mGene);
				else
					sbOffspring.append(pGene);
			}
			else
				sbOffspring.append(pGene);
		}
		while(m.hasNext())
			sbOffspring.append(m.next());
			
		return new Chromosome(sbOffspring.toString());
	}
	
	String readSeq()
	{
		return sSequence;
	}
	double getValue()
	{
		return pfEvaluated.getValue();
	}
	double getTime()
	{
		return pfEvaluated.getTime();
	}
	double getFitness()
	{
		return pfEvaluated.getValue()/pfEvaluated.getTime();
	}
	double getCorrectness()
	{
		return pfEvaluated.getCorrectness();
	}
	
	/**
	 * Intentionally returns values as opposite of expected so they can be sorted with most fit first
	 */
	@Override
	public int compareTo(Chromosome o)
	{
		double thisFit = getFitness();
		double thatFit = o.getFitness();
		int iOrder = 0;
		if(thisFit<thatFit)
			iOrder = 1;
		if(thisFit>thatFit)
			iOrder = -1;
		return iOrder;
	}
	
	public Iterator<Character> iterator()
	{
		iCursor = 0;
		return this;
	}
	public boolean hasNext()
	{
		return iCursor<sSequence.length();
	}
	public Character next()
	{
		return sSequence.charAt(iCursor++);
	}
	@Override
	public void remove()
	{
		return;
	}

	public static void main(String[] args)
	{
		Chromosome hostVector = new Chromosome("01+2+3+4+5+6+7+8+9*2*3*4*5*6*7*8*9^2^3^4");
		Chromosome[] labPopulation = new Chromosome[4];
		Chromosome[] labTestPop = new Chromosome[8];
		
		for(int i=0; i<labPopulation.length;++i)
			labPopulation[i] = new Chromosome();
		
		int i = 0;
		for(Chromosome cur : labPopulation)
		{
			labTestPop[i++] = cur.mate(hostVector);
			labTestPop[i++] = hostVector.mate(cur);
		}
		
		System.out.println("Sequence: "+hostVector.readSeq());
		System.out.println("Value: "+hostVector.getValue());
		System.out.println("Time: "+hostVector.getTime());
		System.out.println("Fitness: "+hostVector.getFitness()+"\n");
		for(Chromosome c : labPopulation)
		{
			System.out.println("Sequence: "+c.readSeq());
			System.out.println("Value: "+c.getValue());
			System.out.println("Time: "+c.getTime());
			System.out.println("Fitness: "+c.getFitness());
			System.out.println("Well-formed: "+c.getCorrectness()+"\n");
		}
		sort(labTestPop);
		System.out.println("---sort---");
		for(Chromosome c : labTestPop)
		{
			System.out.println("Sequence: "+c.readSeq());
			System.out.println("Value: "+c.getValue());
			System.out.println("Time: "+c.getTime());
			System.out.println("Fitness: "+c.getFitness());
			System.out.println("Well-formed: "+c.getCorrectness()+"\n");
		}
	}
	
	private final String sSequence;
	private final PostfixExpression pfEvaluated;
	private int iCursor;
	public static final double FREQ_MUTATION = .01;
	public static final double CROSSOVER_RATE = .4;
	public static final int CHROMOSOME_LENGTH = 100;
	public static final String GENES = "0123456789+-*/%ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	/**
	 * Must be able to handle arbitrary expressions
	 * including malformed expressions
	 */
	class PostfixExpression
	{
		PostfixExpression(Iterable<Character> expression)
		{
			Stack<Double> termStack = new Stack<Double>();
			double firstOpnd, secOpnd;
			double dNumNums=0, dNumOps=0;
			
			long start = System.nanoTime();
			
			for(char c : expression)
				if(isDigit(c))
				{
					termStack.push((double)digit(c, 10));
					dNumNums+=1;
				}
				else
				{
					switch(c)
					{
					case '+':
					{
						if(!termStack.empty())
						{
							secOpnd = termStack.pop();
							if(!termStack.empty())
							{
								firstOpnd = termStack.pop();
								termStack.push(firstOpnd+secOpnd);
								dNumOps+=1;
							}
							else
							{
								termStack.push(secOpnd);
								dNumNums-=1;
							}
						}
						else
							dNumNums-=1;
						break;
					}
					case '-':
					{
						if(!termStack.empty())
						{
							secOpnd = termStack.pop();
							if(!termStack.empty())
							{
								firstOpnd = termStack.pop();
								termStack.push(firstOpnd-secOpnd);
								dNumOps+=1;
							}
							else
							{
								termStack.push(secOpnd);
								dNumNums-=1;
							}
						}
						else
							dNumNums-=1;
						break;
					}
					case '*':
					{
						if(!termStack.empty())
						{
							secOpnd = termStack.pop();
							if(!termStack.empty())
							{
								firstOpnd = termStack.pop();
								termStack.push(firstOpnd*secOpnd);
								dNumOps+=1;
							}
							else
							{
								termStack.push(secOpnd);
								dNumNums-=1;
							}
						}
						else
							dNumNums-=1;
						break;
					}
					case '/':
					{
						if(!termStack.empty())
						{
							secOpnd = termStack.pop();
							if(!termStack.empty())
							{
								firstOpnd = termStack.pop();
								termStack.push(firstOpnd/secOpnd);
								dNumOps+=1;
							}
							else
							{
								termStack.push(secOpnd);
								dNumNums-=1;
							}
						}
						else
							dNumNums-=1;
						break;
					}
					case '%':
					{
						if(!termStack.empty())
						{
							secOpnd = termStack.pop();
							if(!termStack.empty())
							{
								firstOpnd = termStack.pop();
								termStack.push(firstOpnd%secOpnd);
								dNumOps+=1;
							}
							else
							{
								termStack.push(secOpnd);
								dNumNums-=1;
							}
						}
						else
							dNumNums-=1;
						break;
					}
					case 'r':
					{
						if(!termStack.empty())
						{
							secOpnd = termStack.pop();
							if(!termStack.empty())
							{
								firstOpnd = termStack.pop();
								termStack.push(pow(firstOpnd,1.0/secOpnd));
								dNumOps+=1;
							}
							else
							{
								termStack.push(secOpnd);
								dNumNums-=1;
							}
						}
						else
							dNumNums-=1;
						break;
					}
					case '^':
					{
						if(!termStack.empty())
						{
							secOpnd = termStack.pop();
							if(!termStack.empty())
							{
								firstOpnd = termStack.pop();
								termStack.push(pow(firstOpnd,secOpnd));
								dNumOps+=1;
							}
							else
							{
								termStack.push(secOpnd);
								dNumNums-=1;
							}
						}
						else
							dNumNums-=1;
						break;
					}
					default:
						dNumOps-=1;
					}
				}
						
			time = System.nanoTime()-start;
			correctness = dNumNums/(dNumOps+1);
			if(!termStack.empty())
				if(!termStack.peek().isInfinite() && !termStack.peek().isNaN())
				{
					value = termStack.pop();
					return;
				}
			
			value = 0;
		}
		double getValue()
		{
			return value;
		}
		double getTime()
		{
			return time;
		}
		double getCorrectness()
		{
			return correctness;
		}
		
		private double value;
		private double time;
		private double correctness;
	}
}
