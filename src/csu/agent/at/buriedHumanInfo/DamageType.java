package csu.agent.at.buriedHumanInfo;


import csu.model.ConfigConstants;

/**
 * This class used to indicate a single damage type of the human ,
 * and to do the according operation for the damage type .
 * @author nale
 *
 */
public class DamageType {

	/** the damage type index in all damage types*/
	private int damageTypeIndex;
	
	/** the initial collapse damage*/
	private int initialCollapseDamage;
	
	/** the initial bury damage*/
	private int initialBuryDamage;
	
	/** the accurate collapse damage in every time step*/
	private double[] stepCollapseDamage;
	
	/** the accurate bury damage in every time step*/
	private double[] stepBuryDamage;
	
	/** the accurate HP in every time step*/
	private int[] stepHP;
	
	/** the number of time step when the agent will die with this type of damage*/
	private int deathTime;
	
	/** the max value of the collapse damage in 99.7%*/
	private double[] stepCollapseMaxValue;
	
	/** the max value of the bury damage in 99.7%*/
	private double[] stepBuryMaxValue;
	
	/** the min value of the hp in 99.7% */
	private int[] stepHPMinValue;
	
//	/** the min death time in 99.7%*/
//	private int deathMinTime;
	
	/** the configConstants uesd to get parameters*/
	private ConfigConstants config;
	
	private static final int TIMESTEP = 350;
	
	private static final int MAX_DEATH_TIME = 10000;
	
	
	public DamageType(int damageTypeIndex , int collapse , int bury , ConfigConstants config){
		this.damageTypeIndex = damageTypeIndex;
		this.config = config;
		initialCollapseDamage = collapse;
		initialBuryDamage = bury;
		stepHP = new int[TIMESTEP+1];
		stepCollapseDamage = new double[TIMESTEP + 1];
		stepBuryDamage = new double[TIMESTEP + 1];
		stepHPMinValue = new int[TIMESTEP + 1];
		stepCollapseMaxValue = new double[TIMESTEP + 1];
		stepBuryMaxValue = new double[TIMESTEP + 1];
		deathTime = MAX_DEATH_TIME;
		generateDamageStateTable();
	}
	
	public void generateDamageStateTable(){
		stepHP[1] = config.hpMax;
		stepCollapseDamage[1] = initialCollapseDamage;
		stepBuryDamage[1] = initialBuryDamage;
		stepHPMinValue[1] = config.hpMax;
		stepCollapseMaxValue[1] = initialCollapseDamage;
		stepBuryMaxValue[1] = initialBuryDamage;
		for (int step = 2 ; step <= TIMESTEP ; step++){
			if (stepHP[step-1] == 0){
				stepHP[step] = 0;
				stepCollapseDamage[step] = stepCollapseDamage[step-1];
				stepBuryDamage[step] = stepBuryDamage[step-1];
				continue;
			}
			if (stepCollapseDamage[step-1] > 0)
				stepCollapseDamage[step] = stepCollapseDamage[step-1] + 
					(config.collapse_k * stepCollapseDamage[step-1] * stepCollapseDamage[step-1]) +
					config.collapse_l + config.collapse_mean;
			if (stepBuryDamage[step-1] > 0)
				stepBuryDamage[step] = stepBuryDamage[step-1] + 
					(config.bury_k * stepBuryDamage[step-1] * stepBuryDamage[step-1]) + 
					config.bury_l + config.bury_mean;
			int totalDamage = (int)Math.round(stepCollapseDamage[step] + stepBuryDamage[step]);
			stepHP[step] = Math.max((stepHP[step-1] - totalDamage) , 0);
			if (stepHP[step] == 0)
				deathTime = step;
			//max value
			if (stepHPMinValue[step-1] == 0){
				stepHPMinValue[step] = 0;
				stepCollapseMaxValue[step] = stepCollapseMaxValue[step-1];
				stepBuryMaxValue[step] = stepBuryMaxValue[step-1];
				continue;
			}
			if (stepCollapseMaxValue[step-1] > 0){
				stepCollapseMaxValue[step] = stepCollapseMaxValue[step-1] + 
					(config.collapse_k * stepCollapseMaxValue[step-1] * stepCollapseMaxValue[step-1]) +
					config.collapse_l + config.collapse_mean;
				if (step <= 5)
					stepCollapseMaxValue[step] += 3 * config.collapse_sd; 
			}
			if (stepBuryMaxValue[step-1] > 0){
				stepBuryMaxValue[step] = stepBuryMaxValue[step-1] + 
					(config.bury_k * stepBuryMaxValue[step-1] * stepBuryMaxValue[step-1]) + 
					config.bury_l + config.bury_mean;
				if (step <= 5)
					stepBuryMaxValue[step] += 3 * config.bury_sd;
			}
			int totalMaxDamage = (int)Math.round(stepCollapseMaxValue[step] + stepBuryMaxValue[step]);
			stepHPMinValue[step] = Math.max((stepHPMinValue[step-1] - totalMaxDamage) , 0);
//			if (stepHPMinValue[step] == 0)
//				deathMinTime = step;
		}
	}
	
	public int getDeathTime(){
		return deathTime;
	}
	
	public MatchResult matchToState(int hp , int damage , int timeStep){
		
		//TODO TEST
//		int meanHP = stepHP[timeStep];
//		int roundHP = round(meanHP , config.hpPrecision);
//		int meanDamage = getTotalDamage(timeStep);
//		int roundDamage = round(meanDamage , config.damagePrecision);
//		System.out.println(this + "   roundHP : " + roundHP + "   roundDamage : " + roundDamage);
		
		MatchResult hpResult = matchToHP(hp , timeStep);
		MatchResult damageResult = matchToDamage(damage , timeStep);
		if (hpResult == MatchResult.MATCH && damageResult == MatchResult.MATCH)
			return MatchResult.MATCH ;
		else if (hpResult == MatchResult.UNMATCH || damageResult == MatchResult.UNMATCH)
			return MatchResult.UNMATCH;
		else 
			return MatchResult.NOTSURE;
		
	}
	
	public MatchResult matchToHP(int hp , int timeStep){
		int meanHP = stepHP[timeStep];
		int roundHP = round(meanHP , config.hpPrecision);
		if (Math.abs(meanHP % config.hpPrecision - config.hpPrecision / 2) > hpAbsoluteError(timeStep)){
			if (hp == roundHP){
				return MatchResult.MATCH;
			}
			else {
				return MatchResult.UNMATCH;
			}
		}
		else return MatchResult.NOTSURE;
	}
	
	public MatchResult matchToDamage(int damage , int timeStep){
		int meanDamage = getTotalDamage(timeStep);
		int roundDamage = round(meanDamage , config.damagePrecision);
		if (Math.abs(meanDamage % config.damagePrecision - config.damagePrecision / 2) > damageAbsoluteError(timeStep)){
			if (damage == roundDamage){
				return MatchResult.MATCH;
			}
			else {
				return MatchResult.UNMATCH;
			}
		}
		else return MatchResult.NOTSURE;
	}
	
	public int round(int value , int precision){
		int remainder = value % precision;
		value -= remainder;
		if (remainder >= precision / 2)
			value += precision;
		return value;
	}
	
	/**
	 * the absolute error of damage in the special time step
	 * @param timeStep
	 * @return
	 */
	public int damageAbsoluteError(int timeStep){
		int maxDamage = (int)Math.round(stepCollapseMaxValue[timeStep] + stepBuryMaxValue[timeStep]);
		int ABE = maxDamage - getTotalDamage(timeStep); 
		return ABE;
	}
	
	/**
	 * the absolute error of hp in the special time step
	 * @param timeStep
	 * @return
	 */
	public int hpAbsoluteError(int timeStep){
		int ABE = stepHP[timeStep] - stepHPMinValue[timeStep];
		return ABE;
	}
	
	/**
	 * To get the total damage in the special time step
	 * @param timeStep
	 * @return
	 */
	public int getTotalDamage(int timeStep){
		return (int)Math.round(stepCollapseDamage[timeStep] + stepBuryDamage[timeStep]);
	}
	
	/**
	 * To get the hp in the special time step
	 * @param step
	 * @return
	 */
	public int getHP(int step){
		return stepHP[step];
	}
	
	public float getDamage(int step){
		return (float) (stepCollapseDamage[step] + stepBuryDamage[step]);
	}
	
	public int getMinHP(int step){
		return stepHPMinValue[step];
	}
	
	public float getMaxDamage(int step){
		return (float) (stepCollapseMaxValue[step] + stepBuryMaxValue[step]);
	}
	
	/**
	 * To get the index of this damage type
	 * @return
	 */
	public int getDamageTypeIndex(){
		return damageTypeIndex;
	}
	
	@Override
	public String toString(){
		String damageType = null;
		switch(damageTypeIndex){
		case 0:
			damageType = "none_none";
			break;
		case 1:
			damageType = "none_slight";
			break;
		case 2:
			damageType = "none_serious";
			break;
		case 3:
			damageType = "none_critical";
			break;
		case 4:
			damageType = "slight_none";
			break;
		case 5:
			damageType = "slight_slight";
			break;
		case 6:
			damageType = "slight_serious";
			break;
		case 7:
			damageType = "slight_critical";
			break;
		case 8:
			damageType = "serious_none";
			break;
		case 9:
			damageType = "serious_slight";
			break;
		case 10:
			damageType = "serious_serious";
			break;
		case 11:
			damageType = "serious_critical";
		}
		return damageType;
	}
	
	public enum DamageTypeEnum{
		none_none , 
		none_slight ,
		none_serious ,
		none_critical ,
		slight_none ,
		slight_slight ,
		slight_serious ,
		slight_critical ,
		serious_none ,
		serious_slight ,
		serious_serious ,
		serious_critical;
	}
		
}
